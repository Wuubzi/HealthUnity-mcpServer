package com.healthUnity.mcpServer.DTO.Response;

import lombok.Data;

@Data
public class FavoritoDoctorResponseDTO {
    private Long idFavorito;
    private Long idDoctor;
    private String nombre;
    private String apellido;
    private String doctor_image;
    private String especialidad;
    private Double rating;
    private int number_reviews;
}
