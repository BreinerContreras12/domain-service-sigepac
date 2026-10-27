package com.sigepac.administracion.infrastructure.adapters.out.persistence.mapper;

import com.sigepac.administracion.domain.model.EstadoNegocio;
import com.sigepac.administracion.domain.model.Negocio;
import com.sigepac.administracion.domain.vo.CodigoLocal;
import com.sigepac.administracion.domain.vo.Propietario;
import com.sigepac.administracion.infrastructure.adapters.out.persistence.entity.NegocioEntity;

/** Traduce entre el modelo de dominio y el de base de datos. Ninguno de los dos conoce al otro. */
public final class NegocioMapper {

    private NegocioMapper() { }

    public static Negocio aDominio(NegocioEntity e) {
        return Negocio.reconstruir(e.getId(), e.getNombre(), CodigoLocal.de(e.getLocal()),
                Propietario.de(e.getDocumentoPropietario(), e.getNombrePropietario(), e.getTelefonoPropietario()),
                EstadoNegocio.valueOf(e.getEstado()));
    }

    /** Copia el estado del dominio sobre una entidad (nueva o ya cargada, para conservar su @Version). */
    public static void copiarA(NegocioEntity e, Negocio n) {
        e.setNombre(n.getNombre());
        e.setLocal(n.getLocal().valor());
        e.setLocalActivo(n.getEstado() == EstadoNegocio.ACTIVO ? n.getLocal().valor() : null);
        e.setDocumentoPropietario(n.getPropietario().documento().valor());
        e.setNombrePropietario(n.getPropietario().nombre());
        e.setTelefonoPropietario(n.getPropietario().telefono());
        e.setEstado(n.getEstado().name());
    }
}
