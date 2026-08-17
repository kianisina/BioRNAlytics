<script setup>
import { computed, ref} from 'vue'
import { useQuasar } from 'quasar'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/users'
import { useSettingsStore } from '@/main/vue/stores/settings';
const settingsStore = useSettingsStore();
const logoUrl = computed(() => {
    const logo = settingsStore.corporateDesign.logo;
    return logo ? `data:image/png;base64,${logo}` : 'https://upload.wikimedia.org/wikipedia/commons/d/d5/Deutsche_Bahn_AG-Logo.svg';
});
const $q = useQuasar()
const router = useRouter()
const email = ref('')
const userStore = useUserStore()
function sendResetInstructions() {
    userStore.forgotPassword(email.value)
        .then(() => {
            $q.notify({
                type: 'positive',
                message: 'Anweisungen zum Zurücksetzen erfolgreich gesendet.',
                timeout: 1000
            });
        })
        .catch(() => {
            $q.notify({
                type: 'negative',
                message: 'Anweisungen zum Zurücksetzen konnten nicht gesendet werden. Bitte versuchen Sie es später noch einmal.'
            });
        });
}
</script>
<template>
    <q-page>
        <div class="col-auto fixed-center shadow-10 rounded-borders">
            <div class="row-auto relative-position">
                <img :src="logoUrl" alt="Logo" class="small-image">
            </div>
            <div class="row-auto">
                <div class="text-h4 q-ma-sm">Passwort Vergessen</div>
            </div>
            <q-card-section class="custom-card-section">
                <div>
                    Du kommst nicht in Dein Konto?<br><br>
                    Du hast Dein Passwort vergessen? Trage bitte hier die E-Mail-Adresse ein, mit der Du Dich registriert hast.<br><br>
                    Wir senden Dir dann einen neuen Aktivierungslink bzw. eine E-Mail, mit der Du ein neues Passwort wählen kannst.<br>
                </div>
            </q-card-section>
            <div class="row-auto q-ma-sm">
                <q-input v-model="email" label="Email"/>
                <div class="row-auto">
                    <div class="col q-mt-sm">
                        <q-btn label="Senden" @click="sendResetInstructions" color="primary"
                               class="row-auto q-ma-xs"></q-btn>
                    </div>
                </div>
            </div>
        </div>
    </q-page>
</template>
<style scoped>
.small-image {
    width: 330px;
    height: auto;
}
.custom-card-section div {
    width: 300px;
    max-width: 300px;
    word-wrap: break-word;
}
.col-auto {
    background-color: #ffffff;
    color: black;
}
</style>