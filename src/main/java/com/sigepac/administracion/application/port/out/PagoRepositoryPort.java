package com.sigepac.administracion.application.port.out;

import com.sigepac.administracion.domain.model.Pago;
import java.util.List;
import java.util.Optional;

public interface PagoRepositoryPort {

    Pago guardar(Pago pago);

    Optional<Pago> buscarPorId(long id);

    List<Pago> buscarPorNegocio(long negocioId);
}
