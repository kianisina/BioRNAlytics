package com.sp.web.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnalysisJobRepository extends MongoRepository<AnalysisJob, String> {
    // This will let us easily fetch all jobs for the logged-in user
    List<AnalysisJob> findByUsernameOrderByCreatedAtDesc(String username);
}