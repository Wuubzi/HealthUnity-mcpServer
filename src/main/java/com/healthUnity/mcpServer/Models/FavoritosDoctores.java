package com.healthUnity.mcpServer.Models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "favoritos_doctores")
@Data
public class FavoritosDoctores {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_favorito_doctor")
    private Long idFavoritoDoctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_doctor", nullable = false)
    private Doctores doctor;


}
