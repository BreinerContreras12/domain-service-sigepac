package com.sigepac.administracion.application.port.in;

import com.sigepac.administracion.application.port.in.command.BuscarNegociosQuery;
import com.sigepac.administracion.application.port.in.result.PaginaNegocios;

public interface BuscarNegociosUseCase {
    PaginaNegocios buscar(BuscarNegociosQuery consulta);
}
