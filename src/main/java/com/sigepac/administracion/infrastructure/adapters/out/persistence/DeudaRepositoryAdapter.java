package com.sigepac.administracion.infrastructure.adapters.out.persistence;

import com.sigepac.administracion.application.port.out.DeudaRepositoryPort;
import com.sigepac.administracion.domain.model.Deuda;
import com.sigepac.administracion.domain.vo.ConceptoDeuda;
import com.sigepac.administracion.domain.vo.Periodo;
import com.sigepac.administracion.infrastructure.adapters.out.persistence.entity.DeudaEntity;
import com.sigepac.administracion.infrastructure.adapters.out.persistence.mapper.DeudaMapper;
import com.sigepac.administracion.infrastructure.adapters.out.persistence.repository.DeudaJpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class DeudaRepositoryAdapter implements DeudaRepositoryPort {

    private final DeudaJpaRepository jpa;

    public DeudaRepositoryAdapter(DeudaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Deuda guardar(Deuda deuda) {
        // Reutiliza la entidad cargada: así Hibernate conserva su @Version y detecta pagos simultáneos.
        DeudaEntity entidad = deuda.getId() == null
                ? DeudaEntity.nueva()
                : jpa.findById(deuda.getId()).orElseGet(DeudaEntity::nueva);
        DeudaMapper.copiarA(entidad, deuda);
        return DeudaMapper.aDominio(jpa.saveAndFlush(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Deuda> buscarPorId(long id) {
        return jpa.findById(id).map(DeudaMapper::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existe(long negocioId, ConceptoDeuda concepto, Periodo periodo) {
        return jpa.existsByNegocioIdAndConceptoAndAnioAndMes(negocioId, concepto.name(), periodo.anio(), periodo.mes());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Deuda> buscarPorNegocio(long negocioId) {
        return jpa.findByNegocioIdOrderByAnioAscMesAscIdAsc(negocioId).stream().map(DeudaMapper::aDominio).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Deuda> buscarPorNegocios(Collection<Long> negocioIds) {
        if (negocioIds.isEmpty()) {
            return List.of();
        }
        return jpa.findByNegocioIdIn(negocioIds).stream().map(DeudaMapper::aDominio).toList();
    }
}
