package com.healthUnity.mcpServer.DTO;

// Archivo: FavoritoDoctorProjection.java
public interface FavoritoDoctorProjection {
    Long getIdFavorito();
    Long getIdDoctor();
    String getNombre();
    String getApellido();
    String getDoctor_image();
    String getEspecialidad();
    Double getRating();
    Integer getNumber_reviews(); // Usamos Integer para manejar nulls
}
