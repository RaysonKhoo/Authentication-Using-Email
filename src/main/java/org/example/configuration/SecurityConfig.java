package org.example.configuration;

import org.example.jwt.JWTAuthenticationFilter;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class    SecurityConfig {


    @Autowired
    private final UserRegistrationDetailsService userRegistrationDetailsService;
    @Autowired
    private final JWTAuthenticationFilter authenticationFilter;

    public SecurityConfig(UserRegistrationDetailsService userRegistrationDetailsService, JWTAuthenticationFilter authenticationFilter) {
        this.userRegistrationDetailsService = userRegistrationDetailsService;
        this.authenticationFilter = authenticationFilter;
    }

    private static final String[] SECURED_URLs = {"/api/user/**"};
    private static final String[] UN_SECURED_URLs={
            "/authentication/**",
            "/register/**"


    };
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)  // Disable CSRF protection
                .authorizeHttpRequests(
                        req->req.requestMatchers(UN_SECURED_URLs)
                                .permitAll()  // Allow access to the registration and authenticate endpoints
                                .requestMatchers(SECURED_URLs).hasAuthority("ADMIN")  // Require "ROLE_ADMIN" authority for secured URLs
                                .anyRequest()
                                .authenticated()  // Require authentication for all other requests
                ).userDetailsService(userRegistrationDetailsService)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);  // Add the JWT filter before the UsernamePasswordAuthenticationFilter

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    public AuthenticationProvider authenticationProvider(){
        var authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userRegistrationDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return  authenticationProvider;
    }
}
