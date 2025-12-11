package com.platform.talent.candidate.domain.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "ggj_candidates", indexes = {
    @Index(name = "idx_candidate_tenant_status", columnList = "tenant_id, status"),
    @Index(name = "idx_candidate_email", columnList = "email"),
    @Index(name = "idx_candidate_source", columnList = "source"),
    @Index(name = "idx_candidate_created", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String location;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CandidateStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "pipeline_stage", length = 50)
    private PipelineStage pipelineStage;

    @Column(length = 100)
    private String source; // LINKEDIN, REFERRAL, CAREER_SITE, DIRECT_APPLICATION

    @Column(name = "referred_by")
    private UUID referredBy;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "current_company", length = 255)
    private String currentCompany;

    @Column(name = "current_position", length = 255)
    private String currentPosition;

    @Column(name = "expected_salary")
    private Double expectedSalary;

    @Column(name = "salary_currency", length = 3)
    private String salaryCurrency;

    @Column(name = "notice_period_days")
    private Integer noticePeriodDays;

    @Type(JsonBinaryType.class)
    @Column(name = "resume_data", columnDefinition = "jsonb")
    private Map<String, Object> resumeData; // Parsed resume from AI service

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> skills;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> education;

    @Type(JsonBinaryType.class)
    @Column(name = "work_experience", columnDefinition = "jsonb")
    private List<Map<String, Object>> workExperience;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> certifications;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> languages;

    @Type(JsonBinaryType.class)
    @Column(name = "custom_fields", columnDefinition = "jsonb")
    private Map<String, Object> customFields;

    @Column(name = "resume_url", length = 500)
    private String resumeUrl;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(name = "github_url", length = 500)
    private String githubUrl;

    @Column(name = "portfolio_url", length = 500)
    private String portfolioUrl;

    @Column(name = "is_available")
    private Boolean isAvailable;

    @Column(name = "is_remote_interested")
    private Boolean isRemoteInterested;

    @Column(name = "is_relocation_interested")
    private Boolean isRelocationInterested;

    @Column(name = "rating")
    private Double rating; // 1-5 scale

    @Column(name = "last_contacted_at")
    private LocalDateTime lastContactedAt;

    @Column(name = "last_stage_change_at")
    private LocalDateTime lastStageChangeAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;

    @Version
    private Long version;

    // Business methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isActive() {
        return status != CandidateStatus.REJECTED 
            && status != CandidateStatus.WITHDRAWN 
            && status != CandidateStatus.ARCHIVED;
    }

    public boolean canMoveToPipeline() {
        return status == CandidateStatus.NEW || status == CandidateStatus.QUALIFIED;
    }

    public void updateStage(PipelineStage newStage) {
        this.pipelineStage = newStage;
        this.lastStageChangeAt = LocalDateTime.now();
    }
}

