package com.healthUnity.mcpServer.DTO.Request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class RegisterRequestDTO {
    @Email
    String gmail;
}
