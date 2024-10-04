package org.example.expert.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.auth.entity.UserDetailsImpl;
import org.example.expert.domain.auth.service.UserDetailsServiceImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtDecodeFilter extends OncePerRequestFilter {
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String authorization = request.getHeader("Authorization");

            if (authorization == null) {
                response.setHeader("Content-Type", "text/plain; charset=utf-8");
                response.sendError(401, "JWT 토큰이 필요합니다.");
                return;
            }

            String token = jwtUtil.substringToken(authorization);
            Claims claims = jwtUtil.extractClaims(token);

            if (claims == null) {
                response.setHeader("Content-Type", "text/plain; charset=utf-8");
                response.sendError(401, "잘못된 JWT 토큰입니다.");
                return;
            }

            String email = claims.get("email", String.class);
            UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(email);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (SecurityException | MalformedJwtException e) {
            response.setHeader("Content-Type", "text/plain; charset=utf-8");
            response.sendError(401, "유효하지 않는 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            response.setHeader("Content-Type", "text/plain; charset=utf-8");
            response.sendError(401, "만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            response.setHeader("Content-Type", "text/plain; charset=utf-8");
            response.sendError(401, "지원되지 않는 JWT 토큰입니다.");
        } catch (Exception e) {
            response.setHeader("Content-Type", "text/plain; charset=utf-8");
            response.sendError(500, "Internal server error.");
        } finally {
            filterChain.doFilter(request, response);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String[] excludePath = {"/auth/signin", "/auth/signup"};
        String path = request.getRequestURI();
        return Arrays.stream(excludePath).anyMatch(path::startsWith);
    }
}
