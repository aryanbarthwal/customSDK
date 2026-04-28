package com.example.mysdk

internal class NameRepository(
    private val database: SdkDatabase,
) {
    private val dao = database.nameDao()

    suspend fun getAllNames(): List<NameEntity> {
        return dao.getAll()
    }

    suspend fun insertName(value: String) {
        val normalizedValue = value.trim()
        require(normalizedValue.isNotBlank()) {
            "Name must not be blank."
        }
        dao.insert(NameEntity(value = normalizedValue))
    }

    suspend fun deleteName(id: Long) {
        dao.deleteById(id)
    }
}
