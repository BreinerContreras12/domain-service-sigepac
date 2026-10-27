package com.sigepac.administracion.domain.service;

import com.sigepac.administracion.domain.model.Deuda;
import com.sigepac.administracion.domain.model.Negocio;
import com.sigepac.administracion.domain.model.Pago;
import com.sigepac.administracion.domain.vo.Dinero;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calcula saldos. El saldo sale de las deudas (suma de valor - pagado) y siempre debe coincidir con
 * "total de deudas menos total de pagos vigentes": esa igualdad es la regla de integridad (RNF-6).
 */
public class EstadoCuentaService {

    public EstadoCuenta calcular(Negocio negocio, List<Deuda> deudas, List<Pago> pagos) {
        List<Deuda> ordenadas = deudas.stream()
                .sorted(Comparator.comparing(Deuda::getPeriodo).thenComparing(Deuda::getConcepto).thenComparing(Deuda::getId))
                .toList();
        List<Pago> pagosOrdenados = pagos.stream()
                .sorted(Comparator.comparing(Pago::getFecha).thenComparing(Pago::getId))
                .toList();
        Dinero totalDeudas = ordenadas.stream().map(Deuda::getValor).reduce(Dinero.CERO, Dinero::sumar);
        Dinero totalPagado = pagosOrdenados.stream().filter(Pago::estaVigente).map(Pago::getMonto)
                .reduce(Dinero.CERO, Dinero::sumar);
        return new EstadoCuenta(negocio, ordenadas, pagosOrdenados, totalDeudas, totalPagado, saldoDe(ordenadas));
    }

    public Dinero saldoDe(List<Deuda> deudas) {
        return deudas.stream().map(Deuda::saldo).reduce(Dinero.CERO, Dinero::sumar);
    }

    public Map<Long, Dinero> saldosPorNegocio(List<Deuda> deudas) {
        Map<Long, Dinero> saldos = new HashMap<>();
        for (Deuda d : deudas) {
            saldos.merge(d.getNegocioId(), d.saldo(), Dinero::sumar);
        }
        return saldos;
    }
}
