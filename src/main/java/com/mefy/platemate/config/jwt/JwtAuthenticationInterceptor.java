package com.mefy.platemate.config.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT Authentication Interceptor.
 * Spring MVC HandlerInterceptor olarak çalışır — Security filter chain'e dokunmaz.
 * Her istek geldiğinde token kontrolü yapar ve userId'yi request attribute olarak ekler.
 *
 * Controller'lar userId'ye şu şekilde erişir:
 *   Long userId = (Long) request.getAttribute("userId");
 *   veya @RequestAttribute("userId") Long userId
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = extractTokenFromHeader(request);

        if (token == null || !jwtTokenProvider.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"Geçersiz veya eksik token.\"}");
            return false; // İsteği durdur
        }

        // Token geçerli — kullanıcı bilgilerini request'e ekle
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String username = jwtTokenProvider.getUsernameFromToken(token);

        request.setAttribute("userId", userId);
        request.setAttribute("username", username);

        return true; // İsteğe devam et
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
