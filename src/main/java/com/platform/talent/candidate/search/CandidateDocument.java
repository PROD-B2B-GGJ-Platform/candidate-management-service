package com.platform.talent.candidate.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Document(indexName = "candidates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private UUID tenantId;

    @Field(type = FieldType.Text)
    private String firstName;

    @Field(type = FieldType.Text)
    private String lastName;

    @Field(type = FieldType.Keyword)
    private String email;

    @Field(type = FieldType.Text)
    private String phone;

    @Field(type = FieldType.Text)
    private String location;

    @Field(type = FieldType.Keyword)
    private String city;

    @Field(type = FieldType.Keyword)
    private String country;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private String pipelineStage;

    @Field(type = FieldType.Text)
    private String summary;

    @Field(type = FieldType.Integer)
    private Integer yearsOfExperience;

    @Field(type = FieldType.Text)
    private String currentCompany;

    @Field(type = FieldType.Text)
    private String currentPosition;

    @Field(type = FieldType.Keyword)
    private List<String> skills;

    @Field(type = FieldType.Text)
    private String education;

    @Field(type = FieldType.Keyword)
    private List<String> certifications;

    @Field(type = FieldType.Keyword)
    private List<String> languages;

    @Field(type = FieldType.Keyword)
    private String source;

    @Field(type = FieldType.Double)
    private Double expectedSalary;

    @Field(type = FieldType.Keyword)
    private String salaryCurrency;

    @Field(type = FieldType.Boolean)
    private Boolean isAvailable;

    @Field(type = FieldType.Boolean)
    private Boolean isRemoteInterested;

    @Field(type = FieldType.Boolean)
    private Boolean isRelocationInterested;

    @Field(type = FieldType.Double)
    private Double rating;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;
}

