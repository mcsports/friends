package club.mcsports.droplet.friends.database

interface PlayerDataRepository<I, E> {
    suspend fun delete(element: E): Boolean
    fun save(element: E)
    fun find(identifier: I): List<E>
    fun load()
}