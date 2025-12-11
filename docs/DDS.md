# Candidate Management Service - Design Document Specification (DDS)

## Document Information

| Field | Value |
|-------|-------|
| Document Type | Design Document Specification |
| Version | 10.0.0.1 |
| Last Updated | 2025-12-11 |
| Status | Approved |
| Owner | Talent & Recruitment Team |

---

## 1. Overview

The Candidate Management Service is responsible for managing candidate profiles, resume parsing, and advanced search capabilities within the Talent & Recruitment Cluster.

### 1.1 Purpose

Provide comprehensive candidate lifecycle management with AI-powered search using Elasticsearch for fast, relevant candidate discovery.

### 1.2 Key Features

- Candidate CRUD operations
- Resume upload and AI parsing
- Elasticsearch-powered search
- Pipeline stage management
- Bulk operations support
- Multi-tenant data isolation

---

## 2. Architecture

### 2.1 Component Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway                               │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────────┐
│         Candidate Management Service (Port 8092)            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  Controller │  │   Service   │  │    Repository       │  │
│  │    Layer    │──│    Layer    │──│      Layer          │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│                         │                    │              │
│                    ┌────┴────┐          ┌────┴────┐         │
│                    │   ES    │          │  JPA    │         │
│                    │ Client  │          │  Repo   │         │
│                    └────┬────┘          └────┬────┘         │
└─────────────────────────┼───────────────────┼───────────────┘
                          │                   │
              ┌───────────▼───────┐   ┌───────▼───────┐
              │   Elasticsearch   │   │  PostgreSQL   │
              │   (Search Index)  │   │  (Primary DB) │
              └───────────────────┘   └───────────────┘
```

### 2.2 Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Runtime | Java | 21+ |
| Framework | Spring Boot | 3.2.0 |
| Database | PostgreSQL | 15+ |
| Search Engine | Elasticsearch | 8.x |
| ORM | Hibernate/JPA | 6.x |
| Build Tool | Maven | 3.9+ |

---

## 3. Data Model

### 3.1 PostgreSQL Schema

#### Table: candidates

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Unique identifier |
| organization_id | UUID | NOT NULL, INDEX | Multi-tenant isolation |
| first_name | VARCHAR(100) | NOT NULL | First name |
| last_name | VARCHAR(100) | NOT NULL | Last name |
| email | VARCHAR(255) | UNIQUE, NOT NULL | Email address |
| phone | VARCHAR(50) | | Phone number |
| current_title | VARCHAR(255) | | Current job title |
| current_company | VARCHAR(255) | | Current employer |
| location | VARCHAR(255) | | Location |
| years_experience | INTEGER | | Years of experience |
| skills | TEXT[] | | Array of skills |
| resume_url | VARCHAR(500) | | Resume file URL |
| resume_text | TEXT | | Parsed resume text |
| linkedin_url | VARCHAR(500) | | LinkedIn profile |
| salary_expectation | DECIMAL(15,2) | | Expected salary |
| currency | VARCHAR(3) | | Currency code |
| availability | VARCHAR(50) | | IMMEDIATE, 2_WEEKS, 1_MONTH |
| source | VARCHAR(100) | | How candidate was sourced |
| pipeline_stage | VARCHAR(50) | | Current pipeline stage |
| status | VARCHAR(20) | | ACTIVE, HIRED, REJECTED, WITHDRAWN |
| created_at | TIMESTAMP | DEFAULT NOW() | Creation timestamp |
| updated_at | TIMESTAMP | DEFAULT NOW() | Last update timestamp |

### 3.2 Elasticsearch Document

```json
{
  "id": "uuid",
  "organizationId": "uuid",
  "fullName": "John Doe",
  "email": "john@example.com",
  "currentTitle": "Senior Engineer",
  "currentCompany": "TechCorp",
  "location": "New York, NY",
  "yearsExperience": 8,
  "skills": ["Java", "Spring Boot", "PostgreSQL"],
  "salaryExpectation": 150000,
  "availability": "IMMEDIATE",
  "pipelineStage": "APPLIED",
  "status": "ACTIVE",
  "resumeText": "Full text of resume for search...",
  "createdAt": "2025-12-11T10:00:00Z"
}
```

---

## 4. API Design

### 4.1 Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/candidates` | List candidates (paginated) |
| POST | `/api/v1/candidates` | Create candidate |
| GET | `/api/v1/candidates/{id}` | Get candidate by ID |
| PUT | `/api/v1/candidates/{id}` | Update candidate |
| DELETE | `/api/v1/candidates/{id}` | Delete candidate |
| POST | `/api/v1/candidates/search` | Advanced search |
| POST | `/api/v1/candidates/{id}/resume` | Upload resume |
| POST | `/api/v1/candidates/import` | Bulk import (CSV) |
| GET | `/api/v1/candidates/{id}/activity` | Activity timeline |

### 4.2 Search API

```json
POST /api/v1/candidates/search
{
  "keyword": "java spring",
  "skills": ["Java", "Spring Boot"],
  "yearsExperienceMin": 5,
  "yearsExperienceMax": 10,
  "location": "New York",
  "salaryMin": 100000,
  "salaryMax": 200000,
  "availability": ["IMMEDIATE", "2_WEEKS"],
  "status": ["ACTIVE"],
  "pipelineStage": ["APPLIED", "SCREENING"]
}
```

---

## 5. Search Architecture

### 5.1 Elasticsearch Configuration

- Index: `candidates-{organizationId}`
- Shards: 3
- Replicas: 1
- Refresh interval: 1s

### 5.2 Search Features

| Feature | Implementation |
|---------|----------------|
| Full-text search | Multi-match query on name, title, skills, resume |
| Skill matching | Terms query with boost |
| Location search | Geo-distance query (optional) |
| Salary range | Range query |
| Faceted search | Aggregations for filters |

### 5.3 Sync Strategy

- Real-time: Update ES on candidate create/update
- Batch: Nightly full reindex for consistency
- Event-driven: Kafka consumer for distributed updates

---

## 6. Resume Parsing

### 6.1 Integration

```
Candidate Upload → Resume Parser Service → Parsed Data → Candidate Update
```

### 6.2 Parsed Fields

| Field | Source |
|-------|--------|
| Full Name | Header extraction |
| Email | Pattern matching |
| Phone | Pattern matching |
| Skills | NLP entity extraction |
| Experience | Section parsing |
| Education | Section parsing |

---

## 7. Security

### 7.1 Data Protection

- PII encrypted at rest
- Resume files stored in secure object storage
- Access logged for compliance

### 7.2 Authorization

| Role | Permissions |
|------|-------------|
| RECRUITER | Full CRUD access |
| HIRING_MANAGER | Read + feedback |
| INTERVIEWER | Read only |

---

## 8. Monitoring

### 8.1 Key Metrics

- `candidates_created_total`
- `candidates_searched_total`
- `elasticsearch_query_duration_seconds`
- `resume_parse_duration_seconds`

---

## 9. References

- [Elasticsearch Documentation](https://www.elastic.co/guide/index.html)
- [Spring Data Elasticsearch](https://spring.io/projects/spring-data-elasticsearch)

