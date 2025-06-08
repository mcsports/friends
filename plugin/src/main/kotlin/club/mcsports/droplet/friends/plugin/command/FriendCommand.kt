package club.mcsports.droplet.friends.plugin.command

import club.mcsports.droplet.friends.api.FriendsApi
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.incendo.cloud.CommandManager
import java.util.logging.Logger

class FriendCommand(
    commandManager: CommandManager<CommandSource>,
    api: FriendsApi.Coroutine,
    logger: Logger
) {
    private val aliases = listOf("friend", "f")

    init {
        aliases.forEach { alias ->
            commandManager.command(
                commandManager.commandBuilder(alias).handler { context ->
                    context.sender().sendMessage(
                        Component.text("\uD83D\uDC64 ").color(TextColor.color(0xfbbf24))
                            .append(Component.text("Friend Commands").color(NamedTextColor.WHITE))
                    )
                    context.sender()
                        .sendMessage(Component.text("/$alias add <player>").color(TextColor.color(0xa3a3a3)))
                    context.sender()
                        .sendMessage(Component.text("/$alias accept <player>").color(TextColor.color(0xa3a3a3)))
                    context.sender()
                        .sendMessage(Component.text("/$alias deny <player>").color(TextColor.color(0xa3a3a3)))
                    context.sender()
                        .sendMessage(Component.text("/$alias remove <player>").color(TextColor.color(0xa3a3a3)))
                    context.sender()
                        .sendMessage(Component.text("/$alias list <page> <amount>").color(TextColor.color(0xa3a3a3)))
                    context.sender()
                        .sendMessage(
                            Component.text("/$alias requests <page> <amount>").color(TextColor.color(0xa3a3a3))
                        )
                }
            )
        }
        ListFriendsCommand(commandManager, api, aliases)
        ListRequestsCommand(commandManager, api, aliases)
        FriendAddCommand(commandManager, api, logger, aliases)
        FriendRemoveCommand(commandManager, api, logger, aliases)
        FriendDenyCommand(commandManager, api, logger, aliases)
        FriendAcceptCommand(commandManager, api, logger, aliases)
        FriendChatCommand(commandManager, api, logger, aliases)
        FriendReplyCommand(commandManager, api, logger, aliases)
    }
}