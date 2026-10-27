package com.sigepac.administracion.domain.vo;

import com.sigepac.administracion.domain.exception.ValorInvalidoException;
import java.time.YearMonth;

/** Mes y año al que corresponde una deuda. */
public record Periodo(YearMonth valor) implements Comparable<Periodo> {

    public Periodo {
        if (valor == null) {
            throw new ValorInvalidoException("El período es obligatorio");
        }
        if (valor.getYear() < 2000 || valor.getYear() > 2100) {
            throw new ValorInvalidoException("El año del período debe estar entre 2000 y 2100");
        }
    }

    public static Periodo de(int anio, int mes) {
        if (mes < 1 || mes > 12) {
            throw new ValorInvalidoException("El mes debe estar entre 1 y 12");
        }
        return new Periodo(YearMonth.of(anio, mes));
    }

    public int anio() {
        return valor.getYear();
    }

    public int mes() {
        return valor.getMonthValue();
    }

    @Override
    public int compareTo(Periodo otro) {
        return valor.compareTo(otro.valor);
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
