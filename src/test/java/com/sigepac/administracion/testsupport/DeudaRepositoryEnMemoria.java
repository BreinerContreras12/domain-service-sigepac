package com.sigepac.administracion.testsupport;

import com.sigepac.administracion.application.port.out.DeudaRepositoryPort;
import com.sigepac.administracion.domain.model.Deuda;
import com.sigepac.administracion.domain.vo.ConceptoDeuda;
import com.sigepac.administracion.domain.vo.Periodo;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class DeudaRepositoryEnMemoria implements DeudaRepositoryPort {

    private final Map<Long, Deuda> almacen = new LinkedHashMap<>();
    private final AtomicLong secuencia = new AtomicLong();

    private static Deuda copia(Deuda d, long id) {
        return Deuda.reconstruir(id, d.getNegocioId(), d.getConcepto(), d.getPeriodo(), d.getValor(), d.getPagado());
    }

    @Override
    public Deuda guardar(Deuda deuda) {
        long id = deuda.getId() != null ? deuda.getId() : secuencia.incrementAndGet();
        almacen.put(id, copia(deuda, id));
        return copia(deuda, id);
    }

    @Override
    public Optional<Deuda> buscarPorId(long id) {
        return Optional.ofNullable(almacen.get(id)).map(d -> copia(d, id));
    }

    @Override
    public boolean existe(long negocioId, ConceptoDeuda concepto, Periodo periodo) {
        return almacen.values().stream().anyMatch(d -> d.getNegocioId() == negocioId
                && d.getConcepto() == concepto && d.getPeriodo().equals(periodo));
    }

    @Override
    public List<Deuda> buscarPorNegocio(long negocioId) {
        return almacen.values().stream().filter(d -> d.getNegocioId() == negocioId).map(d -> copia(d, d.getId())).toList();
    }

    @Override
    public List<Deuda> buscarPorNegocios(Collection<Long> negocioIds) {
        return almacen.values().stream().filter(d -> negocioIds.contains(d.getNegocioId())).map(d -> copia(d, d.getId())).toList();
    }
}
