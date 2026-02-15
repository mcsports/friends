package club.mcsports.droplet.friends.service

import app.simplecloud.droplet.player.api.PlayerApi
import club.mcsports.droplet.friends.database.FriendsRepository
import club.mcsports.droplet.friends.database.RequestsRepository
import club.mcsports.droplet.friends.extension.asPlayer
import club.mcsports.droplet.friends.extension.asUUID
import com.mcsports.friend.v1.*
import io.grpc.Status

class FriendDataService(
    private val api: PlayerApi.Coroutine,
    private val friends: FriendsRepository,
    private val requests: RequestsRepository
) : FriendDataGrpcKt.FriendDataCoroutineImplBase() {

    override suspend fun listFriends(request: ListFriendsRequest): ListFriendsResponse {
        val player = request.playerId.asUUID()
        val allFriends = friends.find(player)

        if (allFriends.isEmpty()) {
            throw Status.FAILED_PRECONDITION.withDescription("No friends found.")
                .asRuntimeException()
        }
        val startIndex = request.page * request.amount
        if (startIndex >= allFriends.size) {
            throw Status.FAILED_PRECONDITION.withDescription("No friends found past page ${allFriends.size / request.amount}.")
                .asRuntimeException()
        }
        val result = allFriends.subList(startIndex, allFriends.size).take(request.amount)
        val pages = (allFriends.size + request.amount - 1) / request.amount
        return listFriendsResponse {
            this.totalFriends = allFriends.size
            this.pages = pages
            this.friend.addAll(result.map {
                val player = it.id.asPlayer(api)!!
                playerData {
                    this.id = it.id.toString()
                    this.name = player.getName()
                    this.online = player.isOnline()
                    this.server = player.getLastConnectedServerName() ?: "Unknown"
                } to player
            }.sortedByDescending { it.second.isOnline() }.map { it.first })
        }
    }

    override suspend fun listInvites(request: ListFriendInvitesRequest): ListFriendInvitesResponse {
        val player = request.playerId.asUUID()
        val allRequests = requests.findFor(player)

        if (allRequests.isEmpty()) {
            throw Status.FAILED_PRECONDITION.withDescription("No friend requests found.")
                .asRuntimeException()
        }

        val startIndex = request.page * request.amount
        if (startIndex >= allRequests.size) {
            throw Status.FAILED_PRECONDITION.withDescription("No friend requests found past page ${allRequests.size / request.amount}.")
                .asRuntimeException()
        }
        val result = allRequests.subList(startIndex, allRequests.size).take(request.amount)
        val pages = (allRequests.size + request.amount - 1) / request.amount
        return listFriendInvitesResponse {
            this.totalRequests = allRequests.size
            this.pages = pages
            this.friend.addAll(result.map {
                val player = it.sender.asPlayer(api)!!
                playerData {
                    this.id = it.sender.toString()
                    this.name = player.getName()
                    this.online = player.isOnline()
                    this.server = player.getLastConnectedServerName() ?: "Unknown"
                }
            })
        }
    }

    override suspend fun checkFriends(request: CheckFriendsRequest): CheckFriendsResponse {
        val player = request.playerId.asUUID()
        val result =
            request.friendIdsList.map { it.asUUID() }.map { friend ->
                FriendStatus.newBuilder().setAreFriends(friends.areFriends(player, friend))
                    .setPlayerId(friend.toString()).build()
            }
        return checkFriendsResponse {
            this.statuses.addAll(result)
        }
    }
}