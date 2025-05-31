package club.mcsports.droplet.friends.api

import club.mcsports.droplet.friends.api.impl.coroutine.FriendsApiCoroutineImpl
import club.mcsports.droplet.friends.api.impl.future.FriendsApiFutureImpl

interface FriendsApi {
    interface Coroutine {
        fun getData(): DataApi.Coroutine
        fun getInteraction(): InteractionApi.Coroutine
    }

    interface Future {
        fun getData(): DataApi.Future
        fun getInteraction(): InteractionApi.Future
    }

    companion object {
        @JvmStatic
        fun createFutureApi(authSecret: String): Future {
            return createFutureApi(
                System.getenv("CONTROLLER_SECRET"),
                System.getenv("FRIENDS_HOST") ?: "0.0.0.0",
                System.getenv("FRIENDS_PORT")?.toInt() ?: 5832
            )
        }

        @JvmStatic
        fun createFutureApi(authSecret: String, host: String, port: Int): Future {
            return FriendsApiFutureImpl(authSecret, host, port)
        }

        @JvmStatic
        fun createCoroutineApi(): Coroutine {
            return createCoroutineApi(
                System.getenv("CONTROLLER_SECRET"),
                System.getenv("FRIENDS_HOST") ?: "0.0.0.0",
                System.getenv("FRIENDS_PORT")?.toInt() ?: 5832,
            )
        }

        @JvmStatic
        fun createCoroutineApi(authSecret: String, host: String, port: Int): Coroutine {
            return FriendsApiCoroutineImpl(authSecret, host, port)
        }
    }
}