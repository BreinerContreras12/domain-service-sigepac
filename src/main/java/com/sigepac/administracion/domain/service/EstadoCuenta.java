package com.sigepac.administracion.domain.service;

import com.sigepac.administracion.domain.model.Deuda;
import com.sigepac.administracion.domain.model.Negocio;
import com.sigepac.administracion.domain.model.Pago;
import com.sigepac.administracion.domain.vo.Dinero;
import java.util.List;

/** Foto del estado de un negocio: sus deudas, sus pagos y cuánto debe hoy. */
public record EstadoCuenta(Negocio negocio, List<Deuda> deudas, List<Pago> pagos,
                           Dinero totalDeudas, Dinero totalPagado, Dinero saldo) {

    public EstadoCuenta {
        deudas = List.copyOf(deudas);
        pagos = List.copyOf(pagos);
    }
}
