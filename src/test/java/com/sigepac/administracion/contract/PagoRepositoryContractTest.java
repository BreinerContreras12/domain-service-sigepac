package com.sigepac.administracion.contract;

import static org.junit.jupiter.api.Assertions.*;

import com.sigepac.administracion.application.port.out.PagoRepositoryPort;
import com.sigepac.administracion.domain.model.AplicacionPago;
import com.sigepac.administracion.domain.model.EstadoPago;
import com.sigepac.administracion.domain.model.Pago;
import com.sigepac.administracion.domain.vo.Dinero;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

public abstract class PagoRepositoryContractTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 20);

    protected abstract PagoRepositoryPort repo();

    protected abstract long crearNegocio(String local);

    /** Crea una deuda real (si la implementación lo necesita) y devuelve su id. */
    protected abstract long crearDeuda(long negocioId, int mes);

    @Test
    void guardaPagoConSusAplicaciones() {
        long n = crearNegocio("L-1");
        long d1 = crearDeuda(n, 9);
        long d2 = crearDeuda(n, 10);
        Pago p = Pago.nuevo(n, HOY, Dinero.de(150000), HOY);
        p.asignarAplicaciones(List.of(new AplicacionPago(d1, Dinero.de(100000)), new AplicacionPago(d2, Dinero.de(50000))));

        Pago g = repo().guardar(p);
        Pago r = repo().buscarPorId(g.getId().longValue()).orElseThrow();

        assertEquals(Dinero.de(150000), r.getMonto());
        assertEquals(2, r.getAplicaciones().size());
        assertEquals(EstadoPago.VIGENTE, r.getEstado());
    }

    @Test
    void persisteLaAnulacion() {
        long n = crearNegocio("L-1");
        long d1 = crearDeuda(n, 9);
        Pago p = Pago.nuevo(n, HOY, Dinero.de(10), HOY);
        p.asignarAplicaciones(List.of(new AplicacionPago(d1, Dinero.de(10))));
        Pago g = repo().guardar(p);
        g.anular();
        repo().guardar(g);
        assertEquals(EstadoPago.ANULADO, repo().buscarPorId(g.getId().longValue()).orElseThrow().getEstado());
    }

    @Test
    void buscaPorNegocioOrdenadoPorFecha() {
        long n = crearNegocio("L-1");
        long d = crearDeuda(n, 9);
        for (int dia : new int[] {15, 5, 10}) {
            Pago p = Pago.nuevo(n, LocalDate.of(2026, 10, dia), Dinero.de(10), HOY);
            p.asignarAplicaciones(List.of(new AplicacionPago(d, Dinero.de(10))));
            repo().guardar(p);
        }
        List<Pago> lista = repo().buscarPorNegocio(n);
        assertEquals(3, lista.size());
        assertEquals(5, lista.get(0).getFecha().getDayOfMonth());
        assertEquals(15, lista.get(2).getFecha().getDayOfMonth());
    }

    @Test
    void inexistenteDevuelveVacio() {
        assertTrue(repo().buscarPorId(54321).isEmpty());
    }
}
