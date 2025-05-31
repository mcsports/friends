package club.mcsports.droplet.friends.api.impl.future

import app.simplecloud.droplet.api.auth.AuthCallCredentials
import club.mcsports.droplet.friends.api.DataApi
import club.mcsports.droplet.friends.api.FriendsApi
import club.mcsports.droplet.friends.api.InteractionApi
import io.grpc.ManagedChannelBuilder

class FriendsApiFutureImpl(
    authSecret: String,
    host: String,
    port: Int
) : FriendsApi.Future {


    private val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build()
    private val credentials = AuthCallCredentials(authSecret)
    private val dataApi = FriendsDataApiFutureImpl(channel, credentials)
    private val interactionApi = FriendsInteractionApiFutureImpl(channel, credentials)

    override fun getData(): DataApi.Future {
        return dataApi
    }

    override fun getInteraction(): InteractionApi.Future {
        return interactionApi
    }
}