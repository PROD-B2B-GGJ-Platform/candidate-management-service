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
public class LinkedInSearchRequest {
    private String jobTitle;
    private String location;
    private List<String> skills;
    private Integer minExperience;
    private Integer maxExperience;
    private String currentCompany;
    private String industry;
    private Integer maxResults;
    private String requisitionId; // Optional: link to specific requisition
}
