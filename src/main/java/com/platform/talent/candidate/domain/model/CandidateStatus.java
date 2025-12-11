package com.platform.talent.candidate.domain.model;

public enum CandidateStatus {
    NEW,              // Newly added to system
    SCREENING,        // In screening process
    QUALIFIED,        // Passed screening
    INTERVIEWING,     // In interview process
    SHORTLISTED,      // Shortlisted for final consideration
    OFFER_EXTENDED,   // Offer has been extended
    HIRED,            // Successfully hired
    REJECTED,         // Rejected at any stage
    WITHDRAWN,        // Candidate withdrew
    ON_HOLD,          // Application on hold
    ARCHIVED          // Archived for records
}

