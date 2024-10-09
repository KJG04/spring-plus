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
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.auth.service.UserDetailsServiceImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtDecodeFilter extends OncePerRequestFilter {
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {

            try {
                String authorization = request.getHeader("Authorization");

                if (authorization == null) {
                    throw new AuthException("JWT 토큰이 필요합니다.");
                }

                String token = jwtUtil.substringToken(authorization);
                Claims claims = jwtUtil.extractClaims(token);

                if (claims == null) {
                    throw new AuthException("잘못된 JWT 토큰입니다.");
                }

                String email = claims.get("email", String.class);
                UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(email);
                Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
            } catch (SecurityException | MalformedJwtException e) {
                throw new AuthException("유효하지 않는 JWT 서명입니다.", e);
            } catch (ExpiredJwtException e) {
                throw new AuthException("만료된 JWT 토큰입니다.", e);
            } catch (UnsupportedJwtException e) {
                throw new AuthException("지원되지 않는 JWT 토큰입니다.", e);
            } catch (Exception e) {
                throw new AuthException("JWT 토큰 생성중 오류 발생하였습니다.", e);
            }
        } catch (Exception e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String[] excludePath = {"/auth/signin", "/auth/signup", "/error"};
        String path = request.getRequestURI();
        return Arrays.stream(excludePath).anyMatch(path::startsWith);
    }
}
