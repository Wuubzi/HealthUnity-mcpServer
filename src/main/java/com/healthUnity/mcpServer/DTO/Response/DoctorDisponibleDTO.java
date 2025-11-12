package com.healthUnity.mcpServer.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDisponibleDTO {
    private Long idDoctor;
    private String nombre;
    private String apellido;
    private String especialidad;
    private String urlImagen;
    private Double rating;
    private Boolean disponible;
    private String horarioDisponible;
}
