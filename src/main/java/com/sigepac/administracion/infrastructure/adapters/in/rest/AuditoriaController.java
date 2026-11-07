package com.sigepac.administracion.infrastructure.adapters.in.rest;

import com.sigepac.administracion.application.port.in.ConsultarAuditoriaUseCase;
import com.sigepac.administracion.infrastructure.adapters.in.rest.dto.RegistroAuditoriaResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** HU-12: quién cambió qué y cuándo en un negocio. Solo ADMIN (lo exige SecurityConfig para /api/**). */
@RestController
public class AuditoriaController {

    private final ConsultarAuditoriaUseCase consultar;

    public AuditoriaController(ConsultarAuditoriaUseCase consultar) {
        this.consultar = consultar;
    }

    @GetMapping("/api/negocios/{id}/auditoria")
    public List<RegistroAuditoriaResponse> deNegocio(@PathVariable long id) {
        return consultar.deNegocio(id).stream().map(RegistroAuditoriaResponse::de).toList();
    }
}
