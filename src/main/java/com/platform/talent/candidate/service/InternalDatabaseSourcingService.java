package com.platform.talent.candidate.service;

import com.platform.talent.candidate.api.dto.InternalDatabaseSearchRequest;
import com.platform.talent.candidate.domain.model.Candidate;
import com.platform.talent.candidate.domain.model.CandidateStatus;
import com.platform.talent.candidate.domain.repository.CandidateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class InternalDatabaseSourcingService {

    private final CandidateRepository candidateRepository;

    public InternalDatabaseSourcingService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    /**
     * Search internal candidate database based on criteria
     */
    public Page<Candidate> searchCandidates(InternalDatabaseSearchRequest request, UUID tenantId) {
        log.info("Searching internal database for candidates with criteria: {}", request);
        
        Specification<Candidate> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Tenant filter
            predicates.add(cb.equal(root.get("tenantId"), tenantId));
            
            // Keyword search (name, email, summary)
            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String keyword = "%" + request.getKeyword().toLowerCase() + "%";
                Predicate namePredicate = cb.or(
                    cb.like(cb.lower(root.get("firstName")), keyword),
                    cb.like(cb.lower(root.get("lastName")), keyword),
                    cb.like(cb.lower(root.get("email")), keyword)
                );
                predicates.add(namePredicate);
            }
            
            // Skills filter (if skills array is provided)
            if (request.getSkills() != null && !request.getSkills().isEmpty()) {
                // Skills are stored as JSONB, so we need to check if any skill matches
                // This is a simplified version - in production, use JSONB operators
                // For now, we'll search in summary or use a more complex query
                Predicate skillsPredicate = cb.or(
                    request.getSkills().stream()
                        .map(skill -> cb.like(cb.lower(root.get("summary")), "%" + skill.toLowerCase() + "%"))
                        .toArray(Predicate[]::new)
                );
                predicates.add(skillsPredicate);
            }
            
            // Location filter
            if (request.getLocation() != null && !request.getLocation().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + request.getLocation().toLowerCase() + "%"));
            }
            
            // Current company filter
            if (request.getCurrentCompany() != null && !request.getCurrentCompany().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("currentCompany")), "%" + request.getCurrentCompany().toLowerCase() + "%"));
            }
            
            // Current title filter
            if (request.getCurrentTitle() != null && !request.getCurrentTitle().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("currentPosition")), "%" + request.getCurrentTitle().toLowerCase() + "%"));
            }
            
            // Experience range filter
            if (request.getMinExperience() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("yearsOfExperience"), request.getMinExperience()));
            }
            if (request.getMaxExperience() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("yearsOfExperience"), request.getMaxExperience()));
            }
            
            // Status filter
            if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
                try {
                    CandidateStatus status = CandidateStatus.valueOf(request.getStatus().toUpperCase());
                    predicates.add(cb.equal(root.get("status"), status));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid status filter: {}", request.getStatus());
                }
            }
            
            // Source filter
            if (request.getSource() != null && !request.getSource().trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("source")), request.getSource().toLowerCase()));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        int pageSize = request.getMaxResults() != null ? Math.min(request.getMaxResults(), 100) : 20;
        Pageable pageable = PageRequest.of(0, pageSize);
        
        Page<Candidate> results = candidateRepository.findAll(spec, pageable);
        log.info("Found {} candidates in internal database", results.getTotalElements());
        
        return results;
    }
}
