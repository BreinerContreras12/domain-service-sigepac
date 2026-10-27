package com.sigepac.administracion.application.port.out;

import com.sigepac.administracion.domain.model.Deuda;
import com.sigepac.administracion.domain.vo.ConceptoDeuda;
import com.sigepac.administracion.domain.vo.Periodo;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DeudaRepositoryPort {

    Deuda guardar(Deuda deuda);

    Optional<Deuda> buscarPorId(long id);

    boolean existe(long negocioId, ConceptoDeuda concepto, Periodo periodo);

    List<Deuda> buscarPorNegocio(long negocioId);

    List<Deuda> buscarPorNegocios(Collection<Long> negocioIds);
}
