package com.sigepac.administracion.application.port.in;

import com.sigepac.administracion.domain.model.RegistroAuditoria;
import java.util.List;

/** HU-12: la administradora consulta quién cambió qué y cuándo en un negocio. */
public interface ConsultarAuditoriaUseCase {

    List<RegistroAuditoria> deNegocio(long negocioId);
}
