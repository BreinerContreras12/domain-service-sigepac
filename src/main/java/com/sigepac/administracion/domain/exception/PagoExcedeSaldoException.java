package com.sigepac.administracion.domain.exception;

import com.sigepac.administracion.domain.vo.Dinero;

public class PagoExcedeSaldoException extends DominioException {

    private final Dinero saldo;

    public PagoExcedeSaldoException(Dinero saldo, Dinero monto) {
        super("El pago de " + monto.valor().toPlainString() + " supera el saldo pendiente del negocio ("
                + saldo.valor().toPlainString() + ")");
        this.saldo = saldo;
    }

    public Dinero getSaldo() {
        return saldo;
    }
}
