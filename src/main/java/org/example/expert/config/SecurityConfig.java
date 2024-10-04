package org.example.expert.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.auth.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final JwtDecodeFilter jwtDecodeFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService);
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(jwtUtil, authenticationManager);

        http.csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/auth/signin").permitAll()
                        .requestMatchers("/auth/signup").permitAll()
                        .anyRequest().authenticated())
                .logout(LogoutConfigurer::permitAll)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ).authenticationManager(authenticationManager)
                .addFilterBefore(jwtDecodeFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer
                        .authenticationEntryPoint(((request, response, authException) -> {
                                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
                                })
                        )
                        .accessDeniedHandler(((request, response, accessDeniedException) -> {
                                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, accessDeniedException.getMessage());
                                })
                        )
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { // 비밀번호 암호화
        return new BCryptPasswordEncoder();
    }

}
