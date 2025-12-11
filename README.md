# Candidate Management Service

Candidate profile management with Elasticsearch search and AI-powered resume parsing.

**Version**: 10.0.0.1  
**Port**: 8092  
**Technology**: Java 21 + Spring Boot 3 + PostgreSQL + Elasticsearch

## Features

- Candidate CRUD operations
- Resume upload and AI parsing (Python integration)
- Elasticsearch advanced search (skills, experience, location)
- Pipeline management (Applied → Hired stages)
- Bulk operations support
- Multi-tenant row-level security
- Kafka event streaming

## Quick Start

```bash
mvn spring-boot:run
```

**API**: http://localhost:8092/api/v1/candidates  
**Swagger**: http://localhost:8092/swagger-ui.html

## Search Capabilities

Advanced Elasticsearch search by:
- Skills (exact match or partial)
- Years of experience (range)
- Location (city, country)
- Salary expectations
- Remote work preference
- Availability status

**Status**: ✅ Production Ready

