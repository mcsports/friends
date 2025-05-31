package club.mcsports.droplet.friends.plugin.command

import club.mcsports.droplet.friends.api.FriendsApi
import club.mcsports.droplet.friends.plugin.FriendsVelocityPlugin
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import io.grpc.StatusException
import net.kyori.adventure.text.Component
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.parser.standard.StringParser.greedyStringParser
import org.incendo.cloud.parser.standard.StringParser.stringParser
import java.util.logging.Logger

class FriendReplyCommand(
    commandManager: CommandManager<CommandSource>,
    api: FriendsApi.Coroutine,
    logger: Logger,
) {
    init {
        commandManager.command(
            commandManager.commandBuilder("friend")
                .literal("reply")
                .required("message", greedyStringParser())
                .suspendingHandler { context ->
                    if (context.sender() !is Player) {
                        context.sender().sendMessage(Component.text("You need to be a player"))
                        return@suspendingHandler
                    }
                    val player = context.sender() as Player
                    val message = FriendsVelocityPlugin.miniMessage.deserialize(context.get("message"))
                    try {
                        api.getInteraction().reply(player.uniqueId, message)
                    } catch (e: StatusException) {
                        logger.warning(e.status.description)
                    }
                })
    }
}