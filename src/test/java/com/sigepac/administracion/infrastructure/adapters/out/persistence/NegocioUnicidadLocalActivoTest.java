package com.sigepac.administracion.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sigepac.administracion.domain.vo.Dinero;
import com.sigepac.administracion.domain.model.Negocio;
import com.sigepac.administracion.testsupport.Datos;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

/** La base de datos es la última defensa contra dos altas simultáneas en el mismo local. */
@DataJpaTest
@Import(NegocioRepositoryAdapter.class)
class NegocioUnicidadLocalActivoTest {

    @Autowired
    private NegocioRepositoryAdapter adaptador;
    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("Dos negocios ACTIVOS en el mismo local: la base de datos lo rechaza")
    void dosActivosEnElMismoLocalFallan() {
        adaptador.guardar(Datos.panaderia("L-77"));
        assertThrows(DataIntegrityViolationException.class, () -> adaptador.guardar(Datos.panaderia("L-77")));
    }

    @Test
    @DisplayName("Un negocio dado de baja libera el local para otro")
    void unInactivoNoOcupaElLocal() {
        Negocio viejo = adaptador.guardar(Datos.panaderia("L-78"));
        viejo.darDeBaja(Dinero.CERO);
        adaptador.guardar(viejo);
        em.flush();
        em.clear();
        adaptador.guardar(Datos.panaderia("L-78"));   // no debe lanzar
    }
}
