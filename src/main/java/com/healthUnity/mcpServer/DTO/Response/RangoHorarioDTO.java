package com.healthUnity.mcpServer.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;

@Data
@AllArgsConstructor
public class RangoHorarioDTO {
    private LocalTime horaInicio;
    private LocalTime horaFin;
}
