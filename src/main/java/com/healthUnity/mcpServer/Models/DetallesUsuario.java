package com.healthUnity.mcpServer.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "Detalles_usuario")
@Data
public class DetallesUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_usuario")
    private long idDetalleUsuario;

    @Column(name = "nombre")
    private String nombre;
    @Column
    private String apellido;
    @Column
    private String gmail;
    @Column(name = "fecha_nacimiento")
    private Date fechaNacimiento;
    @Column
    private String telefono;

    @Column
    private String genero;
    @Column(name = "url_imagen")
    private String urlImagen;
    @Column
    private String direccion;
}
