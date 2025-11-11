package com.healthUnity.mcpServer.Service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class EjemploTool {



    @Tool(description = "Obtiene el nombre del usuario")
    public static String getNombre(){
        return "Carlos";
    }
}
