package com.healthUnity.mcpServer.DTO.Response;

import lombok.Data;

@Data
public class CompleteProfileResponseDTO {
    private String timestamp;
    private String message;
    private int Status;
    private String url;
}
