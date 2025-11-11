package com.healthUnity.mcpServer;

import com.healthUnity.mcpServer.Service.EjemploTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpServer {

	public static void main(String[] args) {
		SpringApplication.run(McpServer.class, args);
	}


    @Bean
    public ToolCallbackProvider ejemploTools(EjemploTool ejemploTool){
        return MethodToolCallbackProvider.builder().toolObjects(ejemploTool).build();
    }
}
