package com.sigepac.administracion.testsupport;

import com.sigepac.administracion.application.port.out.AuditoriaRepositoryPort;
import com.sigepac.administracion.domain.model.RegistroAuditoria;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class AuditoriaRepositoryEnMemoria implements AuditoriaRepositoryPort {

    private final List<RegistroAuditoria> almacen = new ArrayList<>();
    private final AtomicLong secuencia = new AtomicLong();

    @Override
    public RegistroAuditoria guardar(RegistroAuditoria r) {
        RegistroAuditoria guardado = RegistroAuditoria.reconstruir(secuencia.incrementAndGet(), r.getAccion(),
                r.getEntidadId(), r.getNegocioId(), r.getUsuario(), r.getFechaHora(), r.getDetalle());
        almacen.add(guardado);
        return guardado;
    }

    @Override
    public List<RegistroAuditoria> buscarPorNegocio(long negocioId) {
        return almacen.stream().filter(r -> r.getNegocioId() == negocioId).toList();
    }

    public List<RegistroAuditoria> todos() {
        return List.copyOf(almacen);
    }
}
