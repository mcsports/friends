package club.mcsports.droplet.friends.message

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor

data class MessageLayout(
    val receiving: String = "$prefixString<sender>: <reset><message>",
    val sending: String = "${prefixString}You -> <receiver>: <reset><message>"
) {
    companion object {
        const val prefixString = "<color:#fbbf24>\uD83D\uDC64 "
        val prefix = Component.text("\uD83D\uDC64").color(TextColor.color(0xfbbf24)).append(
            Component.text(" ").color(
                NamedTextColor.WHITE
            )
        )
    }
}
