package com.sigepac.administracion.application.port.in;

import com.sigepac.administracion.application.port.in.command.ActualizarDeudaCommand;
import com.sigepac.administracion.domain.model.Deuda;

public interface ActualizarDeudaUseCase {
    Deuda actualizar(ActualizarDeudaCommand comando);
}
