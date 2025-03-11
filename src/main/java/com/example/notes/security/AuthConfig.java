package com.example.notes.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import com.example.notes.Constants;

/**
 * Security Configuration
 * 
 * @author Igor Peonte <igor.144@gmail.com>
 *
 */
@Configuration
public class AuthConfig {

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // @formatter:off
    
    http
      // Disable all security headers and csrf for demo
      .csrf(csrf -> csrf.disable())
      .headers(headers -> headers.disable())
      
      .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
      
      // Authenticate all
      .authorizeHttpRequests((authz) ->
        authz
          // Allow swagger UI
          .requestMatchers(HttpMethod.GET, "/swagger-ui*/**", "/v3/api-docs/**").permitAll()
          // Allow signup page
          .requestMatchers(HttpMethod.POST, Constants.BASE_URL + Constants.SIGNUP_URL).permitAll()
          
          // Everything authenticated except login page
          .anyRequest().authenticated())
      
      // Using formLogin to handle user's login
      .formLogin(form -> form
          .loginProcessingUrl(Constants.BASE_URL + Constants.LOGIN_URL)
          .usernameParameter("name").passwordParameter("password")
          .successHandler((req, res, auth) -> res.setStatus(HttpStatus.OK.value()))
          .failureHandler(new SimpleUrlAuthenticationFailureHandler())
      );
    
    // @formatter:on

    return http.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
