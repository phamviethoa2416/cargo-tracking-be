package com.example.cargotracking.common.security

import com.example.cargotracking.common.jwt.JwtAuthFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter,

    @Value("\${cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private val allowedOrigins: String,

    @Value("\${cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
    private val allowedMethods: String,

    @Value("\${cors.allowed-headers:*}")
    private val allowedHeaders: String,

    @Value("\${cors.exposed-headers:Authorization,Content-Disposition}")
    private val exposedHeaders: String,

    @Value("\${cors.allow-credentials:true}")
    private val allowCredentials: Boolean,

    @Value("\${cors.max-age:3600}")
    private val maxAge: Long
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/api/auth/**",
                    "/actuator/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml",
                    "/webjars/**"
                ).permitAll()
                it.anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = this@SecurityConfig.allowedOrigins.split(",").map(String::trim)
            allowedMethods = this@SecurityConfig.allowedMethods.split(",").map(String::trim)
            allowedHeaders = if (this@SecurityConfig.allowedHeaders == "*") {
                listOf("Authorization", "Content-Type")
            } else {
                this@SecurityConfig.allowedHeaders.split(",").map(String::trim)
            }
            exposedHeaders = this@SecurityConfig.exposedHeaders.split(",").map(String::trim)
            allowCredentials = this@SecurityConfig.allowCredentials
            maxAge = this@SecurityConfig.maxAge
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/api/**", configuration)
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}