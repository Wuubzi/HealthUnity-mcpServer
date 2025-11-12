package com.healthUnity.mcpServer.DTO.Response;

import lombok.Data;

import java.util.Date;

@Data
public class PacienteResponseDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String gmail;
    private String telefono;
    private String direccion;
    private Date fechaNacimiento;
    private String genero;
    private String url_imagen;
}
