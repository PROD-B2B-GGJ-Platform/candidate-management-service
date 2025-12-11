package com.platform.talent.candidate.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCandidateRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 20)
    private String phone;

    private String location;
    private String city;
    private String country;
    private String source;
    private UUID referredBy;
    private String summary;
    private Integer yearsOfExperience;
    private String currentCompany;
    private String currentPosition;
    private Double expectedSalary;
    private String salaryCurrency;
    private Integer noticePeriodDays;
    private List<String> skills;
    private Map<String, Object> education;
    private List<Map<String, Object>> workExperience;
    private List<String> certifications;
    private List<String> languages;
    private Map<String, Object> customFields;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    private Boolean isAvailable;
    private Boolean isRemoteInterested;
    private Boolean isRelocationInterested;
}

