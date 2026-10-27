package com.sigepac.administracion.infrastructure.adapters.out.persistence;

import com.sigepac.administracion.application.port.out.DeudaRepositoryPort;
import com.sigepac.administracion.contract.DeudaRepositoryContractTest;
import com.sigepac.administracion.domain.model.Deuda;
import com.sigepac.administracion.domain.vo.ConceptoDeuda;
import com.sigepac.administracion.domain.vo.Periodo;
import com.sigepac.administracion.testsupport.Datos;
import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({DeudaRepositoryAdapter.class, NegocioRepositoryAdapter.class})
class DeudaRepositoryAdapterContractTest extends DeudaRepositoryContractTest {

    @Autowired
    private DeudaRepositoryAdapter adaptador;
    @Autowired
    private NegocioRepositoryAdapter negocios;
    @Autowired
    private EntityManager em;

    @Override
    protected DeudaRepositoryPort repo() {
        return new DeudaRepositoryPort() {
            public Deuda guardar(Deuda d) { Deuda r = adaptador.guardar(d); em.flush(); em.clear(); return r; }
            public Optional<Deuda> buscarPorId(long id) { return adaptador.buscarPorId(id); }
            public boolean existe(long n, ConceptoDeuda c, Periodo p) { return adaptador.existe(n, c, p); }
            public List<Deuda> buscarPorNegocio(long n) { return adaptador.buscarPorNegocio(n); }
            public List<Deuda> buscarPorNegocios(Collection<Long> ids) { return adaptador.buscarPorNegocios(ids); }
        };
    }

    @Override
    protected long crearNegocio(String local) {
        return negocios.guardar(Datos.panaderia(local)).getId().longValue();
    }
}
