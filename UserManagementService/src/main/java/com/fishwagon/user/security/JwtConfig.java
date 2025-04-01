package com.fishwagon.user.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component  // ✅ Ensure Spring recognizes this as a bean
@ConfigurationProperties(prefix = "jwt")  // ✅ Binds `jwt.secret` from application.yml
public class JwtConfig {

    private String secret;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
