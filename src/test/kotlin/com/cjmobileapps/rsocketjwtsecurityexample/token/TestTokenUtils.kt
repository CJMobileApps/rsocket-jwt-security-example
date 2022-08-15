package com.cjmobileapps.rsocketjwtsecurityexample.token

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.io.IOException
import java.net.URISyntaxException

class TestTokenUtils {

    @Test
    @Throws(IOException::class, URISyntaxException::class)
    fun generateSetupToken() {
        val setup = HelloUser(userId = "9527", password = "password", role = HelloRole.ADMIN)
        val token: UserToken = TokenUtils.generateAccessToken(setup)!!
        val refreshToken: UserToken = TokenUtils.generateRefreshToken(setup)!!

        println("token: " + token.token)
        assertNotNull(token)
        assertNotNull(refreshToken)
    }
}
