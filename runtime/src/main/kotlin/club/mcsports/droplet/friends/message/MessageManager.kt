package club.mcsports.droplet.friends.message

import app.simplecloud.droplet.player.api.CloudPlayer
import app.simplecloud.droplet.player.api.PlayerApi
import club.mcsports.droplet.friends.database.FriendsRepository
import club.mcsports.droplet.friends.extension.asOnlinePlayer
import club.mcsports.droplet.friends.extension.asOnlinePlayerNullable
import club.mcsports.droplet.friends.extension.asPlayer
import io.grpc.Status
import io.grpc.StatusRuntimeException
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.apache.logging.log4j.LogManager
import java.util.*

class MessageManager(
    private val playerApi: PlayerApi.Coroutine,
    private val friends: FriendsRepository,
    private val layout: MessageLayout = MessageLayout(),
) {
    companion object {
        private val miniMessage = MiniMessage.miniMessage()
    }

    private val componentSerializer: GsonComponentSerializer = GsonComponentSerializer.gson()
    private val lastMessaged = mutableMapOf<UUID, UUID>()
    private val logger = LogManager.getLogger(MessageManager::class.java)

    private fun sendMessage(sender: CloudPlayer, receiver: CloudPlayer, message: Component) {
        val messageTag = TagResolver.resolver("message", Tag.selfClosingInserting(message))
        val senderTag = TagResolver.resolver("sender", Tag.inserting(Component.text(sender.getName())))
        val receiverTag = TagResolver.resolver("receiver", Tag.inserting(Component.text(receiver.getName())))
        val senderComponent = miniMessage.deserialize(layout.sending, messageTag, senderTag, receiverTag)
        val receiverComponent = miniMessage.deserialize(layout.receiving, messageTag, senderTag, receiverTag)
        sender.sendMessage(senderComponent)
        receiver.sendMessage(receiverComponent)
    }

    suspend fun sendMessage(sending: UUID, receiver: String, messageJson: String) {
        val receiverHandle = receiver.asPlayer(playerApi)
        val senderHandle = try {
            sending.asOnlinePlayer(playerApi)
        } catch (e: StatusRuntimeException) {
            logger.warn("Tried to send a private message for a player that is not online (${sending})")
            throw e
        }
        if (receiverHandle == null || !receiverHandle.isOnline()) {
            senderHandle.sendMessage(
                Component.text("This player is not online.").color(TextColor.color(0xdc2626))
            )
            throw StatusRuntimeException(Status.FAILED_PRECONDITION.withDescription("Receiver $receiver is not online."))
        }
        if (!friends.areFriends(sending, receiverHandle.getUniqueId())) {
            senderHandle.sendMessage(
                Component.text("You are not friends with $receiver.").color(TextColor.color(0xdc2626))
            )
            throw StatusRuntimeException(Status.FAILED_PRECONDITION.withDescription("Not friends with $receiver."))
        }

        sendMessage(
            senderHandle,
            receiverHandle.getUniqueId().asOnlinePlayer(playerApi),
            componentSerializer.deserialize(messageJson)
        )
        lastMessaged[sending] = receiverHandle.getUniqueId()
        lastMessaged[receiverHandle.getUniqueId()] = sending
    }

    private suspend fun sendReply(sender: CloudPlayer, message: Component) {
        val receiver = lastMessaged[sender.getUniqueId()]
        if (receiver == null) {
            sender.sendMessage(
                Component.text("Found no prior conversation to reply to.").color(TextColor.color(0xdc2626))
            )
            return
        }
        val receiverHandle = receiver.asPlayer(playerApi)
        if (receiverHandle == null || !receiverHandle.isOnline()) {
            sender.sendMessage(Component.text("The target player is not online.").color(TextColor.color(0xdc2626)))
            lastMessaged.remove(sender.getUniqueId())
            lastMessaged.remove(receiver)
            return
        }
        if (!friends.areFriends(sender.getUniqueId(), receiverHandle.getUniqueId())) {
            sender.sendMessage(
                Component.text("You are not friends with ${receiverHandle.getName()}.").color(TextColor.color(0xdc2626))
            )
            throw StatusRuntimeException(Status.FAILED_PRECONDITION.withDescription("Not friends with ${receiverHandle.getName()}."))
        }
        sendMessage(sender, receiverHandle.getUniqueId().asOnlinePlayer(playerApi), message)
    }

    suspend fun sendReply(sending: UUID, messageJson: String) {
        val senderHandle = sending.asOnlinePlayerNullable(playerApi)
        if (senderHandle == null) {
            logger.warn("Tried to send a private message reply for a player that is not online (${sending})")
            lastMessaged.remove(sending)
            return
        }
        sendReply(senderHandle, componentSerializer.deserialize(messageJson))
    }
}