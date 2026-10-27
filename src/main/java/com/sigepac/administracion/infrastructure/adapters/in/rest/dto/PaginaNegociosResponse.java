package com.sigepac.administracion.infrastructure.adapters.in.rest.dto;

import com.sigepac.administracion.application.port.in.result.PaginaNegocios;
import java.util.List;

public record PaginaNegociosResponse(List<ResumenNegocioResponse> contenido, int pagina, int tamano,
                                     long totalElementos, int totalPaginas) {

    public static PaginaNegociosResponse de(PaginaNegocios p) {
        return new PaginaNegociosResponse(p.contenido().stream().map(ResumenNegocioResponse::de).toList(),
                p.pagina(), p.tamano(), p.totalElementos(), p.totalPaginas());
    }
}
