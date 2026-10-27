package com.sigepac.administracion.domain.vo;

import com.sigepac.administracion.domain.exception.ValorInvalidoException;
import java.util.Locale;

/** Qué se está cobrando: la cuota de administración o el alquiler del local. */
public enum ConceptoDeuda {
    ADMINISTRACION, ALQUILER;

    public static ConceptoDeuda desde(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ValorInvalidoException("El concepto de la deuda es obligatorio (ADMINISTRACION o ALQUILER)");
        }
        String limpio = texto.trim().toUpperCase(Locale.ROOT).replace('Ó', 'O');
        try {
            return valueOf(limpio);
        } catch (IllegalArgumentException e) {
            throw new ValorInvalidoException("Concepto inválido: use ADMINISTRACION o ALQUILER");
        }
    }
}
