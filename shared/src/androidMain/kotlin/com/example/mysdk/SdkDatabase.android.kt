package com.example.mysdk

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.room.Room
import androidx.room.RoomDatabase

private const val databaseName = "mysdk-room.db"

internal object AndroidDatabaseContextHolder {
    var appContext: Context? = null
}

internal fun initializeMySdkAndroidContext(context: Context) {
    AndroidDatabaseContextHolder.appContext = context.applicationContext
}

internal actual fun getDatabaseBuilder(): RoomDatabase.Builder<SdkDatabase> {
    val appContext = checkNotNull(AndroidDatabaseContextHolder.appContext) {
        "Android SDK context is unavailable. Ensure the library manifest is merged correctly before calling MySdk.init(config)."
    }
    val dbFile = appContext.getDatabasePath(databaseName)
    return Room.databaseBuilder<SdkDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}

internal class MySdkInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        context?.applicationContext?.let(::initializeMySdkAndroidContext)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0
}
