package com.healthUnity.mcpServer.DTO.Request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Date;

@Data
public class CompleteProfileRequestDTO {

    @NotBlank
    @Size(max = 20)
    private String nombre;

    @NotBlank
    @Size(max = 20)
    private String apellido;

    @NotBlank
    @Email
    @Size(max = 30)
    private String gmail;

    @NotNull
    @Past
    private Date fechaNacimiento;

    @NotBlank
    @Size(max = 10)
    private String telefono;

    @NotBlank
    @Size(max = 10)
    private String genero;

    @NotBlank
    @Size(max = 20)
    private String direccion;

    private String url_imagen;
}
