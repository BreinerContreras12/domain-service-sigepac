package com.sigepac.administracion.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(name = "negocio", uniqueConstraints = @UniqueConstraint(name = "uk_negocio_local_activo", columnNames = "local_activo"))
public class NegocioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(name = "local_codigo", nullable = false, length = 15)
    private String local;

    /**
     * Solo tiene valor mientras el negocio está ACTIVO. Como la restricción UNIQUE ignora los NULL,
     * la base de datos garantiza "un solo negocio activo por local" sin impedir que los inactivos repitan local.
     */
    @Column(name = "local_activo", length = 15)
    private String localActivo;

    @Column(name = "documento_propietario", nullable = false, length = 20)
    private String documentoPropietario;

    @Column(name = "nombre_propietario", nullable = false, length = 120)
    private String nombrePropietario;

    @Column(name = "telefono_propietario", length = 20)
    private String telefonoPropietario;

    @Column(nullable = false, length = 10)
    private String estado;

    @Version
    private Long version;

    protected NegocioEntity() { }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }
    public String getLocalActivo() { return localActivo; }
    public void setLocalActivo(String localActivo) { this.localActivo = localActivo; }
    public String getDocumentoPropietario() { return documentoPropietario; }
    public void setDocumentoPropietario(String d) { this.documentoPropietario = d; }
    public String getNombrePropietario() { return nombrePropietario; }
    public void setNombrePropietario(String n) { this.nombrePropietario = n; }
    public String getTelefonoPropietario() { return telefonoPropietario; }
    public void setTelefonoPropietario(String t) { this.telefonoPropietario = t; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public static NegocioEntity nueva() { return new NegocioEntity(); }
}
