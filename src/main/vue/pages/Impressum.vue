<script setup>
import { useSettingsStore } from '@/main/vue/stores/settings';
const settingsStore = useSettingsStore();
const imprintText = settingsStore.corporateDesign.imprintText;
</script>
<template>
    <q-page>
        <q-card>
            <q-card-section>
                <div class="text-h6">Impressum</div>
            </q-card-section>
            <q-separator />
            <q-card-section>
                <div v-html="imprintText"></div>
            </q-card-section>
        </q-card>
    </q-page>
</template>
<style scoped>

</style>