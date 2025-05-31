package club.mcsports.droplet.friends.launcher

import com.github.ajalt.clikt.command.main
import org.apache.logging.log4j.LogManager

suspend fun main(args: Array<String>) {
    configureLog4j()
    FriendsStartCommand().main(args)
}

fun configureLog4j() {
    val globalExceptionHandlerLogger = LogManager.getLogger("GlobalExceptionHandler")
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        globalExceptionHandlerLogger.error("Uncaught exception in thread ${thread.name}", throwable)
    }
}