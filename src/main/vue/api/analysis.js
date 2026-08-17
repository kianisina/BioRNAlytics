// src/main/vue/api/analysis.js
import axios from 'axios';

export default {
    // Upload the file and track progress
    uploadFile(file, onProgressCallback) {
        const formData = new FormData();
        formData.append('file', file);
        
        return axios.post('/api/analysis/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
            onUploadProgress: (progressEvent) => {
                const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
                onProgressCallback(percentCompleted);
            }
        });
    },

    // Start the analysis using the fileId
    startAnalysis(fileId, designMatrix) {
        const formData = new FormData();
        formData.append('fileId', fileId);
        formData.append('designMatrix', JSON.stringify(designMatrix));
        
        return axios.post('/api/analysis/start', formData);
    },
    getHistory() {
        return axios.get('/api/analysis/history');
    },
    
    // Fetch available comparisons for a job
    getComparisons(jobId) {
        return axios.get(`/api/analysis/${jobId}/comparisons`);
    },
    
    // Require the comparison name
    getPlot(jobId, comparison) {
        return axios.get(`/api/analysis/plot/${jobId}/${comparison}`, { responseType: 'blob' });
    },
    
    // Require the comparison name
    downloadCsv(jobId, comparison) {
        return axios.get(`/api/analysis/download/${jobId}/${comparison}`, { responseType: 'blob' });
    },
    getCsvText(jobId, comparison) {
        return axios.get(`/api/analysis/download/${jobId}/${comparison}`, { responseType: 'text' });
    },
    getLog(jobId) {
        return axios.get(`/api/analysis/log/${jobId}`);
    },
    getPca(jobId) {
        return axios.get(`/api/analysis/pca/${jobId}`, { responseType: 'blob' });
    },
    getHeatmapText(jobId, comparison) {
        return axios.get(`/api/analysis/download/heatmap/${jobId}/${comparison}`, { responseType: 'text' });
    }
}