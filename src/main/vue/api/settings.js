import axios from 'axios';
export default {
    getSettings: () => axios.get('/api/settings/corporateDesign'),
    saveSettings: (id, formData) => axios.post(`/api/settings/saveSettings/${id}`, formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    }),
};