package com.sigepac.administracion.domain.exception;

import com.sigepac.administracion.domain.vo.Dinero;

public class NegocioConSaldoException extends DominioException {

    private final Dinero saldo;

    public NegocioConSaldoException(Dinero saldo) {
        super("El negocio tiene un saldo pendiente de " + saldo.valor().toPlainString() + " y no puede darse de baja");
        this.saldo = saldo;
    }

    public Dinero getSaldo() {
        return saldo;
    }
}
