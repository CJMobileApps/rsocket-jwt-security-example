package com.cjmobileapps.rsocketjwtsecurityexample

import com.cjmobileapps.rsocketjwtsecurityexample.token.HelloRole
import com.cjmobileapps.rsocketjwtsecurityexample.token.HelloUser
import com.cjmobileapps.rsocketjwtsecurityexample.token.TokenUtils
import com.cjmobileapps.rsocketjwtsecurityexample.token.UserToken
import io.rsocket.metadata.WellKnownMimeType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.dataWithType
import org.springframework.messaging.rsocket.retrieveFlow
import org.springframework.security.rsocket.metadata.BearerTokenMetadata
import org.springframework.util.MimeType
import org.springframework.util.MimeTypeUtils
import java.net.URI
import kotlin.time.ExperimentalTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MessageControllerTest(
    @Autowired val rsocketBuilder: RSocketRequester.Builder,
    @LocalServerPort val serverPort: Int
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


        runBlocking {
            val rSocketRequester = rsocketBuilder.websocket(URI("ws://localhost:${serverPort}/rsocket"))

            launch {

                rSocketRequester.route("api.v1.messages.stream")
                    .metadata(token.token!!, workingAuthenticationMimeType)
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
}