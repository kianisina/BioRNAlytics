import { defineStore } from 'pinia';
import { ref } from 'vue';
import analysisAPI from '../api/analysis';

export const useAnalysisStore = defineStore('analysis', () => {
    // State
    const isUploading = ref(false);
    const uploadProgress = ref(0);
    const uploadedFileId = ref(null);
    const isAnalyzing = ref(false);

    const historyJobs = ref([]);
    const isHistoryLoading = ref(false);

    // Actions
    async function uploadFile(file) {
        isUploading.value = true;
        uploadProgress.value = 0;
        uploadedFileId.value = null;

        try {
            // Pass a callback to update the progress state
            const response = await analysisAPI.uploadFile(file, (progress) => {
                uploadProgress.value = progress;
            });
            
            uploadedFileId.value = response.data.fileId;
            return true;
        } catch (error) {
            console.error('Error uploading file:', error);
            throw error;
        } finally {
            isUploading.value = false;
        }
    }

    async function startAnalysis(designMatrix) {
        if (!uploadedFileId.value) {
            throw new Error("No file uploaded yet");
        }

        isAnalyzing.value = true;
        
        try {
            const response = await analysisAPI.startAnalysis(uploadedFileId.value, designMatrix);
            return response.data; 
        } catch (error) {
            console.error('Error starting analysis:', error);
            throw error;
        } finally {
            isAnalyzing.value = false;
        }
    }

    async function fetchHistory() {
        isHistoryLoading.value = true;
        try {
            const response = await analysisAPI.getHistory();
            historyJobs.value = response.data;
        } catch (error) {
            console.error('Error fetching history:', error);
            throw error;
        } finally {
            isHistoryLoading.value = false;
        }
    }

    async function fetchComparisons(jobId) {
        try {
            const response = await analysisAPI.getComparisons(jobId);
            return response.data; 
        } catch (error) {
            console.error('Error fetching comparisons:', error);
            throw error;
        }
    }

    // UPDATED: Accept comparison parameter
    async function getPlotUrl(jobId, comparison) {
        try {
            const response = await analysisAPI.getPlot(jobId, comparison);
            return window.URL.createObjectURL(new Blob([response.data]));
        } catch (error) {
            console.error('Error fetching plot:', error);
            throw error;
        }
    }

    // UPDATED: Accept comparison parameter and name the file dynamically
    async function downloadResultCsv(jobId, comparison) {
        try {
            const response = await analysisAPI.downloadCsv(jobId, comparison);
            const url = window.URL.createObjectURL(new Blob([response.data]));
            
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', `deseq2_${comparison}_results.csv`);
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            
            window.URL.revokeObjectURL(url);
        } catch (error) {
            console.error('Error downloading CSV:', error);
            throw error;
        }
    }

    async function getPcaUrl(jobId) {
        try {
            
            const response = await analysisAPI.getPca(jobId);
            return window.URL.createObjectURL(new Blob([response.data]));
        } catch (error) {
            console.error('Error fetching PCA:', error);
            throw error;
        }
    }

    async function fetchJobLog(jobId) {
        try {
            const response = await analysisAPI.getLog(jobId);
            return response.data; // Returns the raw text of the log
        } catch (error) {
            console.error('Error fetching log:', error);
            throw error;
        }
    }

    async function fetchCsvText(jobId, comparison) {
        try {
            const response = await analysisAPI.getCsvText(jobId, comparison);
            return response.data;
        } catch (error) {
            console.error('Error fetching CSV text:', error);
            throw error;
        }
    }
    async function fetchHeatmapText(jobId, comparison) {
        try {
            const response = await analysisAPI.getHeatmapText(jobId, comparison);
            return response.data;
        } catch (error) {
            console.error('Error fetching heatmap text:', error);
            throw error;
        }
    }

    return { 
        isUploading, uploadProgress, uploadedFileId, isAnalyzing, historyJobs, isHistoryLoading,
        uploadFile, startAnalysis, fetchComparisons, fetchHistory, getPlotUrl, downloadResultCsv, getPcaUrl, fetchJobLog, fetchCsvText, fetchHeatmapText
    };
});