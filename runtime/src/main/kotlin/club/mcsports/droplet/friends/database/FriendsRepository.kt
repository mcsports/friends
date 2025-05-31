package club.mcsports.droplet.friends.database

import club.mcsports.droplet.friends.generated.db.tables.FriendConnections.Companion.FRIEND_CONNECTIONS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import java.time.LocalDateTime
import java.util.*

data class Friend(
    val id: UUID,
    val of: UUID,
    val since: LocalDateTime = LocalDateTime.now(),
)

class FriendsRepository(
    private val db: Database,
) : PlayerDataRepository<UUID, Friend> {

    private val friends = mutableMapOf<UUID, MutableList<Friend>>()
    private val logger = LogManager.getLogger(FriendsRepository::class.java)

    override suspend fun delete(element: Friend): Boolean {
        if (friends.containsKey(element.of)) {
            val result = friends[element.of] ?: return false
            result.removeIf { it.id == element.id }
            friends[element.of] = result
        }
        if (friends.containsKey(element.id)) {
            val result = friends[element.id] ?: return false
            result.removeIf { it.id == element.of }
            friends[element.id] = result
        }
        return withContext(Dispatchers.IO) {
            db.context.deleteFrom(FRIEND_CONNECTIONS).where(
                (FRIEND_CONNECTIONS.UNIQUE_ID.eq(element.of.toString())
                    .and(FRIEND_CONNECTIONS.FRIEND_ID.eq(element.id.toString()))).or(
                        FRIEND_CONNECTIONS.UNIQUE_ID.eq(element.id.toString())
                            .and(FRIEND_CONNECTIONS.FRIEND_ID.eq(element.of.toString()))
                    )
            ).execute() > 0
        }
    }

    override fun save(element: Friend) {
        insert(element.id, element.of, element.since)
        insert(element.of, element.id, element.since)
    }

    private fun insert(first: UUID, second: UUID, since: LocalDateTime) {
        try {
            db.context.insertInto(
                FRIEND_CONNECTIONS,

                FRIEND_CONNECTIONS.UNIQUE_ID, FRIEND_CONNECTIONS.FRIEND_ID, FRIEND_CONNECTIONS.BEFRIENDED_AT
            ).values(first.toString(), second.toString(), since).onDuplicateKeyUpdate()
                .set(FRIEND_CONNECTIONS.UNIQUE_ID, first.toString())
                .set(FRIEND_CONNECTIONS.FRIEND_ID, second.toString())
                .execute()
        } catch (e: Exception) {
            logger.error(e)
        }
        val connections = friends[first] ?: mutableListOf()
        connections.removeIf { it.id == second }
        connections.add(Friend(second, first, since))
        friends[first] = connections
    }

    override fun find(identifier: UUID): List<Friend> {
        return friends[identifier] ?: emptyList()
    }

    fun areFriends(first: UUID, second: UUID): Boolean {
        return find(first).any { it.id == second || it.of == second } && find(second).any { it.of == first || it.id == first }
    }

    override fun load() {
        db.context.select().from(FRIEND_CONNECTIONS).fetchInto(FRIEND_CONNECTIONS).forEach { result ->
            val current = friends[UUID.fromString(result.uniqueId)] ?: mutableListOf()
            val friend = Friend(
                UUID.fromString(result.friendId),
                of = UUID.fromString(result.uniqueId),
                since = result.befriendedAt ?: LocalDateTime.now()
            )
            current.add(friend)
            friends[UUID.fromString(result.uniqueId)] = current
        }
    }
}