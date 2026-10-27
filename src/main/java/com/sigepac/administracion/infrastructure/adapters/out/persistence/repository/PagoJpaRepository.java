package com.sigepac.administracion.infrastructure.adapters.out.persistence.repository;

import com.sigepac.administracion.infrastructure.adapters.out.persistence.entity.PagoEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoJpaRepository extends JpaRepository<PagoEntity, Long> {

    List<PagoEntity> findByNegocioIdOrderByFechaAscIdAsc(Long negocioId);
}
