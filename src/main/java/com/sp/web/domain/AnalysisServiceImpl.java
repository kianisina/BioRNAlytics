package com.sp.web.domain;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sp.web.config.RabbitMQConfig;

@Service
public class AnalysisServiceImpl implements AnalysisService {

    @Autowired
    private AnalysisJobRepository jobRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${app.storage.base-dir:/var/bioinformatics/data}")
    private String baseStorageDir;

    @Override
    public String uploadTemporaryFile(MultipartFile file, User user) throws IOException {
        String tempFileId = UUID.randomUUID().toString();
        
        // Save to a "staging" directory first
        Path stagingDir = Paths.get(baseStorageDir, "staging");
        Files.createDirectories(stagingDir);
        
        File tempFile = new File(stagingDir.toFile(), tempFileId + ".csv");
        file.transferTo(tempFile);
        
        return tempFileId;
    }

    @Override
    public AnalysisJob createAndStartJob(String tempFileId, String designMatrixJson, User user) throws Exception {
        String jobId = UUID.randomUUID().toString();

        // Save Initial Job State to DB
        AnalysisJob job = new AnalysisJob();
        job.setId(jobId);
        job.setUsername(user.getUsername());
        job.setStatus("RUNNING");
        job.setCreatedAt(LocalDateTime.now());
        jobRepository.save(job);

        try {
            // Parse Design Matrix
            ObjectMapper mapper = new ObjectMapper();
            Map<String, List<String>> designMatrix = mapper.readValue(
            designMatrixJson, new TypeReference<Map<String, List<String>>>() {}
            );
            String[] groupNames = designMatrix.keySet().toArray(new String[0]);
            job.setComparisonSummary(String.join(" vs ", groupNames));

            // Setup Permanent Directory
            Path userJobDir = Paths.get(baseStorageDir, user.getUsername(), jobId);
            Files.createDirectories(userJobDir);

            // Move the staged file to the permanent directory
            Path stagedFilePath = Paths.get(baseStorageDir, "staging", tempFileId + ".csv");
            Path permanentCountsPath = Paths.get(userJobDir.toString(), "counts.csv");
            Files.move(stagedFilePath, permanentCountsPath, StandardCopyOption.REPLACE_EXISTING);
            job.setCountFilePath(permanentCountsPath.toString());

            // Generate coldata.csv
            File colDataFile = new File(userJobDir.toFile(), "coldata.csv");
            try (PrintWriter pw = new PrintWriter(new FileWriter(colDataFile))) {
                pw.println("sample_id,condition");
                for (Map.Entry<String, List<String>> entry : designMatrix.entrySet()) {
                    for (String sample : entry.getValue()) {
                        pw.println(sample + "," + entry.getKey());
                    }
                }
            }
            job.setColDataFilePath(colDataFile.getAbsolutePath());

            

            // Update Job
            job.setStatus("COMPLETED");
            job.setPlotFileName("volcano.png"); // Mocked
            job.setCsvFileName("results.csv");  // Mocked
            
            job.setStatus("QUEUED");
            jobRepository.save(job);

            // Drop the jobId into the RabbitMQ queue
            rabbitTemplate.convertAndSend(RabbitMQConfig.DESEQ2_QUEUE, job.getId());
            return job;

        } catch (Exception e) {
            job.setStatus("FAILED");
            jobRepository.save(job);
            throw e;
        }
    }

    @Override
    public List<AnalysisJob> getUserJobs(String username) {
       
        return jobRepository.findByUsernameOrderByCreatedAtDesc(username);
    }

    @Override
    public List<String> getAvailableComparisons(String username, String jobId) {
        Path jobDir = Paths.get(baseStorageDir, username, jobId);
        List<String> comparisons = new ArrayList<>();

        try {
            if (Files.exists(jobDir)) {
                // Scan the directory for files
                try (Stream<Path> paths = Files.list(jobDir)) {
                    paths.forEach(path -> {
                    String filename = path.getFileName().toString();
                    if (filename.endsWith("_results.csv")) {
                        // Extract the name
                        comparisons.add(filename.replace("_results.csv", ""));
                    }
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return comparisons;
    }
}