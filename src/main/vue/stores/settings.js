import {defineStore} from 'pinia';
import settingAPI from '@/main/vue/api/settings';
export const useSettingsStore = defineStore('settings', {
    state: () => ({
        corporateDesign: {
            companyName: '',
            logo: null,
            fontColor: '',
            backgroundColor: '',
            imprintText: '',
        },
    }),
    actions: {
        async fetchCorporateDesign() {
            try {
                const response = await settingAPI.getSettings();
                this.corporateDesign = response.data;
                console.log(response.data, "helooooo")
            } catch (error) {
                console.error('Error fetching corporate design:', error);
            }
        },
        async saveCorporateDesign(id, formData) {
            return new Promise((resolve, reject) => {
                settingAPI.saveSettings(id, formData).then(() => {
                    console.log("qwerpoiuz")
                    resolve();
                }).catch(error => {
                    console.log("poiuz")
                    reject(error);
                });
            })
        },
        async fetchLogo() {
            const response = await settingAPI.getSettings();
            this.corporateDesign = response.data;
        },
        async fetchCompanyName() {
            const response = await settingAPI.getSettings();
            this.corporateDesign = response.data;
        }
    },
});