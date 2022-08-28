package com.cjmobileapps.rsocketjwtsecurityexample

import com.cjmobileapps.rsocketjwtsecurityexample.token.HelloRole
import com.cjmobileapps.rsocketjwtsecurityexample.token.HelloUser
import com.cjmobileapps.rsocketjwtsecurityexample.token.TokenUtils
import com.cjmobileapps.rsocketjwtsecurityexample.token.UserToken
import io.rsocket.metadata.WellKnownMimeType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler
import org.springframework.messaging.rsocket.dataWithType
import org.springframework.messaging.rsocket.retrieveFlow
import org.springframework.security.rsocket.metadata.BearerTokenMetadata
import org.springframework.util.MimeType
import org.springframework.util.MimeTypeUtils
import reactor.core.publisher.SignalType
import java.net.URI
import java.util.function.Consumer
import kotlin.time.ExperimentalTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MessageControllerTest(
    @Autowired val rsocketBuilder: RSocketRequester.Builder,
    @LocalServerPort val serverPort: Int,
    @Autowired val rSocketStrategies: RSocketStrategies
) {

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun `test that messages API streams latest messages`() {
        val admin = HelloUser(userId = "9527", password = "password", role = HelloRole.ADMIN)

        val token: UserToken = TokenUtils.generateAccessToken(admin)!!

        val nonWorkingAuthenticationMimeType: MimeType =
            MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.string)

        val workingAuthenticationMimeType: MimeType = BearerTokenMetadata.BEARER_AUTHENTICATION_MIME_TYPE

        val responder = RSocketMessageHandler.responder(rSocketStrategies)

        runBlocking {
            val rSocketRequester = rsocketBuilder
                .setupRoute("api.v1.messages.stream")
                .setupData(admin.userId.toString())
                .setupMetadata(token.token!!, workingAuthenticationMimeType)
                .rsocketStrategies(rSocketStrategies)
                .rsocketConnector { connector -> connector.acceptor(responder) }
                .websocket(URI("ws://localhost:${serverPort}/rsocket"))

            rSocketRequester.rsocket()
                ?.onClose()
                ?.doOnError(Consumer { error: Throwable? -> println("Connection CLOSED") })
                ?.doFinally(Consumer { consumer: SignalType? -> println("Client DISCONNECTED") })
                ?.subscribe()


            rSocketRequester.route("api.v1.messages.stream")
                //?.metadata(token.token!!, workingAuthenticationMimeType)
                .dataWithType(flow {
                    emit(
                        "Hey from test class"
                    )
                })
                .retrieveFlow<Void>()
                .collect()
        }
    }
}
