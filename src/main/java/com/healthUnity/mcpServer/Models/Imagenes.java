package com.healthUnity.mcpServer.Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "imagenes")
@ToString(exclude = {"galeria"})
@Data
public class Imagenes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_imagen")
    private Long idImagen;

    @Column(name = "url_imagen")
    private String urlImagen;

    @ManyToOne
    @JoinColumn(name = "id_galeria")
    @JsonIgnoreProperties("imagenes")
    private Galeria galeria;
}
