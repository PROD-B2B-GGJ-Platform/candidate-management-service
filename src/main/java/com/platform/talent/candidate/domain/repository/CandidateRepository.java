package com.platform.talent.candidate.domain.repository;

import com.platform.talent.candidate.domain.model.Candidate;
import com.platform.talent.candidate.domain.model.CandidateStatus;
import com.platform.talent.candidate.domain.model.PipelineStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, UUID> {

    Optional<Candidate> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Candidate> findByEmailAndTenantId(String email, UUID tenantId);

    Page<Candidate> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Candidate> findByTenantIdAndStatus(UUID tenantId, CandidateStatus status, Pageable pageable);

    Page<Candidate> findByTenantIdAndPipelineStage(UUID tenantId, PipelineStage stage, Pageable pageable);

    @Query("SELECT c FROM Candidate c WHERE c.tenantId = :tenantId " +
           "AND (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Candidate> searchByKeyword(@Param("tenantId") UUID tenantId,
                                     @Param("keyword") String keyword,
                                     Pageable pageable);

    @Query("SELECT c FROM Candidate c WHERE c.tenantId = :tenantId " +
           "AND c.source = :source")
    Page<Candidate> findBySource(@Param("tenantId") UUID tenantId,
                                  @Param("source") String source,
                                  Pageable pageable);

    @Query("SELECT c FROM Candidate c WHERE c.tenantId = :tenantId " +
           "AND c.status = :status " +
           "AND c.lastContactedAt < :since")
    List<Candidate> findStaleCandidates(@Param("tenantId") UUID tenantId,
                                         @Param("status") CandidateStatus status,
                                         @Param("since") LocalDateTime since);

    long countByTenantIdAndStatus(UUID tenantId, CandidateStatus status);

    long countByTenantIdAndPipelineStage(UUID tenantId, PipelineStage stage);

    @Query("SELECT COUNT(c) FROM Candidate c WHERE c.tenantId = :tenantId " +
           "AND c.createdAt >= :fromDate")
    long countNewCandidates(@Param("tenantId") UUID tenantId,
                            @Param("fromDate") LocalDateTime fromDate);

    boolean existsByEmailAndTenantId(String email, UUID tenantId);
}

