package club.mcsports.droplet.friends.service

import app.simplecloud.droplet.player.api.PlayerApi
import club.mcsports.droplet.friends.database.FriendsRepository
import club.mcsports.droplet.friends.database.RequestsRepository
import club.mcsports.droplet.friends.extension.asPlayer
import club.mcsports.droplet.friends.extension.asUUID
import com.mcsports.friend.v1.*
import io.grpc.Status
import org.apache.logging.log4j.LogManager
class FriendDataService(
    private val api: PlayerApi.Coroutine,
    private val friends: FriendsRepository,
    private val requests: RequestsRepository
) : FriendDataGrpcKt.FriendDataCoroutineImplBase() {

    private val logger = LogManager.getLogger(FriendDataService::class.java)

    override suspend fun listFriends(request: ListFriendsRequest): ListFriendsResponse {
        val player = request.playerId.asUUID()
        val allFriends = friends.find(player)
        val startIndex = request.page * request.amount
        if (startIndex >= allFriends.size) {
            throw Status.FAILED_PRECONDITION.withDescription("This page does not exist (Friends: ${allFriends.size} Max Page: ${allFriends.size / request.amount})")
                .asRuntimeException()
        }
        val result = allFriends.subList(startIndex, allFriends.size).take(request.amount)
        val pages = allFriends.size / request.amount
        return listFriendsResponse {
            this.pages = pages
            this.friend.addAll(result.map {
                playerData {
                    this.id = it.id.toString()
                    this.name = it.id.asPlayer(api)?.getName() ?: "Unknown"
                }
            })
        }
    }

    override suspend fun listInvites(request: ListFriendInvitesRequest): ListFriendInvitesResponse {
        val player = request.playerId.asUUID()
        val allRequests = requests.findFor(player)
        val startIndex = request.page * request.amount
        if (startIndex >= allRequests.size) {
            throw Status.FAILED_PRECONDITION.withDescription("This page does not exist (Requests: ${allRequests.size} Max Page: ${allRequests.size / request.amount})")
                .asRuntimeException()
        }
        val result = allRequests.subList(startIndex, allRequests.size).take(request.amount)
        val pages = allRequests.size / request.amount
        return listFriendInvitesResponse {
            this.pages = pages
            this.friend.addAll(result.map {
                playerData {
                    this.id = it.sender.toString()
                    this.name = it.sender.asPlayer(api)?.getName() ?: "Unknown"
                }
            })
        }
    }
}