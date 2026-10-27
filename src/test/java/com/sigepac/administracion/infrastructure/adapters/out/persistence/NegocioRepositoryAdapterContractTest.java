package com.sigepac.administracion.infrastructure.adapters.out.persistence;

import com.sigepac.administracion.application.port.out.FiltroNegocio;
import com.sigepac.administracion.application.port.out.NegocioRepositoryPort;
import com.sigepac.administracion.contract.NegocioRepositoryContractTest;
import com.sigepac.administracion.domain.model.Negocio;
import com.sigepac.administracion.domain.vo.CodigoLocal;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(NegocioRepositoryAdapter.class)
class NegocioRepositoryAdapterContractTest extends NegocioRepositoryContractTest {

    @Autowired
    private NegocioRepositoryAdapter adaptador;
    @Autowired
    private EntityManager em;

    /** Después de cada guardar se vacía la caché de Hibernate: así las lecturas van de verdad a la base de datos. */
    @Override
    protected NegocioRepositoryPort repo() {
        return new NegocioRepositoryPort() {
            public Negocio guardar(Negocio n) { Negocio r = adaptador.guardar(n); em.flush(); em.clear(); return r; }
            public Optional<Negocio> buscarPorId(long id) { return adaptador.buscarPorId(id); }
            public Optional<Negocio> buscarPorLocalActivo(CodigoLocal l) { return adaptador.buscarPorLocalActivo(l); }
            public List<Negocio> buscar(FiltroNegocio f) { return adaptador.buscar(f); }
        };
    }
}
