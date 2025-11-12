package com.healthUnity.mcpServer.DTO.Response;

import lombok.Data;

@Data
public class ResponseDTO {
    private String timestamp;
    private String message;
    private int Status;
    private String url;
}
