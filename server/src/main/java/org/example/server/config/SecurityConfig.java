package org.example.server.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final RateLimitFilter rateLimitFilter;
  private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource)
      throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/parents/register", "/api/parents/login").permitAll()
            .requestMatchers("/api/children/check-name", "/api/children/setup-pattern", "/api/children/login")
            .permitAll()
            .requestMatchers("/api/children/leaderboard").permitAll()
            .requestMatchers("/api/store/**", "/api/exercises/**").permitAll()
            .requestMatchers("/api/test/**").permitAll() // TEST ONLY - REMOVE IN PRODUCTION
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            // OAuth2 endpoints
            .requestMatchers("/api/auth/google/callback", "/api/auth/google/callback/json").permitAll()
            .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
            .requestMatchers("/api/parents/me/**").authenticated()
            .requestMatchers("/api/children/me").authenticated()
            .requestMatchers("/api/children/**").authenticated()
            .anyRequest().authenticated())
        // OAuth2 login configuration
        .oauth2Login(oauth2 -> oauth2
            .authorizationEndpoint(authorization -> authorization
                .baseUri("/oauth2/authorization"))
            .redirectionEndpoint(redirection -> redirection
                .baseUri("/oauth2/callback/*"))
            .successHandler(oAuth2AuthenticationSuccessHandler))
        .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
