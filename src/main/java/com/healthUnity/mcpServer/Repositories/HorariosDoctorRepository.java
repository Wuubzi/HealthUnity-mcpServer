package com.healthUnity.mcpServer.Repositories;

import com.healthUnity.mcpServer.Models.HorariosDoctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface HorariosDoctorRepository extends JpaRepository<HorariosDoctor, Long> {

    // ============================================
    // MÉTODOS BÁSICOS DE CONSULTA
    // ============================================

    /**
     * Obtiene todos los horarios de un doctor
     * Útil para mostrar la disponibilidad completa del doctor
     */
    List<HorariosDoctor> findAllByDoctor_IdDoctor(Long idDoctor);

    /**
     * Busca horarios de un doctor para un día específico de la semana
     * @param idDoctor ID del doctor
     * @param diaSemana 1=Lunes, 2=Martes, 3=Miércoles, 4=Jueves, 5=Viernes, 6=Sábado, 7=Domingo
     */
    List<HorariosDoctor> findByDoctor_IdDoctorAndDiaSemana(Long idDoctor, Integer diaSemana);

    /**
     * Obtiene horarios ordenados por día y hora de inicio
     * Útil para mostrar la agenda ordenada del doctor
     */
    @Query("SELECT h FROM HorariosDoctor h " +
            "WHERE h.doctor.idDoctor = :idDoctor " +
            "ORDER BY h.diaSemana ASC, h.horaInicio ASC")
    List<HorariosDoctor> findHorariosOrdenadosByDoctor(@Param("idDoctor") Long idDoctor);

    // ============================================
    // MÉTODOS PARA VERIFICAR DISPONIBILIDAD
    // ============================================

    /**
     * Verifica si un doctor tiene horario en un día y hora específica
     * Retorna horarios que incluyen la hora solicitada
     */
    @Query("SELECT h FROM HorariosDoctor h " +
            "WHERE h.doctor.idDoctor = :idDoctor " +
            "AND h.diaSemana = :diaSemana " +
            "AND h.horaInicio <= :hora " +
            "AND h.horaFin > :hora")
    List<HorariosDoctor> findHorarioDisponibleEnHora(
            @Param("idDoctor") Long idDoctor,
            @Param("diaSemana") Integer diaSemana,
            @Param("hora") LocalTime hora
    );

    /**
     * Verifica si un doctor trabaja en un día específico
     */
    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END " +
            "FROM HorariosDoctor h " +
            "WHERE h.doctor.idDoctor = :idDoctor " +
            "AND h.diaSemana = :diaSemana")
    Boolean doctorTrabajaEnDia(
            @Param("idDoctor") Long idDoctor,
            @Param("diaSemana") Integer diaSemana
    );

    /**
     * Obtiene la hora más temprana que trabaja un doctor en un día
     */
    @Query("SELECT MIN(h.horaInicio) FROM HorariosDoctor h " +
            "WHERE h.doctor.idDoctor = :idDoctor " +
            "AND h.diaSemana = :diaSemana")
    LocalTime findHoraMasTempranaPorDia(
            @Param("idDoctor") Long idDoctor,
            @Param("diaSemana") Integer diaSemana
    );

    /**
     * Obtiene la hora más tarde que trabaja un doctor en un día
     */
    @Query("SELECT MAX(h.horaFin) FROM HorariosDoctor h " +
            "WHERE h.doctor.idDoctor = :idDoctor " +
            "AND h.diaSemana = :diaSemana")
    LocalTime findHoraMasTardePorDia(
            @Param("idDoctor") Long idDoctor,
            @Param("diaSemana") Integer diaSemana
    );

    // ============================================
    // MÉTODOS PARA BÚSQUEDA AVANZADA (SIMPLIFICADOS)
    // ============================================

    /**
     * Obtiene todos los horarios de un día específico (todos los doctores)
     * Útil para reportes o análisis de capacidad
     */
    @Query("SELECT h FROM HorariosDoctor h " +
            "WHERE h.diaSemana = :diaSemana " +
            "ORDER BY h.horaInicio ASC")
    List<HorariosDoctor> findAllByDiaSemana(@Param("diaSemana") Integer diaSemana);

    /**
     * Cuenta cuántos horarios tiene configurado un doctor
     */
    @Query("SELECT COUNT(h) FROM HorariosDoctor h " +
            "WHERE h.doctor.idDoctor = :idDoctor")
    Long countHorariosByDoctor(@Param("idDoctor") Long idDoctor);

    /**
     * Obtiene los días que trabaja un doctor (lista de números de día)
     */
    @Query("SELECT DISTINCT h.diaSemana FROM HorariosDoctor h " +
            "WHERE h.doctor.idDoctor = :idDoctor " +
            "ORDER BY h.diaSemana ASC")
    List<Integer> findDiasLaboralesByDoctor(@Param("idDoctor") Long idDoctor);

    // ============================================
    // MÉTODOS PARA RANGOS DE TIEMPO (SIMPLIFICADOS)
    // ============================================

    /**
     * Busca horarios en un rango de horas específico
     * Usa LocalTime directamente como parámetros
     */
    @Query("SELECT h FROM HorariosDoctor h " +
            "WHERE h.doctor.idDoctor = :idDoctor " +
            "AND h.diaSemana = :diaSemana " +
            "AND h.horaInicio >= :horaInicio " +
            "AND h.horaFin <= :horaFin")
    List<HorariosDoctor> findHorariosEnRango(
            @Param("idDoctor") Long idDoctor,
            @Param("diaSemana") Integer diaSemana,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );

    // ============================================
    // MÉTODOS PARA ESTADÍSTICAS (SIMPLIFICADOS)
    // ============================================

    /**
     * Obtiene el resumen de horarios por día
     * Retorna: día, cantidad de bloques horarios
     */
    @Query("SELECT h.diaSemana, COUNT(h) " +
            "FROM HorariosDoctor h " +
            "WHERE h.doctor.idDoctor = :idDoctor " +
            "GROUP BY h.diaSemana " +
            "ORDER BY h.diaSemana ASC")
    List<Object[]> getResumenHorariosPorDia(@Param("idDoctor") Long idDoctor);

    // ============================================
    // MÉTODOS DE ELIMINACIÓN
    // ============================================

    /**
     * Elimina todos los horarios de un doctor
     * Útil cuando se actualiza completamente la disponibilidad
     */
    void deleteByDoctor_IdDoctor(Long idDoctor);

    /**
     * Elimina horarios de un doctor en un día específico
     */
    void deleteByDoctor_IdDoctorAndDiaSemana(Long idDoctor, Integer diaSemana);
}