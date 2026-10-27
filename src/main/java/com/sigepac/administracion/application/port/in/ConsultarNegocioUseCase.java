package com.sigepac.administracion.application.port.in;

import com.sigepac.administracion.domain.model.Negocio;

public interface ConsultarNegocioUseCase {
    Negocio porId(long negocioId);
}
