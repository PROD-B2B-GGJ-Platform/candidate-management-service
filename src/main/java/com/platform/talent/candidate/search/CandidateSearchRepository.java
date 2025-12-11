package com.platform.talent.candidate.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CandidateSearchRepository extends ElasticsearchRepository<CandidateDocument, String> {

    Page<CandidateDocument> findByTenantId(UUID tenantId, Pageable pageable);

    Page<CandidateDocument> findByTenantIdAndSkillsIn(UUID tenantId, List<String> skills, Pageable pageable);

    Page<CandidateDocument> findByTenantIdAndLocationContaining(UUID tenantId, String location, Pageable pageable);

    Page<CandidateDocument> findByTenantIdAndYearsOfExperienceGreaterThanEqual(
            UUID tenantId, Integer minExperience, Pageable pageable);

    Page<CandidateDocument> findByTenantIdAndIsRemoteInterestedTrue(UUID tenantId, Pageable pageable);
}

