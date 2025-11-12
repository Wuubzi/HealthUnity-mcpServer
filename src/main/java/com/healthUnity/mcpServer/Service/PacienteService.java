package com.healthUnity.mcpServer.Service;


import com.healthUnity.mcpServer.DTO.Request.CompleteProfileRequestDTO;
import com.healthUnity.mcpServer.DTO.Request.RegisterRequestDTO;
import com.healthUnity.mcpServer.DTO.Response.CompleteProfileResponseDTO;
import com.healthUnity.mcpServer.DTO.Response.PacienteResponseDTO;
import com.healthUnity.mcpServer.DTO.Response.RegisterResponseDTO;
import com.healthUnity.mcpServer.DTO.Response.ResponseDTO;
import com.healthUnity.mcpServer.Models.DetallesUsuario;
import com.healthUnity.mcpServer.Models.Paciente;
import com.healthUnity.mcpServer.Repositories.PacienteRepository;
import com.healthUnity.mcpServer.Utils.DateFormatter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final DateFormatter dateFormatter;

    @Autowired
    public PacienteService(PacienteRepository pacienteRepository,
                           DateFormatter dateFormatter) {
        this.pacienteRepository = pacienteRepository;
        this.dateFormatter = dateFormatter;
    }



    private static Paciente getPaciente(CompleteProfileRequestDTO data) {
        Paciente paciente = new Paciente();
        DetallesUsuario detallesUsuario = new DetallesUsuario();
        detallesUsuario.setNombre(data.getNombre());
        detallesUsuario.setApellido(data.getApellido());
        detallesUsuario.setGmail(data.getGmail());
        detallesUsuario.setDireccion(data.getDireccion());
        detallesUsuario.setTelefono(data.getTelefono());
        detallesUsuario.setFechaNacimiento(data.getFechaNacimiento());
        detallesUsuario.setGenero(data.getGenero());
        detallesUsuario.setUrlImagen(data.getUrl_imagen());
        paciente.setDetallesUsuario(detallesUsuario);
        return paciente;
    }

    @Tool(description = "Obtiene la información completa del paciente a partir de su correo (gmail). Este método se utiliza en procesos internos del sistema, no por solicitud directa del usuario. Sirve para que la IA o el MCP recuperen datos de perfil (nombre, apellido, género, fecha de nacimiento, etc.)  cuando se requiere validar información, preparar un contexto de cita o completar flujos administrativos.")
    public PacienteResponseDTO getPaciente(String gmail){
        Optional<Paciente> pacienteOptional = pacienteRepository.findPacienteByDetallesUsuario_Gmail(gmail);

         if (pacienteOptional.isEmpty()) {
             throw new EntityNotFoundException("Paciente no encontrado");
         }
            Paciente paciente = pacienteOptional.get();
            PacienteResponseDTO response = new PacienteResponseDTO();
            response.setId(paciente.getIdPaciente());
            response.setNombre(paciente.getDetallesUsuario().getNombre());
            response.setApellido(paciente.getDetallesUsuario().getApellido());
            response.setGmail(paciente.getDetallesUsuario().getGmail());
            response.setDireccion(paciente.getDetallesUsuario().getDireccion());
            response.setTelefono(paciente.getDetallesUsuario().getTelefono());
            response.setFechaNacimiento(paciente.getDetallesUsuario().getFechaNacimiento());
            response.setGenero(paciente.getDetallesUsuario().getGenero());
            response.setUrl_imagen(paciente.getDetallesUsuario().getUrlImagen());
            return response;
    }

    @Tool(description = "Actualiza el perfil de un paciente existente usando los datos recibidos en un CompleteProfileRequestDTO. Se usa únicamente en procesos automáticos donde el sistema necesita mantener sincronizada la información del paciente (por ejemplo, después de un registro, actualización de datos desde otra fuente o ajuste interno de perfil). No debe invocarse por solicitud directa del usuario.")
    public ResponseDTO updateProfile(CompleteProfileRequestDTO data, HttpServletRequest request) {
        Paciente paciente = pacienteRepository.findPacienteByDetallesUsuario_Gmail(data.getGmail())
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado"));

        // Actualizas los detalles del paciente existente
        DetallesUsuario detalles = paciente.getDetallesUsuario();
        detalles.setNombre(data.getNombre());
        detalles.setApellido(data.getApellido());
        detalles.setGmail(data.getGmail());
        detalles.setDireccion(data.getDireccion());
        detalles.setTelefono(data.getTelefono());
        detalles.setFechaNacimiento(data.getFechaNacimiento());
        detalles.setGenero(data.getGenero());
        detalles.setUrlImagen(data.getUrl_imagen());

        paciente.setDetallesUsuario(detalles);

        pacienteRepository.save(paciente);

        ResponseDTO response = new ResponseDTO();
        response.setStatus(200);
        response.setMessage("Usuario actualizado exitosamente");
        response.setUrl(request.getRequestURL().toString());
        response.setTimestamp(dateFormatter.formatearFecha());
        return response;
    }



}
