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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
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
        int diaSemana = fechaCita.getDayOfWeek().getValue();

        List<Doctores> doctores;
        if (especialidadNombre != null && !especialidadNombre.isEmpty()) {
            doctores = doctorRepository.findByEspecialidad_NombreContainingIgnoreCase(especialidadNombre);
        } else {
            doctores = doctorRepository.findAll();
        }

        return doctores.stream()
                .filter(doctor -> {
                    List<HorariosDoctor> horarios = horariosDoctorRepository
                            .findByDoctor_IdDoctorAndDiaSemana(doctor.getIdDoctor(), diaSemana);

                    boolean tieneHorario = horarios.stream().anyMatch(h ->
                            !horaCita.isBefore(h.getHoraInicio()) &&
                                    !horaCita.isAfter(h.getHoraFin())
                    );

                    if (!tieneHorario) return false;

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

                    dto.setRating(4.5);
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
    @Transactional
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

        List<HorariosDoctor> horarios = horariosDoctorRepository
                .findByDoctor_IdDoctorAndDiaSemana(idDoctor, diaSemana);

        if (horarios.isEmpty()) {
            return List.of();
        }

        List<Citas> citasAgendadas = citasRepository
                .findByDoctor_IdDoctorAndFecha(idDoctor, fechaCita);

        List<LocalTime> horasOcupadas = citasAgendadas.stream()
                .map(Citas::getHora)
                .collect(Collectors.toList());

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

    @Tool(description = """
        Obtiene la próxima cita programada del paciente.
        Parámetros:
        - idPaciente: ID del paciente
        
        Retorna información detallada de la próxima cita pendiente (fecha futura más cercana),
        incluyendo datos del doctor, especialidad, fecha, hora y motivo.
        Usa esta función cuando el usuario pregunte por su próxima cita o cita más cercana.
        Si no hay citas futuras, retorna un mensaje indicándolo.
        """)
    public String consultarProximaCita(Long idPaciente) {
        try {
            Optional<Paciente> pacienteOpt = pacienteRepository.findById(idPaciente);
            if (pacienteOpt.isEmpty()) {
                return "❌ No se encontró el paciente con ID " + idPaciente;
            }

            Citas proximaCita = citasRepository.findFirstByPaciente_IdPacienteAndFechaGreaterThanEqualOrderByFechaAscHoraAsc(
                    idPaciente,
                    LocalDate.now()
            );

            if (proximaCita == null) {
                return "📅 No tienes citas programadas próximamente.\n\n" +
                        "¿Deseas agendar una nueva cita?";
            }

            String nombreDoctor = "Doctor";
            String especialidad = "Medicina General";
            String direccion = "Dirección no disponible";

            if (proximaCita.getDoctor() != null) {
                Doctores doctor = proximaCita.getDoctor();

                if (doctor.getDetallesUsuario() != null) {
                    String nombre = doctor.getDetallesUsuario().getNombre();
                    String apellido = doctor.getDetallesUsuario().getApellido();
                    if (nombre != null || apellido != null) {
                        nombreDoctor = ((nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "")).trim();
                    }

                    String dir = doctor.getDetallesUsuario().getDireccion();
                    if (dir != null && !dir.trim().isEmpty()) {
                        direccion = dir;
                    }
                }

                if (doctor.getEspecialidad() != null && doctor.getEspecialidad().getNombre() != null) {
                    especialidad = doctor.getEspecialidad().getNombre();
                }
            }

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String fechaFormateada = proximaCita.getFecha().format(dateFormatter);

            return String.format(
                    "📅 Tu próxima cita:\n\n" +
                            "• Doctor: %s\n" +
                            "• Especialidad: %s\n" +
                            "• Fecha: %s\n" +
                            "• Hora: %s\n" +
                            "• Motivo: %s\n" +
                            "• Estado: %s\n" +
                            "• Dirección: %s\n\n" +
                            "💡 Recuerda llegar 10 minutos antes de tu cita.",
                    nombreDoctor,
                    especialidad,
                    fechaFormateada,
                    proximaCita.getHora() != null ? proximaCita.getHora().toString() : "No especificada",
                    proximaCita.getRazon() != null ? proximaCita.getRazon() : "Consulta general",
                    proximaCita.getEstado() != null ? proximaCita.getEstado() : "pendiente",
                    direccion
            );
        } catch (Exception e) {
            return "❌ Error al consultar la próxima cita: " + e.getMessage();
        }
    }

    @Tool(description = """
        Consulta las citas del paciente filtradas por estado.
        Parámetros:
        - idPaciente: ID del paciente
        - estado: estado de las citas a consultar. Valores válidos:
          * 'pendiente' - citas programadas
          * 'completada' - citas finalizadas
          * 'cancelada' - citas canceladas
          * null o vacío - todas las citas
        
        Retorna una lista detallada de las citas que coincidan con el estado solicitado,
        incluyendo información del doctor, fecha, hora, especialidad y motivo.
        Usa esta función cuando el usuario pregunte por sus citas con un estado específico.
        """)
    public String consultarCitasPorEstado(Long idPaciente, String estado) {
        try {
            Optional<Paciente> pacienteOpt = pacienteRepository.findById(idPaciente);
            if (pacienteOpt.isEmpty()) {
                return "❌ No se encontró el paciente con ID " + idPaciente;
            }

            List<Citas> citas = citasRepository.findByPacienteAndEstado(idPaciente, estado);

            if (citas == null || citas.isEmpty()) {
                String estadoTexto = (estado == null || estado.isEmpty()) ? "" : " " + estado + "s";
                return String.format("📋 No tienes citas%s registradas.", estadoTexto);
            }

            StringBuilder resultado = new StringBuilder();
            String estadoTitulo;
            if (estado == null || estado.isEmpty()) {
                estadoTitulo = "Todas";
            } else {
                switch (estado.toLowerCase()) {
                    case "pendiente":
                        estadoTitulo = "Pendientes";
                        break;
                    case "completada":
                        estadoTitulo = "Completadas";
                        break;
                    case "cancelada":
                        estadoTitulo = "Canceladas";
                        break;
                    default:
                        estadoTitulo = "Todas";
                }
            }

            resultado.append(String.format("📋 Citas %s (%d):\n\n", estadoTitulo, citas.size()));

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (int i = 0; i < citas.size(); i++) {
                Citas cita = citas.get(i);

                String nombreDoctor = "Doctor";
                String especialidad = "Medicina General";

                if (cita.getDoctor() != null) {
                    Doctores doctor = cita.getDoctor();

                    if (doctor.getDetallesUsuario() != null) {
                        String nombre = doctor.getDetallesUsuario().getNombre();
                        String apellido = doctor.getDetallesUsuario().getApellido();
                        if (nombre != null || apellido != null) {
                            nombreDoctor = ((nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "")).trim();
                        }
                    }

                    if (doctor.getEspecialidad() != null && doctor.getEspecialidad().getNombre() != null) {
                        especialidad = doctor.getEspecialidad().getNombre();
                    }
                }

                String fechaFormateada = cita.getFecha().format(dateFormatter);

                resultado.append(String.format(
                        "%d. 📅 %s\n" +
                                "   • Doctor: %s (%s)\n" +
                                "   • Hora: %s\n" +
                                "   • Motivo: %s\n" +
                                "   • Estado: %s\n",
                        i + 1,
                        fechaFormateada,
                        nombreDoctor,
                        especialidad,
                        cita.getHora() != null ? cita.getHora().toString() : "No especificada",
                        cita.getRazon() != null ? cita.getRazon() : "Consulta general",
                        cita.getEstado() != null ? cita.getEstado() : "pendiente"
                ));

                if (i < citas.size() - 1) {
                    resultado.append("\n");
                }
            }

            return resultado.toString();
        } catch (Exception e) {
            return "❌ Error al consultar las citas: " + e.getMessage();
        }
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