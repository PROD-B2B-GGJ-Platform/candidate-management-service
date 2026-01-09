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
public class LinkedInCandidateProfile {
    private String linkedInId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String location;
    private String currentTitle;
    private String currentCompany;
    private Integer yearsOfExperience;
    private String summary;
    private List<String> skills;
    private String linkedInUrl;
    private String profilePictureUrl;
    private List<LinkedInExperience> experience;
    private List<LinkedInEducation> education;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkedInExperience {
        private String title;
        private String company;
        private String duration;
        private String description;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinkedInEducation {
        private String school;
        private String degree;
        private String field;
        private String year;
    }
}
