package club.mcsports.droplet.friends.message

data class MessageLayout(
    val receiving: String = "<color:#fbbf24><sender>: <reset><message>",
    val sending: String = "<color:#fbbf24>You -> <receiver>: <reset><message>"
)
