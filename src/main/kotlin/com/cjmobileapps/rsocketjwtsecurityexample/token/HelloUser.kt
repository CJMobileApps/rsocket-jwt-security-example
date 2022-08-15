package com.cjmobileapps.rsocketjwtsecurityexample.token

import com.fasterxml.jackson.annotation.JsonProperty

data class HelloUser(
    @JsonProperty("user_id")  val userId: String? = null,
    val password: String? = null,
    val role: String? = null
)
