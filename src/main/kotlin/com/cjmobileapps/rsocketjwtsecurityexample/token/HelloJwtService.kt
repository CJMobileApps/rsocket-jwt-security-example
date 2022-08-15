package com.cjmobileapps.rsocketjwtsecurityexample.token

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class HelloJwtService(
    @Autowired private val userRepository: HelloUserRepository,
    @Autowired private val tokenRepository: HelloTokenRepository
) {
    var tokenId: String? = null

    fun authenticate(principal: String, credential: String): HelloUser? {
        println("principal=$principal,credential=$credential")
        try {
            val user: HelloUser? = userRepository.retrieve(principal)
            if (user?.password == credential) {
                return user
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }

    fun authenticate(refreshToken: String?): Mono<HelloUser> {
        val reactiveJwtDecoder: ReactiveJwtDecoder = TokenUtils.refreshTokenDecoder
        return reactiveJwtDecoder.decode(refreshToken).map { jwt: Jwt ->
            try {
                val user: HelloUser = HelloUser(userId = jwt.subject, role = jwt.getClaim("scope"))
                val auth = tokenRepository.getAuthFromRefreshToken(jwt.id)

                if (user.userId == auth?.userId && user.role == auth?.role) {
                    return@map user
                }
            } catch (e: Exception) {
                println(e)
                return@map null
            }
            HelloUser()
        }
    }

    fun signToken(user: HelloUser): HelloToken {
        val token = HelloToken()
        val accessToken: UserToken = TokenUtils.generateAccessToken(user)!!
        tokenRepository.storeAccessToken(accessToken.tokenId, accessToken.user!!)
        val refreshToken: UserToken = TokenUtils.generateRefreshToken(user)!!
        tokenRepository.storeFreshToken(refreshToken.tokenId, refreshToken.user!!)
        token.accessToken = accessToken.token!!
        token.refreshToken = refreshToken.token!!
        return token
    }

    fun revokeAccessToken() {
        tokenRepository.deleteAccessToken(tokenId)
    }
}
