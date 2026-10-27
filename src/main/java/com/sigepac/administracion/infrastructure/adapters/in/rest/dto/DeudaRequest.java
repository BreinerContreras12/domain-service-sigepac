package com.sigepac.administracion.infrastructure.adapters.in.rest.dto;

import java.math.BigDecimal;

public record DeudaRequest(String concepto, int anio, int mes, BigDecimal valor) { }
