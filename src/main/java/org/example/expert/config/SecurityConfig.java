package org.example.expert.config;

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
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final JwtDecodeFilter jwtDecodeFilter;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService);
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(jwtUtil, authenticationManager);

        http.csrf(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/auth/signin", "/auth/signup", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .logout(LogoutConfigurer::permitAll)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationManager(authenticationManager)
                .addFilterBefore(jwtDecodeFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer
                        .authenticationEntryPoint(((request, response, authException) -> {
                                    System.out.println("authException");

                                    handlerExceptionResolver.resolveException(request, response, null, authException);
                                })
                        )
                        .accessDeniedHandler(((request, response, accessDeniedException) -> {
                                    System.out.println("accessDeniedException");
                                    handlerExceptionResolver.resolveException(request, response, null, accessDeniedException);
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
