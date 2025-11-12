package com.healthUnity.mcpServer.Service;


import com.healthUnity.mcpServer.Models.Especialidades;
import com.healthUnity.mcpServer.Repositories.EspecialidadesRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EspecialidadesService {

    private final EspecialidadesRepository especialidadesRepository;

    @Autowired
    public EspecialidadesService(EspecialidadesRepository especialidadesRepository) {
        this.especialidadesRepository = especialidadesRepository;
    }
    @Tool(description = "Obtiene la lista de especialidades médicas disponibles en el sistema")
    public String getEspecialidades() {
        List<Especialidades> especialidades = especialidadesRepository.findAll();

        // Return as formatted string
        return especialidades.stream()
                .map(e -> String.format("- %s (ID: %d)", e.getNombre(), e.getIdEspecialidad()))
                .collect(Collectors.joining("\n"));
    }


}
