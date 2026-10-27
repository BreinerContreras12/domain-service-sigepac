package com.sigepac.administracion.infrastructure.adapters.out.persistence.repository;

import com.sigepac.administracion.infrastructure.adapters.out.persistence.entity.NegocioEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NegocioJpaRepository extends JpaRepository<NegocioEntity, Long>, JpaSpecificationExecutor<NegocioEntity> {

    Optional<NegocioEntity> findByLocalActivo(String localActivo);
}
