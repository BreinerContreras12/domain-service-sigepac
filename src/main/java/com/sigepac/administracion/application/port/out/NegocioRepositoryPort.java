package com.sigepac.administracion.application.port.out;

import com.sigepac.administracion.domain.model.Negocio;
import com.sigepac.administracion.domain.vo.CodigoLocal;
import java.util.List;
import java.util.Optional;

public interface NegocioRepositoryPort {

    /** Si el negocio no tiene id lo crea y lo devuelve con id asignado; si lo tiene, lo actualiza. */
    Negocio guardar(Negocio negocio);

    Optional<Negocio> buscarPorId(long id);

    /** El negocio ACTIVO que ocupa ese local, si lo hay. */
    Optional<Negocio> buscarPorLocalActivo(CodigoLocal local);

    /** Sin orden garantizado: ordenar es responsabilidad de quien lo llama. */
    List<Negocio> buscar(FiltroNegocio filtro);
}
