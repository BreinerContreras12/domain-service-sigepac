package com.sigepac.administracion.infrastructure.adapters.out.persistence.repository;

import com.sigepac.administracion.infrastructure.adapters.out.persistence.entity.RegistroAuditoriaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaJpaRepository extends JpaRepository<RegistroAuditoriaEntity, Long> {

    List<RegistroAuditoriaEntity> findByNegocioIdOrderByFechaHoraAscIdAsc(Long negocioId);
}
