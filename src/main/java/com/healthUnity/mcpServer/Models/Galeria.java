package com.healthUnity.mcpServer.Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "galerias")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "galeria"})
@ToString(exclude = {"imagenes"})
public class Galeria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_galeria")
    private Long idGaleria;

    @OneToMany(mappedBy = "galeria", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Imagenes> imagenes;
}
