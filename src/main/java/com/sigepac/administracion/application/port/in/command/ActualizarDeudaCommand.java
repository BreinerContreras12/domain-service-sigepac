package com.sigepac.administracion.application.port.in.command;

import java.math.BigDecimal;

public record ActualizarDeudaCommand(long deudaId, BigDecimal valor) { }
