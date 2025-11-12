package com.healthUnity.mcpServer.DTO.Response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CitaResponseDTO {
    private Long idCita;
    private LocalDate fecha;
    private LocalTime hora;
    private Long idDoctor;
    private String nombre_doctor;
    private String apellido_doctor;
    private String doctor_image;
    private String especialidad;
    private String direccion;
    private String estado;
}
