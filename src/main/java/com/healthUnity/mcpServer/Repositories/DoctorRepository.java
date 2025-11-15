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
    // CONSULTAS PAGINADAS IGUAL A LA VERSION BUENA
    // ============================================

    @Query(value = """
        SELECT 
            d.id_doctor,
            du.nombre,
            du.apellido,
            du.url_imagen,
            e.nombre AS especialidad,
            COALESCE(AVG(od.estrellas), 0.0) AS rating,
            COALESCE(COUNT(od.id_opinion_doctor), 0) AS reviews
        FROM doctores d
        JOIN detalles_usuario du ON d.id_detalle_usuario = du.id_detalle_usuario
        JOIN especialidades e ON d.id_especialidad = e.id_especialidad
        LEFT JOIN opiniones_doctores od ON d.id_doctor = od.id_doctor
        GROUP BY 
            d.id_doctor, du.nombre, du.apellido, du.url_imagen, e.nombre, d.experiencia
        ORDER BY 
            CASE WHEN :orderBy = 'rating' THEN COALESCE(AVG(od.estrellas), 0.0) END DESC,
            CASE WHEN :orderBy = 'reviews' THEN COALESCE(COUNT(od.id_opinion_doctor), 0) END DESC,
            CASE WHEN :orderBy = 'relevancia' THEN d.experiencia END DESC
        """,
            countQuery = """
        SELECT COUNT(DISTINCT d.id_doctor)
        FROM doctores d
        """,
            nativeQuery = true)
    Page<Object[]> findAllDoctores(
            @Param("orderBy") String orderBy,
            Pageable pageable
    );

    // ============================================
    // BUSQUEDA POR NOMBRE
    // ============================================

    @Query(value = """
        SELECT 
            d.id_doctor,
            du.nombre,
            du.apellido,
            du.url_imagen,
            e.nombre AS especialidad,
            COALESCE(AVG(od.estrellas), 0.0) AS rating,
            COALESCE(COUNT(od.id_opinion_doctor), 0) AS reviews
        FROM doctores d
        JOIN detalles_usuario du ON d.id_detalle_usuario = du.id_detalle_usuario
        JOIN especialidades e ON d.id_especialidad = e.id_especialidad
        LEFT JOIN opiniones_doctores od ON d.id_doctor = od.id_doctor
        WHERE 
            LOWER(du.nombre) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(du.apellido) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(CONCAT(du.nombre, ' ', du.apellido)) LIKE LOWER(CONCAT('%', :search, '%'))
        GROUP BY 
            d.id_doctor, du.nombre, du.apellido, du.url_imagen, e.nombre, d.experiencia
        ORDER BY 
            CASE WHEN :orderBy = 'rating' THEN COALESCE(AVG(od.estrellas), 0.0) END DESC,
            CASE WHEN :orderBy = 'reviews' THEN COALESCE(COUNT(od.id_opinion_doctor), 0) END DESC,
            CASE WHEN :orderBy = 'relevancia' THEN d.experiencia END DESC
        """,
            countQuery = """
        SELECT COUNT(DISTINCT d.id_doctor)
        FROM doctores d
        JOIN detalles_usuario du ON d.id_detalle_usuario = du.id_detalle_usuario
        WHERE LOWER(du.nombre) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(du.apellido) LIKE LOWER(CONCAT('%', :search, '%'))
        """,
            nativeQuery = true)
    Page<Object[]> findByNombreContaining(
            @Param("search") String search,
            @Param("orderBy") String orderBy,
            Pageable pageable
    );

    // ============================================
    // FILTRO POR ESPECIALIDAD
    // ============================================

    @Query(value = """
        SELECT 
            d.id_doctor,
            du.nombre,
            du.apellido,
            du.url_imagen,
            e.nombre AS especialidad,
            COALESCE(AVG(od.estrellas), 0.0) AS rating,
            COALESCE(COUNT(od.id_opinion_doctor), 0) AS reviews
        FROM doctores d
        JOIN detalles_usuario du ON d.id_detalle_usuario = du.id_detalle_usuario
        JOIN especialidades e ON d.id_especialidad = e.id_especialidad
        LEFT JOIN opiniones_doctores od ON d.id_doctor = od.id_doctor
        WHERE e.id_especialidad = :especialidadId
        GROUP BY 
            d.id_doctor, du.nombre, du.apellido, du.url_imagen, e.nombre, d.experiencia
        ORDER BY 
            CASE WHEN :orderBy = 'rating' THEN COALESCE(AVG(od.estrellas), 0.0) END DESC,
            CASE WHEN :orderBy = 'reviews' THEN COALESCE(COUNT(od.id_opinion_doctor), 0) END DESC,
            CASE WHEN :orderBy = 'relevancia' THEN d.experiencia END DESC
        """,
            countQuery = """
        SELECT COUNT(DISTINCT d.id_doctor)
        FROM doctores d
        WHERE d.id_especialidad = :especialidadId
        """,
            nativeQuery = true)
    Page<Object[]> findByEspecialidadId(
            @Param("especialidadId") Long especialidadId,
            @Param("orderBy") String orderBy,
            Pageable pageable
    );

    // ============================================
    // BUSCAR POR NOMBRE + ESPECIALIDAD
    // ============================================

    @Query(value = """
        SELECT 
            d.id_doctor,
            du.nombre,
            du.apellido,
            du.url_imagen,
            e.nombre AS especialidad,
            COALESCE(AVG(od.estrellas), 0.0) AS rating,
            COALESCE(COUNT(od.id_opinion_doctor), 0) AS reviews
        FROM doctores d
        JOIN detalles_usuario du ON d.id_detalle_usuario = du.id_detalle_usuario
        JOIN especialidades e ON d.id_especialidad = e.id_especialidad
        LEFT JOIN opiniones_doctores od ON d.id_doctor = od.id_doctor
        WHERE 
            (LOWER(du.nombre) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(du.apellido) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(CONCAT(du.nombre, ' ', du.apellido)) LIKE LOWER(CONCAT('%', :search, '%')))
            AND e.id_especialidad = :especialidadId
        GROUP BY 
            d.id_doctor, du.nombre, du.apellido, du.url_imagen, e.nombre, d.experiencia
        ORDER BY 
            CASE WHEN :orderBy = 'rating' THEN COALESCE(AVG(od.estrellas), 0.0) END DESC,
            CASE WHEN :orderBy = 'reviews' THEN COALESCE(COUNT(od.id_opinion_doctor), 0) END DESC,
            CASE WHEN :orderBy = 'relevancia' THEN d.experiencia END DESC
        """,
            countQuery = """
        SELECT COUNT(DISTINCT d.id_doctor)
        FROM doctores d
        JOIN detalles_usuario du ON d.id_detalle_usuario = du.id_detalle_usuario
        WHERE 
            (LOWER(du.nombre) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(du.apellido) LIKE LOWER(CONCAT('%', :search, '%')))
            AND d.id_especialidad = :especialidadId
        """,
            nativeQuery = true)
    Page<Object[]> findByNombreAndEspecialidad(
            @Param("search") String search,
            @Param("especialidadId") Long especialidadId,
            @Param("orderBy") String orderBy,
            Pageable pageable
    );

    // ============================================
    // CONSULTAS EXTRA
    // ============================================

    List<Doctores> findByEspecialidad_NombreContainingIgnoreCase(String especialidadNombre);

    @Query(value = """
        SELECT 
            d.id_doctor,
            du.nombre,
            du.apellido,
            du.url_imagen,
            e.nombre AS especialidad,
            COALESCE(AVG(od.estrellas), 0.0) AS rating,
            COALESCE(COUNT(od.id_opinion_doctor), 0) AS reviews
        FROM doctores d
        JOIN detalles_usuario du ON d.id_detalle_usuario = du.id_detalle_usuario
        JOIN especialidades e ON d.id_especialidad = e.id_especialidad
        LEFT JOIN opiniones_doctores od ON d.id_doctor = od.id_doctor
        WHERE LOWER(e.nombre) LIKE LOWER(CONCAT('%', :especialidad, '%'))
        GROUP BY 
            d.id_doctor, du.nombre, du.apellido, du.url_imagen, e.nombre, d.experiencia
        HAVING COUNT(od.id_opinion_doctor) > 0
        ORDER BY COALESCE(AVG(od.estrellas), 0.0) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTopDoctoresPorEspecialidad(
            @Param("especialidad") String especialidad,
            @Param("limit") int limit
    );

    @Query("SELECT COUNT(d) FROM Doctores d WHERE LOWER(d.especialidad.nombre) LIKE LOWER(CONCAT('%', :especialidad, '%'))")
    Long countByEspecialidad(@Param("especialidad") String especialidad);

    // ============================================
    // DOCTORES SIN CITAS Y DISPONIBLES
    // ============================================

    @Query("""
        SELECT DISTINCT d FROM Doctores d
        WHERE d.idDoctor NOT IN (
          SELECT c.doctor.idDoctor FROM Citas c
          WHERE c.fecha = :fecha
          AND c.hora = :hora
          AND c.estado != 'cancelada'
        )
        """)
    List<Doctores> findDoctoresSinCitasEnFechaHora(
            @Param("fecha") java.time.LocalDate fecha,
            @Param("hora") java.time.LocalTime hora
    );

    @Query("""
        SELECT DISTINCT d FROM Doctores d
        JOIN d.especialidad e
        WHERE LOWER(e.nombre) LIKE LOWER(CONCAT('%', :especialidad, '%'))
        AND d.idDoctor NOT IN (
          SELECT c.doctor.idDoctor FROM Citas c
          WHERE c.fecha = :fecha
          AND c.hora = :hora
          AND c.estado != 'cancelada'
        )
        """)
    List<Doctores> findDoctoresDisponiblesPorEspecialidadFechaHora(
            @Param("especialidad") String especialidad,
            @Param("fecha") java.time.LocalDate fecha,
            @Param("hora") java.time.LocalTime hora
    );


}
