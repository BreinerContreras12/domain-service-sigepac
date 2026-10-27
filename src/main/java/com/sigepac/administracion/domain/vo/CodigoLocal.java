package com.sigepac.administracion.domain.vo;

import com.sigepac.administracion.domain.exception.ValorInvalidoException;
import java.util.Locale;
import java.util.regex.Pattern;

/** Código del local comercial dentro de la plaza (ej. L-101). Letras, números y guion, hasta 15 caracteres. */
public record CodigoLocal(String valor) implements Comparable<CodigoLocal> {

    private static final Pattern FORMATO = Pattern.compile("[A-Z0-9][A-Z0-9-]{0,14}");

    public CodigoLocal {
        if (valor == null || valor.isBlank()) {
            throw new ValorInvalidoException("El código del local es obligatorio");
        }
        valor = valor.trim().toUpperCase(Locale.ROOT);
        if (!FORMATO.matcher(valor).matches()) {
            throw new ValorInvalidoException("Código de local inválido: use letras, números y guion (máximo 15)");
        }
    }

    public static CodigoLocal de(String valor) {
        return new CodigoLocal(valor);
    }

    @Override
    public int compareTo(CodigoLocal otro) {
        return valor.compareTo(otro.valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
