package com.sp.web.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties("security")
public final class SecurityConstant {
// Must be at least 32 characters long for HMAC-SHA256!
    private String authLoginUrl; 
    private String jwtSecret; // 10 days in milliseconds
    private String tokenPrefix;
    private String tokenHeader;
    private String tokenType;
    private String tokenIssuer;
    private String tokenAudience;
    
}
