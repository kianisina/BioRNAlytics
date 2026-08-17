package com.sp.web.domain;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface AnalysisService {
    // Handles the initial file upload and returns a temporary file ID
    String uploadTemporaryFile(MultipartFile file, User user) throws IOException;
    
    // Takes the uploaded file and the design matrix, and starts the DB/R process
    AnalysisJob createAndStartJob(String tempFileId, String designMatrixJson, User user) throws Exception;

    List<AnalysisJob> getUserJobs(String username);
    
    List<String> getAvailableComparisons(String username, String jobId);
}