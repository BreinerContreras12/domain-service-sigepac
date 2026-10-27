package com.sigepac.administracion.domain.model;

import com.sigepac.administracion.domain.exception.ValorDeudaMenorQuePagadoException;
import com.sigepac.administracion.domain.exception.ValorInvalidoException;
import com.sigepac.administracion.domain.vo.ConceptoDeuda;
import com.sigepac.administracion.domain.vo.Dinero;
import com.sigepac.administracion.domain.vo.Periodo;
import java.util.Objects;

/**
 * Lo que un negocio debe por un concepto en un mes. Protege su propia regla:
 * lo pagado nunca supera el valor, y el saldo siempre es valor - pagado.
 */
public class Deuda {

    private final Long id;
    private final long negocioId;
    private final ConceptoDeuda concepto;
    private final Periodo periodo;
    private Dinero valor;
    private Dinero pagado;

    private Deuda(Long id, long negocioId, ConceptoDeuda concepto, Periodo periodo, Dinero valor, Dinero pagado) {
        if (concepto == null || periodo == null) {
            throw new ValorInvalidoException("El concepto y el período son obligatorios");
        }
        if (valor == null || !valor.esMayorQueCero()) {
            throw new ValorInvalidoException("El valor de la deuda debe ser mayor que cero");
        }
        if (pagado == null || pagado.compareTo(valor) > 0) {
            throw new ValorInvalidoException("Lo pagado no puede superar el valor de la deuda");
        }
        this.id = id;
        this.negocioId = negocioId;
        this.concepto = concepto;
        this.periodo = periodo;
        this.valor = valor;
        this.pagado = pagado;
    }

    public static Deuda registrar(long negocioId, ConceptoDeuda concepto, Periodo periodo, Dinero valor) {
        return new Deuda(null, negocioId, concepto, periodo, valor, Dinero.CERO);
    }

    public static Deuda reconstruir(long id, long negocioId, ConceptoDeuda concepto, Periodo periodo, Dinero valor, Dinero pagado) {
        return new Deuda(id, negocioId, concepto, periodo, valor, pagado);
    }

    public Dinero saldo() {
        return Dinero.de(valor.valor().subtract(pagado.valor()));
    }

    public EstadoDeuda estado() {
        return saldo().esMayorQueCero() ? EstadoDeuda.PENDIENTE : EstadoDeuda.PAGADA;
    }

    /** El nuevo valor se recalcula contra lo ya pagado: nunca puede quedar por debajo. */
    public void actualizarValor(Dinero nuevoValor) {
        if (nuevoValor == null || !nuevoValor.esMayorQueCero()) {
            throw new ValorInvalidoException("El valor de la deuda debe ser mayor que cero");
        }
        if (nuevoValor.compareTo(pagado) < 0) {
            throw new ValorDeudaMenorQuePagadoException(pagado);
        }
        this.valor = nuevoValor;
    }

    public void aplicarPago(Dinero monto) {
        if (monto == null || !monto.esMayorQueCero()) {
            throw new ValorInvalidoException("El monto a aplicar debe ser mayor que cero");
        }
        if (monto.compareTo(saldo()) > 0) {
            throw new ValorInvalidoException("El monto a aplicar supera el saldo de la deuda");
        }
        this.pagado = pagado.sumar(monto);
    }

    public void revertirPago(Dinero monto) {
        if (monto == null || !monto.esMayorQueCero() || monto.compareTo(pagado) > 0) {
            throw new ValorInvalidoException("El monto a revertir es inválido para esta deuda");
        }
        this.pagado = Dinero.de(pagado.valor().subtract(monto.valor()));
    }

    public Long getId() { return id; }
    public long getNegocioId() { return negocioId; }
    public ConceptoDeuda getConcepto() { return concepto; }
    public Periodo getPeriodo() { return periodo; }
    public Dinero getValor() { return valor; }
    public Dinero getPagado() { return pagado; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Deuda otra && id != null && Objects.equals(id, otra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
