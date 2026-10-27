package com.sigepac.administracion.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;

@Entity
@Table(name = "deuda", uniqueConstraints = @UniqueConstraint(name = "uk_deuda_negocio_concepto_periodo",
        columnNames = {"negocio_id", "concepto", "anio", "mes"}))
public class DeudaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "negocio_id", nullable = false)
    private Long negocioId;

    @Column(nullable = false, length = 20)
    private String concepto;

    @Column(nullable = false)
    private int anio;

    @Column(nullable = false)
    private int mes;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal pagado;

    /** Bloqueo optimista: dos pagos simultáneos sobre la misma deuda no pueden pisarse. */
    @Version
    private Long version;

    protected DeudaEntity() { }

    public static DeudaEntity nueva() { return new DeudaEntity(); }

    public Long getId() { return id; }
    public Long getNegocioId() { return negocioId; }
    public void setNegocioId(Long negocioId) { this.negocioId = negocioId; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }
    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public BigDecimal getPagado() { return pagado; }
    public void setPagado(BigDecimal pagado) { this.pagado = pagado; }
}
