package com.sigepac.administracion.application.port.in;

import com.sigepac.administracion.application.port.in.command.RegistrarPagoCommand;
import com.sigepac.administracion.application.port.in.result.ComprobantePago;

public interface RegistrarPagoUseCase {
    ComprobantePago registrar(RegistrarPagoCommand comando);
}
