package com.sigepac.administracion.application.port.in;

import com.sigepac.administracion.application.port.in.command.RegistrarDeudaCommand;
import com.sigepac.administracion.domain.model.Deuda;

public interface RegistrarDeudaUseCase {
    Deuda registrar(RegistrarDeudaCommand comando);
}
