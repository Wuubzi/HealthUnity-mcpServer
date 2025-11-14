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

    /**
     * Busca la próxima cita del paciente con fetch join para evitar lazy loading
     */
    @Query("SELECT c FROM Citas c " +
            "LEFT JOIN FETCH c.doctor d " +
            "LEFT JOIN FETCH d.detallesUsuario " +
            "LEFT JOIN FETCH d.especialidad " +
            "WHERE c.paciente.idPaciente = :idPaciente " +
            "AND c.fecha >= :fecha " +
            "ORDER BY c.fecha ASC, c.hora ASC " +
            "LIMIT 1")
    Citas findFirstByPaciente_IdPacienteAndFechaGreaterThanEqualOrderByFechaAscHoraAsc(
            @Param("idPaciente") Long idPaciente,
            @Param("fecha") LocalDate fecha
    );

    /**
     * Busca citas por paciente y estado con fetch join
     */
    @Query("SELECT DISTINCT c FROM Citas c " +
            "LEFT JOIN FETCH c.doctor d " +
            "LEFT JOIN FETCH d.detallesUsuario " +
            "LEFT JOIN FETCH d.especialidad " +
            "WHERE c.paciente.idPaciente = :idPaciente " +
            "AND (:estado IS NULL OR c.estado = :estado) " +
            "ORDER BY c.fecha DESC, c.hora DESC")
    List<Citas> findByPacienteAndEstado(
            @Param("idPaciente") Long idPaciente,
            @Param("estado") String estado
    );

    /**
     * Busca citas por doctor, fecha y hora
     */
    @Query("SELECT c FROM Citas c " +
            "WHERE c.doctor.idDoctor = :idDoctor " +
            "AND c.fecha = :fecha " +
            "AND c.hora = :hora")
    List<Citas> findByDoctor_IdDoctorAndFechaAndHora(
            @Param("idDoctor") Long idDoctor,
            @Param("fecha") LocalDate fecha,
            @Param("hora") LocalTime hora
    );

    /**
     * Busca citas por doctor y fecha
     */
    @Query("SELECT c FROM Citas c " +
            "WHERE c.doctor.idDoctor = :idDoctor " +
            "AND c.fecha = :fecha")
    List<Citas> findByDoctor_IdDoctorAndFecha(
            @Param("idDoctor") Long idDoctor,
            @Param("fecha") LocalDate fecha
    );
}