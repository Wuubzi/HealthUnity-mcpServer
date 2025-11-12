package com.healthUnity.mcpServer.Repositories;


import com.healthUnity.mcpServer.Models.OpinionesDoctores;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface Opinion_DoctoresRepository extends JpaRepository<OpinionesDoctores, Long> {
    @Query(value = "SELECT " +
            "d.id_doctor AS id, " +
            "du.nombre AS nombre_doctor, " +
            "du.apellido AS apellido_doctor, " +
            "du.url_imagen AS doctor_image," +
            "e.nombre AS especialidad, " +
            "AVG(o.estrellas) AS rating, " +
            "COUNT(o.id_opinion_doctor) AS number_reviews " +
            "FROM doctores d " +
            "JOIN opiniones_doctores o ON o.id_doctor = d.id_doctor " +
            "JOIN detalles_usuario du ON d.id_detalle_usuario = du.id_detalle_usuario " +
            "JOIN especialidades e ON d.id_especialidad = e.id_especialidad " +
            "GROUP BY d.id_doctor, du.nombre, du.apellido, du.url_imagen, e.nombre " +
            "ORDER BY rating DESC " +
            "LIMIT 5",
            nativeQuery = true)
    List<Object[]> findTop5DoctoresConRating();

    List<OpinionesDoctores> findAllByDoctor_IdDoctor(Long idDoctor);

}
