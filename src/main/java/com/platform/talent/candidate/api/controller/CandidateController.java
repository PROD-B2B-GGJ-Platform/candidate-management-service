package com.platform.talent.candidate.api.controller;

import com.platform.talent.candidate.api.dto.*;
import com.platform.talent.candidate.domain.model.Candidate;
import com.platform.talent.candidate.domain.model.CandidateStatus;
import com.platform.talent.candidate.domain.model.PipelineStage;
import com.platform.talent.candidate.search.CandidateDocument;
import com.platform.talent.candidate.service.CandidateSearchService;
import com.platform.talent.candidate.service.CandidateService;
import com.platform.talent.candidate.service.LinkedInSourcingService;
import com.platform.talent.candidate.service.InternalDatabaseSourcingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/candidates")
@Tag(name = "Candidate Management", description = "Candidate profile and pipeline management API")
@Slf4j
@CrossOrigin(originPatterns = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3005", "http://127.0.0.1:3005"}, maxAge = 3600, allowCredentials = "true")
public class CandidateController {

    private final CandidateService candidateService;
    private final LinkedInSourcingService linkedInSourcingService;
    private final InternalDatabaseSourcingService internalDatabaseSourcingService;
    
    @Autowired(required = false)
    private CandidateSearchService searchService;
    
    public CandidateController(
        CandidateService candidateService,
        LinkedInSourcingService linkedInSourcingService,
        InternalDatabaseSourcingService internalDatabaseSourcingService
    ) {
        this.candidateService = candidateService;
        this.linkedInSourcingService = linkedInSourcingService;
        this.internalDatabaseSourcingService = internalDatabaseSourcingService;
    }

    @PostMapping
    @Operation(summary = "Create a new candidate")
    public ResponseEntity<CandidateResponse> createCandidate(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @Valid @RequestBody CreateCandidateRequest request) {
        CandidateResponse response = candidateService.createCandidate(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get candidate by ID")
    public ResponseEntity<CandidateResponse> getCandidate(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID id) {
        CandidateResponse response = candidateService.getCandidate(tenantId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List all candidates")
    public ResponseEntity<Page<CandidateResponse>> listCandidates(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @RequestParam(required = false) CandidateStatus status,
            Pageable pageable) {
        try {
            log.info("Listing candidates for tenant: {}, status: {}, page: {}, size: {}", 
                    tenantId, status, pageable.getPageNumber(), pageable.getPageSize());
            Page<CandidateResponse> response = candidateService.listCandidates(tenantId, status, pageable);
            log.info("Found {} candidates", response.getTotalElements());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error listing candidates for tenant: {}", tenantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/search")
    @Operation(summary = "Advanced search candidates")
    public ResponseEntity<Page<CandidateDocument>> searchCandidates(
            @RequestBody CandidateSearchCriteria criteria,
            Pageable pageable) {
        Page<CandidateDocument> response = searchService.searchCandidates(criteria, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{id}/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload and parse resume")
    public ResponseEntity<CandidateResponse> uploadResume(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        CandidateResponse response = candidateService.uploadResume(tenantId, id, file);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/stage")
    @Operation(summary = "Move candidate to pipeline stage")
    public ResponseEntity<Void> moveToPipelineStage(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID id,
            @RequestParam PipelineStage stage) {
        candidateService.moveToPipelineStage(tenantId, id, stage);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete candidate")
    public ResponseEntity<Void> deleteCandidate(
            @RequestHeader("X-Tenant-ID") UUID tenantId,
            @PathVariable UUID id) {
        candidateService.deleteCandidate(tenantId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "candidate-management-service",
            "version", "10.0.0.1"
        ));
    }

    // ==================== SOURCING ENDPOINTS ====================
    
    @GetMapping("/sourcing/linkedin/status")
    @Operation(summary = "Check LinkedIn connection status")
    public ResponseEntity<Map<String, Object>> getLinkedInStatus(
        @RequestHeader("X-User-ID") String userId
    ) {
        boolean isConnected = linkedInSourcingService.isLinkedInConnected(userId);
        return ResponseEntity.ok(Map.of(
            "connected", isConnected,
            "authUrl", isConnected ? null : linkedInSourcingService.getLinkedInAuthUrl("http://localhost:3005/callback/linkedin")
        ));
    }
    
    @PostMapping("/sourcing/linkedin/connect")
    @Operation(summary = "Connect LinkedIn account (OAuth callback)")
    public ResponseEntity<Map<String, String>> connectLinkedIn(
        @RequestParam String code,
        @RequestParam String redirectUri
    ) {
        String accessToken = linkedInSourcingService.exchangeCodeForToken(code, redirectUri);
        return ResponseEntity.ok(Map.of("accessToken", accessToken, "status", "connected"));
    }
    
    @PostMapping("/sourcing/linkedin/search")
    @Operation(summary = "Search LinkedIn for candidates")
    public ResponseEntity<List<LinkedInCandidateProfile>> searchLinkedIn(
        @RequestHeader("X-Tenant-ID") UUID tenantId,
        @RequestHeader(value = "X-LinkedIn-Token", required = false) String accessToken,
        @RequestBody LinkedInSearchRequest request
    ) {
        // Use provided token or get from session
        String token = accessToken != null ? accessToken : "mock_token";
        List<LinkedInCandidateProfile> results = linkedInSourcingService.searchCandidates(request, token);
        return ResponseEntity.ok(results);
    }
    
    @PostMapping("/sourcing/linkedin/import")
    @Operation(summary = "Import LinkedIn candidate to pipeline")
    public ResponseEntity<CandidateResponse> importLinkedInCandidate(
        @RequestHeader("X-Tenant-ID") UUID tenantId,
        @RequestParam(required = false) String requisitionId,
        @RequestBody LinkedInCandidateProfile profile
    ) {
        // Convert LinkedIn profile to CreateCandidateRequest
        CreateCandidateRequest request = CreateCandidateRequest.builder()
            .firstName(profile.getFirstName())
            .lastName(profile.getLastName())
            .email(profile.getEmail())
            .phone(profile.getPhone())
            .location(profile.getLocation())
            .currentCompany(profile.getCurrentCompany())
            .currentPosition(profile.getCurrentTitle())
            .yearsOfExperience(profile.getYearsOfExperience())
            .summary(profile.getSummary())
            .skills(profile.getSkills())
            .linkedinUrl(profile.getLinkedInUrl())
            .source("LINKEDIN")
            .build();
        
        CandidateResponse response = candidateService.createCandidate(tenantId, request);
        
        // If requisitionId is provided, create an application
        if (requisitionId != null) {
            // TODO: Create application linking candidate to requisition
            log.info("Candidate {} imported and linked to requisition {}", response.getId(), requisitionId);
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/sourcing/internal/search")
    @Operation(summary = "Search internal candidate database")
    public ResponseEntity<Page<CandidateResponse>> searchInternalDatabase(
        @RequestHeader("X-Tenant-ID") UUID tenantId,
        @RequestBody InternalDatabaseSearchRequest request
    ) {
        org.springframework.data.domain.Page<com.platform.talent.candidate.domain.model.Candidate> candidates = internalDatabaseSourcingService.searchCandidates(request, tenantId);
        org.springframework.data.domain.Page<CandidateResponse> response = candidates.map(c -> {
            // Convert Candidate to CandidateResponse
            return CandidateResponse.builder()
                .id(c.getId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .location(c.getLocation())
                .currentCompany(c.getCurrentCompany())
                .currentPosition(c.getCurrentPosition())
                .yearsOfExperience(c.getYearsOfExperience())
                .summary(c.getSummary())
                .skills(c.getSkills())
                .linkedinUrl(c.getLinkedinUrl())
                .source(c.getSource())
                .status(c.getStatus())
                .pipelineStage(c.getPipelineStage())
                .build();
        });
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/sourcing/internal/import")
    @Operation(summary = "Import internal candidate to requisition pipeline")
    public ResponseEntity<Map<String, String>> importInternalCandidate(
        @RequestHeader("X-Tenant-ID") UUID tenantId,
        @RequestParam String candidateId,
        @RequestParam String requisitionId
    ) {
        // Move candidate to APPLIED stage and link to requisition
        candidateService.moveToPipelineStage(tenantId, UUID.fromString(candidateId), PipelineStage.APPLIED);
        
        // TODO: Create application linking candidate to requisition
        log.info("Candidate {} imported to requisition {} pipeline", candidateId, requisitionId);
        
        return ResponseEntity.ok(Map.of(
            "status", "imported",
            "candidateId", candidateId,
            "requisitionId", requisitionId
        ));
    }

    // Explicit OPTIONS handler for CORS preflight requests
    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> handleOptions() {
        return ResponseEntity.ok().build();
    }
}

