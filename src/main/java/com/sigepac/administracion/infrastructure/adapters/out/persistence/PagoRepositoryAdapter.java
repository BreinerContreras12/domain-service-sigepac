package com.sigepac.administracion.infrastructure.adapters.out.persistence;

import com.sigepac.administracion.application.port.out.PagoRepositoryPort;
import com.sigepac.administracion.domain.model.Pago;
import com.sigepac.administracion.infrastructure.adapters.out.persistence.entity.PagoEntity;
import com.sigepac.administracion.infrastructure.adapters.out.persistence.mapper.PagoMapper;
import com.sigepac.administracion.infrastructure.adapters.out.persistence.repository.PagoJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class PagoRepositoryAdapter implements PagoRepositoryPort {

    private final PagoJpaRepository jpa;

    public PagoRepositoryAdapter(PagoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Pago guardar(Pago pago) {
        PagoEntity entidad = pago.getId() == null
                ? PagoEntity.nueva()
                : jpa.findById(pago.getId()).orElseGet(PagoEntity::nueva);
        PagoMapper.copiarA(entidad, pago);
        return PagoMapper.aDominio(jpa.saveAndFlush(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pago> buscarPorId(long id) {
        return jpa.findById(id).map(PagoMapper::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pago> buscarPorNegocio(long negocioId) {
        return jpa.findByNegocioIdOrderByFechaAscIdAsc(negocioId).stream().map(PagoMapper::aDominio).toList();
    }
}
