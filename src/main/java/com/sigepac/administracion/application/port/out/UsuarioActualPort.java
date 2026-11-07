package com.sigepac.administracion.application.port.out;

/**
 * HU-12: quién está haciendo la operación. En producción sale del JWT que firmó AuthCore (claim "sub");
 * en las pruebas es un doble fijo. Así la aplicación no importa Spring Security.
 */
public interface UsuarioActualPort {

    String nombreUsuario();
}
