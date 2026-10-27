package com.sigepac.administracion.infrastructure.adapters.in.rest.dto;

public record NegocioRequest(String nombre, String local, String documentoPropietario,
                             String nombrePropietario, String telefonoPropietario) { }
