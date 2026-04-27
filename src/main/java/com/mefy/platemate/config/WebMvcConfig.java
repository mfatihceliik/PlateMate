package com.mefy.platemate.config;

import com.mefy.platemate.config.jwt.JwtAuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMVC konfigürasyonu.
 * JWT Interceptor'ı burada kayıt ederek hangi endpoint'lerin korumalı,
 * hangilerinin açık (public) olacağını belirliyoruz.
 *
 * Yeni bir public endpoint eklendiğinde excludePathPatterns'a eklenmeli.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/**")         // Tüm API endpoint'lerini koru
                .excludePathPatterns(
                        "/api/auth/**",             // Login & Register açık
                        "/api/cities/**",           // Şehir listesi herkese açık
                        "/api/vehicles/search/**",  // Plaka arama herkese açık (anonim arama)
                        "/ws/**"                    // WebSocket endpoint'i (kendi auth mekanizması var)
                );
    }
}
