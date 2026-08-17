package com.sp.web.domain;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sp.web.config.RabbitMQConfig;

@Component
public class AnalysisWorker {

    @Autowired
    private AnalysisJobRepository jobRepository;

    @Value("${app.storage.base-dir:/var/bioinformatics/data}")
    private String baseStorageDir;

    @Value("${app.r.executable}")
    private String rExecutable;

    @Value("${app.r.script-path}")
    private String rScriptLocation;

    // Listens to the queue. concurrency = "1" limits it to 1 active job at a time!
    @RabbitListener(queues = RabbitMQConfig.DESEQ2_QUEUE, concurrency = "1")
    public void processDeseq2Job(String jobId) {
        
        Optional<AnalysisJob> optionalJob = jobRepository.findById(jobId);
        if (optionalJob.isEmpty()) return;

        AnalysisJob job = optionalJob.get();
        
        // Update status in MongoDB
        job.setStatus("RUNNING");
        jobRepository.save(job);

        try {
            // Get the path to the user's specific job folder
            
            String jobDirectoryPath = Paths.get(baseStorageDir, job.getUsername(), job.getId())
                               .toAbsolutePath()
                               .toString();

            
            jobDirectoryPath = jobDirectoryPath.replace("\\", "/");

            // Build the R terminal command
            ProcessBuilder pb = new ProcessBuilder(
                rExecutable,        // Tell Windows exactly where Rscript.exe is
                rScriptLocation,    // Tell Rscript exactly where your deg.R file is
                jobDirectoryPath    // Pass the job directory as an argument
            );

            // Save R logs to the folder for debugging
            File logFile = new File(jobDirectoryPath, "r_execution.log");
            pb.redirectErrorStream(true);
            pb.redirectOutput(logFile);

            // Run the process
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                job.setStatus("COMPLETED");
                job.setPlotFileName("volcano.png");
                job.setCsvFileName("results.csv");
            } else {
                job.setStatus("FAILED");
            }

        } catch (Exception e) {
            job.setStatus("FAILED");
            e.printStackTrace();
        } finally {
            // Always save the final state back to the database
            jobRepository.save(job);
        }
    }
}