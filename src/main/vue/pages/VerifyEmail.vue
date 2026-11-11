<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api'
import { useSettingsStore } from '@/main/vue/stores/settings';

// Wird über den Link in der Bestätigungs-Mail aufgerufen: /verify-email?token=...
const settingsStore = useSettingsStore();
const logoUrl = computed(() => {
    const logo = settingsStore.corporateDesign.logo;
    return logo ? `data:image/png;base64,${logo}` : '/logo.png';
});
const route = useRoute()
const router = useRouter()
// 'loading' | 'success' | 'error'
const status = ref('loading')

onMounted(async () => {
    const token = route.query.token
    if (!token) {
        status.value = 'error'
        return
    }
    try {
        await api.auth.verifyEmail(token)
        status.value = 'success'
    } catch (error) {
        status.value = 'error'
    }
})

function goToLoginPage() {
    router.push('/login')
}
</script>
<template>
    <q-page>
        <div class="col-auto fixed-center shadow-10 rounded-borders q-pa-md">
            <div class="row-auto relative-position">
                <img :src="logoUrl" alt="Logo" class="small-image">
            </div>
            <div class="row-auto">
                <div class="text-h5 q-ma-sm">E-Mail-Bestätigung</div>
            </div>
            <div class="row-auto q-ma-sm">
                <div v-if="status === 'loading'" class="flex flex-center q-pa-md">
                    <q-spinner color="primary" size="3em" />
                </div>
                <div v-else-if="status === 'success'">
                    <q-icon name="check_circle" color="positive" size="2em" class="q-mr-sm" />
                    Deine E-Mail-Adresse wurde bestätigt. Du kannst dich jetzt anmelden.
                </div>
                <div v-else>
                    <q-icon name="error" color="negative" size="2em" class="q-mr-sm" />
                    Der Link ist ungültig oder abgelaufen. Bitte registriere dich erneut
                    oder wende dich an einen Admin.
                </div>
                <div v-if="status !== 'loading'" class="row-auto q-mt-md">
                    <q-btn label="Zum Login" @click="goToLoginPage" color="primary" class="q-ma-xs" />
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
    max-width: 360px;
}
</style>
