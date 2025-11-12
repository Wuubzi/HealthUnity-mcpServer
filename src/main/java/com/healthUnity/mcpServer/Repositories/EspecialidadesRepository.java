package com.healthUnity.mcpServer.Repositories;


import com.healthUnity.mcpServer.Models.Especialidades;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EspecialidadesRepository extends JpaRepository<Especialidades, Long> {
}
