package com.platform.talent.candidate.domain.converter;

import com.platform.talent.candidate.domain.model.CandidateStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class CandidateStatusConverter implements AttributeConverter<CandidateStatus, String> {

    @Override
    public String convertToDatabaseColumn(CandidateStatus status) {
        if (status == null) {
            return null;
        }
        return status.name();
    }

    @Override
    public CandidateStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return CandidateStatus.NEW; // Default value
        }

        // Try exact match first
        try {
            return CandidateStatus.valueOf(dbData.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            // Handle case-insensitive matching and common mappings
            String normalized = dbData.toUpperCase().trim();
            
            // Map common variations
            switch (normalized) {
                case "ACTIVE":
                    return CandidateStatus.NEW;
                case "INTERVIEW":
                    return CandidateStatus.INTERVIEWING;
                case "OFFER":
                    return CandidateStatus.OFFER_EXTENDED;
                default:
                    // Try to find a match ignoring case
                    return Stream.of(CandidateStatus.values())
                            .filter(status -> status.name().equalsIgnoreCase(normalized))
                            .findFirst()
                            .orElse(CandidateStatus.NEW); // Default to NEW if no match found
            }
        }
    }
}
