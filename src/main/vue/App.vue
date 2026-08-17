<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/main/vue/stores/users';
import { useSettingsStore } from '@/main/vue/stores/settings';

const userStore = useUserStore();
const settingsStore = useSettingsStore();
const leftDrawerOpen = ref(false);
const router = useRouter();
const currentUser = ref(null);

// 1. Add a loading state
const isLoadingSettings = ref(true); 

function toggleLeftDrawer() {
    leftDrawerOpen.value = !leftDrawerOpen.value;
}

function handleLogout() {
    userStore.logout();
    router.push('/login');
}

// 2. Wait for the fetch to complete before turning off the loading state
onMounted(async () => {
    try {
        await settingsStore.fetchCorporateDesign();
    } catch (error) {
        console.error('Failed to fetch corporate design', error);
    } finally {
        isLoadingSettings.value = false;
    }
});

// Create a computed property for the logo URL
const logoUrl = computed(() => {
    const logo = settingsStore.corporateDesign?.logo;
    return logo ? `data:image/png;base64,${logo}` : 'https://upload.wikimedia.org/wikipedia/commons/d/d5/Deutsche_Bahn_AG-Logo.svg';
});
const companyName = computed(() => {
    return settingsStore.corporateDesign?.companyName || 'Bioinformatics';
});
</script>

<template>
    <q-layout view="hHh lpR fFf" :style="{backgroundColor: settingsStore.corporateDesign.backgroundColor, color:settingsStore.corporateDesign.fontColor}">
        <q-header v-if="userStore.authenticated" elevated class="bg-primary text-white" height-hint="98">
            <q-toolbar :style="{backgroundColor: settingsStore.corporateDesign.backgroundColor, color:settingsStore.corporateDesign.fontColor}">
                
                <q-spinner v-if="isLoadingSettings" size="2em" class="q-mr-sm" />
                <img v-else-if="logoUrl" :src="logoUrl" alt="Logo" style="height: 50px; margin-right: 10px;" />
                
                <q-toolbar-title>
                    <q-spinner-dots v-if="isLoadingSettings" color="primary" size="2em" />
                
                    <div v-else class="text-h4 q-ma-sm">{{ companyName }}</div>
                </q-toolbar-title>
            </q-toolbar>
            <q-tabs v-if="isLoadingSettings" align="left">
                <q-tab disable>
                <q-spinner-dots color="primary" size="2em" />
                </q-tab>
            </q-tabs>

            <q-tabs 
                v-else 
                align="left" 
                :style="{
                    backgroundColor: settingsStore.corporateDesign?.backgroundColor, 
                    color: settingsStore.corporateDesign?.fontColor
                }"
            >
                <q-route-tab to="/profile" label="Profil" icon="account_circle" />
                <q-route-tab to="/home" label="Home" icon="home" />
                <q-route-tab to="/history" label="History" icon="history" />
                <q-route-tab to="/admin-settings" label="admin settings" icon="settings" />
            </q-tabs>
        </q-header>
        <q-drawer show-if-above v-model="leftDrawerOpen" side="left" bordered>
            <q-list>
                <q-item v-if="userStore.authenticated" clickable>
                    <q-item-section avatar>
                        <q-icon name="newspaper" />
                    </q-item-section>
                    <q-item-section>
                        <router-link to="/" class="col-sm-auto custom-link" :style="{color:settingsStore.corporateDesign.fontColor}">Übersicht</router-link>
                    </q-item-section>
                </q-item>
                <q-item v-if="userStore.authenticated" @click="handleLogout" clickable>
                    <q-item-section avatar>
                        <q-icon name="logout" />
                    </q-item-section>
                    <q-item-section>
                        Abmelden
                    </q-item-section>
                </q-item>
                <q-item v-else clickable>
                    <q-item-section avatar>
                        <q-icon name="login" />
                    </q-item-section>
                    <q-item-section>
                        <router-link to="/login" :style="{color:settingsStore.corporateDesign.fontColor}">Anmelden</router-link>
                    </q-item-section>
                </q-item>
                <q-item v-if="!userStore.authenticated" clickable>
                    <q-item-section avatar>
                        <q-icon name="person_add" />
                    </q-item-section>
                    <q-item-section>
                        <router-link to="/register" :style="{color:settingsStore.corporateDesign.fontColor}" >Konto erstellen</router-link>
                    </q-item-section>
                </q-item>
            </q-list>
        </q-drawer>
        <q-page-container>
            <router-view />
        </q-page-container>
        <q-footer bordered class="bg-grey-8 text-white">
            <q-toolbar>
                <q-toolbar-title>
                    <div></div>
                </q-toolbar-title>
                <q-item clickable>
                    <q-item-section avatar>
                        <q-icon name="newspaper" />
                    </q-item-section>
                    <q-item-section>
                        <router-link class="link-white custom-link test" to="/impressum">Impressum</router-link>
                    </q-item-section>
                </q-item>
            </q-toolbar>
        </q-footer>
    </q-layout>
</template>

<style scoped>
.toolbar-title {
    text-align: center;
}
.custom-link {
    text-decoration: none; /* Unterstreichung entfernen */
}
.test {
    color: white;
}
</style>