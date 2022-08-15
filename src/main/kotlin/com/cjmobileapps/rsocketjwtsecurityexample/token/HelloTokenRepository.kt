package com.cjmobileapps.rsocketjwtsecurityexample.token

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.stereotype.Repository
import java.util.concurrent.TimeUnit

@Repository
class HelloTokenRepository {
    var accessTokenTable: Cache<String, HelloUser> = CacheBuilder.newBuilder()
        .expireAfterWrite(TokenUtils.ACCESS_EXPIRE, TimeUnit.MINUTES).build()
    var freshTokenTable: Cache<String, HelloUser> = CacheBuilder.newBuilder()
        .expireAfterWrite(TokenUtils.REFRESH_EXPIRE, TimeUnit.DAYS).build()

    fun storeAccessToken(tokenId: String?, user: HelloUser) {
        if (tokenId != null) {
            accessTokenTable.put(tokenId, user)
        }
    }

    fun storeFreshToken(tokenId: String?, user: HelloUser) {
        freshTokenTable.put(tokenId!!, user)
    }

    fun getAuthFromAccessToken(tokenId: String?): HelloUser? {
        return accessTokenTable.getIfPresent(tokenId!!)
    }

    fun getAuthFromRefreshToken(tokenId: String?): HelloUser? {
        return freshTokenTable.getIfPresent(tokenId!!)
    }

    fun deleteAccessToken(tokenId: String?) {
        accessTokenTable.invalidate(tokenId!!)
    }

    fun deleteFreshToken(tokenId: String?) {
        freshTokenTable.invalidate(tokenId!!)
    }
}
