package com.sinop.sist

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.sinop.sist.data.local.database.SistDatabase
import com.sinop.sist.data.remote.api.FinanceApiService
import com.sinop.sist.data.remote.provider.FvtFundPriceProvider
import com.sinop.sist.data.repository.AccountRepositoryImpl
import com.sinop.sist.data.repository.AssetRepositoryImpl
import com.sinop.sist.data.repository.BudgetRepositoryImpl
import com.sinop.sist.data.repository.CategoryRepositoryImpl
import com.sinop.sist.data.repository.DebtRepositoryImpl
import com.sinop.sist.data.repository.InstallmentRepositoryImpl
import com.sinop.sist.data.repository.PriceCacheRepositoryImpl
import com.sinop.sist.data.repository.RecurringTransactionRepositoryImpl
import com.sinop.sist.data.repository.TransactionRepositoryImpl
import com.sinop.sist.domain.repository.AccountRepository
import com.sinop.sist.domain.usecase.RefreshAssetPricesUseCase
import com.sinop.sist.worker.BudgetCheckWorker
import com.sinop.sist.worker.MarketCloseNotificationWorker
import com.sinop.sist.worker.RecurringTransactionWorker
import com.sinop.sist.domain.repository.AssetRepository
import com.sinop.sist.domain.repository.BudgetRepository
import com.sinop.sist.domain.repository.CategoryRepository
import com.sinop.sist.domain.repository.DebtRepository
import com.sinop.sist.domain.repository.InstallmentRepository
import com.sinop.sist.domain.repository.PriceCacheRepository
import com.sinop.sist.domain.repository.RecurringTransactionRepository
import com.sinop.sist.domain.repository.TransactionRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SistApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = AppContainer(this)
        scheduleWorkers()
    }

    fun scheduleWorkers() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val budgetWork = PeriodicWorkRequestBuilder<BudgetCheckWorker>(1, java.util.concurrent.TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        val recurringWork = PeriodicWorkRequestBuilder<RecurringTransactionWorker>(1, java.util.concurrent.TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).apply {
            enqueueUniquePeriodicWork(
                BudgetCheckWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                budgetWork
            )
            enqueueUniquePeriodicWork(
                "recurring_transaction_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                recurringWork
            )
        }

        MarketCloseNotificationWorker.scheduleNext(this)
    }

    companion object {
        var instance: SistApplication? = null
            private set
    }
}

class AppContainer(private val application: Application) {

    private val database by lazy { SistDatabase.getDatabase(application) }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://query1.finance.yahoo.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val financeApiService: FinanceApiService by lazy {
        retrofit.create(FinanceApiService::class.java)
    }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepositoryImpl(database.transactionDao())
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(database.categoryDao())
    }

    val budgetRepository: BudgetRepository by lazy {
        BudgetRepositoryImpl(database.budgetDao(), database.transactionDao())
    }

    val recurringTransactionRepository: RecurringTransactionRepository by lazy {
        RecurringTransactionRepositoryImpl(database.recurringTransactionDao(), database.transactionDao())
    }

    val debtRepository: DebtRepository by lazy {
        DebtRepositoryImpl(database.debtDao())
    }

    val installmentRepository: InstallmentRepository by lazy {
        InstallmentRepositoryImpl(database.installmentDao())
    }

    val assetRepository: AssetRepository by lazy {
        AssetRepositoryImpl(database.assetDao(), database.assetTransactionDao(), database.priceCacheDao())
    }

    val priceCacheRepository: PriceCacheRepository by lazy {
        PriceCacheRepositoryImpl(database.priceCacheDao())
    }

    val fvtFundPriceProvider: FvtFundPriceProvider by lazy {
        FvtFundPriceProvider(application)
    }

    val refreshAssetPricesUseCase: RefreshAssetPricesUseCase by lazy {
        RefreshAssetPricesUseCase(financeApiService, fvtFundPriceProvider, priceCacheRepository)
    }

    val accountRepository: AccountRepository by lazy {
        AccountRepositoryImpl(database.accountDao())
    }
}
