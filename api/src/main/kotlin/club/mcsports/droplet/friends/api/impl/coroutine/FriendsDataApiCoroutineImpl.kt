package club.mcsports.droplet.friends.api.impl.coroutine

import app.simplecloud.droplet.api.auth.AuthCallCredentials
import club.mcsports.droplet.friends.api.DataApi
import com.mcsports.friend.v1.*
import io.grpc.ManagedChannel
import java.util.*

class FriendsDataApiCoroutineImpl(
    credentials: AuthCallCredentials,
    channel: ManagedChannel,
) : DataApi.Coroutine {

    private val api = FriendDataGrpcKt.FriendDataCoroutineStub(channel).withCallCredentials(credentials)

    override suspend fun getFriends(
        player: UUID, page: Int, amount: Int
    ): ListFriendsResponse {
        return api.listFriends(listFriendsRequest {
            this.amount = amount
            this.page = page
            this.playerId = player.toString()
        })
    }

    override suspend fun getRequests(
        player: UUID, page: Int, amount: Int
    ): ListFriendInvitesResponse {
        return api.listInvites(listFriendInvitesRequest {
            this.amount = amount
            this.page = page
            this.playerId = player.toString()
        })
    }
}