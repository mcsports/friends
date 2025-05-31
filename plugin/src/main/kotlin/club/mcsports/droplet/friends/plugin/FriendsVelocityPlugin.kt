package club.mcsports.droplet.friends.plugin

import club.mcsports.droplet.friends.api.FriendsApi
import club.mcsports.droplet.friends.plugin.command.FriendCommand
import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.TypeLiteral
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Dependency
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.velocity.CloudInjectionModule
import org.incendo.cloud.velocity.VelocityCommandManager
import java.util.logging.Logger


@Plugin(
    id = "mcsports-friends", name = "Friends", version = "1.0.0", authors = ["ugede"], dependencies = [
        Dependency(id = "simplecloud-api")
    ]
)
class FriendsVelocityPlugin() {
    lateinit var server: ProxyServer

    lateinit var logger: Logger

    lateinit var api: FriendsApi.Coroutine
    lateinit var injector: Injector

    @Inject
    constructor(server: ProxyServer, logger: Logger, injector: Injector) : this() {
        this.server = server
        this.logger = logger
        this.injector = injector
    }

    @Subscribe
    fun onInit(event: ProxyInitializeEvent) {
        api = FriendsApi.createCoroutineApi()
        logger.info("Initializing mcsports-friends")
        val childInjector: Injector = this.injector.createChildInjector(
            CloudInjectionModule(
                CommandSource::class.java,
                ExecutionCoordinator.simpleCoordinator(),
                SenderMapper.identity()
            )
        )
        val commandManager: VelocityCommandManager<CommandSource> = childInjector.getInstance(
            Key.get(object : TypeLiteral<VelocityCommandManager<CommandSource>>() {
            })
        )
        FriendCommand(commandManager, api, logger)
    }

}