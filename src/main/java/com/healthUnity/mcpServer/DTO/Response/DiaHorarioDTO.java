package com.healthUnity.mcpServer.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
@Data
@AllArgsConstructor
public class DiaHorarioDTO {
    private int diaSemana;
    private List<RangoHorarioDTO> horarios;
}
