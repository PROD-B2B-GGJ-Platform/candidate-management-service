package com.platform.talent.candidate.service;

import com.platform.talent.candidate.api.dto.*;
import com.platform.talent.candidate.domain.model.Candidate;
import com.platform.talent.candidate.domain.model.CandidateStatus;
import com.platform.talent.candidate.domain.model.PipelineStage;
import com.platform.talent.candidate.domain.repository.CandidateRepository;
import com.platform.talent.candidate.service.integration.ResumeParserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class CandidateService {

    private final CandidateRepository candidateRepository;
    
    @Autowired(required = false)
    private CandidateSearchService searchService;
    
    @Autowired(required = false)
    private ResumeParserService resumeParserService;
    
    @Autowired(required = false)
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    public CandidateService(
        CandidateRepository candidateRepository
    ) {
        this.candidateRepository = candidateRepository;
    }

    @Transactional
    public CandidateResponse createCandidate(UUID tenantId, CreateCandidateRequest request) {
        log.info("Creating candidate for tenant: {}", tenantId);

        // Check for duplicate email
        if (candidateRepository.existsByEmailAndTenantId(request.getEmail(), tenantId)) {
            throw new RuntimeException("Candidate with email " + request.getEmail() + " already exists");
        }

        Candidate candidate = Candidate.builder()
                .tenantId(tenantId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .location(request.getLocation())
                .city(request.getCity())
                .country(request.getCountry())
                .status(CandidateStatus.NEW)
                .pipelineStage(PipelineStage.APPLIED)
                .source(request.getSource())
                .referredBy(request.getReferredBy())
                .summary(request.getSummary())
                .yearsOfExperience(request.getYearsOfExperience())
                .currentCompany(request.getCurrentCompany())
                .currentPosition(request.getCurrentPosition())
                .expectedSalary(request.getExpectedSalary())
                .salaryCurrency(request.getSalaryCurrency())
                .noticePeriodDays(request.getNoticePeriodDays())
                .skills(request.getSkills())
                .education(request.getEducation())
                .workExperience(request.getWorkExperience())
                .certifications(request.getCertifications())
                .languages(request.getLanguages())
                .customFields(request.getCustomFields())
                .linkedinUrl(request.getLinkedinUrl())
                .githubUrl(request.getGithubUrl())
                .portfolioUrl(request.getPortfolioUrl())
                .isAvailable(request.getIsAvailable())
                .isRemoteInterested(request.getIsRemoteInterested())
                .isRelocationInterested(request.getIsRelocationInterested())
                .build();

        candidate = candidateRepository.save(candidate);

        // Index in Elasticsearch if available
        if (searchService != null) {
            try {
                searchService.indexCandidate(candidate);
            } catch (Exception e) {
                log.warn("Failed to index candidate in search: {}", candidate.getId(), e);
            }
        }

        // Publish Kafka event
        publishCandidateEvent("candidate.created", candidate);

        log.info("Candidate created: {}", candidate.getId());
        return mapToResponse(candidate);
    }

    @Transactional
    public CandidateResponse uploadResume(UUID tenantId, UUID candidateId, MultipartFile resume) {
        log.info("Uploading resume for candidate: {}", candidateId);

        Candidate candidate = candidateRepository.findByIdAndTenantId(candidateId, tenantId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        // Parse resume using AI service
        Map<String, Object> parsedData;
        if (resumeParserService != null) {
            parsedData = resumeParserService.parseResume(candidateId, resume);
        } else {
            log.warn("ResumeParserService not available, skipping resume parsing");
            parsedData = Map.of("status", "skipped", "message", "Resume parser service not available");
        }
        candidate.setResumeData(parsedData);

        // Extract and update fields from parsed resume
        if (parsedData.containsKey("skills")) {
            candidate.setSkills((java.util.List<String>) parsedData.get("skills"));
        }
        if (parsedData.containsKey("yearsOfExperience")) {
            candidate.setYearsOfExperience(((Number) parsedData.get("yearsOfExperience")).intValue());
        }
        if (parsedData.containsKey("education")) {
            candidate.setEducation((Map<String, Object>) parsedData.get("education"));
        }

        candidate = candidateRepository.save(candidate);

        // Re-index with updated data if available
        if (searchService != null) {
            try {
                searchService.indexCandidate(candidate);
            } catch (Exception e) {
                log.warn("Failed to re-index candidate in search: {}", candidateId, e);
            }
        }

        log.info("Resume uploaded and parsed for candidate: {}", candidateId);
        return mapToResponse(candidate);
    }

    @Transactional
    public void moveToPipelineStage(UUID tenantId, UUID candidateId, PipelineStage newStage) {
        log.info("Moving candidate {} to stage: {}", candidateId, newStage);

        Candidate candidate = candidateRepository.findByIdAndTenantId(candidateId, tenantId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        candidate.updateStage(newStage);

        // Update status based on stage
        if (newStage == PipelineStage.SCREENING || newStage == PipelineStage.PHONE_SCREEN) {
            candidate.setStatus(CandidateStatus.SCREENING);
        } else if (newStage == PipelineStage.INTERVIEW_SCHEDULED || newStage == PipelineStage.INTERVIEW_COMPLETED) {
            candidate.setStatus(CandidateStatus.INTERVIEWING);
        } else if (newStage == PipelineStage.OFFER_EXTENDED) {
            candidate.setStatus(CandidateStatus.OFFER_EXTENDED);
        } else if (newStage == PipelineStage.OFFER_ACCEPTED) {
            candidate.setStatus(CandidateStatus.HIRED);
        } else if (newStage == PipelineStage.REJECTED) {
            candidate.setStatus(CandidateStatus.REJECTED);
        } else if (newStage == PipelineStage.WITHDRAWN) {
            candidate.setStatus(CandidateStatus.WITHDRAWN);
        }

        candidateRepository.save(candidate);

        // Publish event
        publishCandidateEvent("candidate.stage.changed", candidate);

        log.info("Candidate moved to stage: {}", newStage);
    }

    @Transactional(readOnly = true)
    public CandidateResponse getCandidate(UUID tenantId, UUID candidateId) {
        Candidate candidate = candidateRepository.findByIdAndTenantId(candidateId, tenantId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));
        return mapToResponse(candidate);
    }

    @Transactional(readOnly = true)
    public Page<CandidateResponse> listCandidates(UUID tenantId, CandidateStatus status, Pageable pageable) {
        try {
            log.debug("Listing candidates for tenant: {}, status: {}, page: {}, size: {}", 
                tenantId, status, pageable.getPageNumber(), pageable.getPageSize());
            
            Page<Candidate> candidates;
            if (status != null) {
                candidates = candidateRepository.findByTenantIdAndStatus(tenantId, status, pageable);
            } else {
                candidates = candidateRepository.findByTenantId(tenantId, pageable);
            }
            
            log.debug("Found {} candidates", candidates.getTotalElements());
            return candidates.map(this::mapToResponse);
        } catch (Exception e) {
            log.error("Error listing candidates for tenant: {}", tenantId, e);
            throw e;
        }
    }

    @Transactional
    public void deleteCandidate(UUID tenantId, UUID candidateId) {
        log.info("Deleting candidate: {}", candidateId);

        Candidate candidate = candidateRepository.findByIdAndTenantId(candidateId, tenantId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        candidateRepository.delete(candidate);
        
        // Delete from search index if available
        if (searchService != null) {
            try {
                searchService.deleteFromIndex(candidateId);
            } catch (Exception e) {
                log.warn("Failed to delete candidate from search index: {}", candidateId, e);
            }
        }

        log.info("Candidate deleted: {}", candidateId);
    }

    private void publishCandidateEvent(String eventType, Candidate candidate) {
        if (kafkaTemplate == null) {
            log.debug("KafkaTemplate not available, skipping event publication: {}", eventType);
            return;
        }
        try {
            kafkaTemplate.send("talent.candidate.events", candidate.getId().toString(), Map.of(
                "eventType", eventType,
                "candidateId", candidate.getId(),
                "tenantId", candidate.getTenantId(),
                "status", candidate.getStatus(),
                "stage", candidate.getPipelineStage(),
                "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("Failed to publish candidate event: {}", eventType, e);
        }
    }

    private CandidateResponse mapToResponse(Candidate candidate) {
        try {
            if (candidate == null) {
                log.error("Cannot map null candidate to response");
                throw new IllegalArgumentException("Candidate cannot be null");
            }
            
            return CandidateResponse.builder()
                    .id(candidate.getId())
                    .tenantId(candidate.getTenantId())
                    .firstName(candidate.getFirstName())
                    .lastName(candidate.getLastName())
                    .email(candidate.getEmail())
                    .phone(candidate.getPhone())
                    .location(candidate.getLocation())
                    .city(candidate.getCity())
                    .country(candidate.getCountry())
                    .status(candidate.getStatus())
                    .pipelineStage(candidate.getPipelineStage())
                    .source(candidate.getSource())
                    .referredBy(candidate.getReferredBy())
                    .summary(candidate.getSummary())
                    .yearsOfExperience(candidate.getYearsOfExperience())
                    .currentCompany(candidate.getCurrentCompany())
                    .currentPosition(candidate.getCurrentPosition())
                    .expectedSalary(candidate.getExpectedSalary())
                    .salaryCurrency(candidate.getSalaryCurrency())
                    .noticePeriodDays(candidate.getNoticePeriodDays())
                    .resumeData(candidate.getResumeData())
                    .skills(candidate.getSkills())
                    .education(candidate.getEducation())
                    .workExperience(candidate.getWorkExperience())
                    .certifications(candidate.getCertifications())
                    .languages(candidate.getLanguages())
                    .resumeUrl(candidate.getResumeUrl())
                    .linkedinUrl(candidate.getLinkedinUrl())
                    .githubUrl(candidate.getGithubUrl())
                    .portfolioUrl(candidate.getPortfolioUrl())
                    .isAvailable(candidate.getIsAvailable())
                    .isRemoteInterested(candidate.getIsRemoteInterested())
                    .isRelocationInterested(candidate.getIsRelocationInterested())
                    .rating(candidate.getRating())
                    .lastContactedAt(candidate.getLastContactedAt())
                    .createdAt(candidate.getCreatedAt())
                    .updatedAt(candidate.getUpdatedAt())
                    .build();
        } catch (Exception e) {
            log.error("Error mapping candidate to response: {}", candidate != null ? candidate.getId() : "null", e);
            throw new RuntimeException("Failed to map candidate to response", e);
        }
    }
}

