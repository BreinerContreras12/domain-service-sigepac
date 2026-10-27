package com.sigepac.administracion.application.port.in.command;

public record RegistrarNegocioCommand(String nombre, String local, String documentoPropietario,
                                      String nombrePropietario, String telefonoPropietario) { }
