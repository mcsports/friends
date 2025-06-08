package club.mcsports.droplet.friends.plugin.command

import club.mcsports.droplet.friends.api.FriendsApi
import club.mcsports.droplet.friends.plugin.FriendsVelocityPlugin
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import io.grpc.StatusException
import net.kyori.adventure.text.Component
import org.incendo.cloud.Command
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.parser.standard.StringParser.greedyStringParser
import org.incendo.cloud.parser.standard.StringParser.stringParser
import java.util.logging.Logger

class FriendChatCommand(
    commandManager: CommandManager<CommandSource>,
    private val api: FriendsApi.Coroutine,
    private val logger: Logger,
    aliases: List<String>
) {
    init {
        aliases.forEach { alias ->
            commandManager.command(
                commandManager.commandBuilder(alias)
                    .literal("chat")
                    .actualCommand()
            )
        }
        commandManager.command(
            commandManager.commandBuilder("msg")
                .actualCommand()
        )
    }

    private fun Command.Builder<CommandSource>.actualCommand(): Command.Builder<CommandSource> {
        return this.required("player", stringParser())
            .required("message", greedyStringParser())
            .suspendingHandler { context ->
                if (context.sender() !is Player) {
                    context.sender().sendMessage(Component.text("You need to be a player"))
                    return@suspendingHandler
                }
                val player = context.sender() as Player
                val target = context.get<String>("player")
                val message = FriendsVelocityPlugin.miniMessage.deserialize(context.get("message"))
                try {
                    api.getInteraction().message(player.uniqueId, target, message)
                } catch (e: StatusException) {
                    logger.warning(e.status.description)
                }
            }
    }
}