package com.healthUnity.mcpServer.Service;

import com.healthUnity.mcpServer.DTO.FavoritoDoctorProjection;
import com.healthUnity.mcpServer.Models.Doctores;
import com.healthUnity.mcpServer.Models.FavoritosDoctores;
import com.healthUnity.mcpServer.Models.Paciente;
import com.healthUnity.mcpServer.Repositories.DoctorRepository;
import com.healthUnity.mcpServer.Repositories.FavoritoDoctorRepository;
import com.healthUnity.mcpServer.Repositories.PacienteRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final PacienteRepository pacienteRepository;
    private final FavoritoDoctorRepository favoritoDoctorRepository;

    @Autowired
    public DoctorService(DoctorRepository doctorRepository, PacienteRepository pacienteRepository, FavoritoDoctorRepository favoritoDoctorRepository) {
        this.doctorRepository = doctorRepository;
        this.pacienteRepository = pacienteRepository;
        this.favoritoDoctorRepository = favoritoDoctorRepository;
    }

    @Tool(description = """
        OBTENER DOCTORES FAVORITOS - Lista todos los doctores guardados como favoritos por el paciente.
        
        USA ESTE MÉTODO CUANDO:
        - El usuario pregunta "cuáles son mis doctores favoritos", "mis doctores guardados"
        - Dice "muéstrame mis favoritos", "qué doctores tengo en favoritos"
        - Quiere ver la lista completa de doctores que ha guardado
        - Necesita seleccionar un doctor de sus favoritos para agendar una cita
        
        PARÁMETROS REQUERIDOS:
        - idPaciente: ID del paciente actual (REQUERIDO)
        
        RETORNA: String con la lista formateada de doctores favoritos incluyendo:
        - Número de orden en la lista
        - Nombre completo del doctor
        - Especialidad
        - Calificación (rating) y número de reseñas
        - ID del doctor (necesario para agendar citas)
        - ID del favorito (necesario para eliminar de favoritos)
        
        Si no tiene favoritos, retorna un mensaje indicándolo.
        
        IMPORTANTE: 
        - El ID del doctor se usa para agendar citas
        - El ID del favorito se usa para eliminar de la lista de favoritos
        - Son IDs diferentes, no los confundas
        """)
    @Transactional(readOnly = true)
    public String getDoctoresFavoritos(Long idPaciente) {
        try {
            // 1. Obtener los datos usando la Projection
            List<FavoritoDoctorProjection> projections =
                    favoritoDoctorRepository.findAllFavoritosDtoByPacienteId(idPaciente);

            // 2. Validar si hay favoritos
            if (projections == null || projections.isEmpty()) {
                return "No tienes doctores guardados en favoritos aún. Puedes agregar doctores a favoritos para acceder rápidamente a ellos.";
            }

            // 3. Construir la respuesta en formato String
            StringBuilder resultado = new StringBuilder();
            resultado.append("📋 TUS DOCTORES FAVORITOS:\n");
            resultado.append("═".repeat(50)).append("\n\n");

            for (int i = 0; i < projections.size(); i++) {
                FavoritoDoctorProjection p = projections.get(i);

                resultado.append(String.format("👨‍⚕️ %d. Dr(a). %s %s\n",
                        i + 1,
                        p.getNombre(),
                        p.getApellido()
                ));

                resultado.append(String.format("   🏥 Especialidad: %s\n", p.getEspecialidad()));

                // Rating y reseñas
                int numReviews = p.getNumber_reviews() != null ? p.getNumber_reviews() : 0;
                if (p.getRating() != null) {
                    resultado.append(String.format("   ⭐ Calificación: %.1f/5.0 (%d reseñas)\n",
                            p.getRating(),
                            numReviews
                    ));
                } else {
                    resultado.append("   ⭐ Sin calificaciones aún\n");
                }

                resultado.append(String.format("   🆔 ID Doctor: %d (usa este para agendar citas)\n",
                        p.getIdDoctor()
                ));
                resultado.append(String.format("   🔖 ID Favorito: %d (usa este para eliminar de favoritos)\n",
                        p.getIdFavorito()
                ));

                resultado.append("\n");
            }

            resultado.append("═".repeat(50)).append("\n");
            resultado.append(String.format("Total: %d doctor(es) en favoritos", projections.size()));

            return resultado.toString();
        } catch (Exception e) {
            return "Error al obtener doctores favoritos: " + e.getMessage();
        }
    }

    @Tool(description = """
            AGREGAR DOCTOR A FAVORITOS - Guarda un doctor en la lista de favoritos del paciente.
            
            USA ESTE MÉTODO CUANDO:
            - El usuario quiere "guardar" o "agregar a favoritos" un doctor específico
            - Dice "quiero guardar este doctor", "añadir a favoritos", "marcar como favorito"
            - Necesita acceso rápido a un doctor que consulta frecuentemente
            
            PARÁMETROS REQUERIDOS:
            - idDoctor: ID del doctor a agregar (REQUERIDO)
            - idPaciente: ID del paciente actual (REQUERIDO)
            
            RETORNA: String confirmando la operación exitosa o mensaje de error
            
            IMPORTANTE: Verifica que tanto el doctor como el paciente existan antes de agregar.
            """)
    @Transactional
    public String añadirFavoritos(Long idDoctor, Long idPaciente) {
        try {
            Optional<Doctores> doctorOptional = doctorRepository.findById(idDoctor);
            if (doctorOptional.isEmpty()) {
                return "Error: Doctor no encontrado con ID " + idDoctor;
            }

            Optional<Paciente> pacienteOptional = pacienteRepository.findById(idPaciente);
            if (pacienteOptional.isEmpty()) {
                return "Error: Paciente no encontrado con ID " + idPaciente;
            }

            Doctores doctor = doctorOptional.get();
            Paciente paciente = pacienteOptional.get();

            // Verificar si ya existe en favoritos
            boolean yaExiste = favoritoDoctorRepository.existsByDoctorIdDoctorAndPacienteIdPaciente(idDoctor, idPaciente);
            if (yaExiste) {
                return "Este doctor ya está en tus favoritos";
            }

            FavoritosDoctores favoritoDoctores = new FavoritosDoctores();
            favoritoDoctores.setDoctor(doctor);
            favoritoDoctores.setPaciente(paciente);
            favoritoDoctorRepository.save(favoritoDoctores);

            // Acceder a los datos dentro de la transacción
            String nombreDoctor = doctor.getDetallesUsuario() != null ?
                    doctor.getDetallesUsuario().getNombre() : "Doctor";
            String nombrePaciente = paciente.getDetallesUsuario() != null ?
                    paciente.getDetallesUsuario().getNombre() : "Paciente";
            String especialidad = doctor.getEspecialidad() != null ?
                    doctor.getEspecialidad().getNombre() : "";

            return String.format("✅ Doctor %s (%s) agregado exitosamente a favoritos",
                    nombreDoctor, especialidad);
        } catch (Exception e) {
            return "Error al agregar doctor a favoritos: " + e.getMessage();
        }
    }

    @Tool(description = """
            ELIMINAR DOCTOR DE FAVORITOS - Remueve un doctor de la lista de favoritos del paciente.
            
            USA ESTE MÉTODO CUANDO:
            - El usuario quiere "eliminar", "quitar" o "remover" un doctor de favoritos
            - Dice "ya no quiero este doctor en favoritos", "borrar de favoritos"
            - Necesita limpiar su lista de doctores guardados
            
            PARÁMETROS REQUERIDOS:
            - idFavorito: ID del registro de favorito (REQUERIDO, NO el ID del doctor)
            
            RETORNA: String confirmando la eliminación exitosa o mensaje de error
            
            IMPORTANTE: Se requiere el ID del favorito, no el ID del doctor. 
            Este ID se obtiene al listar los favoritos del paciente.
            """)
    @Transactional
    public String eliminarFavoritos(Long idFavorito) {
        try {
            Optional<FavoritosDoctores> favoritoDoctorOptional = favoritoDoctorRepository.findById(idFavorito);
            if (favoritoDoctorOptional.isEmpty()) {
                return "Error: Favorito no encontrado con ID " + idFavorito;
            }

            FavoritosDoctores favorito = favoritoDoctorOptional.get();

            // Acceder al nombre dentro de la transacción
            String nombreDoctor = favorito.getDoctor() != null &&
                    favorito.getDoctor().getDetallesUsuario() != null ?
                    favorito.getDoctor().getDetallesUsuario().getNombre() : "Doctor";

            favoritoDoctorRepository.delete(favorito);

            return "✅ Doctor " + nombreDoctor + " eliminado exitosamente de favoritos";
        } catch (Exception e) {
            return "Error al eliminar doctor de favoritos: " + e.getMessage();
        }
    }
}