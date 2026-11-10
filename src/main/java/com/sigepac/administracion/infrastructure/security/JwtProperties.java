package com.sigepac.administracion.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Clave compartida con AuthCore (Base64 o texto plano UTF-8). */
@ConfigurationProperties(prefix = "seguridad.jwt")
public record JwtProperties(String secretBase64, String secret) {
    public String claveEfectiva() {
        if (secretBase64 != null && !secretBase64.isBlank()) {
            return secretBase64;
        }
        return secret;
    }
}
