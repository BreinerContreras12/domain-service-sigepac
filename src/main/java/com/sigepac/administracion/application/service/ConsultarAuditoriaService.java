package com.sigepac.administracion.application.service;

import com.sigepac.administracion.application.port.in.ConsultarAuditoriaUseCase;
import com.sigepac.administracion.application.port.out.AuditoriaRepositoryPort;
import com.sigepac.administracion.application.port.out.NegocioRepositoryPort;
import com.sigepac.administracion.domain.exception.NegocioNoEncontradoException;
import com.sigepac.administracion.domain.model.RegistroAuditoria;
import java.util.List;

/** HU-12: historial de cambios de un negocio (sus datos, sus deudas y sus pagos). */
public class ConsultarAuditoriaService implements ConsultarAuditoriaUseCase {

    private final NegocioRepositoryPort negocios;
    private final AuditoriaRepositoryPort auditoria;

    public ConsultarAuditoriaService(NegocioRepositoryPort negocios, AuditoriaRepositoryPort auditoria) {
        this.negocios = negocios;
        this.auditoria = auditoria;
    }

    @Override
    public List<RegistroAuditoria> deNegocio(long negocioId) {
        negocios.buscarPorId(negocioId).orElseThrow(() -> new NegocioNoEncontradoException(negocioId));
        return auditoria.buscarPorNegocio(negocioId);
    }
}
