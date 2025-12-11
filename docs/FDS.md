# Candidate Management Service - Functional Design Specification (FDS)

## Document Information

| Field | Value |
|-------|-------|
| Document Type | Functional Design Specification |
| Version | 10.0.0.1 |
| Last Updated | 2025-12-11 |
| Status | Approved |
| Owner | Talent & Recruitment Team |

---

## 1. Introduction

### 1.1 Purpose

This document describes the functional requirements for the Candidate Management Service, which provides comprehensive candidate lifecycle management within the Talent & Recruitment Cluster.

### 1.2 Scope

- Candidate profile management
- Resume parsing and storage
- Advanced candidate search
- Pipeline stage tracking
- Bulk operations

---

## 2. Functional Requirements

### 2.1 Candidate Management

#### FR-001: Create Candidate

| Attribute | Description |
|-----------|-------------|
| Description | Create a new candidate profile |
| Actors | Recruiter |
| Preconditions | User authenticated with CREATE permission |
| Postconditions | Candidate created, indexed in Elasticsearch |

**Required Fields:**
- firstName
- lastName
- email (unique per organization)

**Optional Fields:**
- phone, currentTitle, currentCompany, location
- yearsExperience, skills[], salaryExpectation
- linkedinUrl, source

---

#### FR-002: Search Candidates

| Attribute | Description |
|-----------|-------------|
| Description | Search candidates using multiple criteria |
| Actors | All authenticated users |
| Preconditions | User authenticated |
| Postconditions | Paginated results returned |

**Search Criteria:**

| Criteria | Type | Description |
|----------|------|-------------|
| keyword | String | Full-text search |
| skills | String[] | Skills to match |
| yearsExperienceMin | Integer | Minimum experience |
| yearsExperienceMax | Integer | Maximum experience |
| location | String | Location filter |
| salaryMin | Decimal | Minimum salary |
| salaryMax | Decimal | Maximum salary |
| availability | String[] | Availability status |
| pipelineStage | String[] | Current pipeline stage |
| status | String[] | Candidate status |

---

#### FR-003: Upload Resume

| Attribute | Description |
|-----------|-------------|
| Description | Upload and parse candidate resume |
| Actors | Recruiter |
| Preconditions | Candidate exists |
| Postconditions | Resume stored, parsed data extracted |

**Supported Formats:**
- PDF
- DOCX
- DOC
- TXT

**Parsed Data:**
- Contact information
- Work experience
- Education
- Skills
- Certifications

---

#### FR-004: Update Pipeline Stage

| Attribute | Description |
|-----------|-------------|
| Description | Move candidate to different pipeline stage |
| Actors | Recruiter, HR Manager |
| Preconditions | Candidate exists |
| Postconditions | Stage updated, event published |

**Pipeline Stages:**
1. NEW
2. SOURCED
3. APPLIED
4. SCREENING
5. PHONE_SCREEN
6. INTERVIEW
7. OFFER
8. HIRED
9. REJECTED

---

### 2.2 Bulk Operations

#### FR-005: Bulk Import

| Attribute | Description |
|-----------|-------------|
| Description | Import multiple candidates via CSV |
| Actors | Recruiter, Admin |
| Preconditions | Valid CSV file |
| Postconditions | Candidates created, errors reported |

**CSV Format:**
```csv
firstName,lastName,email,phone,skills,yearsExperience
John,Doe,john@email.com,555-1234,"Java,Python",5
Jane,Smith,jane@email.com,555-5678,"React,Node.js",3
```

---

## 3. Non-Functional Requirements

### 3.1 Performance

| Requirement | Target |
|-------------|--------|
| Search Response | < 500ms (p95) |
| Create/Update | < 200ms |
| Bulk Import | 1000 records/minute |

### 3.2 Data Retention

- Active candidates: Indefinite
- Rejected candidates: 2 years
- Withdrawn candidates: 1 year
- Resume files: 3 years

---

## 4. Integration Points

### 4.1 Inbound

| Source | Purpose |
|--------|---------|
| Career Site | Candidate applications |
| Job Boards | Imported candidates |
| LinkedIn | Profile imports |

### 4.2 Outbound

| Target | Purpose |
|--------|---------|
| Application Tracking | Link to applications |
| Interview Service | Schedule interviews |
| Email Service | Notifications |

---

## 5. Acceptance Criteria

### 5.1 Search

- [ ] Full-text search returns relevant results
- [ ] Skill matching works with partial matches
- [ ] Filters can be combined
- [ ] Pagination works correctly
- [ ] Results sorted by relevance

### 5.2 Resume Parsing

- [ ] PDF files parsed correctly
- [ ] Skills extracted accurately (>80%)
- [ ] Contact info extracted
- [ ] Large files handled gracefully

---

## 6. Glossary

| Term | Definition |
|------|------------|
| Candidate | A person being considered for a job |
| Pipeline Stage | Current status in hiring process |
| Source | How the candidate was found |

