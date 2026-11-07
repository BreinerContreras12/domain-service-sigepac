package com.sigepac.administracion.infrastructure.adapters.out.persistence;

import com.sigepac.administracion.application.port.out.AuditoriaRepositoryPort;
import com.sigepac.administracion.domain.model.RegistroAuditoria;
import com.sigepac.administracion.infrastructure.adapters.out.persistence.mapper.AuditoriaMapper;
import com.sigepac.administracion.infrastructure.adapters.out.persistence.repository.AuditoriaJpaRepository;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** HU-12: guarda la bitácora en la misma transacción del caso de uso que la llama. */
@Component
@Transactional
public class AuditoriaRepositoryAdapter implements AuditoriaRepositoryPort {

    private final AuditoriaJpaRepository jpa;

    public AuditoriaRepositoryAdapter(AuditoriaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public RegistroAuditoria guardar(RegistroAuditoria registro) {
        return AuditoriaMapper.aDominio(jpa.saveAndFlush(AuditoriaMapper.aEntidad(registro)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroAuditoria> buscarPorNegocio(long negocioId) {
        return jpa.findByNegocioIdOrderByFechaHoraAscIdAsc(negocioId).stream().map(AuditoriaMapper::aDominio).toList();
    }
}
