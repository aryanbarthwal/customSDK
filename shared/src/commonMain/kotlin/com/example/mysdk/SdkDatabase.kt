package com.example.mysdk

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

@Database(entities = [NameEntity::class], version = 1, exportSchema = false)
@ConstructedBy(SdkDatabaseConstructor::class)
internal abstract class SdkDatabase : RoomDatabase() {
    abstract fun nameDao(): NameDao
}

@Suppress("KotlinNoActualForExpect")
internal expect object SdkDatabaseConstructor : RoomDatabaseConstructor<SdkDatabase> {
    override fun initialize(): SdkDatabase
}

internal expect fun getDatabaseBuilder(): RoomDatabase.Builder<SdkDatabase>

internal fun createSdkDatabase(): SdkDatabase {
    return getDatabaseBuilder()
        .setDriver(BundledSQLiteDriver())
        .build()
}
