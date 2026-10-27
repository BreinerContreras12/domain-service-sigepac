package com.sigepac.administracion.domain.exception;

import com.sigepac.administracion.domain.vo.Dinero;

public class ValorDeudaMenorQuePagadoException extends DominioException {

    public ValorDeudaMenorQuePagadoException(Dinero pagado) {
        super("El nuevo valor de la deuda no puede ser menor que lo ya pagado (" + pagado.valor().toPlainString() + ")");
    }
}
