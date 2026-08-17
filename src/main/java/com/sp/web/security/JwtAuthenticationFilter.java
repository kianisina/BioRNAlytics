package com.sp.web.security;

import com.sp.web.domain.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
public final class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private static final int TEN_DAYS_IN_MILLIS = 864_000_000;
    private static final Logger LOG = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final AuthenticationManager authenticationManager;
    private final SecurityConstant securityConstants;
    // constructor for the Authentication  filter
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager,
                                   final SecurityConstant securityConstants) {
        this.authenticationManager = authenticationManager;
        this.securityConstants = securityConstants;
        setFilterProcessesUrl(this.securityConstants.getAuthLoginUrl());
    }
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
            password);
        return authenticationManager.authenticate(authenticationToken);
    }
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain filterChain, Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            UserDetails user = (UserDetails) authentication.getPrincipal();
            List<String> roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
            byte[] signingKey = securityConstants.getJwtSecret().getBytes();
            String token = Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(signingKey), Jwts.SIG.HS512)
                .header().add("typ", securityConstants.getTokenType())
                .and()
                .issuer(securityConstants.getTokenIssuer())
                .audience().add(securityConstants.getTokenAudience())
                .and()
                .subject(user.getUsername())
                .expiration(new Date(System.currentTimeMillis() + TEN_DAYS_IN_MILLIS))
                .claim("rol", roles)
                .compact();
            response.addHeader(securityConstants.getTokenHeader(), securityConstants.getTokenPrefix() + token);
        } else {
            LOG.error("Principal not expected Type: {}",principal.getClass().getName());
        }
    }
}