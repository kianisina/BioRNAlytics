package com.sp.web.web;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sp.web.domain.AnalysisJob;
import com.sp.web.domain.AnalysisService;
import com.sp.web.domain.User;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    @Value("${app.storage.base-dir:/var/bioinformatics/data}")
    private String baseStorageDir;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file, 
            @AuthenticationPrincipal User user) {
        try {
            String tempFileId = analysisService.uploadTemporaryFile(file, user);
            Map<String, String> response = new HashMap<>();
            response.put("fileId", tempFileId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed.");
        }
    }

    @PostMapping("/start")
    public ResponseEntity<?> startAnalysis(
            @RequestParam("fileId") String fileId,
            @RequestParam("designMatrix") String designMatrixJson,
            @AuthenticationPrincipal User user) {
        try {
            AnalysisJob job = analysisService.createAndStartJob(fileId, designMatrixJson, user);
            return ResponseEntity.ok(job);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Analysis failed: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getUserJobs(@AuthenticationPrincipal User user) {
        // Fetch jobs sorted by newest first
        List<AnalysisJob> jobs = analysisService.getUserJobs(user.getUsername());
        return ResponseEntity.ok(jobs);
    }

    
    @GetMapping("/{jobId}/comparisons")
    public ResponseEntity<List<String>> getComparisons(@PathVariable String jobId, @AuthenticationPrincipal User user) {
        List<String> comparisons = analysisService.getAvailableComparisons(user.getUsername(), jobId);
        return ResponseEntity.ok(comparisons);
    }

    
    @GetMapping("/plot/{jobId}/{comparison}")
    public ResponseEntity<Resource> getPlot(
            @PathVariable String jobId, 
            @PathVariable String comparison, 
            @AuthenticationPrincipal User user) {
        try {
            // Dynamically build the filename based on the requested comparison
            String fileName = comparison + "_volcano.png";
            Path plotPath = Paths.get(baseStorageDir, user.getUsername(), jobId, fileName);
            Resource resource = new UrlResource(plotPath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Download a specific CSV Result
    @GetMapping("/download/{jobId}/{comparison}")
    public ResponseEntity<Resource> downloadCsv(
            @PathVariable String jobId, 
            @PathVariable String comparison, 
            @AuthenticationPrincipal User user) {
        try {
            // Dynamically build the filename based on the requested comparison
            String fileName = comparison + "_results.csv";
            Path csvPath = Paths.get(baseStorageDir, user.getUsername(), jobId, fileName);
            Resource resource = new UrlResource(csvPath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"deseq2_" + fileName + "\"")
                        .contentType(MediaType.parseMediaType("text/csv"))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    

    @GetMapping("/pca/{jobId}")
    public ResponseEntity<Resource> getPcaPlot(@PathVariable String jobId, @AuthenticationPrincipal User user) {
        try {
            // The R script saves this as exactly "dataset_pca.png"
            Path pcaPath = Paths.get(baseStorageDir, user.getUsername(), jobId, "dataset_pca.png");
            Resource resource = new UrlResource(pcaPath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/log/{jobId}")
    public ResponseEntity<String> getJobLog(@PathVariable String jobId, @AuthenticationPrincipal User user) {
        try {
            Path logPath = Paths.get(baseStorageDir, user.getUsername(), jobId, "r_execution.log");
            
            if (Files.exists(logPath)) {
                String logContent = Files.readString(logPath);
                return ResponseEntity.ok(logContent);
            } else {
                return ResponseEntity.ok("Log file wurde nicht gefunden. Möglicherweise ist der Prozess vorzeitig abgestürzt.");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Fehler beim Lesen der Log-Datei.");
        }
    }

    @GetMapping("/download/heatmap/{jobId}/{comparison}")
    public ResponseEntity<Resource> downloadHeatmapCsv(
            @PathVariable String jobId, 
            @PathVariable String comparison, 
            @AuthenticationPrincipal User user) {
        try {
            String fileName = comparison + "_heatmap.csv";
            Path filePath = Paths.get(baseStorageDir, user.getUsername(), jobId, fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_PLAIN) // Serve as text for PapaParse
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}