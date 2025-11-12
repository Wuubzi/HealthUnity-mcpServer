package com.healthUnity.mcpServer.Service;


import com.healthUnity.mcpServer.DTO.Response.CitaResponseDTO;
import com.healthUnity.mcpServer.DTO.Response.ResponseDTO;
import com.healthUnity.mcpServer.DTO.Response.DoctorDisponibleDTO;
import com.healthUnity.mcpServer.Models.Citas;
import com.healthUnity.mcpServer.Models.Doctores;
import com.healthUnity.mcpServer.Models.Paciente;
import com.healthUnity.mcpServer.Models.HorariosDoctor;
import com.healthUnity.mcpServer.Repositories.CitasRepository;
import com.healthUnity.mcpServer.Repositories.DoctorRepository;
import com.healthUnity.mcpServer.Repositories.PacienteRepository;
import com.healthUnity.mcpServer.Repositories.HorariosDoctorRepository;
import com.healthUnity.mcpServer.Utils.DateFormatter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CitasService {
    private final CitasRepository citasRepository;
    private final DoctorRepository doctorRepository;
    private final PacienteRepository pacienteRepository;
    private final HorariosDoctorRepository horariosDoctorRepository;
    private final DateFormatter dateFormatter;

    @Autowired
    public CitasService(CitasRepository citasRepository,
                        PacienteRepository pacienteRepository,
                        DoctorRepository doctorRepository,
                        HorariosDoctorRepository horariosDoctorRepository,
                        DateFormatter dateFormatter) {
        this.citasRepository = citasRepository;
        this.pacienteRepository = pacienteRepository;
        this.doctorRepository = doctorRepository;
        this.horariosDoctorRepository = horariosDoctorRepository;
        this.dateFormatter = dateFormatter;
    }

    @Tool(description = """
        Busca doctores disponibles según especialidad, fecha y hora solicitada.
        Parámetros:
        - especialidadNombre: nombre de la especialidad (ej: 'Cardiología', 'Pediatría')
        - fecha: fecha en formato YYYY-MM-DD (ej: '2025-11-14')
        - hora: hora en formato HH:mm (ej: '08:00', '14:30')
        - idPaciente: ID del paciente que solicita la cita
        
        Retorna una lista de doctores que tienen horario disponible en ese día y hora,
        con información de su especialidad, rating y disponibilidad.
        Usa esta función cuando el usuario solicite una cita con día y hora específicos.
        """)
    public List<DoctorDisponibleDTO> buscarDoctoresDisponibles(
            String especialidadNombre,
            String fecha,
            String hora,
            Long idPaciente) {

        LocalDate fechaCita = LocalDate.parse(fecha);
        LocalTime horaCita = LocalTime.parse(hora);
        int diaSemana = fechaCita.getDayOfWeek().getValue(); // 1=Lunes, 7=Domingo

        // Buscar doctores por especialidad
        List<Doctores> doctores;
        if (especialidadNombre != null && !especialidadNombre.isEmpty()) {
            doctores = doctorRepository.findByEspecialidad_NombreContainingIgnoreCase(especialidadNombre);
        } else {
            doctores = doctorRepository.findAll();
        }

        return doctores.stream()
                .filter(doctor -> {
                    // Verificar horarios del doctor
                    List<HorariosDoctor> horarios = horariosDoctorRepository
                            .findByDoctor_IdDoctorAndDiaSemana(doctor.getIdDoctor(), diaSemana);

                    // Verificar si la hora está dentro de algún rango de horario
                    boolean tieneHorario = horarios.stream().anyMatch(h ->
                            !horaCita.isBefore(h.getHoraInicio()) &&
                                    !horaCita.isAfter(h.getHoraFin())
                    );

                    if (!tieneHorario) return false;

                    // Verificar que no tenga cita a esa hora
                    List<Citas> citasExistentes = citasRepository
                            .findByDoctor_IdDoctorAndFechaAndHora(
                                    doctor.getIdDoctor(),
                                    fechaCita,
                                    horaCita
                            );

                    return citasExistentes.isEmpty();
                })
                .map(doctor -> {
                    DoctorDisponibleDTO dto = new DoctorDisponibleDTO();
                    dto.setIdDoctor(doctor.getIdDoctor());

                    if (doctor.getDetallesUsuario() != null) {
                        dto.setNombre(doctor.getDetallesUsuario().getNombre());
                        dto.setApellido(doctor.getDetallesUsuario().getApellido());
                        dto.setUrlImagen(doctor.getDetallesUsuario().getUrlImagen());
                    }

                    if (doctor.getEspecialidad() != null) {
                        dto.setEspecialidad(doctor.getEspecialidad().getNombre());
                    }

                    // Aquí podrías agregar el rating si lo tienes
                    dto.setRating(4.5); // Por ahora un valor por defecto
                    dto.setDisponible(true);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Tool(description = """
        Crea una nueva cita médica automáticamente.
        Parámetros:
        - idPaciente: ID del paciente que solicita la cita
        - idDoctor: ID del doctor seleccionado
        - fecha: fecha de la cita en formato YYYY-MM-DD
        - hora: hora de la cita en formato HH:mm
        - razon: motivo de la consulta (opcional)
        
        Retorna un mensaje de confirmación con los detalles de la cita creada.
        Usa esta función después de que el usuario confirme el doctor y horario,
        o cuando tengas todos los datos necesarios para crear la cita.
        """)
    public String crearCitaAutomatica(
            Long idPaciente,
            Long idDoctor,
            String fecha,
            String hora,
            String razon) {

        Optional<Paciente> pacienteOpt = pacienteRepository.findById(idPaciente);
        if (pacienteOpt.isEmpty()) {
            return "Error: No se encontró el paciente con ID " + idPaciente;
        }

        Optional<Doctores> doctorOpt = doctorRepository.findById(idDoctor);
        if (doctorOpt.isEmpty()) {
            return "Error: No se encontró el doctor con ID " + idDoctor;
        }

        Doctores doctor = doctorOpt.get();
        Paciente paciente = pacienteOpt.get();

        // Crear la cita
        Citas cita = new Citas();
        cita.setPaciente(paciente);
        cita.setDoctor(doctor);
        cita.setFecha(LocalDate.parse(fecha));
        cita.setHora(LocalTime.parse(hora));
        cita.setRazon(razon != null ? razon : "Consulta general");
        cita.setEstado("pendiente");

        citasRepository.save(cita);

        String nombreDoctor = doctor.getDetallesUsuario() != null
                ? doctor.getDetallesUsuario().getNombre() + " " + doctor.getDetallesUsuario().getApellido()
                : "Doctor";

        String especialidad = doctor.getEspecialidad() != null
                ? doctor.getEspecialidad().getNombre()
                : "Medicina General";

        return String.format(
                "✅ Cita creada exitosamente!\n\n" +
                        "📋 Detalles de tu cita:\n" +
                        "• Doctor: %s\n" +
                        "• Especialidad: %s\n" +
                        "• Fecha: %s\n" +
                        "• Hora: %s\n" +
                        "• Motivo: %s\n" +
                        "• Estado: Confirmada\n\n" +
                        "Te enviaremos un recordatorio 24 horas antes de tu cita.",
                nombreDoctor,
                especialidad,
                fecha,
                hora,
                cita.getRazon()
        );
    }

    @Tool(description = """
        Obtiene los horarios disponibles de un doctor específico para una fecha.
        Parámetros:
        - idDoctor: ID del doctor
        - fecha: fecha a consultar en formato YYYY-MM-DD
        
        Retorna lista de horarios disponibles para ese día, excluyendo las horas
        que ya tienen citas agendadas.
        Útil para mostrar opciones de horario al usuario cuando selecciona un doctor.
        """)
    public List<String> obtenerHorariosDisponibles(Long idDoctor, String fecha) {
        LocalDate fechaCita = LocalDate.parse(fecha);
        int diaSemana = fechaCita.getDayOfWeek().getValue();

        // Obtener horarios del doctor para ese día
        List<HorariosDoctor> horarios = horariosDoctorRepository
                .findByDoctor_IdDoctorAndDiaSemana(idDoctor, diaSemana);

        if (horarios.isEmpty()) {
            return List.of();
        }

        // Obtener citas ya agendadas
        List<Citas> citasAgendadas = citasRepository
                .findByDoctor_IdDoctorAndFecha(idDoctor, fechaCita);

        List<LocalTime> horasOcupadas = citasAgendadas.stream()
                .map(Citas::getHora)
                .collect(Collectors.toList());

        // Generar horarios disponibles (cada 30 minutos)
        return horarios.stream()
                .flatMap(horario -> {
                    List<String> slots = new java.util.ArrayList<>();
                    LocalTime tiempo = horario.getHoraInicio();

                    while (!tiempo.isAfter(horario.getHoraFin().minusMinutes(30))) {
                        if (!horasOcupadas.contains(tiempo)) {
                            slots.add(tiempo.toString());
                        }
                        tiempo = tiempo.plusMinutes(30);
                    }

                    return slots.stream();
                })
                .collect(Collectors.toList());
    }

    // Métodos existentes...
    public Citas getCitaProxima(Long idPaciente) {
        Optional<Paciente> pacienteOptional = pacienteRepository.findById(idPaciente);
        if (pacienteOptional.isEmpty()) {
            throw new EntityNotFoundException("Paciente no encontrado");
        }

        Citas cita = citasRepository.findFirstByPaciente_IdPacienteAndFechaGreaterThanEqualOrderByFechaAscHoraAsc(
                idPaciente,
                LocalDate.now()
        );
        return cita;
    }

    public List<CitaResponseDTO> getCitas(Long idPaciente, String estado) {
        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new EntityNotFoundException("Paciente no encontrado"));
        List<Citas> citas = citasRepository.findByPacienteAndEstado(idPaciente, estado);
        return citas.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private CitaResponseDTO convertToDTO(Citas cita) {
        CitaResponseDTO dto = new CitaResponseDTO();
        dto.setIdCita(cita.getIdCita());
        dto.setFecha(cita.getFecha());
        dto.setHora(cita.getHora());
        dto.setEstado(cita.getEstado());

        if (cita.getDoctor() != null) {
            dto.setIdDoctor(cita.getDoctor().getIdDoctor());
            if (cita.getDoctor().getDetallesUsuario() != null) {
                dto.setNombre_doctor(cita.getDoctor().getDetallesUsuario().getNombre());
                dto.setDoctor_image(cita.getDoctor().getDetallesUsuario().getUrlImagen());
                dto.setApellido_doctor(cita.getDoctor().getDetallesUsuario().getApellido());
                dto.setDireccion(cita.getDoctor().getDetallesUsuario().getDireccion());
            }

            if (cita.getDoctor().getEspecialidad() != null) {
                dto.setEspecialidad(cita.getDoctor().getEspecialidad().getNombre());
            }
        }

        return dto;
    }

    private ResponseDTO getResponseDTO(int status, String message, HttpServletRequest url) {
        ResponseDTO response = new ResponseDTO();
        response.setStatus(status);
        response.setMessage(message);
        response.setUrl(url.getRequestURL().toString());
        response.setTimestamp(dateFormatter.formatearFecha());
        return response;
    }
}