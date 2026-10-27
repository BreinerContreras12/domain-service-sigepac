package com.sigepac.administracion.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class AplicacionPagoEmbeddable {

    @Column(name = "deuda_id", nullable = false)
    private Long deudaId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal monto;

    protected AplicacionPagoEmbeddable() { }

    public AplicacionPagoEmbeddable(Long deudaId, BigDecimal monto) {
        this.deudaId = deudaId;
        this.monto = monto;
    }

    public Long getDeudaId() { return deudaId; }
    public BigDecimal getMonto() { return monto; }
}
