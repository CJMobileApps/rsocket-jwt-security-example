package com.cjmobileapps.rsocketjwtsecurityexample.token

import com.fasterxml.jackson.annotation.JsonProperty

data class HelloToken(
    @JsonProperty("access_token") var accessToken: String = "",
    @JsonProperty("refresh_token") var refreshToken: String = ""
)
