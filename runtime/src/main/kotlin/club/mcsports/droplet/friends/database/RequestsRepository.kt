package club.mcsports.droplet.friends.database

import club.mcsports.droplet.friends.generated.db.tables.FriendRequests.Companion.FRIEND_REQUESTS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import java.time.LocalDateTime
import java.util.*

data class FriendRequest(
    val sender: UUID,
    val receiver: UUID,
    val since: LocalDateTime = LocalDateTime.now(),
)

class RequestsRepository(
    private val db: Database,
) : PlayerDataRepository<UUID, FriendRequest> {

    //Receiver as key, as requests being drawn back won't be happening often
    private val requests = mutableMapOf<UUID, MutableList<FriendRequest>>()
    private val logger = LogManager.getLogger(RequestsRepository::class.java)

    override suspend fun delete(element: FriendRequest): Boolean {
        val list = requests[element.receiver] ?: mutableListOf()
        list.removeIf { it.sender == element.sender }
        requests[element.receiver] = list
        return withContext(Dispatchers.IO) {
            db.context.deleteFrom(FRIEND_REQUESTS).where(
                FRIEND_REQUESTS.RECEIVER_ID.eq(element.receiver.toString())
                    .and(FRIEND_REQUESTS.SENDER_ID.eq(element.sender.toString()))
            ).execute() > 0
        }
    }

    override fun save(element: FriendRequest) {
        val list = requests[element.receiver] ?: mutableListOf()
        list.removeIf { it.sender == element.sender }
        list.add(element)
        requests[element.receiver] = list
        db.context.insertInto(
            FRIEND_REQUESTS,

            FRIEND_REQUESTS.SENDER_ID, FRIEND_REQUESTS.RECEIVER_ID, FRIEND_REQUESTS.SENT_AT
        ).values(element.sender.toString(), element.receiver.toString(), element.since)
            .onDuplicateKeyIgnore().execute()
    }

    override fun find(identifier: UUID): List<FriendRequest> {
        val all = mutableListOf<FriendRequest>()
        requests.values.forEach {
            all.addAll(it.filter { req -> req.sender == identifier })
        }
        return all
    }

    fun findFor(receiver: UUID): List<FriendRequest> {
        return requests[receiver] ?: emptyList()
    }


    override fun load() {
        db.context.selectFrom(FRIEND_REQUESTS).fetchInto(FRIEND_REQUESTS).forEach {
            val sender = UUID.fromString(it.senderId)
            val receiver = UUID.fromString(it.receiverId)
            val list = requests[receiver] ?: mutableListOf()
            list.add(FriendRequest(sender, receiver, it.sentAt ?: LocalDateTime.now()))
            requests[receiver] = list
        }
    }
}