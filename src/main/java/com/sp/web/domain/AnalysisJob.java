package com.sp.web.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "analysis_jobs")
public class AnalysisJob {
    @Id
    private String id;
    private String username;
    private String status; // "RUNNING", "COMPLETED", "FAILED"
    private LocalDateTime createdAt;
    
    private String comparisonSummary;
    
    // File paths
    private String countFilePath;
    private String colDataFilePath;
    private String plotFileName;
    private String csvFileName;
}