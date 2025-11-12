package com.healthUnity.mcpServer.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopDoctorsResponseDTO {
    private Long id;
    private String nombre_doctor;
    private String apellido_doctor;
    private String doctor_image;
    private String especialidad;
    private Double rating;
    private int number_reviews;

}
