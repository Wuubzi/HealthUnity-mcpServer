package com.healthUnity.mcpServer.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class DoctorDTOResponse {
    private Long idDoctor;
    private String nombre;
    private String apellido;
    private String doctor_image;
    private String especialidad;
    private Double rating;
    private int number_reviews;

    public DoctorDTOResponse(Object[] row) {
        this.idDoctor = ((Number) row[0]).longValue();
        this.nombre = (String) row[1];
        this.apellido = (String) row[2];
        this.doctor_image = (String) row[3];
        this.especialidad = (String) row[4];
        this.rating = row[5] != null ? ((Number) row[5]).doubleValue() : 0.0;
        this.number_reviews = row[6] != null ? ((Number) row[6]).intValue() : 0;
    }
}
