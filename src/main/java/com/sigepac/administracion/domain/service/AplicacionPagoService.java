package com.sigepac.administracion.domain.service;

import com.sigepac.administracion.domain.exception.PagoExcedeSaldoException;
import com.sigepac.administracion.domain.exception.ValorInvalidoException;
import com.sigepac.administracion.domain.model.AplicacionPago;
import com.sigepac.administracion.domain.model.Deuda;
import com.sigepac.administracion.domain.vo.Dinero;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Servicio de dominio (Semana 6): reparte un pago entre varias deudas. La regla vive aquí porque
 * involucra a varias entidades y no le pertenece a ninguna sola.
 * Regla propuesta: se paga primero la deuda más antigua (período, y luego id).
 */
public class AplicacionPagoService {

    /**
     * Valida primero y muta después: si el pago excede el saldo, ninguna deuda cambia.
     * Modifica las deudas recibidas y devuelve cómo se repartió el pago.
     */
    public List<AplicacionPago> aplicar(Dinero monto, List<Deuda> deudas) {
        if (monto == null || !monto.esMayorQueCero()) {
            throw new ValorInvalidoException("El monto del pago debe ser mayor que cero");
        }
        List<Deuda> pendientes = deudas.stream()
                .filter(d -> d.saldo().esMayorQueCero())
                .sorted(Comparator.comparing(Deuda::getPeriodo).thenComparing(Deuda::getId))
                .toList();
        Dinero saldoTotal = pendientes.stream().map(Deuda::saldo).reduce(Dinero.CERO, Dinero::sumar);
        if (monto.compareTo(saldoTotal) > 0) {
            throw new PagoExcedeSaldoException(saldoTotal, monto);
        }

        List<AplicacionPago> aplicaciones = new ArrayList<>();
        Dinero restante = monto;
        for (Deuda deuda : pendientes) {
            if (!restante.esMayorQueCero()) {
                break;
            }
            Dinero saldo = deuda.saldo();
            Dinero aplicado = saldo.compareTo(restante) <= 0 ? saldo : restante;
            deuda.aplicarPago(aplicado);
            aplicaciones.add(new AplicacionPago(deuda.getId(), aplicado));
            restante = Dinero.de(restante.valor().subtract(aplicado.valor()));
        }
        return aplicaciones;
    }
}
