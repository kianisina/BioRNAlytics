<script setup>
import { computed, ref, onMounted } from 'vue';
import { useQuasar } from 'quasar';
import { useRouter } from 'vue-router';
import { useUserStore } from '../stores/users';
import { useSettingsStore } from '@/main/vue/stores/settings';

const $q = useQuasar();
const router = useRouter();
const userStore = useUserStore();
const settingsStore = useSettingsStore();

const username = ref('');
const password = ref('');
const isLoadingLogo = ref(true); 
const isLoadingSettings = ref(true); 

// Fetch the logo URL from settings store
const logoUrl = computed(() => {
    const logo = settingsStore.corporateDesign?.logo;
    return logo 
        ? `data:image/png;base64,${logo}` 
        : 'https://upload.wikimedia.org/wikipedia/commons/d/d5/Deutsche_Bahn_AG-Logo.svg';
});
const companyName = computed(() => {
    return settingsStore.corporateDesign?.companyName || 'Bioinformatics';
});


onMounted(async () => {
    try {
        await settingsStore.fetchLogo();
        await settingsStore.fetchCorporateDesign(); 
    } catch (error) {
        console.error('Failed to fetch logo', error);
    } finally {
        isLoadingLogo.value = false;
        isLoadingSettings.value = false;
    }
});

function login() {
    userStore.requestToken({
        username: username.value,
        password: password.value
    }).then(() => {
        router.push('/');
        userStore.requestCurrentUser();
    }).catch((error) => {
        $q.notify({
            type: 'negative',
            message: 'Login Fehlgeschlagen',
            caption: 'Falsches Passwort oder Benutzername',
            timeout: 1000
        });
    });
}

function goToRegisterPage() {
    router.push('/register');
}

function goToForgotPasswordPage() {
    router.push('/forget-password'); 
}
</script>
<template>
    <q-page>
        <div class="col-auto fixed-center shadow-10 rounded-borders">
            
            <div class="row-auto relative-position flex flex-center" style="min-height: 150px;">
                <q-spinner v-if="isLoadingLogo" color="primary" size="3em" />
                <img v-else :src="logoUrl" alt="Logo" class="small-image">
            </div>

            <div class="row-auto flex flex-center" style="min-height: 48px;">
                <q-spinner-dots v-if="isLoadingSettings" color="primary" size="2em" />
                
                <div v-else class="text-h4 q-ma-sm">{{ companyName }}</div>
            </div>
            
            <div class="q-ma-sw">
                <q-input v-model="username" label="Benutzername"/>
                <q-input v-model="password" type="password" label="Passwort"/>
                
                <div class="row-auto">
                    <div class="col q-mt-sm">
                        <q-btn label="Anmelden" @click="login()" color="primary" class="row-auto q-ma-xs"></q-btn>
                        <q-btn label="Registrieren" @click="goToRegisterPage()" color="secondary" class="row-auto q-ma-xs"></q-btn>
                    </div>
                </div>
                
                <div class="q-mt-sm">
                    <span class="forget-password" @click="goToForgotPasswordPage()">Passwort vergessen?</span>
                </div>
            </div>
        </div>
    </q-page>
</template>
<style scoped>
.small-image {
    width: 280px;
    height: auto;
}
.forget-password {
    cursor: pointer;
    color: #0000EE;
    text-decoration: underline;
}
.col-auto {
    background-color: #ffffff;
    color: black;
}
</style>