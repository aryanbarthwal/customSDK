package com.example.mysdk

internal object SdkStore {
    private val databaseHolder = lazy(LazyThreadSafetyMode.SYNCHRONIZED, ::createSdkDatabase)

    val database: SdkDatabase
        get() = databaseHolder.value

    val names: NameRepository by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        NameRepository(database)
    }

    fun initialize() {
        database
    }
}
