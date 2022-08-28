package com.cjmobileapps.rsocketjwtsecurityexample

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.annotation.ConnectMapping
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Controller
import reactor.core.publisher.SignalType
import javax.annotation.PreDestroy


@MessageMapping("api.v1.messages")
@Controller
class MessageController {

    val rsocketRequesterMap = mutableMapOf<String, RSocketRequester>()

    @PreAuthorize("hasRole('ADMIN')")
    @MessageMapping("stream")
    suspend fun receive(
        @Payload inboundMessages: Flow<String>,
        @AuthenticationPrincipal jwt: Jwt,
//        @AuthenticationPrincipal authenticationPrincipal: AuthenticationPrincipal
    ) {
        val userId = jwt.subject

        if (rsocketRequesterMap.containsKey(userId)) {
            println("MessageController: jwt: ${jwt.subject}")
            println("MessageController: inbound message: " + inboundMessages.first())
        }
    }

    @Suppress("unused")
    @ConnectMapping("stream")
    fun connect(rsocketRequester: RSocketRequester, userId: String) {
        println("connected with userId $userId")
        rsocketRequester.rsocket()
            ?.onClose()
            ?.doOnError { error: Throwable? -> println("Connection CLOSED") }
            ?.doFinally { consumer: SignalType? ->
                println("Client DISCONNECTED")
            }
            ?.subscribe()

        rsocketRequesterMap[userId] = rsocketRequester
    }

    @PreDestroy
    fun shutdown() {
        println("Detaching all remaining clients...")
        rsocketRequesterMap.clear()
        println("Shutting down.")
    }
}
