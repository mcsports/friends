package club.mcsports.droplet.friends

import app.simplecloud.droplet.api.auth.AuthCallCredentials
import app.simplecloud.droplet.api.auth.AuthSecretInterceptor
import app.simplecloud.droplet.api.droplet.Droplet
import app.simplecloud.droplet.player.api.PlayerApi
import build.buf.gen.simplecloud.controller.v1.ControllerDropletServiceGrpcKt
import club.mcsports.droplet.friends.controller.Attacher
import club.mcsports.droplet.friends.database.DatabaseFactory
import club.mcsports.droplet.friends.database.FriendsRepository
import club.mcsports.droplet.friends.database.RequestsRepository
import club.mcsports.droplet.friends.database.SettingsRepository
import club.mcsports.droplet.friends.launcher.FriendsStartCommand
import club.mcsports.droplet.friends.service.FriendDataService
import club.mcsports.droplet.friends.service.FriendInteractionService
import io.grpc.ManagedChannelBuilder
import io.grpc.Server
import io.grpc.ServerBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.apache.logging.log4j.LogManager

class FriendRuntime(
    private val args: FriendsStartCommand,
) {

    private val database = DatabaseFactory.createDatabase(args.databaseUrl)
    private val logger = LogManager.getLogger(FriendRuntime::class.java)
    private val friends = FriendsRepository(database)
    private val requests = RequestsRepository(database)
    private val settings = SettingsRepository(database)

    private val api = PlayerApi.createCoroutineApi(args.authSecret)

    private val server = createGrpcServer()
    private val callCredentials = AuthCallCredentials(args.authSecret)
    private val channel =
        ManagedChannelBuilder.forAddress(args.controllerGrpcHost, args.controllerGrpcPort).usePlaintext().build()
    private val controllerStub = ControllerDropletServiceGrpcKt.ControllerDropletServiceCoroutineStub(channel)
        .withCallCredentials(callCredentials)
    private val attacher =
        Attacher(Droplet("friends", "internal-friends", args.grpcHost, args.grpcPort, 8081), channel, controllerStub)


    suspend fun start() {
        logger.info("Starting friend droplet")
        logger.info("Setting up database...")
        database.setup()
        logger.info("Loading initial data...")
        friends.load()
        requests.load()
        settings.load()
        logger.info("Attaching to Controller...")
        attacher.enforceAttachBlocking()
        attacher.enforceAttach()
        startGrpcServer()

        suspendCancellableCoroutine { continuation ->
            Runtime.getRuntime().addShutdownHook(Thread {
                server.shutdown()
                continuation.resume(Unit) { cause, _, _ ->
                    logger.info("Server shutdown due to: $cause")
                }
            })
        }
    }

    private fun startGrpcServer() {
        logger.info("Starting gRPC server...")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                server.start()
                server.awaitTermination()
            } catch (e: Exception) {
                logger.error("Error in gRPC server", e)
                throw e
            }
        }
    }

    private fun createGrpcServer(): Server {
        return ServerBuilder.forPort(args.grpcPort)
            .addService(FriendInteractionService(api, friends, requests, settings))
            .addService(FriendDataService(api, friends, requests))
            .intercept(AuthSecretInterceptor(args.controllerGrpcHost, args.authorizationPort))
            .build()
    }
}