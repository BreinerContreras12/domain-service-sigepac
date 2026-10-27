package com.sigepac.administracion.contract;

import static org.junit.jupiter.api.Assertions.*;

import com.sigepac.administracion.application.port.out.DeudaRepositoryPort;
import com.sigepac.administracion.domain.model.Deuda;
import com.sigepac.administracion.domain.vo.ConceptoDeuda;
import com.sigepac.administracion.domain.vo.Dinero;
import com.sigepac.administracion.domain.vo.Periodo;
import java.util.List;
import org.junit.jupiter.api.Test;

public abstract class DeudaRepositoryContractTest {

    protected abstract DeudaRepositoryPort repo();

    /** Crea (en la implementación real) un negocio y devuelve su id, para respetar claves foráneas. */
    protected abstract long crearNegocio(String local);

    private Deuda nueva(long negocio, ConceptoDeuda c, int mes, long valor) {
        return Deuda.registrar(negocio, c, Periodo.de(2026, mes), Dinero.de(valor));
    }

    @Test
    void guardaYRecuperaConSusMontos() {
        long n = crearNegocio("L-1");
        Deuda g = repo().guardar(nueva(n, ConceptoDeuda.ALQUILER, 10, 123456));
        Deuda r = repo().buscarPorId(g.getId().longValue()).orElseThrow();
        assertEquals(Dinero.de(123456), r.getValor());
        assertEquals(Periodo.de(2026, 10), r.getPeriodo());
        assertEquals(Dinero.CERO, r.getPagado());
    }

    @Test
    void persisteLoPagadoAlActualizar() {
        long n = crearNegocio("L-1");
        Deuda g = repo().guardar(nueva(n, ConceptoDeuda.ALQUILER, 10, 100000));
        g.aplicarPago(Dinero.de(40000));
        repo().guardar(g);
        assertEquals(Dinero.de(60000), repo().buscarPorId(g.getId().longValue()).orElseThrow().saldo());
    }

    @Test
    void existeSegunNegocioConceptoYPeriodo() {
        long n = crearNegocio("L-1");
        repo().guardar(nueva(n, ConceptoDeuda.ALQUILER, 10, 1));
        assertTrue(repo().existe(n, ConceptoDeuda.ALQUILER, Periodo.de(2026, 10)));
        assertFalse(repo().existe(n, ConceptoDeuda.ADMINISTRACION, Periodo.de(2026, 10)));
        assertFalse(repo().existe(n, ConceptoDeuda.ALQUILER, Periodo.de(2026, 11)));
    }

    @Test
    void buscaPorUnoOVariosNegocios() {
        long a = crearNegocio("L-1");
        long b = crearNegocio("L-2");
        repo().guardar(nueva(a, ConceptoDeuda.ALQUILER, 9, 1));
        repo().guardar(nueva(a, ConceptoDeuda.ALQUILER, 10, 1));
        repo().guardar(nueva(b, ConceptoDeuda.ALQUILER, 10, 1));
        assertEquals(2, repo().buscarPorNegocio(a).size());
        assertEquals(3, repo().buscarPorNegocios(List.of(a, b)).size());
        assertTrue(repo().buscarPorNegocios(List.of()).isEmpty());
    }
}
