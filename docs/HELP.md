# Candidate Management Service - Help Guide

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 15+
- Elasticsearch 8.x

### Running Locally

```bash
# Clone repository
git clone https://github.com/PROD-B2B-GGJ-Platform/candidate-management-service.git
cd candidate-management-service

# Set environment variables
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=gograbjob_candidates
export ES_HOST=localhost
export ES_PORT=9200

# Run the service
mvn spring-boot:run
```

### Access Points

- Service: http://localhost:8092
- Swagger UI: http://localhost:8092/swagger-ui.html
- Health: http://localhost:8092/actuator/health

---

## API Examples

### Create Candidate

```bash
curl -X POST http://localhost:8092/api/v1/candidates \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phone": "+1-555-1234",
    "currentTitle": "Senior Software Engineer",
    "currentCompany": "TechCorp",
    "yearsExperience": 8,
    "skills": ["Java", "Spring Boot", "PostgreSQL"],
    "salaryExpectation": 150000,
    "availability": "IMMEDIATE"
  }'
```

### Search Candidates

```bash
curl -X POST http://localhost:8092/api/v1/candidates/search \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "keyword": "java spring",
    "skills": ["Java"],
    "yearsExperienceMin": 5,
    "location": "New York"
  }'
```

### Upload Resume

```bash
curl -X POST http://localhost:8092/api/v1/candidates/{id}/resume \
  -H "Authorization: Bearer <token>" \
  -F "file=@resume.pdf"
```

---

## Troubleshooting

### Elasticsearch Connection Issues

```bash
# Check ES is running
curl http://localhost:9200/_cluster/health

# Check index exists
curl http://localhost:9200/candidates-*/_count
```

### Search Not Returning Results

1. Verify candidate was indexed
2. Check ES index health
3. Wait for refresh (up to 1 second)

### Resume Parsing Failures

- Check file size (max 10MB)
- Verify file format (PDF, DOCX)
- Check resume parser service is running

---

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| SERVER_PORT | 8092 | Service port |
| DB_HOST | localhost | Database host |
| ES_HOST | localhost | Elasticsearch host |
| ES_PORT | 9200 | Elasticsearch port |

---

## Support

- GitHub: https://github.com/PROD-B2B-GGJ-Platform/candidate-management-service
- Team: Talent & Recruitment Team

