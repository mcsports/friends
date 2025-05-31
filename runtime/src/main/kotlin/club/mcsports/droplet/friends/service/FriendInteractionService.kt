package club.mcsports.droplet.friends.service

import app.simplecloud.droplet.player.api.PlayerApi
import club.mcsports.droplet.friends.database.*
import club.mcsports.droplet.friends.extension.asOnlinePlayer
import club.mcsports.droplet.friends.extension.asOnlinePlayerNullable
import club.mcsports.droplet.friends.extension.asPlayer
import club.mcsports.droplet.friends.extension.asUUID
import club.mcsports.droplet.friends.message.MessageManager
import com.mcsports.friend.v1.*
import io.grpc.Status
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

class FriendInteractionService(
    private val playerApi: PlayerApi.Coroutine,
    private val friends: FriendsRepository,
    private val requests: RequestsRepository,
    private val settings: SettingsRepository,
) : FriendInteractionGrpcKt.FriendInteractionCoroutineImplBase() {

    private val message = MessageManager(playerApi, friends)

    override suspend fun inviteFriend(request: InviteFriendRequest): InviteFriendResponse {
        val player = request.playerId.asUUID().asOnlinePlayer(playerApi)
        val target = request.friendName.asPlayer(playerApi)
        if (target == null) {
            player.sendMessage(
                Component.text("${request.friendName} has never joined the club.").color(TextColor.color(0xdc2626))
            )
            throw Status.FAILED_PRECONDITION.withDescription("Player ${request.friendName} is unknown.")
                .asRuntimeException()
        }

        if(player.getUniqueId() == target.getUniqueId()) {
            player.sendMessage(
                Component.text("You can not befriend yourself.").color(TextColor.color(0xdc2626))
            )
            throw Status.FAILED_PRECONDITION.withDescription("Can not befriend yourself.")
                .asRuntimeException()
        }

        if (requests.findFor(target.getUniqueId()).any { it.sender == player.getUniqueId() }) {
            player.sendMessage(
                Component.text("You already want to be friends with ${request.friendName}.")
                    .color(TextColor.color(0xdc2626))
            )
            throw Status.FAILED_PRECONDITION.withDescription("Request from ${request.friendName} already present.")
                .asRuntimeException()
        }

        if(friends.areFriends(player.getUniqueId(), target.getUniqueId())) {
            player.sendMessage(
                Component.text("You are already friends with ${request.friendName}.").color(TextColor.color(0xdc2626))
            )
            throw Status.FAILED_PRECONDITION.withDescription("Already friends with ${request.friendName}.")
                .asRuntimeException()
        }

        if (!requests.findFor(player.getUniqueId()).any { it.sender == target.getUniqueId() }) {
            requests.save(FriendRequest(player.getUniqueId(), target.getUniqueId()))
            player.sendMessage(Component.text("Friend request sent to ${target.getName()}!"))
            if (target.isOnline()) {
                request.friendName.asOnlinePlayer(playerApi)
                    .sendMessage(Component.text("You got a friend request from ${player.getName()}!"))
            }
        } else {
            requests.delete(FriendRequest(target.getUniqueId(), player.getUniqueId()))
            friends.save(Friend(player.getUniqueId(), target.getUniqueId()))
            player.sendMessage(Component.text("You are now friends with ${target.getName()}!"))
            if (target.isOnline()) {
                request.friendName.asOnlinePlayer(playerApi)
                    .sendMessage(Component.text("You are now friends with ${player.getName()}!"))
            }
        }
        return inviteFriendResponse { }
    }

    override suspend fun approveFriend(request: ApproveFriendRequest): ApproveFriendResponse {
        val player = request.playerId.asUUID().asOnlinePlayer(playerApi)
        val target = request.friendName.asPlayer(playerApi)

        if (target == null) {
            player.sendMessage(
                Component.text("${request.friendName} has never joined the club.")
                    .color(TextColor.color(0xdc2626))
            )
            throw Status.FAILED_PRECONDITION.withDescription("Player ${request.friendName} is unknown.")
                .asRuntimeException()
        }
        if (!requests.findFor(player.getUniqueId()).any { it.sender == target.getUniqueId() }) {
            player.sendMessage(
                Component.text("No friend request from ${request.friendName} found.").color(TextColor.color(0xdc2626))
            )
            throw Status.FAILED_PRECONDITION.withDescription("No request from ${request.friendName} present.")
                .asRuntimeException()
        }

        requests.delete(FriendRequest(target.getUniqueId(), player.getUniqueId()))
        friends.save(Friend(player.getUniqueId(), target.getUniqueId()))
        player.sendMessage(Component.text("You are now friends with ${target.getName()}!"))
        if (target.isOnline()) {
            request.friendName.asOnlinePlayer(playerApi)
                .sendMessage(Component.text("You are now friends with ${player.getName()}!"))
        }
        return approveFriendResponse { }
    }

    override suspend fun rejectFriend(request: RejectFriendRequest): RejectFriendResponse {
        val player = request.playerId.asUUID().asOnlinePlayer(playerApi)
        val target = request.friendName.asPlayer(playerApi)

        if (target == null) {
            player.sendMessage(
                Component.text("Player ${request.friendName} has never joined the club.")
                    .color(TextColor.color(0xdc2626))
            )
            throw Status.FAILED_PRECONDITION.withDescription("Player ${request.friendName} is unknown.")
                .asRuntimeException()
        }

        if (!requests.findFor(player.getUniqueId()).any { it.sender == target.getUniqueId() }) {
            player.sendMessage(
                Component.text("No friend request from ${request.friendName} found.").color(TextColor.color(0xdc2626))
            )
            throw Status.FAILED_PRECONDITION.withDescription("No request from ${request.friendName} present.")
                .asRuntimeException()
        }

        requests.delete(FriendRequest(target.getUniqueId(), player.getUniqueId()))
        player.sendMessage(
            Component.text("You rejected ${target.getName()}'s friend request.").color(TextColor.color(0xdc2626))
        )
        return rejectFriendResponse { }
    }

    override suspend fun removeFriend(request: RemoveFriendRequest): RemoveFriendResponse {
        val player = request.playerId.asUUID().asOnlinePlayer(playerApi)
        val target = request.friendName.asPlayer(playerApi)
        if (target == null) {
            player.sendMessage(
                Component.text("Player ${request.friendName} has never joined the club.")
                    .color(TextColor.color(0xdc2626))
            )
            throw Status.FAILED_PRECONDITION.withDescription("Player ${request.friendName} is unknown.")
                .asRuntimeException()
        }

        if (!friends.areFriends(player.getUniqueId(), target.getUniqueId())) {
            player.sendMessage(
                Component.text("You are not friends with ${request.friendName}.").color(TextColor.color(0xdc2626))
            )
            throw Status.FAILED_PRECONDITION.withDescription("Not friends with ${request.friendName}.")
                .asRuntimeException()
        }
        friends.delete(Friend(player.getUniqueId(), target.getUniqueId()))
        player.sendMessage(
            Component.text("You are not friends with ${target.getName()} anymore.").color(TextColor.color(0xdc2626))
        )
        return removeFriendResponse { }
    }

    override suspend fun jump(request: JumpRequest): JumpResponse {
        val player = request.playerId.asUUID().asOnlinePlayer(playerApi)
        val target = request.friendName.asOnlinePlayerNullable(playerApi)
        if (target == null) {
            player.sendMessage(
                Component.text("Player ${request.friendName} is not online.")
                    .color(TextColor.color(0xdc2626))
            )
            throw Status.FAILED_PRECONDITION.withDescription("Player ${request.friendName} is unknown.")
                .asRuntimeException()
        }

        if (!friends.areFriends(player.getUniqueId(), target.getUniqueId())) {
            player.sendMessage(
                Component.text("You are not friends with ${request.friendName}.").color(TextColor.color(0xdc2626))
            )
            throw Status.FAILED_PRECONDITION.withDescription("No request from ${request.friendName} present.")
                .asRuntimeException()
        }
        if (target.getConnectedServerName() != null) {
            playerApi.connectPlayer(player.getUniqueId(), target.getConnectedServerName()!!)
            player.sendMessage(Component.text("Connecting to ${target.getName()} playing on ${target.getConnectedServerName()}!"))
        }
        return jumpResponse { }
    }

    override suspend fun reply(request: ReplyRequest): ReplyResponse {
        val player = request.playerId.asUUID()
        message.sendReply(player, request.component.json)
        return replyResponse { }
    }

    override suspend fun sendMessage(request: SendMessageRequest): SendMessageResponse {
        val player = request.playerId.asUUID()
        val target = request.friendName
        message.sendMessage(player, target, request.component.json)
        return sendMessageResponse { }
    }

}