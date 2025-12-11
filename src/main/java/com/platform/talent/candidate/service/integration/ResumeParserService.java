package com.platform.talent.candidate.service.integration;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeParserService {

    private final RestTemplate restTemplate;

    @Value("${integration.resume-parser.url:http://ai-resume-parser:8000}")
    private String resumeParserUrl;

    @CircuitBreaker(name = "resumeParser", fallbackMethod = "parseResumeFallback")
    @Retry(name = "resumeParser")
    public Map<String, Object> parseResume(UUID candidateId, MultipartFile resumeFile) {
        try {
            log.info("Parsing resume for candidate: {}", candidateId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(resumeFile.getBytes()) {
                @Override
                public String getFilename() {
                    return resumeFile.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String url = resumeParserUrl + "/api/v1/parse";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Successfully parsed resume for candidate: {}", candidateId);
                return response.getBody();
            } else {
                log.warn("Resume parsing returned non-OK status for candidate: {}", candidateId);
                return Map.of("error", "Failed to parse resume");
            }
        } catch (Exception e) {
            log.error("Failed to parse resume for candidate: {}", candidateId, e);
            throw new RuntimeException("Resume parsing failed", e);
        }
    }

    public Map<String, Object> parseResumeFallback(UUID candidateId, MultipartFile resumeFile, Exception e) {
        log.warn("Resume parser service unavailable, using fallback for candidate: {}", candidateId);
        return Map.of(
            "status", "fallback",
            "message", "Resume parser service unavailable, manual review required",
            "candidateId", candidateId.toString()
        );
    }
}

