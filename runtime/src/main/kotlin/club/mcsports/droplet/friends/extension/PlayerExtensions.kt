package club.mcsports.droplet.friends.extension

import app.simplecloud.droplet.player.api.CloudPlayer
import app.simplecloud.droplet.player.api.OfflineCloudPlayer
import app.simplecloud.droplet.player.api.PlayerApi
import io.grpc.Status
import io.grpc.StatusException
import java.util.*

suspend fun UUID.asOnlinePlayer(api: PlayerApi.Coroutine): CloudPlayer {
    return try {
        api.getOnlinePlayer(this)
    } catch (e: StatusException) {
        throw Status.FAILED_PRECONDITION.withDescription(e.status.description).asRuntimeException()
    }
}

suspend fun UUID.asOnlinePlayerNullable(api: PlayerApi.Coroutine): CloudPlayer? {
    return try {
        api.getOnlinePlayer(this)
    } catch (_: StatusException) {
        null
    }
}

suspend fun UUID.asPlayer(api: PlayerApi.Coroutine): OfflineCloudPlayer? {
    return try {
        api.getOfflinePlayer(this)
    } catch (_: StatusException) {
        null
    }
}

suspend fun String.asOnlinePlayer(api: PlayerApi.Coroutine): CloudPlayer {
    return try {
        api.getOnlinePlayer(this)
    } catch (e: StatusException) {
        throw Status.FAILED_PRECONDITION.withDescription(e.status.description).asRuntimeException()
    }
}

suspend fun String.asOnlinePlayerNullable(api: PlayerApi.Coroutine): CloudPlayer? {
    return try {
        api.getOnlinePlayer(this)
    } catch (_: StatusException) {
        null
    }
}

suspend fun String.asPlayer(api: PlayerApi.Coroutine): OfflineCloudPlayer? {
    return try {
        api.getOfflinePlayer(this)
    } catch (_: StatusException) {
        null
    }
}