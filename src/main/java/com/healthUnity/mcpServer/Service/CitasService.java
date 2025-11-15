package com.healthUnity.mcpServer.Service;

import com.healthUnity.mcpServer.DTO.Response.CitaResponseDTO;
import com.healthUnity.mcpServer.DTO.Response.DoctorRatingDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final GmailService gmailService;
    private final DateFormatter dateFormatter;

    @Autowired
    public CitasService(CitasRepository citasRepository,
                        PacienteRepository pacienteRepository,
                        DoctorRepository doctorRepository,
                        GmailService gmailService,
                        HorariosDoctorRepository horariosDoctorRepository,
                        DateFormatter dateFormatter) {
        this.citasRepository = citasRepository;
        this.pacienteRepository = pacienteRepository;
        this.doctorRepository = doctorRepository;
        this.horariosDoctorRepository = horariosDoctorRepository;
        this.gmailService = gmailService;
        this.dateFormatter = dateFormatter;
    }

    @Tool(description = """
                BÚSQUEDA POR HORARIO DISPONIBLE - Encuentra doctores que tienen tiempo libre en fecha/hora específica.
            
                USA ESTE MÉTODO CUANDO el usuario mencione:
                - "qué doctores están disponibles el [fecha] a las [hora]"
                - "horarios disponibles"
                - "quién puede atenderme el [día] a las [hora]"
                - "disponibilidad para [fecha y hora]"
                - "quiero agendar para [fecha] a las [hora]"
            
                PARÁMETROS REQUERIDOS:
                - especialidadNombre: especialidad médica (ej: 'Cardiología', 'Pediatría')
                - fecha: fecha en formato YYYY-MM-DD (ej: '2025-11-15')
                - hora: hora en formato HH:mm (ej: '09:00', '14:30')
                - idPaciente: ID del paciente
            
                RETORNA: Lista de doctores que NO tienen citas en ese horario específico.
            
                IMPORTANTE: Este método busca por DISPONIBILIDAD HORARIA, no por calificación.
            
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
            BÚSQUEDA POR CALIFICACIÓN - Filtra doctores por rating y número de reviews.
            
               USA ESTE MÉTODO CUANDO el usuario mencione:
               - "mejores doctores"
               - "doctores con buena calificación"
               - "doctores con rating mayor a X"
               - "doctores mejor calificados"
               - "doctores con más de X reviews"
               - "quiero un doctor con al menos X estrellas"
               - NO menciona fecha ni hora específica
            
               PARÁMETROS OPCIONALES:
               - especialidadNombre: especialidad médica (opcional)
               - ratingMinimo: rating mínimo (ej: 4.0, 4.5) - OPCIONAL
               - numeroMinReviews: mínimo de reviews (ej: 10, 50) - OPCIONAL
            
               RETORNA: String formateado con lista de doctores ordenados por rating.
            
               IMPORTANTE: NO uses este método si el usuario pregunta por horarios o disponibilidad.
               Si no se especifica rating ni reviews, muestra TODOS los doctores ordenados por rating.
        """)
    public String buscarDoctoresPorRating(Double ratingMinimo, Integer numeroMinReviews, String especialidadNombre) {
        try {
            List<Object[]> doctoresData;

            // Determinar qué query usar según los parámetros
            if (especialidadNombre != null && !especialidadNombre.isEmpty()) {
                doctoresData = doctorRepository.findTopDoctoresPorEspecialidad(especialidadNombre, 100);
            } else {
                // Usamos findAllDoctores sin paginación, obteniendo muchos resultados
                Page<Object[]> page = doctorRepository.findAllDoctores("rating",
                        PageRequest.of(0, 100));
                doctoresData = page.getContent();
            }

            if (doctoresData.isEmpty()) {
                return "❌ No se encontraron doctores en el sistema.";
            }

            // Mapear Object[] a un DTO más manejable
            List<DoctorRatingDTO> doctoresFiltrados = doctoresData.stream()
                    .map(obj -> new DoctorRatingDTO(
                            ((Number) obj[0]).longValue(),     // id_doctor
                            (String) obj[1],                    // nombre
                            (String) obj[2],                    // apellido
                            (String) obj[3],                    // url_imagen
                            (String) obj[4],                    // especialidad
                            ((Number) obj[5]).doubleValue(),    // rating
                            ((Number) obj[6]).intValue()        // reviews
                    ))
                    .filter(doctor -> {
                        boolean cumpleRating = ratingMinimo == null || doctor.getRating() >= ratingMinimo;
                        boolean cumpleReviews = numeroMinReviews == null || doctor.getReviews() >= numeroMinReviews;
                        return cumpleRating && cumpleReviews;
                    })
                    .sorted((d1, d2) -> {
                        int compareRating = Double.compare(d2.getRating(), d1.getRating());
                        if (compareRating != 0) return compareRating;
                        return Integer.compare(d2.getReviews(), d1.getReviews());
                    })
                    .toList();

            if (doctoresFiltrados.isEmpty()) {
                return "❌ No se encontraron doctores que cumplan con los criterios solicitados.";
            }

            // Construir el mensaje de respuesta
            StringBuilder resultado = new StringBuilder();
            String titulo = "👨‍⚕️ Doctores ";

            if (ratingMinimo != null && numeroMinReviews != null) {
                titulo += String.format("con rating ≥ %.1f y ≥ %d reviews", ratingMinimo, numeroMinReviews);
            } else if (ratingMinimo != null) {
                titulo += String.format("con rating ≥ %.1f", ratingMinimo);
            } else if (numeroMinReviews != null) {
                titulo += String.format("con ≥ %d reviews", numeroMinReviews);
            } else {
                titulo += "ordenados por calificación";
            }

            if (especialidadNombre != null && !especialidadNombre.isEmpty()) {
                titulo += " - " + especialidadNombre;
            }

            resultado.append(titulo).append(" (").append(doctoresFiltrados.size()).append("):\n\n");

            // Mostrar hasta 10 doctores
            for (int i = 0; i < Math.min(doctoresFiltrados.size(), 10); i++) {
                DoctorRatingDTO doctor = doctoresFiltrados.get(i);

                String nombreCompleto = (doctor.getNombre() + " " + doctor.getApellido()).trim();
                if (nombreCompleto.isEmpty()) {
                    nombreCompleto = "Doctor";
                }

                String especialidad = doctor.getEspecialidad() != null ?
                        doctor.getEspecialidad() : "Medicina General";

                String estrellas = "⭐".repeat((int) Math.round(doctor.getRating()));

                resultado.append(String.format(
                        "%d. %s\n" +
                                "   • ID: %d\n" +
                                "   • Especialidad: %s\n" +
                                "   • Rating: %.1f %s (%d reviews)\n",
                        i + 1,
                        nombreCompleto,
                        doctor.getIdDoctor(),
                        especialidad,
                        doctor.getRating(),
                        estrellas,
                        doctor.getReviews()
                ));

                if (i < Math.min(doctoresFiltrados.size(), 10) - 1) {
                    resultado.append("\n");
                }
            }

            if (doctoresFiltrados.size() > 10) {
                resultado.append(String.format("\n\n... y %d doctores más.", doctoresFiltrados.size() - 10));
            }

            return resultado.toString();

        } catch (Exception e) {
            return "❌ Error al buscar doctores por calificación: " + e.getMessage();
        }
    }

    @Tool(description = """
            OBTENER MEJOR DOCTOR - Encuentra EL MEJOR doctor de una especialidad específica.
            
                USA ESTE MÉTODO CUANDO el usuario pregunte:
                - "quién es el mejor doctor de [especialidad]"
                - "el mejor cardiólogo"
                - "el doctor más recomendado de [especialidad]"
                - "el número 1 en [especialidad]"
            
                PARÁMETROS REQUERIDOS:
                - especialidadNombre: nombre de la especialidad (REQUERIDO)
            
                RETORNA: String con información del doctor #1 con mejor rating.
            
                IMPORTANTE: Solo retorna UN doctor (el mejor). Para ver varios, usa buscarDoctoresPorRating.
        """)
    public String obtenerMejorDoctor(String especialidadNombre) {
        try {
            // Usar el query nativo que ya calcula rating y reviews
            List<Object[]> doctoresData = doctorRepository.findTopDoctoresPorEspecialidad(especialidadNombre, 1);

            if (doctoresData.isEmpty()) {
                return "❌ No se encontraron doctores para la especialidad: " + especialidadNombre;
            }

            // Tomar el primer resultado (ya viene ordenado por mejor rating)
            Object[] mejorDoctorData = doctoresData.get(0);

            Long idDoctor = ((Number) mejorDoctorData[0]).longValue();
            String nombre = (String) mejorDoctorData[1];
            String apellido = (String) mejorDoctorData[2];
            String especialidad = (String) mejorDoctorData[4];
            Double rating = ((Number) mejorDoctorData[5]).doubleValue();
            Integer reviews = ((Number) mejorDoctorData[6]).intValue();

            // Construir nombre completo
            String nombreCompleto = (nombre + " " + apellido).trim();
            if (nombreCompleto.isEmpty()) {
                nombreCompleto = "Doctor";
            }

            // Construir especialidad
            if (especialidad == null || especialidad.isEmpty()) {
                especialidad = "Medicina General";
            }

            String estrellas = "⭐".repeat((int) Math.round(rating));

            return String.format(
                    "🏆 Mejor doctor de %s:\n\n" +
                            "• Nombre: %s\n" +
                            "• ID: %d\n" +
                            "• Rating: %.1f %s\n" +
                            "• Reviews: %d pacientes\n\n" +
                            "¿Deseas agendar una cita con este doctor?",
                    especialidad,
                    nombreCompleto,
                    idDoctor,
                    rating,
                    estrellas,
                    reviews
            );
        } catch (Exception e) {
            return "❌ Error al buscar el mejor doctor: " + e.getMessage();
        }
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

        try {
            // Validar paciente
            Optional<Paciente> pacienteOpt = pacienteRepository.findById(idPaciente);
            if (pacienteOpt.isEmpty()) {
                return "Error: No se encontró el paciente con ID " + idPaciente;
            }

            // Validar doctor
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

            // Obtener datos del doctor para el email
            String nombreDoctor = doctor.getDetallesUsuario() != null
                    ? doctor.getDetallesUsuario().getNombre() + " " + doctor.getDetallesUsuario().getApellido()
                    : "Doctor";

            String especialidad = doctor.getEspecialidad() != null
                    ? doctor.getEspecialidad().getNombre()
                    : "Medicina General";

            String direccionDoctor = doctor.getDetallesUsuario() != null && doctor.getDetallesUsuario().getDireccion() != null
                    ? doctor.getDetallesUsuario().getDireccion()
                    : "Dirección no especificada";

            String urlImagenDoctor = doctor.getDetallesUsuario() != null && doctor.getDetallesUsuario().getUrlImagen() != null
                    ? doctor.getDetallesUsuario().getUrlImagen()
                    : "https://via.placeholder.com/200x200/4A90E2/FFFFFF?text=Doctor";

            // Obtener datos del paciente para el email
            String nombrePaciente = paciente.getDetallesUsuario() != null && paciente.getDetallesUsuario().getNombre() != null
                    ? paciente.getDetallesUsuario().getNombre()
                    : "Paciente";

            String emailPaciente = paciente.getDetallesUsuario() != null && paciente.getDetallesUsuario().getGmail() != null
                    ? paciente.getDetallesUsuario().getGmail()
                    : null;

            // Enviar email de confirmación
            if (emailPaciente != null && !emailPaciente.isEmpty()) {
                try {
                    gmailService.sendConfirmAppointment(
                            emailPaciente,
                            nombrePaciente,
                            nombreDoctor,
                            direccionDoctor,
                            especialidad,
                            cita.getFecha(),
                            cita.getHora(),
                            cita.getRazon(),
                            urlImagenDoctor
                    );
                } catch (Exception emailException) {
                    // Log el error pero no fallar la creación de la cita
                    System.err.println("Error al enviar email de confirmación: " + emailException.getMessage());
                }
            }

            return String.format(
                    "✅ Cita creada exitosamente!\n\n" +
                            "📋 Detalles de tu cita:\n" +
                            "• ID Cita: %d\n" +
                            "• Doctor: %s\n" +
                            "• Especialidad: %s\n" +
                            "• Fecha: %s\n" +
                            "• Hora: %s\n" +
                            "• Motivo: %s\n" +
                            "• Estado: Confirmada\n\n" +
                            "📧 Te hemos enviado un email de confirmación a %s\n" +
                            "Te enviaremos un recordatorio 24 horas antes de tu cita.",
                    cita.getIdCita(),
                    nombreDoctor,
                    especialidad,
                    fecha,
                    hora,
                    cita.getRazon(),
                    emailPaciente != null ? emailPaciente : "tu correo registrado"
            );
        } catch (Exception e) {
            return "❌ Error al crear la cita: " + e.getMessage();
        }
    }

    @Tool(description = """
        Reprograma una cita médica existente a una nueva fecha y hora.
        Parámetros:
        - idCita: ID de la cita a reprogramar
        - idPaciente: ID del paciente (para validación)
        - nuevaFecha: nueva fecha en formato YYYY-MM-DD
        - nuevaHora: nueva hora en formato HH:mm
        
        Actualiza la fecha y hora de una cita pendiente. Verifica que el doctor esté disponible.
        Retorna un mensaje de confirmación con los nuevos detalles o error.
        Usa esta función cuando el usuario quiera cambiar la fecha/hora de una cita existente.
        """)
    @Transactional
    public String reprogramarCita(
            Long idCita,
            Long idPaciente,
            String nuevaFecha,
            String nuevaHora) {

        try {
            Optional<Citas> citaOpt = citasRepository.findById(idCita);

            if (citaOpt.isEmpty()) {
                return "❌ No se encontró la cita con ID " + idCita;
            }

            Citas cita = citaOpt.get();

            if (!cita.getPaciente().getIdPaciente().equals(idPaciente)) {
                return "❌ No tienes permisos para reprogramar esta cita.";
            }

            if ("cancelada".equalsIgnoreCase(cita.getEstado())) {
                return "❌ No se puede reprogramar una cita cancelada. Debes crear una nueva cita.";
            }

            if ("completada".equalsIgnoreCase(cita.getEstado())) {
                return "❌ No se puede reprogramar una cita que ya fue completada.";
            }

            LocalDate fechaNueva = LocalDate.parse(nuevaFecha);
            LocalTime horaNueva = LocalTime.parse(nuevaHora);
            int diaSemana = fechaNueva.getDayOfWeek().getValue();

            List<HorariosDoctor> horarios = horariosDoctorRepository
                    .findByDoctor_IdDoctorAndDiaSemana(cita.getDoctor().getIdDoctor(), diaSemana);

            boolean tieneHorario = horarios.stream().anyMatch(h ->
                    !horaNueva.isBefore(h.getHoraInicio()) &&
                            !horaNueva.isAfter(h.getHoraFin())
            );

            if (!tieneHorario) {
                return "❌ El doctor no tiene disponibilidad en ese día y hora. Por favor elige otro horario.";
            }

            List<Citas> citasExistentes = citasRepository
                    .findByDoctor_IdDoctorAndFechaAndHora(
                            cita.getDoctor().getIdDoctor(),
                            fechaNueva,
                            horaNueva
                    );

            if (!citasExistentes.isEmpty()) {
                return "❌ El doctor ya tiene una cita agendada en ese horario. Por favor elige otro horario.";
            }

            LocalDate fechaAnterior = cita.getFecha();
            LocalTime horaAnterior = cita.getHora();

            cita.setFecha(fechaNueva);
            cita.setHora(horaNueva);
            citasRepository.save(cita);

            String nombreDoctor = "Doctor";
            String especialidad = "Medicina General";

            if (cita.getDoctor() != null) {
                if (cita.getDoctor().getDetallesUsuario() != null) {
                    String nombre = cita.getDoctor().getDetallesUsuario().getNombre();
                    String apellido = cita.getDoctor().getDetallesUsuario().getApellido();
                    if (nombre != null || apellido != null) {
                        nombreDoctor = ((nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "")).trim();
                    }
                }

                if (cita.getDoctor().getEspecialidad() != null && cita.getDoctor().getEspecialidad().getNombre() != null) {
                    especialidad = cita.getDoctor().getEspecialidad().getNombre();
                }
            }

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String fechaAnteriorFormateada = fechaAnterior.format(dateFormatter);
            String fechaNuevaFormateada = fechaNueva.format(dateFormatter);

            return String.format(
                    "✅ Cita reprogramada exitosamente!\n\n" +
                            "📋 Cambios realizados:\n" +
                            "• Doctor: %s (%s)\n" +
                            "• Fecha anterior: %s a las %s\n" +
                            "• Nueva fecha: %s a las %s\n" +
                            "• Motivo: %s\n" +
                            "• Estado: Confirmada\n\n" +
                            "Te enviaremos un recordatorio 24 horas antes de tu nueva cita.",
                    nombreDoctor,
                    especialidad,
                    fechaAnteriorFormateada,
                    horaAnterior != null ? horaAnterior.toString() : "No especificada",
                    fechaNuevaFormateada,
                    horaNueva.toString(),
                    cita.getRazon() != null ? cita.getRazon() : "Consulta general"
            );
        } catch (Exception e) {
            return "❌ Error al reprogramar la cita: " + e.getMessage();
        }
    }

    @Tool(description = """
            OBTENER HORARIOS DE UN DOCTOR - Muestra las horas libres de un doctor en una fecha.
            
                USA ESTE MÉTODO CUANDO:
                - El usuario YA seleccionó un doctor específico (tienes el ID)
                - Quiere saber "qué horas tiene libres el doctor X"
                - Necesitas mostrar opciones de horario para un doctor en particular
            
                PARÁMETROS REQUERIDOS:
                - idDoctor: ID del doctor (REQUERIDO)
                - fecha: fecha en formato YYYY-MM-DD (REQUERIDO)
            
                RETORNA: Lista de strings con horarios disponibles (ej: ["09:00", "09:30", "10:00"])
            
                IMPORTANTE: Este método requiere que YA tengas el ID del doctor seleccionado.
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
                            "• ID Cita: %d\n" +
                            "• Doctor: %s\n" +
                            "• Especialidad: %s\n" +
                            "• Fecha: %s\n" +
                            "• Hora: %s\n" +
                            "• Motivo: %s\n" +
                            "• Estado: %s\n" +
                            "• Dirección: %s\n\n" +
                            "💡 Recuerda llegar 10 minutos antes de tu cita.",
                    proximaCita.getIdCita(),
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
          CONSULTAR CITAS POR ESTADO – Muestra las citas del paciente según su estado.
            
                  USA ESTE MÉTODO CUANDO:
                  - El usuario pregunta explícitamente por SUS CITAS
                    (ej: "mis citas", "citas completadas", "qué citas tengo", "mis citas pendientes")
                  - El usuario quiere ver citas según un estado específico
                    (pendientes, completadas o canceladas)
                  - Necesitas listar o filtrar las citas del paciente por su estado actual
            
                  NO USAR ESTE MÉTODO CUANDO:
                  - El usuario pida "mis doctores favoritos"
                  - El usuario pida doctores guardados
                  - El usuario quiera información de doctores
                  - El usuario pregunte por opiniones, especialidades o perfiles de doctores
                  - El usuario no mencione citas en absoluto
            
                  PARÁMETROS REQUERIDOS:
                  - idPaciente: ID del paciente (REQUERIDO)
                  - estado: estado de las citas a consultar. Valores válidos:
                      * 'pendiente'   → citas programadas
                      * 'completada'  → citas finalizadas
                      * 'cancelada'   → citas canceladas
                      * null o vacío  → todas las citas
            
                  RETORNA:
                  Lista detallada de las citas del paciente, incluyendo:
                  - doctor
                  - fecha
                  - hora
                  - especialidad
                  - motivo
            
                  IMPORTANTE:
                  Úsalo EXCLUSIVAMENTE para solicitudes que involucren CITAS.
            
            
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
                                "   • ID: %d\n" +
                                "   • Doctor: %s (%s)\n" +
                                "   • Hora: %s\n" +
                                "   • Motivo: %s\n" +
                                "   • Estado: %s\n",
                        i + 1,
                        fechaFormateada,
                        cita.getIdCita(),
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