package club.mcsports.droplet.friends.api.impl.coroutine

import app.simplecloud.droplet.api.auth.AuthCallCredentials
import club.mcsports.droplet.friends.api.InteractionApi
import com.mcsports.friend.v1.*
import io.grpc.ManagedChannel
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import java.util.*

class FriendsInteractionApiCoroutineImpl(
    credentials: AuthCallCredentials,
    channel: ManagedChannel,
) : InteractionApi.Coroutine {
    private val api = FriendInteractionGrpcKt.FriendInteractionCoroutineStub(channel).withCallCredentials(credentials)
    private val gson = GsonComponentSerializer.gson()

    override suspend fun invite(player: UUID, target: String): InviteFriendResponse {
        return api.inviteFriend(inviteFriendRequest {
            this.playerId = player.toString()
            this.friendName = target
        })
    }

    override suspend fun acceptInvite(player: UUID, target: String): ApproveFriendResponse {
        return api.approveFriend(approveFriendRequest {
            this.playerId = player.toString()
            this.friendName = target
        })
    }

    override suspend fun rejectInvite(player: UUID, target: String): RejectFriendResponse {
        return api.rejectFriend(rejectFriendRequest {
            this.playerId = player.toString()
            this.friendName = target
        })
    }

    override suspend fun removeFriend(player: UUID, target: String): RemoveFriendResponse {
        return api.removeFriend(removeFriendRequest {
            this.playerId = player.toString()
            this.friendName = target
        })
    }

    override suspend fun message(
        player: UUID,
        target: String,
        message: Component
    ): SendMessageResponse {
        return api.sendMessage(sendMessageRequest {
            this.playerId = player.toString()
            this.friendName = target
            this.component = adventureComponent { json = gson.serialize(message) }
        })
    }

    override suspend fun reply(player: UUID, message: Component): ReplyResponse {
        return api.reply(replyRequest {
            this.playerId = player.toString()
            this.component = adventureComponent { json = gson.serialize(message) }
        })
    }

    override suspend fun jump(player: UUID, friend: String): JumpResponse {
        return api.jump(jumpRequest {
            this.playerId = player.toString()
            this.friendName = friend
        })
    }
}