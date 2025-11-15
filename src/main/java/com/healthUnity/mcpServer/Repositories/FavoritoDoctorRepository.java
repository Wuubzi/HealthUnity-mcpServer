package com.healthUnity.mcpServer.Repositories;


import com.healthUnity.mcpServer.DTO.FavoritoDoctorProjection;
import com.healthUnity.mcpServer.Models.FavoritosDoctores;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoritoDoctorRepository extends JpaRepository<FavoritosDoctores, Long> {

    @Query(value = """
        SELECT
            fd.id_favorito_doctor AS idFavorito,
            d.id_doctor AS idDoctor,
            du.nombre AS nombre,
            du.apellido AS apellido,
            du.url_imagen AS doctor_image,
            e.nombre AS especialidad,
            COALESCE(AVG(od.estrellas), 0.0) AS rating,
            COUNT(od.id_opinion_doctor) AS number_reviews
        FROM 
            favoritos_doctores fd
        JOIN 
            doctores d ON d.id_doctor = fd.id_doctor
        JOIN 
            detalles_usuario du ON d.id_detalle_usuario = du.id_detalle_usuario
        JOIN 
            especialidades e ON d.id_especialidad = e.id_especialidad
        LEFT JOIN 
            opiniones_doctores od ON od.id_doctor = d.id_doctor
        WHERE 
            fd.id_paciente = :idPaciente
        GROUP BY 
            fd.id_favorito_doctor, d.id_doctor, du.nombre, du.apellido, du.url_imagen, e.nombre
        """, nativeQuery = true)
    List<FavoritoDoctorProjection> findAllFavoritosDtoByPacienteId(@Param("idPaciente") Long idPaciente);

    // En FavoritoDoctorRepository.java
    boolean existsByDoctorIdDoctorAndPacienteIdPaciente(Long idDoctor, Long idPaciente);
}
