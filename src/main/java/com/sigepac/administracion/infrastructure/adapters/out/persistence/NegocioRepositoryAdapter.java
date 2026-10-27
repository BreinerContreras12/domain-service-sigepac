package com.sigepac.administracion.infrastructure.adapters.out.persistence;

import com.sigepac.administracion.application.port.out.FiltroNegocio;
import com.sigepac.administracion.application.port.out.NegocioRepositoryPort;
import com.sigepac.administracion.domain.model.Negocio;
import com.sigepac.administracion.domain.vo.CodigoLocal;
import com.sigepac.administracion.infrastructure.adapters.out.persistence.entity.NegocioEntity;
import com.sigepac.administracion.infrastructure.adapters.out.persistence.mapper.NegocioMapper;
import com.sigepac.administracion.infrastructure.adapters.out.persistence.repository.NegocioJpaRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class NegocioRepositoryAdapter implements NegocioRepositoryPort {

    private final NegocioJpaRepository jpa;

    public NegocioRepositoryAdapter(NegocioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Negocio guardar(Negocio negocio) {
        // Si ya existe se reutiliza la entidad cargada para conservar su @Version.
        NegocioEntity entidad = negocio.getId() == null
                ? NegocioEntity.nueva()
                : jpa.findById(negocio.getId()).orElseGet(NegocioEntity::nueva);
        NegocioMapper.copiarA(entidad, negocio);
        return NegocioMapper.aDominio(jpa.saveAndFlush(entidad));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Negocio> buscarPorId(long id) {
        return jpa.findById(id).map(NegocioMapper::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Negocio> buscarPorLocalActivo(CodigoLocal local) {
        return jpa.findByLocalActivo(local.valor()).map(NegocioMapper::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Negocio> buscar(FiltroNegocio f) {
        Specification<NegocioEntity> spec = (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (!f.incluirInactivos()) {
                condiciones.add(cb.equal(root.get("estado"), "ACTIVO"));
            }
            if (f.nombreContiene() != null) {
                String patron = "%" + escapar(f.nombreContiene().toLowerCase(Locale.ROOT)) + "%";
                condiciones.add(cb.like(cb.lower(root.get("nombre")), patron, '\\'));
            }
            if (f.local() != null) {
                condiciones.add(cb.equal(root.get("local"), f.local().valor()));
            }
            if (f.documentoPropietario() != null) {
                condiciones.add(cb.equal(root.get("documentoPropietario"), f.documentoPropietario().valor()));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
        return jpa.findAll(spec, Sort.by("local", "id")).stream().map(NegocioMapper::aDominio).toList();
    }

    /** Para que un "%" o "_" escrito por el usuario se busque como texto y no como comodín. */
    private static String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
