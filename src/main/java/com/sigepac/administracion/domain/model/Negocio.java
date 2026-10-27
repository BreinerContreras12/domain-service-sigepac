package com.sigepac.administracion.domain.model;

import com.sigepac.administracion.domain.exception.NegocioConSaldoException;
import com.sigepac.administracion.domain.exception.NegocioInactivoException;
import com.sigepac.administracion.domain.exception.ValorInvalidoException;
import com.sigepac.administracion.domain.vo.CodigoLocal;
import com.sigepac.administracion.domain.vo.Dinero;
import com.sigepac.administracion.domain.vo.Propietario;
import java.util.Objects;

/**
 * Entidad (Semana 6) con identidad propia: el id lo asigna el repositorio al guardar por primera vez.
 * Un negocio no se borra: se da de baja (INACTIVO) para conservar su historial de deudas y pagos.
 */
public class Negocio {

    private final Long id;
    private String nombre;
    private CodigoLocal local;
    private Propietario propietario;
    private EstadoNegocio estado;

    private Negocio(Long id, String nombre, CodigoLocal local, Propietario propietario, EstadoNegocio estado) {
        if (local == null) {
            throw new ValorInvalidoException("El local es obligatorio");
        }
        if (propietario == null) {
            throw new ValorInvalidoException("El propietario es obligatorio");
        }
        if (estado == null) {
            throw new ValorInvalidoException("El estado del negocio es obligatorio");
        }
        this.id = id;
        this.nombre = validarNombre(nombre);
        this.local = local;
        this.propietario = propietario;
        this.estado = estado;
    }

    /** Factory Method (Semana 7): un negocio nuevo nace ACTIVO y sin id. */
    public static Negocio registrar(String nombre, CodigoLocal local, Propietario propietario) {
        return new Negocio(null, nombre, local, propietario, EstadoNegocio.ACTIVO);
    }

    /** Rehidrata un negocio ya guardado (lo usan los adaptadores de persistencia). */
    public static Negocio reconstruir(long id, String nombre, CodigoLocal local, Propietario propietario, EstadoNegocio estado) {
        return new Negocio(id, nombre, local, propietario, estado);
    }

    public void asegurarActivo() {
        if (estado == EstadoNegocio.INACTIVO) {
            throw new NegocioInactivoException(id);
        }
    }

    public void editar(String nombre, CodigoLocal local, Propietario propietario) {
        asegurarActivo();
        if (local == null || propietario == null) {
            throw new ValorInvalidoException("El local y el propietario son obligatorios");
        }
        this.nombre = validarNombre(nombre);
        this.local = local;
        this.propietario = propietario;
    }

    /** Baja lógica: libera el local, pero solo si el negocio no debe nada. */
    public void darDeBaja(Dinero saldoPendiente) {
        asegurarActivo();
        if (saldoPendiente.esMayorQueCero()) {
            throw new NegocioConSaldoException(saldoPendiente);
        }
        this.estado = EstadoNegocio.INACTIVO;
    }

    private static String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ValorInvalidoException("El nombre del negocio es obligatorio");
        }
        String limpio = nombre.trim();
        if (limpio.length() > 120) {
            throw new ValorInvalidoException("El nombre del negocio no puede superar 120 caracteres");
        }
        return limpio;
    }

    public boolean estaActivo() { return estado == EstadoNegocio.ACTIVO; }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public CodigoLocal getLocal() { return local; }
    public Propietario getPropietario() { return propietario; }
    public EstadoNegocio getEstado() { return estado; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Negocio otro && id != null && Objects.equals(id, otro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
