package com.sigepac.administracion.application.port.in.result;

import com.sigepac.administracion.domain.model.Negocio;
import com.sigepac.administracion.domain.vo.Dinero;

public record ResumenNegocio(Negocio negocio, Dinero saldoPendiente) { }
