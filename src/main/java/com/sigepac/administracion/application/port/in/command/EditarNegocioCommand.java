package com.sigepac.administracion.application.port.in.command;

public record EditarNegocioCommand(long id, String nombre, String local, String documentoPropietario,
                                   String nombrePropietario, String telefonoPropietario) { }
