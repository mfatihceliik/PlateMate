package com.mefy.platemate.config.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.mefy.platemate.business.utilities.constants.Messages;
import com.mefy.platemate.core.utilities.messages.IMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT Authentication Interceptor.
 * Works as Spring MVC HandlerInterceptor — does not touch Security filter chain.
 * Checks token on every request and adds userId as request attribute.
 * <p>
 * Controllers access userId like this:
 * Long userId = (Long) request.getAttribute("userId");
 * or @RequestAttribute("userId") Long userId
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final IMessageService messageService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = extractTokenFromHeader(request);

        if (token == null || !jwtTokenProvider.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            String msg = messageService.getMessage(Messages.AUTH_TOKEN_INVALID).replace("\"", "\\\"");
            response.getWriter().write("{\"success\":false,\"message\":\"" + msg + "\"}");
            return false; // Stop request
        }

        // Token valid — add user info to request
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String username = jwtTokenProvider.getUsernameFromToken(token);

        request.setAttribute("userId", userId);
        request.setAttribute("username", username);

        return true; // Continue request
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
