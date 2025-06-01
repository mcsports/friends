package club.mcsports.droplet.friends.plugin.command

import club.mcsports.droplet.friends.api.FriendsApi
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import org.incendo.cloud.CommandManager
import java.util.logging.Logger

class FriendCommand(
    commandManager: CommandManager<CommandSource>,
    api: FriendsApi.Coroutine,
    logger: Logger
) {
    init {
        commandManager.command(
            commandManager.commandBuilder("friend").handler { context ->
                context.sender().sendMessage(Component.text("/friend add <player>"))
                context.sender().sendMessage(Component.text("/friend accept <player>"))
                context.sender().sendMessage(Component.text("/friend deny <player>"))
                context.sender().sendMessage(Component.text("/friend remove <player>"))
                context.sender().sendMessage(Component.text("/friend list"))
                context.sender().sendMessage(Component.text("/friend requests"))
            }
        )
        ListFriendsCommand(commandManager, api)
        ListRequestsCommand(commandManager, api)
        FriendAddCommand(commandManager, api, logger)
        FriendRemoveCommand(commandManager, api, logger)
        FriendDenyCommand(commandManager, api, logger)
        FriendAcceptCommand(commandManager, api, logger)
        FriendChatCommand(commandManager, api, logger)
        FriendReplyCommand(commandManager, api, logger)
    }
}