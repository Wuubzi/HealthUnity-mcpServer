package com.healthUnity.mcpServer.DTO;

import lombok.Data;

@Data
public class ChatMessage {
    private String from;
    private String content;
}
