package club.mcsports.droplet.friends.api.impl.future

import app.simplecloud.droplet.api.auth.AuthCallCredentials
import app.simplecloud.droplet.api.future.toCompletable
import club.mcsports.droplet.friends.api.DataApi
import com.mcsports.friend.v1.*
import io.grpc.ManagedChannel
import java.util.*
import java.util.concurrent.CompletableFuture

class FriendsDataApiFutureImpl(
    channel: ManagedChannel,
    credentials: AuthCallCredentials,
) : DataApi.Future {

    private val api = FriendDataGrpc.newFutureStub(channel).withCallCredentials(credentials)

    override fun getFriends(
        player: UUID,
        page: Int,
        amount: Int
    ): CompletableFuture<ListFriendsResponse> {
        return api.listFriends(
            ListFriendsRequest.newBuilder().setPage(page).setAmount(amount).setPlayerId(player.toString()).build()
        ).toCompletable()
    }

    override fun getRequests(
        player: UUID,
        page: Int,
        amount: Int
    ): CompletableFuture<ListFriendInvitesResponse> {
        return api.listInvites(
            ListFriendInvitesRequest.newBuilder().setPage(page).setAmount(amount).setPlayerId(player.toString()).build()
        ).toCompletable()
    }
}