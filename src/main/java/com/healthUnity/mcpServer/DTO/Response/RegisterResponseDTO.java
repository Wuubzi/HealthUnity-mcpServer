package com.healthUnity.mcpServer.DTO.Response;

import lombok.Data;

@Data
public class RegisterResponseDTO {
    private String timestamp;
    private int Status;
    private String url;
    private boolean profileCompleted;
}
