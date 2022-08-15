package com.cjmobileapps.rsocketjwtsecurityexample.token

import com.cjmobileapps.rsocketjwtsecurityexample.token.HelloRole.ADMIN
import com.cjmobileapps.rsocketjwtsecurityexample.token.HelloRole.USER
import org.springframework.stereotype.Repository

@Repository
class HelloUserRepository {
    private val userTable: MutableMap<String, HelloUser> = HashMap()

    init {
        val admin = HelloUser(userId = "9527", password = "password", role = ADMIN)
        userTable["9527"] = admin
        val user = HelloUser(userId = "0000", password = "Zero4", role = USER)
        userTable["0000"] = user
    }

    fun retrieve(userId: String): HelloUser? {
        return userTable[userId]
    }
}