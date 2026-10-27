package com.sigepac.administracion.testsupport;

import com.sigepac.administracion.domain.model.Negocio;
import com.sigepac.administracion.domain.vo.CodigoLocal;
import com.sigepac.administracion.domain.vo.Propietario;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/** Datos de ejemplo. El reloj fijo es el 20 de octubre de 2026, 10:00 en Bogotá. */
public final class Datos {

    private Datos() { }

    public static Clock relojFijo() {
        return Clock.fixed(Instant.parse("2026-10-20T15:00:00Z"), ZoneId.of("America/Bogota"));
    }

    public static Propietario propietario() {
        return Propietario.de("1090123456", "Ana Pérez", "3001234567");
    }

    public static Negocio panaderia(String local) {
        return Negocio.registrar("Panadería El Trigal", CodigoLocal.de(local), propietario());
    }
}
