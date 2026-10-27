package com.sigepac.administracion.application.port.in;

import com.sigepac.administracion.application.port.in.result.PaginaNegocios;

public interface ListarNegociosUseCase {
    PaginaNegocios listar(boolean incluirInactivos, int pagina, int tamano);
}
