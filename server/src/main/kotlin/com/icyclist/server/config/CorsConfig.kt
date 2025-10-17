package com.icyclist.server.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig : WebMvcConfigurer {
    
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")  // 允许所有来源
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的HTTP方法
            .allowedHeaders("*")  // 允许所有请求头
            .allowCredentials(true)  // 允许携带凭证
            .maxAge(3600)  // 预检请求缓存时间
    }
}

