package com.platform.talent.candidate.api.controller;

import com.platform.talent.candidate.api.dto.*;
import com.platform.talent.candidate.domain.model.CandidateStatus;
import com.platform.talent.candidate.domain.model.PipelineStage;
import com.platform.talent.candidate.search.CandidateDocument;
import com.platform.talent.candidate.service.CandidateSearchService;
import com.platform.talent.candidate.service.CandidateService;
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

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/candidates")
@Tag(name = "Candidate Management", description = "Candidate profile and pipeline management API")
public class CandidateController {

    private final CandidateService candidateService;
    
    @Autowired(required = false)
    private CandidateSearchService searchService;
    
    public CandidateController(CandidateService candidateService) {
        this.candidateService = candidateService;
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
        Page<CandidateResponse> response = candidateService.listCandidates(tenantId, status, pageable);
        return ResponseEntity.ok(response);
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
}

