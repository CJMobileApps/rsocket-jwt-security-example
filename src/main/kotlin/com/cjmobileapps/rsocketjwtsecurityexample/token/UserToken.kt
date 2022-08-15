package com.cjmobileapps.rsocketjwtsecurityexample.token

data class UserToken(
    val tokenId: String? = null,
    val token: String? = null,
    val user: HelloUser? = null
)
