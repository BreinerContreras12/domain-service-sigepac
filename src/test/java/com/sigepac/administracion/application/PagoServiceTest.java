package com.sigepac.administracion.application;

import static org.junit.jupiter.api.Assertions.*;

import com.sigepac.administracion.application.port.in.command.RegistrarPagoCommand;
import com.sigepac.administracion.application.port.in.result.ComprobantePago;
import com.sigepac.administracion.domain.exception.*;
import com.sigepac.administracion.domain.model.EstadoPago;
import com.sigepac.administracion.domain.model.Negocio;
import com.sigepac.administracion.domain.service.EstadoCuenta;
import com.sigepac.administracion.domain.vo.Dinero;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PagoServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 20);
    private final Escenario e = new Escenario();

    private RegistrarPagoCommand pago(long negocio, long monto) {
        return new RegistrarPagoCommand(negocio, HOY, BigDecimal.valueOf(monto));
    }

    private long negocioConDosDeudas() {
        long id = e.negocio("Uno", "L-1", "1090123456").getId().longValue();
        e.deuda(id, "ADMINISTRACION", 9, 100000);
        e.deuda(id, "ADMINISTRACION", 10, 100000);
        return id;
    }

    @Test
    @DisplayName("El comprobante muestra saldo anterior y saldo nuevo")
    void comprobante() {
        long id = negocioConDosDeudas();
        ComprobantePago c = e.pagoService.registrar(pago(id, 130000));
        assertEquals(Dinero.de(200000), c.saldoAnterior());
        assertEquals(Dinero.de(70000), c.saldoNuevo());
        assertEquals(2, c.pago().getAplicaciones().size());
        assertNotNull(c.pago().getId());
    }

    @Test
    void sinSaldoParaPagarNoQuedaNada() {
        long id = negocioConDosDeudas();
        assertThrows(PagoExcedeSaldoException.class, () -> e.pagoService.registrar(pago(id, 200001)));
        assertEquals(0, e.pagos.cantidad());
        assertEquals(Dinero.de(200000), e.estadoCuentaService.porNegocio(id).saldo());
    }

    @Test
    void fechaFuturaNegocioInexistenteOInactivo() {
        long id = negocioConDosDeudas();
        assertThrows(ValorInvalidoException.class,
                () -> e.pagoService.registrar(new RegistrarPagoCommand(id, HOY.plusDays(1), BigDecimal.TEN)));
        assertThrows(NegocioNoEncontradoException.class, () -> e.pagoService.registrar(pago(99, 10)));
    }

    @Test
    @DisplayName("Anular un pago devuelve los saldos y no se puede anular dos veces")
    void anularRestauraSaldos() {
        long id = negocioConDosDeudas();
        long pagoId = e.pagoService.registrar(pago(id, 130000)).pago().getId().longValue();

        var anulado = e.pagoService.anular(pagoId);

        assertEquals(EstadoPago.ANULADO, anulado.getEstado());
        EstadoCuenta ec = e.estadoCuentaService.porNegocio(id);
        assertEquals(Dinero.de(200000), ec.saldo());
        assertEquals(Dinero.CERO, ec.totalPagado());
        assertThrows(PagoYaAnuladoException.class, () -> e.pagoService.anular(pagoId));
        assertThrows(PagoNoEncontradoException.class, () -> e.pagoService.anular(999));
    }

    @Test
    void despuesDeAnularSePuedeVolverAPagar() {
        long id = negocioConDosDeudas();
        long pagoId = e.pagoService.registrar(pago(id, 200000)).pago().getId().longValue();
        assertThrows(PagoExcedeSaldoException.class, () -> e.pagoService.registrar(pago(id, 1)));
        e.pagoService.anular(pagoId);
        e.pagoService.registrar(pago(id, 200000));
        assertEquals(Dinero.CERO, e.estadoCuentaService.porNegocio(id).saldo());
    }
}
