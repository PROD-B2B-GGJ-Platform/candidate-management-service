package com.platform.talent.candidate.service;

import com.platform.talent.candidate.api.dto.CandidateSearchCriteria;
import com.platform.talent.candidate.domain.model.Candidate;
import com.platform.talent.candidate.search.CandidateDocument;
import com.platform.talent.candidate.search.CandidateSearchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CandidateSearchService {

    @Autowired(required = false)
    private CandidateSearchRepository searchRepository;
    
    @Autowired(required = false)
    private ElasticsearchOperations elasticsearchOperations;

    public void indexCandidate(Candidate candidate) {
        if (searchRepository == null) {
            log.debug("Search repository not available, skipping indexing");
            return;
        }
        try {
            CandidateDocument document = mapToDocument(candidate);
            searchRepository.save(document);
            log.info("Indexed candidate: {}", candidate.getId());
        } catch (Exception e) {
            log.error("Failed to index candidate: {}", candidate.getId(), e);
        }
    }

    public void deleteFromIndex(UUID candidateId) {
        if (searchRepository == null) {
            log.debug("Search repository not available, skipping delete");
            return;
        }
        try {
            searchRepository.deleteById(candidateId.toString());
            log.info("Removed candidate from index: {}", candidateId);
        } catch (Exception e) {
            log.error("Failed to remove candidate from index: {}", candidateId, e);
        }
    }

    public Page<CandidateDocument> searchCandidates(CandidateSearchCriteria criteria, Pageable pageable) {
        if (elasticsearchOperations == null) {
            log.debug("Elasticsearch not available, returning empty results");
            return Page.empty(pageable);
        }
        try {
            Criteria elasticCriteria = Criteria.where("tenantId").is(criteria.getTenantId());

            if (criteria.getKeywords() != null && !criteria.getKeywords().isEmpty()) {
                elasticCriteria = elasticCriteria.and(
                    Criteria.where("firstName").contains(criteria.getKeywords())
                        .or("lastName").contains(criteria.getKeywords())
                        .or("summary").contains(criteria.getKeywords())
                        .or("currentPosition").contains(criteria.getKeywords())
                );
            }

            if (criteria.getSkills() != null && !criteria.getSkills().isEmpty()) {
                elasticCriteria = elasticCriteria.and(
                    Criteria.where("skills").in(criteria.getSkills())
                );
            }

            if (criteria.getLocation() != null) {
                elasticCriteria = elasticCriteria.and(
                    Criteria.where("location").contains(criteria.getLocation())
                );
            }

            if (criteria.getMinExperience() != null) {
                elasticCriteria = elasticCriteria.and(
                    Criteria.where("yearsOfExperience").greaterThanEqual(criteria.getMinExperience())
                );
            }

            if (criteria.getMaxExperience() != null) {
                elasticCriteria = elasticCriteria.and(
                    Criteria.where("yearsOfExperience").lessThanEqual(criteria.getMaxExperience())
                );
            }

            if (criteria.getIsRemoteInterested() != null && criteria.getIsRemoteInterested()) {
                elasticCriteria = elasticCriteria.and(
                    Criteria.where("isRemoteInterested").is(true)
                );
            }

            if (criteria.getMinSalary() != null) {
                elasticCriteria = elasticCriteria.and(
                    Criteria.where("expectedSalary").greaterThanEqual(criteria.getMinSalary())
                );
            }

            if (criteria.getMaxSalary() != null) {
                elasticCriteria = elasticCriteria.and(
                    Criteria.where("expectedSalary").lessThanEqual(criteria.getMaxSalary())
                );
            }

            CriteriaQuery query = new CriteriaQuery(elasticCriteria).setPageable(pageable);
            SearchHits<CandidateDocument> searchHits = elasticsearchOperations.search(query, CandidateDocument.class);

            List<CandidateDocument> candidates = searchHits.stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toList());

            return new PageImpl<>(candidates, pageable, searchHits.getTotalHits());
        } catch (Exception e) {
            log.error("Failed to search candidates", e);
            return Page.empty(pageable);
        }
    }

    private CandidateDocument mapToDocument(Candidate candidate) {
        return CandidateDocument.builder()
                .id(candidate.getId().toString())
                .tenantId(candidate.getTenantId())
                .firstName(candidate.getFirstName())
                .lastName(candidate.getLastName())
                .email(candidate.getEmail())
                .phone(candidate.getPhone())
                .location(candidate.getLocation())
                .city(candidate.getCity())
                .country(candidate.getCountry())
                .status(candidate.getStatus().name())
                .pipelineStage(candidate.getPipelineStage() != null ? candidate.getPipelineStage().name() : null)
                .summary(candidate.getSummary())
                .yearsOfExperience(candidate.getYearsOfExperience())
                .currentCompany(candidate.getCurrentCompany())
                .currentPosition(candidate.getCurrentPosition())
                .skills(candidate.getSkills())
                .certifications(candidate.getCertifications())
                .languages(candidate.getLanguages())
                .source(candidate.getSource())
                .expectedSalary(candidate.getExpectedSalary())
                .salaryCurrency(candidate.getSalaryCurrency())
                .isAvailable(candidate.getIsAvailable())
                .isRemoteInterested(candidate.getIsRemoteInterested())
                .isRelocationInterested(candidate.getIsRelocationInterested())
                .rating(candidate.getRating())
                .createdAt(candidate.getCreatedAt())
                .updatedAt(candidate.getUpdatedAt())
                .build();
    }
}

