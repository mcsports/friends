package club.mcsports.droplet.friends.api

import com.mcsports.friend.v1.*
import net.kyori.adventure.text.Component
import java.util.*
import java.util.concurrent.CompletableFuture

interface InteractionApi {
    interface Coroutine {
        suspend fun invite(player: UUID, target: String): InviteFriendResponse
        suspend fun acceptInvite(player: UUID, target: String): ApproveFriendResponse
        suspend fun rejectInvite(player: UUID, target: String): RejectFriendResponse
        suspend fun removeFriend(player: UUID, target: String): RemoveFriendResponse
        suspend fun message(player: UUID, target: String, message: Component): SendMessageResponse
        suspend fun reply(player: UUID, message: Component): ReplyResponse
        suspend fun jump(player: UUID, friend: String): JumpResponse
    }

    interface Future {
        fun invite(player: UUID, target: String): CompletableFuture<InviteFriendResponse>
        fun acceptInvite(player: UUID, target: String): CompletableFuture<ApproveFriendResponse>
        fun rejectInvite(player: UUID, target: String): CompletableFuture<RejectFriendResponse>
        fun removeFriend(player: UUID, target: String): CompletableFuture<RemoveFriendResponse>
        fun message(player: UUID, target: String, message: Component): CompletableFuture<SendMessageResponse>
        fun reply(player: UUID, message: Component): CompletableFuture<ReplyResponse>
        fun jump(player: UUID, friend: String): CompletableFuture<JumpResponse>
    }
}