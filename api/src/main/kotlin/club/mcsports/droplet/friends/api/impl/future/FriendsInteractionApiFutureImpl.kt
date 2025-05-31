package club.mcsports.droplet.friends.api.impl.future

import app.simplecloud.droplet.api.auth.AuthCallCredentials
import app.simplecloud.droplet.api.future.toCompletable
import club.mcsports.droplet.friends.api.InteractionApi
import com.mcsports.friend.v1.*
import io.grpc.ManagedChannel
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import java.util.*
import java.util.concurrent.CompletableFuture

class FriendsInteractionApiFutureImpl(
    channel: ManagedChannel,
    credentials: AuthCallCredentials,
) : InteractionApi.Future {

    private val api = FriendInteractionGrpc.newFutureStub(channel).withCallCredentials(credentials)
    private val gson = GsonComponentSerializer.gson()
    override fun invite(
        player: UUID,
        target: String
    ): CompletableFuture<InviteFriendResponse> {
        return api.inviteFriend(
            InviteFriendRequest.newBuilder().setPlayerId(player.toString()).setFriendName(target).build()
        ).toCompletable()
    }

    override fun acceptInvite(
        player: UUID,
        target: String
    ): CompletableFuture<ApproveFriendResponse> {
        return api.approveFriend(
            ApproveFriendRequest.newBuilder().setPlayerId(player.toString()).setFriendName(target).build()
        ).toCompletable()
    }

    override fun rejectInvite(
        player: UUID,
        target: String
    ): CompletableFuture<RejectFriendResponse> {
        return api.rejectFriend(
            RejectFriendRequest.newBuilder().setPlayerId(player.toString()).setFriendName(target).build()
        ).toCompletable()
    }

    override fun removeFriend(
        player: UUID,
        target: String
    ): CompletableFuture<RemoveFriendResponse> {
        return api.removeFriend(
            RemoveFriendRequest.newBuilder().setPlayerId(player.toString()).setFriendName(target).build()
        ).toCompletable()
    }

    override fun message(
        player: UUID,
        target: String,
        message: Component
    ): CompletableFuture<SendMessageResponse> {
        return api.sendMessage(
            SendMessageRequest.newBuilder().setPlayerId(player.toString()).setFriendName(target).setComponent(
                AdventureComponent.newBuilder().setJson(gson.serialize(message)).build()
            ).build()
        ).toCompletable()
    }

    override fun reply(
        player: UUID,
        message: Component
    ): CompletableFuture<ReplyResponse> {
        return api.reply(
            ReplyRequest.newBuilder().setPlayerId(player.toString())
                .setComponent(AdventureComponent.newBuilder().setJson(gson.serialize(message)).build()).build()
        ).toCompletable()
    }

    override fun jump(
        player: UUID,
        friend: String
    ): CompletableFuture<JumpResponse> {
        return api.jump(JumpRequest.newBuilder().setPlayerId(player.toString()).setFriendName(friend).build())
            .toCompletable()
    }
}