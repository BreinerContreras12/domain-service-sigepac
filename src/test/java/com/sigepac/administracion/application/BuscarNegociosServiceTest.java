package com.sigepac.administracion.application;

import static org.junit.jupiter.api.Assertions.*;

import com.sigepac.administracion.application.port.in.command.RegistrarPagoCommand;
import com.sigepac.administracion.application.port.in.command.BuscarNegociosQuery;
import com.sigepac.administracion.application.port.in.result.PaginaNegocios;
import com.sigepac.administracion.domain.exception.ValorInvalidoException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BuscarNegociosServiceTest {

    private final Escenario e = new Escenario();
    private long a, b, c;

    @BeforeEach
    void datos() {
        a = e.negocio("Panadería Trigal", "L-3", "1111111111").getId().longValue();
        b = e.negocio("Café Central", "L-1", "2222222222").getId().longValue();
        c = e.negocio("Papelería Trigo", "L-2", "1111111111").getId().longValue();
        e.deuda(a, "ALQUILER", 10, 100000);
        e.deuda(b, "ALQUILER", 10, 50000);
        e.pagoService.registrar(new RegistrarPagoCommand(b, LocalDate.of(2026, 10, 20), BigDecimal.valueOf(50000)));
    }

    private BuscarNegociosQuery q(String nombre, String local, String doc, Boolean conSaldo, boolean inactivos, int pag, int tam) {
        return new BuscarNegociosQuery(nombre, local, doc, conSaldo, inactivos, pag, tam);
    }

    @Test
    void listaOrdenadaPorLocal() {
        PaginaNegocios p = e.buscar.listar(false, 0, 10);
        assertEquals(3, p.totalElementos());
        assertEquals("L-1", p.contenido().get(0).negocio().getLocal().valor());
        assertEquals("L-3", p.contenido().get(2).negocio().getLocal().valor());
    }

    @Test
    void filtraPorNombreSinImportarMayusculas() {
        assertEquals(2, e.buscar.buscar(q("TRIG", null, null, null, false, 0, 10)).totalElementos());
    }

    @Test
    void filtraPorLocalYPorDocumento() {
        assertEquals(1, e.buscar.buscar(q(null, "l-2", null, null, false, 0, 10)).totalElementos());
        assertEquals(2, e.buscar.buscar(q(null, null, "1111111111", null, false, 0, 10)).totalElementos());
    }

    @Test
    @DisplayName("conSaldo=true trae solo quienes deben; false, solo quienes están al día")
    void filtraPorSaldo() {
        assertEquals(1, e.buscar.buscar(q(null, null, null, true, false, 0, 10)).totalElementos());   // solo a
        assertEquals(2, e.buscar.buscar(q(null, null, null, false, false, 0, 10)).totalElementos());  // b y c
    }

    @Test
    void paginaYCalculaTotales() {
        PaginaNegocios p = e.buscar.listar(false, 1, 2);
        assertEquals(1, p.contenido().size());
        assertEquals(2, p.totalPaginas());
        assertEquals(3, p.totalElementos());
        assertTrue(e.buscar.listar(false, 5, 2).contenido().isEmpty());
    }

    @Test
    void inactivosSoloSiSePide() {
        e.negocioService.darDeBaja(c);
        assertEquals(2, e.buscar.listar(false, 0, 10).totalElementos());
        assertEquals(3, e.buscar.listar(true, 0, 10).totalElementos());
    }

    @Test
    void sinResultadosDevuelvePaginaVacia() {
        PaginaNegocios p = e.buscar.buscar(q("zzz", null, null, null, false, 0, 10));
        assertEquals(0, p.totalElementos());
        assertTrue(p.contenido().isEmpty());
    }

    @Test
    void parametrosInvalidos() {
        assertThrows(ValorInvalidoException.class, () -> e.buscar.listar(false, -1, 10));
        assertThrows(ValorInvalidoException.class, () -> e.buscar.listar(false, 0, 0));
        assertThrows(ValorInvalidoException.class, () -> e.buscar.listar(false, 0, 101));
        assertThrows(ValorInvalidoException.class, () -> e.buscar.buscar(q("  ", null, null, null, false, 0, 10)));
    }
}
