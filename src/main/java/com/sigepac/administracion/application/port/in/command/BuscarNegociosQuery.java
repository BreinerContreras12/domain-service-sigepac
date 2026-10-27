package com.sigepac.administracion.application.port.in.command;

/** Todos los criterios son opcionales (null = no filtrar). Una cadena vacía NO es lo mismo que null: es un error. */
public record BuscarNegociosQuery(String nombre, String local, String documentoPropietario, Boolean conSaldo,
                                  boolean incluirInactivos, int pagina, int tamano) { }
