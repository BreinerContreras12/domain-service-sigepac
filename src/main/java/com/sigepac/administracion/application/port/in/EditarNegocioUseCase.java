package com.sigepac.administracion.application.port.in;

import com.sigepac.administracion.application.port.in.command.EditarNegocioCommand;
import com.sigepac.administracion.domain.model.Negocio;

public interface EditarNegocioUseCase {
    Negocio editar(EditarNegocioCommand comando);
}
