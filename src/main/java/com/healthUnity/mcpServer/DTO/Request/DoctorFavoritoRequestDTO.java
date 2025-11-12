package com.healthUnity.mcpServer.DTO.Request;

import lombok.Data;

@Data
public class DoctorFavoritoRequestDTO {
    private Long idDoctor;
    private Long idPaciente;
}
