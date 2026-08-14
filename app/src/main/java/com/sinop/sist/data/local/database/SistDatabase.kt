package com.sinop.sist.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sinop.sist.data.local.converter.Converters
import com.sinop.sist.data.local.dao.AccountDao
import com.sinop.sist.data.local.dao.AssetDao
import com.sinop.sist.data.local.dao.AssetTransactionDao
import com.sinop.sist.data.local.dao.BudgetDao
import com.sinop.sist.data.local.dao.CategoryDao
import com.sinop.sist.data.local.dao.DebtDao
import com.sinop.sist.data.local.dao.InstallmentDao
import com.sinop.sist.data.local.dao.PriceCacheDao
import com.sinop.sist.data.local.dao.PriceAlertDao
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
import com.sinop.sist.data.local.entity.PriceAlertEntity
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
        PriceAlertEntity::class,
        AccountEntity::class
    ],
    version = 4,
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
    abstract fun priceAlertDao(): PriceAlertDao
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile
        private var INSTANCE: SistDatabase? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN toAccountId INTEGER")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `price_alerts` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`assetId` INTEGER NOT NULL, " +
                            "`targetPrice` REAL NOT NULL, " +
                            "`isAbove` INTEGER NOT NULL, " +
                            "`isActive` INTEGER NOT NULL, " +
                            "`triggeredAt` TEXT, " +
                            "`createdAt` TEXT NOT NULL)"
                )
            }
        }

        fun getDatabase(context: Context): SistDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SistDatabase::class.java,
                    "sist_database"
                )
                    .addMigrations(MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
