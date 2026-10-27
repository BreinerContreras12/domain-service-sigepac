package com.sigepac.administracion.testsupport;

import com.sigepac.administracion.application.port.out.PagoRepositoryPort;
import com.sigepac.administracion.domain.model.Pago;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class PagoRepositoryEnMemoria implements PagoRepositoryPort {

    private final Map<Long, Pago> almacen = new LinkedHashMap<>();
    private final AtomicLong secuencia = new AtomicLong();

    private static Pago copia(Pago p, long id) {
        return Pago.reconstruir(id, p.getNegocioId(), p.getFecha(), p.getMonto(), p.getEstado(), p.getAplicaciones());
    }

    @Override
    public Pago guardar(Pago pago) {
        long id = pago.getId() != null ? pago.getId() : secuencia.incrementAndGet();
        almacen.put(id, copia(pago, id));
        return copia(pago, id);
    }

    @Override
    public Optional<Pago> buscarPorId(long id) {
        return Optional.ofNullable(almacen.get(id)).map(p -> copia(p, id));
    }

    @Override
    public List<Pago> buscarPorNegocio(long negocioId) {
        return almacen.values().stream().filter(p -> p.getNegocioId() == negocioId)
                .sorted(Comparator.comparing(Pago::getFecha).thenComparing(Pago::getId))
                .map(p -> copia(p, p.getId())).toList();
    }

    public int cantidad() {
        return almacen.size();
    }
}
