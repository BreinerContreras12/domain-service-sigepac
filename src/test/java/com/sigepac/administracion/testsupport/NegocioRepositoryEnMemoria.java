package com.sigepac.administracion.testsupport;

import com.sigepac.administracion.application.port.out.FiltroNegocio;
import com.sigepac.administracion.application.port.out.NegocioRepositoryPort;
import com.sigepac.administracion.domain.model.EstadoNegocio;
import com.sigepac.administracion.domain.model.Negocio;
import com.sigepac.administracion.domain.vo.CodigoLocal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class NegocioRepositoryEnMemoria implements NegocioRepositoryPort {

    private final Map<Long, Negocio> almacen = new LinkedHashMap<>();
    private final AtomicLong secuencia = new AtomicLong();

    private static Negocio copia(Negocio n, long id) {
        return Negocio.reconstruir(id, n.getNombre(), n.getLocal(), n.getPropietario(), n.getEstado());
    }

    @Override
    public Negocio guardar(Negocio negocio) {
        long id = negocio.getId() != null ? negocio.getId() : secuencia.incrementAndGet();
        almacen.put(id, copia(negocio, id));
        return copia(negocio, id);
    }

    @Override
    public Optional<Negocio> buscarPorId(long id) {
        return Optional.ofNullable(almacen.get(id)).map(n -> copia(n, id));
    }

    @Override
    public Optional<Negocio> buscarPorLocalActivo(CodigoLocal local) {
        return almacen.values().stream()
                .filter(n -> n.getEstado() == EstadoNegocio.ACTIVO && n.getLocal().equals(local))
                .findFirst().map(n -> copia(n, n.getId()));
    }

    @Override
    public List<Negocio> buscar(FiltroNegocio f) {
        List<Negocio> resultado = new ArrayList<>();
        for (Negocio n : almacen.values()) {
            if (!f.incluirInactivos() && n.getEstado() != EstadoNegocio.ACTIVO) continue;
            if (f.nombreContiene() != null
                    && !n.getNombre().toLowerCase(Locale.ROOT).contains(f.nombreContiene().toLowerCase(Locale.ROOT))) continue;
            if (f.local() != null && !n.getLocal().equals(f.local())) continue;
            if (f.documentoPropietario() != null && !n.getPropietario().documento().equals(f.documentoPropietario())) continue;
            resultado.add(copia(n, n.getId()));
        }
        return resultado;
    }

    public int cantidad() {
        return almacen.size();
    }
}
