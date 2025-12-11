package com.platform.talent.candidate.api.dto;

import com.platform.talent.candidate.domain.model.CandidateStatus;
import com.platform.talent.candidate.domain.model.PipelineStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponse {
    private UUID id;
    private UUID tenantId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String location;
    private String city;
    private String country;
    private CandidateStatus status;
    private PipelineStage pipelineStage;
    private String source;
    private UUID referredBy;
    private String summary;
    private Integer yearsOfExperience;
    private String currentCompany;
    private String currentPosition;
    private Double expectedSalary;
    private String salaryCurrency;
    private Integer noticePeriodDays;
    private Map<String, Object> resumeData;
    private List<String> skills;
    private Map<String, Object> education;
    private List<Map<String, Object>> workExperience;
    private List<String> certifications;
    private List<String> languages;
    private String resumeUrl;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    private Boolean isAvailable;
    private Boolean isRemoteInterested;
    private Boolean isRelocationInterested;
    private Double rating;
    private LocalDateTime lastContactedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

