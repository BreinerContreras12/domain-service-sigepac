package com.sigepac.administracion.application.service;

import com.sigepac.administracion.application.port.in.AnularPagoUseCase;
import com.sigepac.administracion.application.port.in.RegistrarPagoUseCase;
import com.sigepac.administracion.application.port.in.command.RegistrarPagoCommand;
import com.sigepac.administracion.application.port.in.result.ComprobantePago;
import com.sigepac.administracion.application.port.out.DeudaRepositoryPort;
import com.sigepac.administracion.application.port.out.NegocioRepositoryPort;
import com.sigepac.administracion.application.port.out.PagoRepositoryPort;
import com.sigepac.administracion.application.port.out.TransaccionPort;
import com.sigepac.administracion.domain.exception.DeudaNoEncontradaException;
import com.sigepac.administracion.domain.exception.NegocioNoEncontradoException;
import com.sigepac.administracion.domain.exception.PagoNoEncontradoException;
import com.sigepac.administracion.domain.model.AplicacionPago;
import com.sigepac.administracion.domain.model.Deuda;
import com.sigepac.administracion.domain.model.Negocio;
import com.sigepac.administracion.domain.model.Pago;
import com.sigepac.administracion.domain.service.AplicacionPagoService;
import com.sigepac.administracion.domain.service.EstadoCuentaService;
import com.sigepac.administracion.domain.vo.Dinero;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * HU-10: registrar un pago y actualizar el estado de cuenta. Todo ocurre dentro de una sola transacción
 * y, además, se valida todo antes de guardar nada: o queda todo o no queda nada.
 */
public class PagoService implements RegistrarPagoUseCase, AnularPagoUseCase {

    private final NegocioRepositoryPort negocios;
    private final DeudaRepositoryPort deudas;
    private final PagoRepositoryPort pagos;
    private final TransaccionPort transaccion;
    private final Clock reloj;
    private final AplicacionPagoService aplicacion;
    private final EstadoCuentaService estadoCuenta;

    public PagoService(NegocioRepositoryPort negocios, DeudaRepositoryPort deudas, PagoRepositoryPort pagos,
                       TransaccionPort transaccion, Clock reloj, AplicacionPagoService aplicacion,
                       EstadoCuentaService estadoCuenta) {
        this.negocios = negocios;
        this.deudas = deudas;
        this.pagos = pagos;
        this.transaccion = transaccion;
        this.reloj = reloj;
        this.aplicacion = aplicacion;
        this.estadoCuenta = estadoCuenta;
    }

    @Override
    public ComprobantePago registrar(RegistrarPagoCommand c) {
        return transaccion.ejecutar(() -> {
            Negocio negocio = negocios.buscarPorId(c.negocioId())
                    .orElseThrow(() -> new NegocioNoEncontradoException(c.negocioId()));
            negocio.asegurarActivo();

            Pago pago = Pago.nuevo(negocio.getId(), c.fecha(), Dinero.de(c.monto()), LocalDate.now(reloj));
            List<Deuda> deudasNegocio = deudas.buscarPorNegocio(negocio.getId());
            Dinero saldoAnterior = estadoCuenta.saldoDe(deudasNegocio);

            List<AplicacionPago> aplicaciones = aplicacion.aplicar(pago.getMonto(), deudasNegocio);
            pago.asignarAplicaciones(aplicaciones);

            // Recién aquí se guarda: ya se validó todo.
            aplicaciones.forEach(a -> deudasNegocio.stream()
                    .filter(d -> d.getId() == a.deudaId())
                    .findFirst()
                    .ifPresent(deudas::guardar));
            Pago guardado = pagos.guardar(pago);
            return new ComprobantePago(guardado, saldoAnterior, estadoCuenta.saldoDe(deudasNegocio));
        });
    }

    @Override
    public Pago anular(long pagoId) {
        return transaccion.ejecutar(() -> {
            Pago pago = pagos.buscarPorId(pagoId).orElseThrow(() -> new PagoNoEncontradoException(pagoId));

            // Se cargan todas las deudas afectadas antes de cambiar nada.
            List<Deuda> afectadas = new ArrayList<>();
            for (AplicacionPago a : pago.getAplicaciones()) {
                afectadas.add(deudas.buscarPorId(a.deudaId()).orElseThrow(() -> new DeudaNoEncontradaException(a.deudaId())));
            }
            pago.anular();
            for (int i = 0; i < afectadas.size(); i++) {
                afectadas.get(i).revertirPago(pago.getAplicaciones().get(i).monto());
            }
            afectadas.forEach(deudas::guardar);
            return pagos.guardar(pago);
        });
    }
}
