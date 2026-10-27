package com.sigepac.administracion.application.service;

import com.sigepac.administracion.application.port.in.ConsultarEstadoCuentaUseCase;
import com.sigepac.administracion.application.port.out.DeudaRepositoryPort;
import com.sigepac.administracion.application.port.out.NegocioRepositoryPort;
import com.sigepac.administracion.application.port.out.PagoRepositoryPort;
import com.sigepac.administracion.domain.exception.NegocioNoEncontradoException;
import com.sigepac.administracion.domain.model.Negocio;
import com.sigepac.administracion.domain.service.EstadoCuenta;
import com.sigepac.administracion.domain.service.EstadoCuentaService;

public class ConsultarEstadoCuentaService implements ConsultarEstadoCuentaUseCase {

    private final NegocioRepositoryPort negocios;
    private final DeudaRepositoryPort deudas;
    private final PagoRepositoryPort pagos;
    private final EstadoCuentaService estadoCuenta;

    public ConsultarEstadoCuentaService(NegocioRepositoryPort negocios, DeudaRepositoryPort deudas,
                                        PagoRepositoryPort pagos, EstadoCuentaService estadoCuenta) {
        this.negocios = negocios;
        this.deudas = deudas;
        this.pagos = pagos;
        this.estadoCuenta = estadoCuenta;
    }

    @Override
    public EstadoCuenta porNegocio(long negocioId) {
        Negocio negocio = negocios.buscarPorId(negocioId).orElseThrow(() -> new NegocioNoEncontradoException(negocioId));
        return estadoCuenta.calcular(negocio, deudas.buscarPorNegocio(negocioId), pagos.buscarPorNegocio(negocioId));
    }
}
