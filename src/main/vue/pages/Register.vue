<script setup>
import { computed, ref} from 'vue';
import { useUserStore } from '../stores/users';
import { useQuasar } from 'quasar';
import { useRouter } from 'vue-router';
import { useSettingsStore } from '@/main/vue/stores/settings';
const settingsStore = useSettingsStore();
const logoUrl = computed(() => {
    const logo = settingsStore.corporateDesign.logo;
    return logo ? `data:image/png;base64,${logo}` : 'https://upload.wikimedia.org/wikipedia/commons/d/d5/Deutsche_Bahn_AG-Logo.svg';
});
const $q = useQuasar();
const router = useRouter();
const userStore = useUserStore();
const username = ref('');
const password = ref('');
const email = ref('');
const firstname = ref('');
const lastname = ref('');
const age = ref('');
const center = ref('');
const company = ref([
    'Universität Bielefeld',
    'Hochschule Bielefeld'
]);
async function register() {
    try {
        await userStore.registerUser({
            username: username.value,
            password: password.value,
            email: email.value,
            firstname: firstname.value,
            lastname: lastname.value,
            age: age.value,
            center: center.value
        });
        $q.notify({
            type: 'positive',
            message: 'Registrierung erfolgreich',
            caption: 'Du kannst dich nach der Freischaltung des Kontos durch Admin anmelden.',
            timeout: 1000
        });
        goToLoginPage();
    } catch (error) {
        $q.notify({
            type: 'negative',
            message: error.response.data,
            caption: 'Bitte versuche es erneut.',
            timeout: 1000
        });
    }
}
function goToLoginPage() {
    router.push('/login');
}
</script>
<template>
    <q-page>
        <div class="col-auto fixed-center shadow-10 rounded-borders">
            <div class="row-auto relative-position">
                <img :src="logoUrl" alt="Logo" class="small-image">
            </div>
            <div class="scrollable-div">
                <div class="row-auto q-ma-sm">
                    <q-input v-model="username" label="Benutzername" />
                    <q-input v-model="firstname" label="Vorname" />
                    <q-input v-model="lastname" label="Nachname" />
                    <q-input v-model="age" label="Age" />
                    <q-input v-model="password" type="password" label="Passwort" />
                    <q-input v-model="email" type="email" label="E-Mail" />
                    <q-card-section>
                        <q-select outlined v-model="center" :options="company" label="Company Name" />
                    </q-card-section>
                    
                    <div class="row-auto">
                        <div class="col q-mt-sm">
                            <q-btn label="Registrieren" @click="register()" color="primary" class="row-auto q-ma-xs" />
                            <q-btn label="Zurück zum Login" @click="goToLoginPage()" color="secondary" class="row-auto q-ma-xs" />
                        </div>
                    </div>
                </div>
            </div>

        </div>
    </q-page>
</template>
<style scoped>
.small-image {
    width: 340px;
    height: auto;
}
.col-auto {
    background-color: #ffffff;
}
.scrollable-div {
    max-height: 400px;
    overflow-y: auto;
}
</style>