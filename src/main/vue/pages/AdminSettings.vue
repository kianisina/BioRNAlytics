
<script setup>
import { ref, provide } from 'vue';
import { useSettingsStore } from '@/main/vue/stores/settings';
import UserManagement from '@/main/vue/views/UserManagement.vue';
import { useQuasar } from "quasar";

const settingsStore = useSettingsStore();
const companyName = ref('');
const fontColor = ref('#000000');
const backgroundColor = ref('#FFFFFF');
const imprintText = ref('');
const logoFile = ref(null); 
const companyId = ref('m-map');
const $q = useQuasar()
const selectedTab = ref('userManagement');

const switchTab = (tabName) => {
    selectedTab.value = tabName;
};

provide('switchTab', switchTab);

const fetchSettings = async () => {
    await settingsStore.fetchCorporateDesign();
    companyName.value = settingsStore.corporateDesign.companyName;
    fontColor.value = settingsStore.corporateDesign.fontColor;
    backgroundColor.value = settingsStore.corporateDesign.backgroundColor;
    imprintText.value = settingsStore.corporateDesign.imprintText;
    
    
    logoFile.value = null; 
};

const saveSettings = async () => {
    const formData = new FormData();
    
    
    if (logoFile.value instanceof File) {
        formData.append('file', logoFile.value);
    }
    
    formData.append('companyName', companyName.value);
    formData.append('fontColor', fontColor.value);
    formData.append('backgroundColor', backgroundColor.value);
    formData.append('imprintText', imprintText.value);

    await settingsStore.saveCorporateDesign(companyId.value, formData).then(() => {
        $q.notify({
            type: 'positive',
            message: 'Speichern erfolgreich',
            caption: 'Einstellungen konnte erfolgreich gespeichert werden.',
            timeout: 1000
        });
        fetchSettings(); // Refresh settings (this will also reset logoFile to null)
    }).catch(() => {
        console.log('Error saving settings');
    });
};

const handleFileUpload = (event) => {
    if (event.target.files && event.target.files.length > 0) {
        logoFile.value = event.target.files[0];
    }
};

fetchSettings();
</script>
<template>
    <q-page class="ee">
        <q-card>
            <q-card-section>
                <div class="text-h6">Admin Einstellungen</div>
                <q-tabs
                    align="left"
                    v-model="selectedTab"
                >
                    <q-tab name="corporateDesign" label="Corporate Design" icon="palette" />
                    <q-tab name="userManagement" label="Nutzer verwalten" icon="people" />
                </q-tabs>
            </q-card-section>
            <q-separator />
            <q-card-section>
                <q-tab-panels v-model="selectedTab" animated>
                    <q-tab-panel name="corporateDesign">
                        <q-form @submit.prevent="saveSettings">
                            <q-input v-model="companyName" label="Name der Firma" required />
                            <input type="file" @change="handleFileUpload" class="input" />
                            <q-input v-model="fontColor" label="Schriftfarbe" type="color" required />
                            <q-input v-model="backgroundColor" label="Hintergrundfarbe" type="color" required />
                            <q-input v-model="imprintText" label="Impressum Text" type="textarea" required />
                            <q-btn label="speichern" type="submit" color="primary" />
                        </q-form>
                    </q-tab-panel>
                    <q-tab-panel name="userManagement">
                        <UserManagement />
                    </q-tab-panel>
                </q-tab-panels>
            </q-card-section>
        </q-card>
    </q-page>
</template>
<style scoped>
.input {
    color: black;
}
.ee {
    background-color: #ffffff;
    color: black;
}
</style>