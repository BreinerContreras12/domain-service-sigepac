package com.sigepac.administracion.application.port.in.result;

import com.sigepac.administracion.domain.model.Pago;
import com.sigepac.administracion.domain.vo.Dinero;

/** Lo que recibe la administradora al registrar un pago. */
public record ComprobantePago(Pago pago, Dinero saldoAnterior, Dinero saldoNuevo) { }
