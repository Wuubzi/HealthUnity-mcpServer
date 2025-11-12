package com.healthUnity.mcpServer.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedDoctorResponse {
    private List<DoctorDTOResponse> content;
    private Integer currentPage;
    private Long totalItems;
    private Integer totalPages;
    private Boolean hasNext;
    private Boolean hasPrevious;
    private Integer itemsPerPage;
}