-- Candidate Management Service - Initial Schema
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE ggj_candidates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    location VARCHAR(255),
    city VARCHAR(100),
    country VARCHAR(100),
    status VARCHAR(50) NOT NULL,
    pipeline_stage VARCHAR(50),
    source VARCHAR(100),
    referred_by UUID,
    summary TEXT,
    years_of_experience INTEGER,
    current_company VARCHAR(255),
    current_position VARCHAR(255),
    expected_salary NUMERIC(15, 2),
    salary_currency VARCHAR(3),
    notice_period_days INTEGER,
    resume_data JSONB,
    skills JSONB,
    education JSONB,
    work_experience JSONB,
    certifications JSONB,
    languages JSONB,
    custom_fields JSONB,
    resume_url VARCHAR(500),
    linkedin_url VARCHAR(500),
    github_url VARCHAR(500),
    portfolio_url VARCHAR(500),
    is_available BOOLEAN DEFAULT TRUE,
    is_remote_interested BOOLEAN DEFAULT FALSE,
    is_relocation_interested BOOLEAN DEFAULT FALSE,
    rating NUMERIC(2, 1),
    last_contacted_at TIMESTAMP,
    last_stage_change_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    version BIGINT DEFAULT 0,
    CONSTRAINT uq_candidate_email_tenant UNIQUE (email, tenant_id)
);

-- Indexes
CREATE INDEX idx_candidate_tenant_status ON ggj_candidates(tenant_id, status);
CREATE INDEX idx_candidate_email ON ggj_candidates(email);
CREATE INDEX idx_candidate_source ON ggj_candidates(source);
CREATE INDEX idx_candidate_created ON ggj_candidates(created_at);
CREATE INDEX idx_candidate_stage ON ggj_candidates(pipeline_stage);
CREATE INDEX idx_candidate_skills ON ggj_candidates USING GIN(skills);

-- Full-text search
CREATE INDEX idx_candidate_name_search ON ggj_candidates USING GIN(
    to_tsvector('english', first_name || ' ' || last_name)
);

COMMENT ON TABLE ggj_candidates IS 'Candidate profiles with resume parsing and search';

