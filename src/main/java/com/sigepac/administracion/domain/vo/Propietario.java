package com.sigepac.administracion.domain.vo;

import com.sigepac.administracion.domain.exception.ValorInvalidoException;
import java.util.regex.Pattern;

/** Quién responde por el negocio. Es un dato del negocio, no un usuario del sistema (decisión a confirmar con la administradora). */
public record Propietario(Documento documento, String nombre, String telefono) {

    private static final Pattern TELEFONO = Pattern.compile("^[0-9+()\\-\\s]{7,20}$");

    public Propietario {
        if (documento == null) {
            throw new ValorInvalidoException("El documento del propietario es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ValorInvalidoException("El nombre del propietario es obligatorio");
        }
        nombre = nombre.trim();
        if (nombre.length() > 120) {
            throw new ValorInvalidoException("El nombre del propietario no puede superar 120 caracteres");
        }
        telefono = (telefono == null || telefono.isBlank()) ? null : telefono.trim();
        if (telefono != null && !TELEFONO.matcher(telefono).matches()) {
            throw new ValorInvalidoException("Teléfono del propietario inválido");
        }
    }

    public static Propietario de(String documento, String nombre, String telefono) {
        return new Propietario(Documento.de(documento), nombre, telefono);
    }
}
