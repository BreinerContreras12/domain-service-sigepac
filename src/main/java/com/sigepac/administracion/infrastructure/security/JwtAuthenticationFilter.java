package com.sigepac.administracion.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Solo extrae el token del encabezado y le pregunta al validador. No sabe cómo se firma un JWT. */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtTokenValidator validador;

    public JwtAuthenticationFilter(JwtTokenValidator validador) {
        this.validador = validador;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain cadena)
            throws ServletException, IOException {
        String encabezado = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (encabezado != null && encabezado.startsWith(PREFIJO)) {
            validador.validar(encabezado.substring(PREFIJO.length()).trim()).ifPresent(usuario -> {
                var listaRoles = new ArrayList<>(usuario.roles());
                // Si el token es del usuario admin o no trae roles explícitos, se otorga ADMIN
                if (listaRoles.isEmpty() || "admin".equalsIgnoreCase(usuario.username())) {
                    if (!listaRoles.contains("ADMIN") && !listaRoles.contains("ROLE_ADMIN")) {
                        listaRoles.add("ADMIN");
                    }
                }
                var autoridades = listaRoles.stream()
                        .map(rol -> rol.startsWith("ROLE_") ? rol : "ROLE_" + rol)
                        .map(SimpleGrantedAuthority::new)
                        .toList();
                SecurityContextHolder.getContext()
                        .setAuthentication(new UsernamePasswordAuthenticationToken(usuario, null, autoridades));
            });
        }
        cadena.doFilter(request, response);   // sin autenticación, Spring Security responde 401
    }
}
