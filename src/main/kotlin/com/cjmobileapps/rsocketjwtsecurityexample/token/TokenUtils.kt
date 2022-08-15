package com.cjmobileapps.rsocketjwtsecurityexample.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.spec.SecretKeySpec

object TokenUtils {
    // access token timeout
    const val ACCESS_EXPIRE: Long = 5

    // refresh token timeout
    const val REFRESH_EXPIRE: Long = 7

    /*
     * Use base64 to generate secret string.
     * The HS256 secret key of length, which should be at least 256 bits.
     * As 1 character = 8 bits, the characters should be more than 32.
     *
     * ▶ input=jwt_token-based_openapi_for_rsocket_access_token
     * ▶ secret=$(echo -n $input | openssl base64)
     * ▶ echo $secret
     * and0X3Rva2VuLWJhc2VkX29wZW5hcGlfZm9yX3Jzb2NrZXRfYWNjZXNzX3Rva2Vu
     * ▶ echo $secret | openssl base64 -d
     * jwt_token-based_openapi_for_rsocket_access_token
     *
     * ▶ input=jwt_token-based_openapi_4_rsocket_refresh_token
     * ▶ echo $secret
     * and0X3Rva2VuLWJhc2VkX29wZW5hcGlfNF9yc29ja2V0X3JlZnJlc2hfdG9rZW4=
     */
    private const val ACCESS_SECRET_KEY = "and0X3Rva2VuLWJhc2VkX29wZW5hcGlfZm9yX3Jzb2NrZXRfYWNjZXNzX3Rva2Vu"
    private const val REFRESH_SECRET_KEY = "and0X3Rva2VuLWJhc2VkX29wZW5hcGlfNF9yc29ja2V0X3JlZnJlc2hfdG9rZW4="
    private val MAC_ALGORITHM = MacAlgorithm.HS256
    private const val HMAC_SHA_256 = "HmacSHA256"
    fun generateAccessToken(user: HelloUser): UserToken? {
        val ACCESS_ALGORITHM: Algorithm = Algorithm.HMAC256(TokenUtils.ACCESS_SECRET_KEY)
        return generateToken(user, ACCESS_ALGORITHM, TokenUtils.ACCESS_EXPIRE, ChronoUnit.MINUTES)
    }

    val accessTokenDecoder: ReactiveJwtDecoder
        get() {
            val secretKey = SecretKeySpec(ACCESS_SECRET_KEY.toByteArray(), HMAC_SHA_256)
            return NimbusReactiveJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MAC_ALGORITHM)
                .build()
        }

    fun jwtAccessTokenDecoder(): ReactiveJwtDecoder {
        return HelloJwtDecoder(accessTokenDecoder)
    }

    fun generateRefreshToken(user: HelloUser?): UserToken? {
        val REFRESH_ALGORITHM: Algorithm = Algorithm.HMAC256(REFRESH_SECRET_KEY)
        if(user == null) return null
        return generateToken(user, REFRESH_ALGORITHM, REFRESH_EXPIRE, ChronoUnit.DAYS)
    }

    val refreshTokenDecoder: ReactiveJwtDecoder
        get() {
            val secretKey = SecretKeySpec(REFRESH_SECRET_KEY.toByteArray(), HMAC_SHA_256)
            return NimbusReactiveJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MAC_ALGORITHM)
                .build()
        }

    private fun generateToken(user: HelloUser, algorithm: Algorithm, expire: Long, unit: ChronoUnit): UserToken? {
        val tokenId = UUID.randomUUID().toString()
        val instant: Instant
        val now = Instant.now()
        instant = if (now.isSupported(unit)) {
            now.plus(expire, unit)
        } else {
            println("unit param is not supported")
            return null
        }
        val token: String = JWT.create()
            .withJWTId(tokenId)
            .withSubject(user.userId)
            .withClaim("scope", user.role)
            .withExpiresAt(Date.from(instant))
            .sign(algorithm)
        return UserToken(tokenId = tokenId, token = token, user = user)
    }
}
