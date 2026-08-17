<script setup>
import { computed,ref } from 'vue'
import { useQuasar } from 'quasar'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/users'
import { useSettingsStore } from '@/main/vue/stores/settings';
const settingsStore = useSettingsStore();
const logoUrl = computed(() => {
    const logo = settingsStore.corporateDesign.logo;
    return logo ? `data:image/png;base64,${logo}` : 'https://upload.wikimedia.org/wikipedia/commons/d/d5/Deutsche_Bahn_AG-Logo.svg';
});
const $q = useQuasar()
const route = useRoute()
const router = useRouter()
const token = ref(route.query.token)
const password = ref('')
const confirmPassword = ref('')
const userStore = useUserStore()
function resetPassword() {
    if (password.value !== confirmPassword.value) {
        $q.notify({
            type: 'negative',
            message: "Passwörter stimmen nicht überein",
            timeout: 1000
        })
        return
    }
    userStore.resetPassword(token.value, password.value)
        .then(() => {
            $q.notify({
                type: 'positive',
                message: 'Das Zurücksetzen des Passworts war erfolgreich.',
                timeout: 1000
            })
            router.push('/login') // Redirect to login page after successful reset
        })
        .catch(() => {
            $q.notify({
                type: 'negative',
                message: 'Das Zurücksetzen des Passworts ist fehlgeschlagen.',
                timeout: 1000
            })
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
                <div class="text-h4 q-ma-sm">Passwort zurücksetzen</div>
            </div>
            <div class="row-auto q-ma-sm">
                <q-input v-model="password" type="password" label="New Password"/>
                <q-input v-model="confirmPassword" type="password" label="Confirm Password"/>
                <div class="row-auto">
                    <div class="col q-mt-sm">
                        <q-btn label="Passwort zurücksetzen" @click="resetPassword" color="primary"
                               class="row-auto q-ma-xs"></q-btn>
                    </div>
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
.col-auto {
    background-color: #ffffff;
}
</style>