package com.healthUnity.mcpServer.Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pacientes")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paciente")
    private Long  idPaciente;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_detalle_usuario")
    private DetallesUsuario detallesUsuario;
}
