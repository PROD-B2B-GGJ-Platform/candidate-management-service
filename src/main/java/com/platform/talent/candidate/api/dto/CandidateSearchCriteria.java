package com.platform.talent.candidate.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateSearchCriteria {
    private UUID tenantId;
    private String keywords;
    private List<String> skills;
    private String location;
    private Integer minExperience;
    private Integer maxExperience;
    private Double minSalary;
    private Double maxSalary;
    private Boolean isRemoteInterested;
    private Boolean isRelocationInterested;
    private String status;
    private String pipelineStage;
}

