package com.sigepac.administracion.contract;

import static org.junit.jupiter.api.Assertions.*;

import com.sigepac.administracion.application.port.out.FiltroNegocio;
import com.sigepac.administracion.application.port.out.NegocioRepositoryPort;
import com.sigepac.administracion.domain.model.EstadoNegocio;
import com.sigepac.administracion.domain.model.Negocio;
import com.sigepac.administracion.domain.vo.CodigoLocal;
import com.sigepac.administracion.testsupport.Datos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Toda implementación del puerto (memoria, JPA…) debe pasar estas mismas pruebas. */
public abstract class NegocioRepositoryContractTest {

    protected abstract NegocioRepositoryPort repo();

    @Test
    @DisplayName("guardar asigna id y buscarPorId devuelve los mismos datos")
    void guardaYRecupera() {
        Negocio g = repo().guardar(Datos.panaderia("L-1"));
        assertNotNull(g.getId());
        Negocio r = repo().buscarPorId(g.getId().longValue()).orElseThrow();
        assertEquals("L-1", r.getLocal().valor());
        assertEquals("1090123456", r.getPropietario().documento().valor());
    }

    @Test
    void actualizarConservaElId() {
        Negocio g = repo().guardar(Datos.panaderia("L-1"));
        g.editar("Nuevo", CodigoLocal.de("L-9"), Datos.propietario());
        Negocio u = repo().guardar(g);
        assertEquals(g.getId(), u.getId());
        assertEquals("Nuevo", repo().buscarPorId(g.getId().longValue()).orElseThrow().getNombre());
    }

    @Test
    void localActivoSoloEncuentraActivos() {
        Negocio g = repo().guardar(Datos.panaderia("L-1"));
        assertTrue(repo().buscarPorLocalActivo(CodigoLocal.de("L-1")).isPresent());
        g.darDeBaja(com.sigepac.administracion.domain.vo.Dinero.CERO);
        repo().guardar(g);
        assertTrue(repo().buscarPorLocalActivo(CodigoLocal.de("L-1")).isEmpty());
        assertEquals(EstadoNegocio.INACTIVO, repo().buscarPorId(g.getId().longValue()).orElseThrow().getEstado());
    }

    @Test
    void buscarAplicaFiltros() {
        repo().guardar(Datos.panaderia("L-1"));
        repo().guardar(Negocio.registrar("Café", CodigoLocal.de("L-2"), Datos.propietario()));
        assertEquals(2, repo().buscar(new FiltroNegocio(null, null, null, false)).size());
        assertEquals(1, repo().buscar(new FiltroNegocio("caf", null, null, false)).size());
        assertEquals(1, repo().buscar(new FiltroNegocio(null, CodigoLocal.de("L-1"), null, false)).size());
    }

    @Test
    void inexistenteDevuelveVacio() {
        assertTrue(repo().buscarPorId(12345).isEmpty());
    }
}
