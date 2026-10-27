package com.sigepac.administracion.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sigepac.administracion.domain.exception.NegocioConSaldoException;
import com.sigepac.administracion.domain.exception.NegocioInactivoException;
import com.sigepac.administracion.domain.exception.ValorInvalidoException;
import com.sigepac.administracion.domain.model.EstadoNegocio;
import com.sigepac.administracion.domain.model.Negocio;
import com.sigepac.administracion.domain.vo.CodigoLocal;
import com.sigepac.administracion.domain.vo.Dinero;
import com.sigepac.administracion.testsupport.Datos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NegocioTest {

    @Test
    void unNegocioNuevoNaceActivoYSinId() {
        Negocio n = Datos.panaderia("L-101");
        assertTrue(n.estaActivo());
        assertNull(n.getId());
        assertEquals("Panadería El Trigal", n.getNombre());
    }

    @Test
    void exigeNombreYLimitaSuLongitud() {
        assertThrows(ValorInvalidoException.class, () -> Negocio.registrar(" ", CodigoLocal.de("L-1"), Datos.propietario()));
        assertThrows(ValorInvalidoException.class,
                () -> Negocio.registrar("x".repeat(121), CodigoLocal.de("L-1"), Datos.propietario()));
    }

    @Test
    void editaNombreLocalYPropietario() {
        Negocio n = Datos.panaderia("L-101");
        n.editar("Café Central", CodigoLocal.de("L-202"), Datos.propietario());
        assertEquals("Café Central", n.getNombre());
        assertEquals("L-202", n.getLocal().valor());
    }

    @Test
    @DisplayName("Dar de baja es lógico: queda INACTIVO y ya no admite cambios")
    void bajaLogica() {
        Negocio n = Datos.panaderia("L-101");
        n.darDeBaja(Dinero.CERO);
        assertEquals(EstadoNegocio.INACTIVO, n.getEstado());
        assertFalse(n.estaActivo());
        assertThrows(NegocioInactivoException.class, () -> n.editar("Otro", CodigoLocal.de("L-1"), Datos.propietario()));
        assertThrows(NegocioInactivoException.class, () -> n.darDeBaja(Dinero.CERO));
    }

    @Test
    @DisplayName("No se da de baja un negocio con saldo pendiente")
    void bajaConSaldoPendienteFalla() {
        Negocio n = Datos.panaderia("L-101");
        NegocioConSaldoException e = assertThrows(NegocioConSaldoException.class, () -> n.darDeBaja(Dinero.de(50000)));
        assertEquals(Dinero.de(50000), e.getSaldo());
        assertTrue(n.estaActivo());
    }
}
