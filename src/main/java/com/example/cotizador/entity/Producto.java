package com.example.cotizador.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
public class Producto {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, updatable = false)
    private String codigo;

    private String descripcion;

    @Column(precision = 12, scale = 2)
    private BigDecimal precio;

    @PrePersist
    @PreUpdate
    public void prepararDatos() {
        if (codigo == null) {
            codigo = "PRD-" + UUID.randomUUID().toString().substring(0, 8);
        }
        if (precio != null) {
            precio = precio.setScale(2, RoundingMode.HALF_UP);
        }
    }

    /* ===== Getters & Setters ===== */

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }
}