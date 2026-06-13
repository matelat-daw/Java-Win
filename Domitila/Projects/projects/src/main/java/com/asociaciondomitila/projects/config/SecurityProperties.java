package com.asociaciondomitila.projects.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private List<String> allowedOriginPatterns = new ArrayList<>(List.of(
            "http://localhost",
            "http://localhost:*",
            "https://localhost",
            "https://localhost:*",
            "http://127.0.0.1",
            "http://127.0.0.1:*",
            "https://127.0.0.1",
            "https://127.0.0.1:*"
    ));

    private Cookie cookie = new Cookie();

    @Getter
    @Setter
    public static class Cookie {
        private boolean secure;
        private boolean httpOnly = true;
        private String sameSite = "Lax";
        private String path = "/";
        private String accessTokenName = "auth_token";
        private String refreshTokenName = "refresh_token";
    }
}