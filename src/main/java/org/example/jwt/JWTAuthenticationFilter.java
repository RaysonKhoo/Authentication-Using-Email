package org.example.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.example.configuration.UserRegistrationDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JWTService jwtService;
    @Autowired
    private UserRegistrationDetailsService userRegistrationDetailsService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token =null;
        String email =null;
        String authHeader = request.getHeader("Authorization");

        if (authHeader !=null && authHeader.startsWith("Bearer")){
            token = authHeader.substring(7);
            try {
                email = jwtService.extractUsernameFromToken(token);
            } catch (Exception e) {
                logger.error("JWT token extraction failed", e);
            }
        }
        if(email != null && SecurityContextHolder.getContext().getAuthentication()== null){
            UserDetails userdetails = userRegistrationDetailsService.loadUserByUsername(email);
            if(jwtService.validateToken(token,userdetails)){
                var authToken = new UsernamePasswordAuthenticationToken(userdetails, null, userdetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request,response);
    }
}
