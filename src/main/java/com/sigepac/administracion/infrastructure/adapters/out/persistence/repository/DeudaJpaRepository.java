package com.sigepac.administracion.infrastructure.adapters.out.persistence.repository;

import com.sigepac.administracion.infrastructure.adapters.out.persistence.entity.DeudaEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeudaJpaRepository extends JpaRepository<DeudaEntity, Long> {

    boolean existsByNegocioIdAndConceptoAndAnioAndMes(Long negocioId, String concepto, int anio, int mes);

    List<DeudaEntity> findByNegocioIdOrderByAnioAscMesAscIdAsc(Long negocioId);

    List<DeudaEntity> findByNegocioIdIn(Collection<Long> negocioIds);
}
