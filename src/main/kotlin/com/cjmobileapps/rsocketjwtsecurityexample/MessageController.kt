package com.cjmobileapps.rsocketjwtsecurityexample

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller

@MessageMapping("api.v1.messages")
@Controller
class MessageController {

    @MessageMapping("stream")
    suspend fun receive(
        @Payload inboundMessages: Flow<String>,
        @AuthenticationPrincipal jwt: String
    ) {
        println("MessageController: jwt: $jwt")
        println("MessageController: inbound message: " + inboundMessages.first())
    }
}
