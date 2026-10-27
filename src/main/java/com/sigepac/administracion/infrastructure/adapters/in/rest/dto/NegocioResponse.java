package com.sigepac.administracion.infrastructure.adapters.in.rest.dto;

import com.sigepac.administracion.domain.model.Negocio;

public record NegocioResponse(Long id, String nombre, String local, String estado, String documentoPropietario,
                              String nombrePropietario, String telefonoPropietario) {

    public static NegocioResponse de(Negocio n) {
        return new NegocioResponse(n.getId(), n.getNombre(), n.getLocal().valor(), n.getEstado().name(),
                n.getPropietario().documento().valor(), n.getPropietario().nombre(), n.getPropietario().telefono());
    }
}
