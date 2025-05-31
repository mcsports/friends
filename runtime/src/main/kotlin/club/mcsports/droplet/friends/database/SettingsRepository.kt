package club.mcsports.droplet.friends.database

import app.simplecloud.plugin.api.shared.repository.LoadableRepository
import club.mcsports.droplet.friends.generated.db.tables.FriendSettings.Companion.FRIEND_SETTINGS
import java.util.*

data class FriendSettings(
    val player: UUID,
    val seeFriendJoins: Boolean = true,
)

class SettingsRepository(
    private val db: Database,
) : LoadableRepository<UUID, FriendSettings> {

    private val settings = mutableListOf<FriendSettings>()

    override fun load(): List<FriendSettings> {
        db.context.selectFrom(FRIEND_SETTINGS).fetchInto(FRIEND_SETTINGS).forEach { it ->
            settings.add(
                FriendSettings(
                    UUID.fromString(it.uniqueId), it.seeJoins == true
                )
            )
        }
        return settings
    }

    override fun delete(element: FriendSettings): Boolean {
        if (!settings.removeIf { it.player == element.player }) return false
        return db.context.deleteFrom(FRIEND_SETTINGS).where(FRIEND_SETTINGS.UNIQUE_ID.eq(element.player.toString()))
            .execute() > 0
    }

    override fun save(element: FriendSettings) {
        settings.removeIf { it.player == element.player }
        settings.add(element)
        db.context.insertInto(
            FRIEND_SETTINGS,

            FRIEND_SETTINGS.UNIQUE_ID,
            FRIEND_SETTINGS.SEE_JOINS
        ).values(element.player.toString(), element.seeFriendJoins).onDuplicateKeyUpdate()
            .set(FRIEND_SETTINGS.SEE_JOINS, element.seeFriendJoins).execute()
    }

    override fun find(identifier: UUID): FriendSettings? {
        return settings.find { it.player == identifier }
    }

    override fun getAll(): List<FriendSettings> {
        return settings
    }
}