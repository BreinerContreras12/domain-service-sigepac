package com.sigepac.administracion.application;

import static org.junit.jupiter.api.Assertions.*;

import com.sigepac.administracion.application.port.in.command.*;
import com.sigepac.administracion.domain.exception.*;
import com.sigepac.administracion.domain.model.EstadoNegocio;
import com.sigepac.administracion.domain.model.Negocio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NegocioServiceTest {

    private final Escenario e = new Escenario();

    @Test
    void registraYConsulta() {
        Negocio n = e.negocio("Panadería", "l-101", "1090123456");
        assertNotNull(n.getId());
        assertEquals("L-101", e.negocioService.porId(n.getId().longValue()).getLocal().valor());
    }

    @Test
    @DisplayName("Si faltan datos, el mensaje lista TODOS los campos faltantes")
    void listaTodosLosCamposFaltantes() {
        ValorInvalidoException ex = assertThrows(ValorInvalidoException.class, () -> e.negocioService.registrar(
                new RegistrarNegocioCommand(" ", null, "", null, null)));
        assertTrue(ex.getMessage().startsWith("Faltan campos obligatorios"));
        assertTrue(ex.getMessage().contains("nombre"));
        assertTrue(ex.getMessage().contains("local"));
        assertEquals(0, e.negocios.cantidad());
    }

    @Test
    void unLocalActivoSoloTieneUnNegocio() {
        e.negocio("Uno", "L-1", "1090123456");
        assertThrows(LocalOcupadoException.class, () -> e.negocio("Dos", "l-1", "2090123456"));
        assertEquals(1, e.negocios.cantidad());
    }

    @Test
    void editarSinCambiarElLocalNoChocaConsigoMismo() {
        Negocio n = e.negocio("Uno", "L-1", "1090123456");
        Negocio editado = e.negocioService.editar(new EditarNegocioCommand(n.getId().longValue(),
                "Uno Renovado", "L-1", "1090123456", "Ana", "3001234567"));
        assertEquals("Uno Renovado", editado.getNombre());
    }

    @Test
    void editarHaciaUnLocalOcupadoFalla() {
        e.negocio("Uno", "L-1", "1090123456");
        Negocio dos = e.negocio("Dos", "L-2", "2090123456");
        assertThrows(LocalOcupadoException.class, () -> e.negocioService.editar(new EditarNegocioCommand(
                dos.getId().longValue(), "Dos", "L-1", "2090123456", "Luis", null)));
    }

    @Test
    void consultarInexistenteDa404() {
        assertThrows(NegocioNoEncontradoException.class, () -> e.negocioService.porId(99));
    }

    @Test
    @DisplayName("Baja lógica: libera el local para otro negocio")
    void bajaLiberaElLocal() {
        Negocio n = e.negocio("Uno", "L-1", "1090123456");
        Negocio baja = e.negocioService.darDeBaja(n.getId().longValue());
        assertEquals(EstadoNegocio.INACTIVO, baja.getEstado());
        e.negocio("Nuevo", "L-1", "2090123456");
    }

    @Test
    void bajaConSaldoPendienteSeRechaza() {
        Negocio n = e.negocio("Uno", "L-1", "1090123456");
        e.deuda(n.getId().longValue(), "ADMINISTRACION", 10, 100000);
        assertThrows(NegocioConSaldoException.class, () -> e.negocioService.darDeBaja(n.getId().longValue()));
        assertEquals(EstadoNegocio.ACTIVO, e.negocioService.porId(n.getId().longValue()).getEstado());
    }
}
