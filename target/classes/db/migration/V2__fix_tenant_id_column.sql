-- Fix tenant_id column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'ggj_candidates' 
        AND column_name = 'tenant_id'
    ) THEN
        ALTER TABLE ggj_candidates ADD COLUMN tenant_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000';
        -- Update existing rows with a default tenant ID (you may want to update this with actual tenant IDs)
        -- For now, we'll use a placeholder
        CREATE INDEX IF NOT EXISTS idx_candidate_tenant_status ON ggj_candidates(tenant_id, status);
    END IF;
END $$;
