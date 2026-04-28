@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.example.mysdk

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

private const val databaseName = "mysdk-room.db"

internal actual fun getDatabaseBuilder(): RoomDatabase.Builder<SdkDatabase> {
    val dbFilePath = documentDirectory() + "/$databaseName"
    return Room.databaseBuilder<SdkDatabase>(
        name = dbFilePath,
    )
}

private fun documentDirectory(): String {
    val directory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(directory?.path)
}
