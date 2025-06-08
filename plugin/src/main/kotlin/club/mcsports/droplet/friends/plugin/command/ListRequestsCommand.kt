package club.mcsports.droplet.friends.plugin.command

import club.mcsports.droplet.friends.api.FriendsApi
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import io.grpc.StatusException
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor
import org.incendo.cloud.CommandManager
import org.incendo.cloud.kotlin.coroutines.extension.suspendingHandler
import org.incendo.cloud.parser.standard.IntegerParser.integerParser

class ListRequestsCommand(
    commandManager: CommandManager<CommandSource>,
    api: FriendsApi.Coroutine,
    aliases: List<String>,
) {

    init {
        aliases.forEach { alias ->
            commandManager.command(
                commandManager.commandBuilder(alias)
                    .literal("requests")
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
                        val requests = try {
                            api.getData().getRequests(player.uniqueId, page - 1, amount)
                        } catch (e: StatusException) {
                            context.sender().sendMessage(
                                Component.text(e.status.description ?: "An unknown error occurred.").color(
                                    TextColor.color(0xdc2626)
                                )
                            )
                            return@suspendingHandler
                        }
                        requests.friendList.forEach { friend ->
                            player.sendMessage(
                                Component.text("${friend.name} ")
                                    .append(
                                        Component.text("Accept").color(TextColor.color(0xa3e635)).clickEvent(
                                            ClickEvent.runCommand("friend accept ${friend.name}")
                                        ).hoverEvent(HoverEvent.showText(Component.text("Befriend ${friend.name}")))
                                            .append(
                                                Component.text(" | ").color(TextColor.color(0xa3a3a3)).append(
                                                    Component.text("Deny").clickEvent(
                                                        ClickEvent.runCommand("friend deny ${friend.name}")
                                                    )
                                                        .hoverEvent(HoverEvent.showText(Component.text("Ignore ${friend.name}")))
                                                        .color(TextColor.color(0xdc2626))
                                                )
                                            )
                                    )
                            )
                        }
                        player.sendMessage(Component.empty())
                        player.sendMessage(
                            Component.text("Showing ${requests.friendList.size} item(s) on page $page / ${requests.pages}.")
                                .color(TextColor.color(0xa3a3a3))
                        )
                    }
            )
        }
    }
}