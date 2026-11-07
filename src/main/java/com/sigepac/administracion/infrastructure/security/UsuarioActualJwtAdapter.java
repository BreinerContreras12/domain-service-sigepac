package com.sigepac.administracion.infrastructure.security;

import com.sigepac.administracion.application.port.out.UsuarioActualPort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * HU-12: el usuario que hace el cambio es el "sub" del JWT de AuthCore, que JwtAuthenticationFilter ya dejó
 * en el contexto de seguridad. El cliente NO envía quién es: no se puede falsificar desde el cuerpo de la petición.
 */
@Component
public class UsuarioActualJwtAdapter implements UsuarioActualPort {

    @Override
    public String nombreUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioAutenticado usuario) {
            return usuario.username();
        }
        // No debería pasar: /api/** exige token. Si pasa, se prefiere fallar a guardar una auditoría anónima.
        throw new IllegalStateException("No hay un usuario autenticado para registrar la auditoría");
    }
}
