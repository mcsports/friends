package club.mcsports.droplet.friends.launcher

import app.simplecloud.droplet.api.secret.AuthFileSecretFactory
import club.mcsports.droplet.friends.FriendRuntime
import com.github.ajalt.clikt.command.SuspendingCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.clikt.sources.PropertiesValueSource
import com.github.ajalt.clikt.sources.ValueSource
import java.io.File
import java.nio.file.Path



class FriendsStartCommand : SuspendingCliktCommand() {

    init {
        context {
            valueSource = PropertiesValueSource.from(File("friends.properties"), false, ValueSource.envvarKey())
        }
    }

    private val defaultDatabaseUrl = "jdbc:sqlite:database.db"

    val databaseUrl: String by option(help = "Database URL (default: ${defaultDatabaseUrl})", envvar = "DATABASE_URL")
        .default(defaultDatabaseUrl)

    val grpcHost: String by option(help = "Grpc host (default: 0.0.0.0)", envvar = "GRPC_HOST").default("0.0.0.0")
    val grpcPort: Int by option(help = "Grpc port (default: 5832)", envvar = "GRPC_PORT").int().default(5832)

    val controllerGrpcHost: String by option(
        help = "Controller Grpc host (default: 0.0.0.0)",
        envvar = "CONTROLLER_GRPC_HOST"
    ).default("0.0.0.0")
    val controllerGrpcPort: Int by option(
        help = "Controller Grpc port (default: 5816)",
        envvar = "CONTROLLER_GRPC_PORT"
    ).int().default(5816)

    val pubSubGrpcPort: Int by option(help = "PubSub Grpc port (default: 5817)", envvar = "PUBSUB_GRPC_PORT").int()
        .default(5817)

    val playerPubSubGrpcPort: Int by option(
        help = "Player PubSub Grpc port (default: 5827)", envvar = "PLAYER_PUBSUB_GRPC_PORT"
    ).int().default(5827)
    val playerGrpcPort: Int by option(help = "Player Grpc port (default: 5826)", envvar = "PLAYER_GRPC_PORT").int()
        .default(5826)

    val playerGrpcHost: String by option(
        help = "Player Grpc host (default: 0.0.0.0)",
        envvar = "PLAYER_GRPC_HOST"
    ).default("0.0.0.0")

    val authorizationPort: Int by option(
        help = "Authorization port (default: 5818)", envvar = "AUTHORIZATION_PORT"
    ).int().default(5818)

    private val authSecretPath: Path by option(
        help = "Path to auth secret file (default: .auth.secret)", envvar = "AUTH_SECRET_PATH"
    ).path().default(Path.of(".secrets", "auth.secret"))

    val authSecret: String by option(
        help = "Auth secret",
        envvar = "AUTH_SECRET_KEY"
    ).defaultLazy { AuthFileSecretFactory.loadOrCreate(authSecretPath) }

    override suspend fun run() {
        FriendRuntime(this).start()
    }

}