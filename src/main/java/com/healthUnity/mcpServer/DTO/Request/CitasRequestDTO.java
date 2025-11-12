package com.healthUnity.mcpServer.DTO.Request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CitasRequestDTO {
    @NotNull
    private Long idDoctor;
    @NotNull
    private Long idPaciente;
    @NotNull
    private LocalDate fecha;
    @NotNull
    private LocalTime hora;
    @NotEmpty
    private String razon;
}
