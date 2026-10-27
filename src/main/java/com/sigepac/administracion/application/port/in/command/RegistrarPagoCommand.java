package com.sigepac.administracion.application.port.in.command;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistrarPagoCommand(long negocioId, LocalDate fecha, BigDecimal monto) { }
