package com.healthUnity.mcpServer;

import com.healthUnity.mcpServer.Service.CitasService;
import com.healthUnity.mcpServer.Service.EspecialidadesService;
import com.healthUnity.mcpServer.Service.PacienteService;
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
    public ToolCallbackProvider ejemploTools(CitasService citasService, EspecialidadesService especialidadesService, PacienteService pacienteService){
        return MethodToolCallbackProvider.builder().toolObjects(especialidadesService,citasService,pacienteService).build();
    }
}
