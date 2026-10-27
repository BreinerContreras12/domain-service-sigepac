package com.sigepac.administracion.application.port.in;

import com.sigepac.administracion.domain.model.Negocio;

public interface DarDeBajaNegocioUseCase {
    Negocio darDeBaja(long negocioId);
}
