package com.sigepac.administracion.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sigepac.administracion.domain.exception.ValorInvalidoException;
import com.sigepac.administracion.domain.vo.CodigoLocal;
import com.sigepac.administracion.domain.vo.ConceptoDeuda;
import com.sigepac.administracion.domain.vo.Periodo;
import com.sigepac.administracion.domain.vo.Propietario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValoresTest {

    @Test
    @DisplayName("CodigoLocal se normaliza a mayúsculas y rechaza formatos inválidos")
    void codigoLocal() {
        assertEquals("L-101", CodigoLocal.de("  l-101 ").valor());
        assertThrows(ValorInvalidoException.class, () -> CodigoLocal.de(null));
        assertThrows(ValorInvalidoException.class, () -> CodigoLocal.de("  "));
        assertThrows(ValorInvalidoException.class, () -> CodigoLocal.de("L 101"));
        assertThrows(ValorInvalidoException.class, () -> CodigoLocal.de("L-1234567890123456"));
    }

    @Test
    void conceptoAceptaAdministracionYAlquilerSinImportarMayusculas() {
        assertEquals(ConceptoDeuda.ADMINISTRACION, ConceptoDeuda.desde("administracion"));
        assertEquals(ConceptoDeuda.ADMINISTRACION, ConceptoDeuda.desde(" Administración "));
        assertEquals(ConceptoDeuda.ALQUILER, ConceptoDeuda.desde("ALQUILER"));
        assertThrows(ValorInvalidoException.class, () -> ConceptoDeuda.desde("multa"));
        assertThrows(ValorInvalidoException.class, () -> ConceptoDeuda.desde(null));
    }

    @Test
    void periodoValidaMesYAnio() {
        assertEquals("2026-10", Periodo.de(2026, 10).toString());
        assertTrue(Periodo.de(2026, 9).compareTo(Periodo.de(2026, 10)) < 0);
        assertThrows(ValorInvalidoException.class, () -> Periodo.de(2026, 13));
        assertThrows(ValorInvalidoException.class, () -> Periodo.de(2026, 0));
        assertThrows(ValorInvalidoException.class, () -> Periodo.de(1850, 5));
    }

    @Test
    void propietarioExigeDocumentoYNombreYValidaTelefono() {
        assertEquals("Ana Pérez", Propietario.de("1090123456", " Ana Pérez ", null).nombre());
        assertNull(Propietario.de("1090123456", "Ana", "  ").telefono());
        assertThrows(ValorInvalidoException.class, () -> Propietario.de("1090123456", " ", null));
        assertThrows(ValorInvalidoException.class, () -> Propietario.de("12", "Ana", null));
        assertThrows(ValorInvalidoException.class, () -> Propietario.de("1090123456", "Ana", "abc"));
    }
}
