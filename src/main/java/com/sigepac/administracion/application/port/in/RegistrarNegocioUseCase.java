package com.sigepac.administracion.application.port.in;

import com.sigepac.administracion.application.port.in.command.RegistrarNegocioCommand;
import com.sigepac.administracion.domain.model.Negocio;

public interface RegistrarNegocioUseCase {
    Negocio registrar(RegistrarNegocioCommand comando);
}
