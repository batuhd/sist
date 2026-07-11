package com.sinop.sist.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sinop.sist.data.local.converter.Converters
import com.sinop.sist.data.local.dao.AccountDao
import com.sinop.sist.data.local.dao.AssetDao
import com.sinop.sist.data.local.dao.AssetTransactionDao
import com.sinop.sist.data.local.dao.BudgetDao
import com.sinop.sist.data.local.dao.CategoryDao
import com.sinop.sist.data.local.dao.DebtDao
import com.sinop.sist.data.local.dao.InstallmentDao
import com.sinop.sist.data.local.dao.PriceCacheDao
import com.sinop.sist.data.local.dao.RecurringTransactionDao
import com.sinop.sist.data.local.dao.TransactionDao
import com.sinop.sist.data.local.entity.AccountEntity
import com.sinop.sist.data.local.entity.AssetEntity
import com.sinop.sist.data.local.entity.AssetTransactionEntity
import com.sinop.sist.data.local.entity.BudgetEntity
import com.sinop.sist.data.local.entity.CategoryEntity
import com.sinop.sist.data.local.entity.DebtEntity
import com.sinop.sist.data.local.entity.InstallmentEntity
import com.sinop.sist.data.local.entity.PriceCacheEntity
import com.sinop.sist.data.local.entity.RecurringTransactionEntity
import com.sinop.sist.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        RecurringTransactionEntity::class,
        DebtEntity::class,
        InstallmentEntity::class,
        AssetEntity::class,
        AssetTransactionEntity::class,
        PriceCacheEntity::class,
        AccountEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SistDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun debtDao(): DebtDao
    abstract fun installmentDao(): InstallmentDao
    abstract fun assetDao(): AssetDao
    abstract fun assetTransactionDao(): AssetTransactionDao
    abstract fun priceCacheDao(): PriceCacheDao
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile
        private var INSTANCE: SistDatabase? = null

        fun getDatabase(context: Context): SistDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SistDatabase::class.java,
                    "sist_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
