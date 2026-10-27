package com.sigepac.administracion.infrastructure.adapters.in.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagoRequest(LocalDate fecha, BigDecimal monto) { }
