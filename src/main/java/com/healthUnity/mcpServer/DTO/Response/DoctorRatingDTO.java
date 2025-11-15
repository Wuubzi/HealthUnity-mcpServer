package com.healthUnity.mcpServer.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorRatingDTO {
    private Long idDoctor;
    private String nombre;
    private String apellido;
    private String urlImagen;
    private String especialidad;
    private Double rating;
    private Integer reviews;
}
