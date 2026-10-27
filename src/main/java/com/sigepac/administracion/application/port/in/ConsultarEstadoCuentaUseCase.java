package com.sigepac.administracion.application.port.in;

import com.sigepac.administracion.domain.service.EstadoCuenta;

public interface ConsultarEstadoCuentaUseCase {
    EstadoCuenta porNegocio(long negocioId);
}
