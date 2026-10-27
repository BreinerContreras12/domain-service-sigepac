package com.sigepac.administracion.application.port.in.command;

import java.math.BigDecimal;

public record RegistrarDeudaCommand(long negocioId, String concepto, int anio, int mes, BigDecimal valor) { }
