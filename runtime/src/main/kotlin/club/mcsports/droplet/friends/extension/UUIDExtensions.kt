package club.mcsports.droplet.friends.extension

import io.grpc.Status
import java.util.*

fun String.asUUID(): UUID {
    return try {
        UUID.fromString(this)
    } catch (_: IllegalArgumentException) {
        throw Status.FAILED_PRECONDITION.withDescription("No player UUID provided").asRuntimeException()
    }
}