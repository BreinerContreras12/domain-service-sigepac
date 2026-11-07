package com.sigepac.administracion.application.port.out;

import com.sigepac.administracion.domain.model.RegistroAuditoria;
import java.util.List;

/** HU-12: dónde se guarda la bitácora de auditoría. Solo se agrega y se consulta: nunca se edita ni se borra. */
public interface AuditoriaRepositoryPort {

    RegistroAuditoria guardar(RegistroAuditoria registro);

    /** Del más antiguo al más reciente. */
    List<RegistroAuditoria> buscarPorNegocio(long negocioId);
}
