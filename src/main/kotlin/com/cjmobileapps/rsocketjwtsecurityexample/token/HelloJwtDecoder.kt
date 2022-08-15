package com.cjmobileapps.rsocketjwtsecurityexample.token

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import reactor.core.publisher.Mono

class HelloJwtDecoder(
    private val reactiveJwtDecoder: ReactiveJwtDecoder
) : ReactiveJwtDecoder {

    @Autowired
    private lateinit var tokenRepository: HelloTokenRepository

    @Autowired
    private lateinit var helloJwtService: HelloJwtService


    override fun decode(token: String?): Mono<Jwt> {
        println("decode " + token)
        return reactiveJwtDecoder.decode(token)
    }
}