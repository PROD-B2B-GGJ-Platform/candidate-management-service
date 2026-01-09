-- Add object_type_id column to ggj_candidates table for metamodel linking
-- This links runtime candidate data to the metamodel object type definitions

-- Step 1: Add object_type_id column (nullable initially, will be populated then made NOT NULL)
ALTER TABLE ggj_candidates 
ADD COLUMN IF NOT EXISTS object_type_id UUID;

-- Step 2: Populate object_type_id
-- Object Type Code: 'CANDIDATE' (object_type_id: 11200000-0000-0000-0000-000000000000)
-- Verified: This UUID matches SeedDataLoader.java createObjectType("11200000-0000-0000-0000-000000000000", "CANDIDATE", ...)
DO $$
DECLARE
    candidate_object_type_id UUID := '11200000-0000-0000-0000-000000000000'::UUID;
BEGIN
    -- Use hardcoded UUID from metamodel (SeedDataLoader.java)
    -- The metamodel object types are in gograbjob_admintool database, so we use the verified UUID directly
    UPDATE ggj_candidates
    SET object_type_id = candidate_object_type_id
    WHERE object_type_id IS NULL;
    
    RAISE NOTICE 'Populated object_type_id for CANDIDATE: %', candidate_object_type_id;
END $$;

-- Step 3: Add foreign key constraint (commented out - cross-database FK not possible)
-- Note: Foreign key to gograbjob_admintool.object_types is not possible across databases
-- The object_type_id is stored for reference but FK constraint is skipped
-- DO $$
-- BEGIN
--     ALTER TABLE ggj_candidates
--     ADD CONSTRAINT fk_candidates_object_type
--     FOREIGN KEY (object_type_id)
--     REFERENCES platform_services.ggj_object_types(object_type_id);
-- END $$;

-- Step 4: Create index for fast lookups
CREATE INDEX IF NOT EXISTS idx_candidates_object_type ON ggj_candidates(object_type_id);

-- Step 5: Make column NOT NULL after population (only if all records have been populated)
-- Uncomment this after verifying all records are populated:
-- ALTER TABLE ggj_candidates ALTER COLUMN object_type_id SET NOT NULL;

COMMENT ON COLUMN ggj_candidates.object_type_id IS 'Links to platform_services.ggj_object_types for metamodel integration';