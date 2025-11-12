package com.healthUnity.mcpServer.Repositories;

import com.healthUnity.mcpServer.Models.Doctores;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctores, Long> {

    // ============================================
    // MÉTODOS DE BÚSQUEDA PAGINADA CON RATINGS
    // ============================================

    /**
     * Obtiene todos los doctores con su rating y número de reviews
     * Ordena por rating, reviews o relevancia según el parámetro orderBy
     */
    @Query(value = "SELECT d.id_doctor, du.nombre, du.apellido, du.url_imagen, " +
            "e.nombre as especialidad, COALESCE(AVG(od.estrellas), 0) as rating, " +
            "COUNT(od.id_opinion) as reviews " +
            "FROM doctores d " +
            "INNER JOIN detalles_usuario du ON d.id_detalles_usuario = du.id_detalles_usuario " +
            "LEFT JOIN especialidad e ON d.id_especialidad = e.id_especialidad " +
            "LEFT JOIN opiniones_doctores od ON d.id_doctor = od.id_doctor " +
            "GROUP BY d.id_doctor, du.nombre, du.apellido, du.url_imagen, e.nombre " +
            "ORDER BY " +
            "CASE WHEN :orderBy = 'rating' THEN COALESCE(AVG(od.estrellas), 0) END DESC, " +
            "CASE WHEN :orderBy = 'reviews' THEN COUNT(od.id_opinion) END DESC, " +
            "CASE WHEN :orderBy = 'relevancia' THEN (COALESCE(AVG(od.estrellas), 0) * 0.7 + (COUNT(od.id_opinion) * 0.3)) END DESC",
            nativeQuery = true)
    Page<Object[]> findAllDoctores(@Param("orderBy") String orderBy, Pageable pageable);

    /**
     * Busca doctores por nombre (nombre o apellido)
     */
    @Query(value = "SELECT d.id_doctor, du.nombre, du.apellido, du.url_imagen, " +
            "e.nombre as especialidad, COALESCE(AVG(od.estrellas), 0) as rating, " +
            "COUNT(od.id_opinion) as reviews " +
            "FROM doctores d " +
            "INNER JOIN detalles_usuario du ON d.id_detalles_usuario = du.id_detalles_usuario " +
            "LEFT JOIN especialidad e ON d.id_especialidad = e.id_especialidad " +
            "LEFT JOIN opiniones_doctores od ON d.id_doctor = od.id_doctor " +
            "WHERE LOWER(CONCAT(du.nombre, ' ', du.apellido)) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "GROUP BY d.id_doctor, du.nombre, du.apellido, du.url_imagen, e.nombre " +
            "ORDER BY " +
            "CASE WHEN :orderBy = 'rating' THEN COALESCE(AVG(od.estrellas), 0) END DESC, " +
            "CASE WHEN :orderBy = 'reviews' THEN COUNT(od.id_opinion) END DESC, " +
            "CASE WHEN :orderBy = 'relevancia' THEN (COALESCE(AVG(od.estrellas), 0) * 0.7 + (COUNT(od.id_opinion) * 0.3)) END DESC",
            nativeQuery = true)
    Page<Object[]> findByNombreContaining(@Param("search") String search,
                                          @Param("orderBy") String orderBy,
                                          Pageable pageable);

    /**
     * Busca doctores por especialidad
     */
    @Query(value = "SELECT d.id_doctor, du.nombre, du.apellido, du.url_imagen, " +
            "e.nombre as especialidad, COALESCE(AVG(od.estrellas), 0) as rating, " +
            "COUNT(od.id_opinion) as reviews " +
            "FROM doctores d " +
            "INNER JOIN detalles_usuario du ON d.id_detalles_usuario = du.id_detalles_usuario " +
            "LEFT JOIN especialidad e ON d.id_especialidad = e.id_especialidad " +
            "LEFT JOIN opiniones_doctores od ON d.id_doctor = od.id_doctor " +
            "WHERE d.id_especialidad = :especialidadId " +
            "GROUP BY d.id_doctor, du.nombre, du.apellido, du.url_imagen, e.nombre " +
            "ORDER BY " +
            "CASE WHEN :orderBy = 'rating' THEN COALESCE(AVG(od.estrellas), 0) END DESC, " +
            "CASE WHEN :orderBy = 'reviews' THEN COUNT(od.id_opinion) END DESC, " +
            "CASE WHEN :orderBy = 'relevancia' THEN (COALESCE(AVG(od.estrellas), 0) * 0.7 + (COUNT(od.id_opinion) * 0.3)) END DESC",
            nativeQuery = true)
    Page<Object[]> findByEspecialidadId(@Param("especialidadId") Long especialidadId,
                                        @Param("orderBy") String orderBy,
                                        Pageable pageable);

    /**
     * Busca doctores por nombre y especialidad combinados
     */
    @Query(value = "SELECT d.id_doctor, du.nombre, du.apellido, du.url_imagen, " +
            "e.nombre as especialidad, COALESCE(AVG(od.estrellas), 0) as rating, " +
            "COUNT(od.id_opinion) as reviews " +
            "FROM doctores d " +
            "INNER JOIN detalles_usuario du ON d.id_detalles_usuario = du.id_detalles_usuario " +
            "LEFT JOIN especialidad e ON d.id_especialidad = e.id_especialidad " +
            "LEFT JOIN opiniones_doctores od ON d.id_doctor = od.id_doctor " +
            "WHERE LOWER(CONCAT(du.nombre, ' ', du.apellido)) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "AND d.id_especialidad = :especialidadId " +
            "GROUP BY d.id_doctor, du.nombre, du.apellido, du.url_imagen, e.nombre " +
            "ORDER BY " +
            "CASE WHEN :orderBy = 'rating' THEN COALESCE(AVG(od.estrellas), 0) END DESC, " +
            "CASE WHEN :orderBy = 'reviews' THEN COUNT(od.id_opinion) END DESC, " +
            "CASE WHEN :orderBy = 'relevancia' THEN (COALESCE(AVG(od.estrellas), 0) * 0.7 + (COUNT(od.id_opinion) * 0.3)) END DESC",
            nativeQuery = true)
    Page<Object[]> findByNombreAndEspecialidad(@Param("search") String search,
                                               @Param("especialidadId") Long especialidadId,
                                               @Param("orderBy") String orderBy,
                                               Pageable pageable);

    // ============================================
    // MÉTODOS PARA SISTEMA DE CITAS
    // ============================================

    /**
     * Busca doctores por nombre de especialidad (case insensitive)
     * Útil para búsqueda en lenguaje natural: "cardiólogo", "pediatra", etc.
     */
    List<Doctores> findByEspecialidad_NombreContainingIgnoreCase(String especialidadNombre);



    /**
     * Busca doctores con mejor rating de una especialidad específica
     * Útil para recomendar los mejores doctores
     */
    @Query(value = "SELECT d.id_doctor, du.nombre, du.apellido, du.url_imagen, " +
            "e.nombre as especialidad, COALESCE(AVG(od.estrellas), 0) as rating, " +
            "COUNT(od.id_opinion) as reviews " +
            "FROM doctores d " +
            "INNER JOIN detalles_usuario du ON d.id_detalles_usuario = du.id_detalles_usuario " +
            "LEFT JOIN especialidad e ON d.id_especialidad = e.id_especialidad " +
            "LEFT JOIN opiniones_doctores od ON d.id_doctor = od.id_doctor " +
            "WHERE LOWER(e.nombre) LIKE LOWER(CONCAT('%', :especialidad, '%')) " +
            "GROUP BY d.id_doctor, du.nombre, du.apellido, du.url_imagen, e.nombre " +
            "HAVING COUNT(od.id_opinion) > 0 " +
            "ORDER BY COALESCE(AVG(od.estrellas), 0) DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findTopDoctoresPorEspecialidad(
            @Param("especialidad") String especialidad,
            @Param("limit") int limit
    );

    /**
     * Cuenta cuántos doctores hay de una especialidad
     */
    @Query("SELECT COUNT(d) FROM Doctores d " +
            "WHERE LOWER(d.especialidad.nombre) LIKE LOWER(CONCAT('%', :especialidad, '%'))")
    Long countByEspecialidad(@Param("especialidad") String especialidad);

    /**
     * Busca doctores que NO tengan citas en una fecha y hora específica
     * Útil para encontrar doctores realmente disponibles
     */
    @Query("SELECT DISTINCT d FROM Doctores d " +
            "WHERE d.idDoctor NOT IN (" +
            "  SELECT c.doctor.idDoctor FROM Citas c " +
            "  WHERE c.fecha = :fecha " +
            "  AND c.hora = :hora " +
            "  AND c.estado != 'cancelada'" +
            ")")
    List<Doctores> findDoctoresSinCitasEnFechaHora(
            @Param("fecha") java.time.LocalDate fecha,
            @Param("hora") java.time.LocalTime hora
    );

    /**
     * Busca doctores por especialidad que NO tengan citas en una fecha y hora
     * Combina disponibilidad de horario con ausencia de citas
     */
    @Query("SELECT DISTINCT d FROM Doctores d " +
            "JOIN d.especialidad e " +
            "WHERE LOWER(e.nombre) LIKE LOWER(CONCAT('%', :especialidad, '%')) " +
            "AND d.idDoctor NOT IN (" +
            "  SELECT c.doctor.idDoctor FROM Citas c " +
            "  WHERE c.fecha = :fecha " +
            "  AND c.hora = :hora " +
            "  AND c.estado != 'cancelada'" +
            ")")
    List<Doctores> findDoctoresDisponiblesPorEspecialidadFechaHora(
            @Param("especialidad") String especialidad,
            @Param("fecha") java.time.LocalDate fecha,
            @Param("hora") java.time.LocalTime hora
    );
}