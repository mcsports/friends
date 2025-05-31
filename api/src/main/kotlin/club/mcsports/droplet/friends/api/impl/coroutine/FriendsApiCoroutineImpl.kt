package club.mcsports.droplet.friends.api.impl.coroutine

import app.simplecloud.droplet.api.auth.AuthCallCredentials
import club.mcsports.droplet.friends.api.DataApi
import club.mcsports.droplet.friends.api.FriendsApi
import club.mcsports.droplet.friends.api.InteractionApi
import io.grpc.ManagedChannelBuilder

class FriendsApiCoroutineImpl(
    authSecret: String,
    host: String,
    port: Int
) : FriendsApi.Coroutine {

    private val channel = ManagedChannelBuilder.forAddress(host, port)
        .usePlaintext()
        .build()
    private val credentials = AuthCallCredentials(authSecret)
    private val dataApi = FriendsDataApiCoroutineImpl(credentials, channel)
    private val interactionApi = FriendsInteractionApiCoroutineImpl(credentials, channel)


    override fun getData(): DataApi.Coroutine {
        return dataApi
    }

    override fun getInteraction(): InteractionApi.Coroutine {
        return interactionApi
    }
}