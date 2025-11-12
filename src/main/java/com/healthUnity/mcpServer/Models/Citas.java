package com.healthUnity.mcpServer.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "citas")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Citas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Long idCita;
    @Column
    private String razon;
    @Column
    private LocalDate fecha;
    @Column
    private LocalTime hora;
    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'pendiente'")
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_doctor", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Doctores doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonIgnore
    private Paciente paciente;

    @PrePersist
    public void prePersist() {
        if (estado == null) {
            estado = "pendiente";
        }
    }

}
