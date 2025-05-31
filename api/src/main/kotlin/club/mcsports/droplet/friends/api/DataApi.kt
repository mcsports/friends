package club.mcsports.droplet.friends.api

import com.mcsports.friend.v1.ListFriendInvitesResponse
import com.mcsports.friend.v1.ListFriendsResponse
import java.util.*
import java.util.concurrent.CompletableFuture

interface DataApi {
    interface Coroutine {
        suspend fun getFriends(player: UUID, page: Int, amount: Int): ListFriendsResponse
        suspend fun getRequests(player: UUID, page: Int, amount: Int): ListFriendInvitesResponse
    }

    interface Future {
        fun getFriends(player: UUID, page: Int, amount: Int): CompletableFuture<ListFriendsResponse>
        fun getRequests(player: UUID, page: Int, amount: Int): CompletableFuture<ListFriendInvitesResponse>
    }
}