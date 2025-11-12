package com.healthUnity.mcpServer.Repositories;

import com.healthUnity.mcpServer.Models.Citas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitasRepository extends JpaRepository<Citas, Long> {

    // ============================================
    // MÉTODOS PARA PACIENTES
    // ============================================

    /**
     * Obtiene la próxima cita de un paciente (fecha >= hoy)
     * Ordenada por fecha y hora ascendente
     */
    Citas findFirstByPaciente_IdPacienteAndFechaGreaterThanEqualOrderByFechaAscHoraAsc(
            Long idPaciente,
            LocalDate fecha
    );

    /**
     * Obtiene todas las citas de un paciente con un estado específico
     * Estados comunes: 'pendiente', 'confirmada', 'completada', 'cancelada'
     */
    @Query("SELECT c FROM Citas c WHERE c.paciente.idPaciente = :idPaciente AND c.estado = :estado")
    List<Citas> findByPacienteAndEstado(
            @Param("idPaciente") Long idPaciente,
            @Param("estado") String estado
    );

    /**
     * Obtiene todas las citas de un paciente
     */
    List<Citas> findByPaciente_IdPaciente(Long idPaciente);

    /**
     * Obtiene citas futuras de un paciente
     */
    @Query("SELECT c FROM Citas c " +
            "WHERE c.paciente.idPaciente = :idPaciente " +
            "AND c.fecha >= :fecha " +
            "ORDER BY c.fecha ASC, c.hora ASC")
    List<Citas> findCitasFuturasPaciente(
            @Param("idPaciente") Long idPaciente,
            @Param("fecha") LocalDate fecha
    );

    /**
     * Obtiene citas pasadas de un paciente
     */
    @Query("SELECT c FROM Citas c " +
            "WHERE c.paciente.idPaciente = :idPaciente " +
            "AND c.fecha < :fecha " +
            "ORDER BY c.fecha DESC, c.hora DESC")
    List<Citas> findCitasHistorialPaciente(
            @Param("idPaciente") Long idPaciente,
            @Param("fecha") LocalDate fecha
    );

    // ============================================
    // MÉTODOS PARA DOCTORES
    // ============================================

    /**
     * Obtiene todas las citas de un doctor en una fecha específica
     */
    List<Citas> findByDoctor_IdDoctorAndFecha(
            Long idDoctor,
            LocalDate fecha
    );

    /**
     * Verifica si existe una cita en una fecha y hora exacta
     */
    List<Citas> findByDoctor_IdDoctorAndFechaAndHora(
            Long idDoctor,
            LocalDate fecha,
            LocalTime hora
    );

    /**
     * Obtiene citas de un doctor en un rango de fechas
     */
    @Query("SELECT c FROM Citas c " +
            "WHERE c.doctor.idDoctor = :idDoctor " +
            "AND c.fecha BETWEEN :fechaInicio AND :fechaFin " +
            "ORDER BY c.fecha ASC, c.hora ASC")
    List<Citas> findCitasDoctorEnRango(
            @Param("idDoctor") Long idDoctor,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    /**
     * Obtiene las próximas citas de un doctor
     */
    @Query("SELECT c FROM Citas c " +
            "WHERE c.doctor.idDoctor = :idDoctor " +
            "AND c.fecha >= :fecha " +
            "AND c.estado != 'cancelada' " +
            "ORDER BY c.fecha ASC, c.hora ASC")
    List<Citas> findProximasCitasDoctor(
            @Param("idDoctor") Long idDoctor,
            @Param("fecha") LocalDate fecha
    );

    // ============================================
    // MÉTODOS PARA VERIFICAR DISPONIBILIDAD
    // ============================================

    /**
     * Verifica si existe una cita en un rango de tiempo
     * Útil para evitar solapamientos (citas de 30 min o 1 hora)
     */
    @Query("SELECT c FROM Citas c WHERE c.doctor.idDoctor = :idDoctor " +
            "AND c.fecha = :fecha " +
            "AND c.hora >= :horaInicio " +
            "AND c.hora < :horaFin " +
            "AND c.estado != 'cancelada'")
    List<Citas> findCitasEnRango(
            @Param("idDoctor") Long idDoctor,
            @Param("fecha") LocalDate fecha,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );

    /**
     * Cuenta cuántas citas tiene un doctor en una fecha
     */
    @Query("SELECT COUNT(c) FROM Citas c " +
            "WHERE c.doctor.idDoctor = :idDoctor " +
            "AND c.fecha = :fecha " +
            "AND c.estado != 'cancelada'")
    Long countCitasDoctorEnFecha(
            @Param("idDoctor") Long idDoctor,
            @Param("fecha") LocalDate fecha
    );

    /**
     * Verifica si un doctor tiene disponibilidad en una fecha y hora
     * Retorna true si NO hay citas (está disponible)
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN false ELSE true END " +
            "FROM Citas c " +
            "WHERE c.doctor.idDoctor = :idDoctor " +
            "AND c.fecha = :fecha " +
            "AND c.hora = :hora " +
            "AND c.estado != 'cancelada'")
    Boolean isDoctorDisponible(
            @Param("idDoctor") Long idDoctor,
            @Param("fecha") LocalDate fecha,
            @Param("hora") LocalTime hora
    );

    // ============================================
    // MÉTODOS PARA ESTADÍSTICAS Y REPORTES
    // ============================================

    /**
     * Cuenta total de citas por estado
     */
    @Query("SELECT c.estado, COUNT(c) FROM Citas c GROUP BY c.estado")
    List<Object[]> countCitasPorEstado();

    /**
     * Obtiene las citas de hoy para recordatorios
     */
    @Query("SELECT c FROM Citas c " +
            "WHERE c.fecha = :fecha " +
            "AND c.estado = 'pendiente' " +
            "ORDER BY c.hora ASC")
    List<Citas> findCitasDeHoy(@Param("fecha") LocalDate fecha);

    /**
     * Obtiene citas que necesitan recordatorio (24 horas antes)
     */
    @Query("SELECT c FROM Citas c " +
            "WHERE c.fecha = :fecha " +
            "AND c.estado = 'pendiente' " +
            "ORDER BY c.hora ASC")
    List<Citas> findCitasParaRecordatorio(@Param("fecha") LocalDate fecha);

    /**
     * Cuenta citas por paciente en un periodo
     */
    @Query("SELECT COUNT(c) FROM Citas c " +
            "WHERE c.paciente.idPaciente = :idPaciente " +
            "AND c.fecha BETWEEN :fechaInicio AND :fechaFin")
    Long countCitasPacienteEnPeriodo(
            @Param("idPaciente") Long idPaciente,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    /**
     * Obtiene las últimas N citas de un paciente con un doctor
     * Útil para el historial médico
     */
    @Query("SELECT c FROM Citas c " +
            "WHERE c.paciente.idPaciente = :idPaciente " +
            "AND c.doctor.idDoctor = :idDoctor " +
            "AND c.estado = 'completada' " +
            "ORDER BY c.fecha DESC, c.hora DESC")
    List<Citas> findHistorialPacienteConDoctor(
            @Param("idPaciente") Long idPaciente,
            @Param("idDoctor") Long idDoctor
    );

    /**
     * Busca conflictos de citas (mismo paciente, misma fecha/hora)
     */
    @Query("SELECT c FROM Citas c " +
            "WHERE c.paciente.idPaciente = :idPaciente " +
            "AND c.fecha = :fecha " +
            "AND c.hora = :hora " +
            "AND c.estado != 'cancelada'")
    List<Citas> findConflictosCitaPaciente(
            @Param("idPaciente") Long idPaciente,
            @Param("fecha") LocalDate fecha,
            @Param("hora") LocalTime hora
    );
}