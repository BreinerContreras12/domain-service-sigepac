package com.sigepac.administracion.application.port.out;

import com.sigepac.administracion.domain.vo.CodigoLocal;
import com.sigepac.administracion.domain.vo.Documento;

/** Criterios que sabe resolver el repositorio. El saldo se filtra después, en la aplicación, porque sale de las deudas. */
public record FiltroNegocio(String nombreContiene, CodigoLocal local, Documento documentoPropietario, boolean incluirInactivos) { }
