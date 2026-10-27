package com.sigepac.administracion.infrastructure.adapters.out.persistence;

import com.sigepac.administracion.application.port.out.PagoRepositoryPort;
import com.sigepac.administracion.contract.PagoRepositoryContractTest;
import com.sigepac.administracion.domain.model.Deuda;
import com.sigepac.administracion.domain.model.Pago;
import com.sigepac.administracion.domain.vo.ConceptoDeuda;
import com.sigepac.administracion.domain.vo.Dinero;
import com.sigepac.administracion.domain.vo.Periodo;
import com.sigepac.administracion.testsupport.Datos;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({PagoRepositoryAdapter.class, DeudaRepositoryAdapter.class, NegocioRepositoryAdapter.class})
class PagoRepositoryAdapterContractTest extends PagoRepositoryContractTest {

    @Autowired
    private PagoRepositoryAdapter adaptador;
    @Autowired
    private DeudaRepositoryAdapter deudas;
    @Autowired
    private NegocioRepositoryAdapter negocios;
    @Autowired
    private EntityManager em;

    @Override
    protected PagoRepositoryPort repo() {
        return new PagoRepositoryPort() {
            public Pago guardar(Pago p) { Pago r = adaptador.guardar(p); em.flush(); em.clear(); return r; }
            public Optional<Pago> buscarPorId(long id) { return adaptador.buscarPorId(id); }
            public List<Pago> buscarPorNegocio(long n) { return adaptador.buscarPorNegocio(n); }
        };
    }

    @Override
    protected long crearNegocio(String local) {
        return negocios.guardar(Datos.panaderia(local)).getId().longValue();
    }

    @Override
    protected long crearDeuda(long negocioId, int mes) {
        Deuda d = deudas.guardar(Deuda.registrar(negocioId, ConceptoDeuda.ALQUILER, Periodo.de(2026, mes), Dinero.de(100000)));
        return d.getId().longValue();
    }
}
