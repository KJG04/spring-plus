package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.entity.UserDetailsImpl;
import org.example.expert.domain.user.entity.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final JwtUtil jwtUtil;

    public AuthenticationFilter(JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        super(authenticationManager);
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/auth/signin");
        setUsernameParameter("email");
        setPasswordParameter("password");
    }

    // 로그인 시도
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            SigninRequest signinRequest = new ObjectMapper().readValue(request.getInputStream(), SigninRequest.class);
            return getAuthenticationManager()
                    .authenticate(new UsernamePasswordAuthenticationToken(signinRequest.getEmail(), signinRequest.getPassword(), null));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 로그인 성공
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        UserDetailsImpl userDetails = (UserDetailsImpl) authResult.getPrincipal();
        User user = userDetails.getUser();
        String token = jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole(), user.getNickname());

        if (token == null) {
            response.setHeader("Content-Type", "text/plain; charset=utf-8");
            response.sendError(500, "토큰 생성 실패");
            return;
        }

        SigninResponse signinResponse = new SigninResponse(token);
        response.setHeader("Content-Type", "application/json; charset=utf-8");
        response.setStatus(200);
        response.getWriter().write(new ObjectMapper().writeValueAsString(signinResponse));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setHeader("Content-Type", "text/plain; charset=utf-8");
        response.sendError(401, "로그인 실패");
    }
}
