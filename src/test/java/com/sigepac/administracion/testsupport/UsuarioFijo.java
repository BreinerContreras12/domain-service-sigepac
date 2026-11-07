package com.sigepac.administracion.testsupport;

import com.sigepac.administracion.application.port.out.UsuarioActualPort;

/** En las pruebas unitarias no hay JWT: el usuario es fijo. */
public class UsuarioFijo implements UsuarioActualPort {

    private final String usuario;

    public UsuarioFijo(String usuario) {
        this.usuario = usuario;
    }

    @Override
    public String nombreUsuario() {
        return usuario;
    }
}
