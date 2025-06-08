package club.mcsports.droplet.friends.plugin.command

import club.mcsports.droplet.friends.api.FriendsApi
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import io.grpc.StatusException
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.parser.standard.IntegerParser.integerParser

class ListFriendsCommand(
    commandManager: CommandManager<CommandSource>,
    api: FriendsApi.Coroutine,
    aliases: List<String>,
) {
    init {
        aliases.forEach { alias ->
            commandManager.command(
                commandManager.commandBuilder(alias)
                    .literal("list")
                    .optional("page", integerParser(1))
                    .optional("amount", integerParser(1, 50))
                    .suspendingHandler { context ->
                        if (context.sender() !is Player) {
                            context.sender().sendMessage(Component.text("You need to be a player"))
                            return@suspendingHandler
                        }
                        val player = context.sender() as Player
                        val page = context.getOrDefault("page", 1)
                        val amount = context.getOrDefault("amount", 50)
                        val friends = try {
                            api.getData().getFriends(player.uniqueId, page - 1, amount)
                        } catch (e: StatusException) {
                            context.sender().sendMessage(
                                Component.text(e.status.description ?: "An unknown error occurred.").color(
                                    TextColor.color(0xdc2626)
                                )
                            )
                            return@suspendingHandler
                        }
                        friends.friendList.forEach { friend ->
                            var base = Component.text("${friend.name} ")
                            base = if (friend.online) {
                                base.append(
                                    Component.text("● Online on ${friend.server}").color(TextColor.color(0xa3e635))
                                )
                            } else {
                                base.append(Component.text("● Offline").color(TextColor.color(0xdc2626)))
                            }
                            player.sendMessage(base)
                        }
                        player.sendMessage(Component.empty())
                        player.sendMessage(
                            Component.text("Showing ${friends.friendList.size} item(s) on page $page / ${friends.pages}.")
                                .color(TextColor.color(0xa3a3a3))
                        )
                    }
            )
        }
    }
}