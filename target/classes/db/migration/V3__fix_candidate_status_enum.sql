-- Fix candidate status enum values
-- Maps stage values to status and normalizes invalid status values

DO $$
BEGIN
    -- Update NULL status values based on stage
    UPDATE ggj_candidates
    SET status = CASE
        WHEN pipeline_stage = 'APPLIED' THEN 'NEW'
        WHEN pipeline_stage = 'SCREENING' OR pipeline_stage = 'PHONE_SCREEN' THEN 'SCREENING'
        WHEN pipeline_stage = 'TECHNICAL_ASSESSMENT' THEN 'SCREENING'
        WHEN pipeline_stage = 'INTERVIEW_SCHEDULED' OR pipeline_stage = 'INTERVIEW_COMPLETED' THEN 'INTERVIEWING'
        WHEN pipeline_stage = 'REFERENCE_CHECK' OR pipeline_stage = 'BACKGROUND_CHECK' THEN 'QUALIFIED'
        WHEN pipeline_stage = 'OFFER_EXTENDED' THEN 'OFFER_EXTENDED'
        WHEN pipeline_stage = 'OFFER_ACCEPTED' THEN 'HIRED'
        WHEN pipeline_stage = 'OFFER_REJECTED' THEN 'REJECTED'
        WHEN pipeline_stage = 'REJECTED' THEN 'REJECTED'
        WHEN pipeline_stage = 'WITHDRAWN' THEN 'WITHDRAWN'
        ELSE 'NEW'
    END
    WHERE status IS NULL OR status = '';

    -- Normalize invalid status values (case-insensitive)
    UPDATE ggj_candidates
    SET status = CASE UPPER(TRIM(status))
        WHEN 'ACTIVE' THEN 'NEW'  -- Map ACTIVE to NEW
        WHEN 'NEW' THEN 'NEW'
        WHEN 'SCREENING' THEN 'SCREENING'
        WHEN 'QUALIFIED' THEN 'QUALIFIED'
        WHEN 'INTERVIEWING' THEN 'INTERVIEWING'
        WHEN 'INTERVIEW' THEN 'INTERVIEWING'  -- Map INTERVIEW to INTERVIEWING
        WHEN 'SHORTLISTED' THEN 'SHORTLISTED'
        WHEN 'OFFER_EXTENDED' THEN 'OFFER_EXTENDED'
        WHEN 'OFFER' THEN 'OFFER_EXTENDED'  -- Map OFFER to OFFER_EXTENDED
        WHEN 'HIRED' THEN 'HIRED'
        WHEN 'REJECTED' THEN 'REJECTED'
        WHEN 'WITHDRAWN' THEN 'WITHDRAWN'
        WHEN 'ON_HOLD' THEN 'ON_HOLD'
        WHEN 'ARCHIVED' THEN 'ARCHIVED'
        ELSE 'NEW'  -- Default to NEW for any unrecognized values
    END
    WHERE status IS NOT NULL 
      AND UPPER(TRIM(status)) NOT IN ('NEW', 'SCREENING', 'QUALIFIED', 'INTERVIEWING', 'SHORTLISTED', 
                                       'OFFER_EXTENDED', 'HIRED', 'REJECTED', 'WITHDRAWN', 'ON_HOLD', 'ARCHIVED');

    -- Also update based on stage if status still doesn't match enum
    UPDATE ggj_candidates
    SET status = CASE
        WHEN pipeline_stage = 'APPLIED' THEN 'NEW'
        WHEN pipeline_stage = 'SCREENING' OR pipeline_stage = 'PHONE_SCREEN' THEN 'SCREENING'
        WHEN pipeline_stage = 'TECHNICAL_ASSESSMENT' THEN 'SCREENING'
        WHEN pipeline_stage = 'INTERVIEW_SCHEDULED' OR pipeline_stage = 'INTERVIEW_COMPLETED' THEN 'INTERVIEWING'
        WHEN pipeline_stage = 'REFERENCE_CHECK' OR pipeline_stage = 'BACKGROUND_CHECK' THEN 'QUALIFIED'
        WHEN pipeline_stage = 'OFFER_EXTENDED' THEN 'OFFER_EXTENDED'
        WHEN pipeline_stage = 'OFFER_ACCEPTED' THEN 'HIRED'
        WHEN pipeline_stage = 'OFFER_REJECTED' THEN 'REJECTED'
        WHEN pipeline_stage = 'REJECTED' THEN 'REJECTED'
        WHEN pipeline_stage = 'WITHDRAWN' THEN 'WITHDRAWN'
        ELSE status  -- Keep existing status if stage doesn't match
    END
    WHERE pipeline_stage IS NOT NULL
      AND (status IS NULL OR status NOT IN ('NEW', 'SCREENING', 'QUALIFIED', 'INTERVIEWING', 'SHORTLISTED', 
                                             'OFFER_EXTENDED', 'HIRED', 'REJECTED', 'WITHDRAWN', 'ON_HOLD', 'ARCHIVED'));

    -- Ensure all candidates have a valid status (default to NEW)
    UPDATE ggj_candidates
    SET status = 'NEW'
    WHERE status IS NULL OR status = '';

    RAISE NOTICE 'Fixed candidate status values';
END $$;

-- Verify all status values are valid
DO $$
DECLARE
    invalid_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO invalid_count
    FROM ggj_candidates
    WHERE status NOT IN ('NEW', 'SCREENING', 'QUALIFIED', 'INTERVIEWING', 'SHORTLISTED', 
                         'OFFER_EXTENDED', 'HIRED', 'REJECTED', 'WITHDRAWN', 'ON_HOLD', 'ARCHIVED');
    
    IF invalid_count > 0 THEN
        RAISE WARNING 'Found % candidates with invalid status values', invalid_count;
    ELSE
        RAISE NOTICE 'All candidate status values are valid';
    END IF;
END $$;
