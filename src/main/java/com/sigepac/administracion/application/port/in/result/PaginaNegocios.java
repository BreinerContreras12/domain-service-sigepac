package com.sigepac.administracion.application.port.in.result;

import java.util.List;

public record PaginaNegocios(List<ResumenNegocio> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public PaginaNegocios {
        contenido = List.copyOf(contenido);
    }
}
