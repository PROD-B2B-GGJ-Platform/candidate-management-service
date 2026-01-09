package com.platform.talent.candidate.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternalDatabaseSearchRequest {
    private String keyword; // Search in name, email, skills, summary
    private List<String> skills;
    private String location;
    private String currentCompany;
    private String currentTitle;
    private Integer minExperience;
    private Integer maxExperience;
    private String status; // ACTIVE, INACTIVE, etc.
    private String source; // Filter by source
    private Integer maxResults;
    private String requisitionId; // Optional: link to specific requisition
}
