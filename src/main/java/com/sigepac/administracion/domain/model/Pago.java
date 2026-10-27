package com.sigepac.administracion.domain.model;

import com.sigepac.administracion.domain.exception.PagoYaAnuladoException;
import com.sigepac.administracion.domain.exception.ValorInvalidoException;
import com.sigepac.administracion.domain.vo.Dinero;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Un pago hecho por un negocio. No se edita: si hubo un error, se anula y se registra otro. */
public class Pago {

    private final Long id;
    private final long negocioId;
    private final LocalDate fecha;
    private final Dinero monto;
    private EstadoPago estado;
    private final List<AplicacionPago> aplicaciones;

    private Pago(Long id, long negocioId, LocalDate fecha, Dinero monto, EstadoPago estado, List<AplicacionPago> aplicaciones) {
        if (fecha == null) {
            throw new ValorInvalidoException("La fecha del pago es obligatoria");
        }
        if (monto == null || !monto.esMayorQueCero()) {
            throw new ValorInvalidoException("El monto del pago debe ser mayor que cero");
        }
        if (estado == null) {
            throw new ValorInvalidoException("El estado del pago es obligatorio");
        }
        this.id = id;
        this.negocioId = negocioId;
        this.fecha = fecha;
        this.monto = monto;
        this.estado = estado;
        this.aplicaciones = new ArrayList<>(aplicaciones);
    }

    /** `hoy` llega desde fuera (un Clock inyectado): el dominio no consulta el reloj del sistema. */
    public static Pago nuevo(long negocioId, LocalDate fecha, Dinero monto, LocalDate hoy) {
        if (fecha != null && fecha.isAfter(hoy)) {
            throw new ValorInvalidoException("La fecha del pago no puede ser futura");
        }
        return new Pago(null, negocioId, fecha, monto, EstadoPago.VIGENTE, List.of());
    }

    public static Pago reconstruir(long id, long negocioId, LocalDate fecha, Dinero monto, EstadoPago estado,
                                   List<AplicacionPago> aplicaciones) {
        return new Pago(id, negocioId, fecha, monto, estado, aplicaciones);
    }

    /** Se asigna una sola vez y debe sumar exactamente el monto del pago. */
    public void asignarAplicaciones(List<AplicacionPago> nuevas) {
        if (!aplicaciones.isEmpty()) {
            throw new ValorInvalidoException("El pago ya tiene sus aplicaciones asignadas");
        }
        Dinero suma = nuevas.stream().map(AplicacionPago::monto).reduce(Dinero.CERO, Dinero::sumar);
        if (suma.compareTo(monto) != 0) {
            throw new ValorInvalidoException("Las aplicaciones no suman el monto del pago");
        }
        aplicaciones.addAll(nuevas);
    }

    public void anular() {
        if (estado == EstadoPago.ANULADO) {
            throw new PagoYaAnuladoException(id);
        }
        this.estado = EstadoPago.ANULADO;
    }

    public boolean estaVigente() { return estado == EstadoPago.VIGENTE; }
    public Long getId() { return id; }
    public long getNegocioId() { return negocioId; }
    public LocalDate getFecha() { return fecha; }
    public Dinero getMonto() { return monto; }
    public EstadoPago getEstado() { return estado; }
    public List<AplicacionPago> getAplicaciones() { return Collections.unmodifiableList(aplicaciones); }

    @Override
    public boolean equals(Object o) {
        return o instanceof Pago otro && id != null && Objects.equals(id, otro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
