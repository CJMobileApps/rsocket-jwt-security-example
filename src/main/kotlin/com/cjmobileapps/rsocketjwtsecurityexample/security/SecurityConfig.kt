package com.cjmobileapps.rsocketjwtsecurityexample.security

import com.cjmobileapps.rsocketjwtsecurityexample.token.TokenUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity
import org.springframework.security.config.annotation.rsocket.RSocketSecurity
import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor
import org.springframework.web.util.pattern.PathPatternRouteMatcher

@Configuration
@EnableRSocketSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {

    @Bean
    fun authorization(rsocketSecurity: RSocketSecurity): PayloadSocketAcceptorInterceptor {
        val security: RSocketSecurity =
            rsocketSecurity.authorizePayload { authorize: RSocketSecurity.AuthorizePayloadsSpec ->
                authorize
                    .anyRequest().authenticated()
                    .anyExchange().permitAll()
            }
                .jwt { jwtSpec ->
                    jwtSpec.authenticationManager(jwtReactiveAuthenticationManager(jwtDecoder()))
                }
        return security.build()
    }

    @Bean
    fun jwtDecoder(): ReactiveJwtDecoder {
        return TokenUtils.jwtAccessTokenDecoder()
    }

    @Bean
    fun jwtReactiveAuthenticationManager(decoder: ReactiveJwtDecoder): JwtReactiveAuthenticationManager {
        val converter = JwtAuthenticationConverter()
        val authoritiesConverter = JwtGrantedAuthoritiesConverter()
        authoritiesConverter.setAuthorityPrefix("ROLE_")
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter)
        val manager = JwtReactiveAuthenticationManager(decoder)
        manager.setJwtAuthenticationConverter(ReactiveJwtAuthenticationConverterAdapter(converter))
        return manager
    }

    @Bean
    fun rsocketMessageHandler() = RSocketMessageHandler() .apply {
        argumentResolverConfigurer.addCustomResolver(AuthenticationPrincipalArgumentResolver())
        routeMatcher = PathPatternRouteMatcher()
        rSocketStrategies = rsocketStrategies()
    }

    @Bean
    fun rsocketStrategies() = RSocketStrategies.builder()
        .routeMatcher(PathPatternRouteMatcher())
        .build()
}