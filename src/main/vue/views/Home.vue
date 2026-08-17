<script setup>
import { ref, computed } from 'vue';
import { useQuasar } from 'quasar';
import { useAnalysisStore } from '@/main/vue/stores/analysis';
import { useRouter } from 'vue-router';

const $q = useQuasar();
const analysisStore = useAnalysisStore();
const router = useRouter();

// Local Form State
const countFile = ref(null);
const groups = ref([
    { name: 'control', samples: 'H3_control, H5_control, H12_control' },
    { name: 'Ser', samples: 'H3_Ser, H5_Ser, H12_Ser' }
]);

// Methods to add/remove groups
function addGroup() {
    groups.value.push({ name: '', samples: '' });
}

function removeGroup(index) {
    if (groups.value.length > 2) {
        groups.value.splice(index, 1);
    } else {
        $q.notify({ type: 'warning', message: 'Sie benötigen mindestens 2 Gruppen.' });
    }
}

function startAnalysis() {
    // Validation: Check if any group is empty
    const hasEmptyGroups = groups.value.some(g => !g.name.trim() || !g.samples.trim());
    if (hasEmptyGroups) {
        $q.notify({ type: 'warning', message: 'Bitte füllen Sie alle Gruppennamen und Samples aus.' });
        return;
    }

    // Dynamically build the design matrix for ANY number of groups!
    const designMatrix = {};
    groups.value.forEach(group => {
        designMatrix[group.name.trim()] = group.samples.split(',').map(s => s.trim());
    });

    analysisStore.startAnalysis(designMatrix)
        .then(() => {
            $q.notify({ type: 'positive', message: 'Analyse gestartet!' });
            router.push('/');
        })
        .catch(() => {
            $q.notify({ type: 'negative', message: 'Fehler beim Starten der Analyse.' });
        });
}
function handleFileSelection(file) {
    if (!file) return;

    analysisStore.uploadFile(file)
        .then(() => {
            $q.notify({ type: 'positive', message: 'Upload abgeschlossen!' });
        })
        .catch(() => {
            $q.notify({ type: 'negative', message: 'Upload fehlgeschlagen.' });
            countFile.value = null; // Reset the input if it fails
        });
}


// Computed property checking the store state
const isReadyToStart = computed(() => {
    return analysisStore.uploadedFileId !== null && !analysisStore.isAnalyzing;
});
</script>

<template>
    <div padding class="bg-white text-black">
        <div class="row justify-center">
            <q-card class="col-12 col-md-8 shadow-3">
                
                <q-card-section>
                    <div class="text-h6">1. Upload Count Data</div>
                    
                    <q-file 
                        v-model="countFile" 
                        label="Count Matrix (.csv, .txt)" 
                        outlined 
                        accept=".txt, .csv, .tsv"
                        class="q-mt-sm"
                        @update:model-value="handleFileSelection"
                        :disable="analysisStore.isUploading || analysisStore.isAnalyzing"
                    >
                        <template v-slot:prepend><q-icon name="cloud_upload" /></template>
                        
                        <template v-slot:append v-if="analysisStore.uploadedFileId">
                            <q-icon name="check_circle" color="positive" />
                        </template>
                    </q-file>

                    <div v-if="analysisStore.isUploading" class="q-mt-md">
                        <div class="text-caption">Uploading... {{ analysisStore.uploadProgress }}%</div>
                        <q-linear-progress :value="analysisStore.uploadProgress / 100" color="primary" class="q-mt-xs" />
                    </div>
                </q-card-section>

                <q-separator />

                <q-card-section>
    <div class="row justify-between items-center q-mb-md">
        <div class="text-h6">2. Experimental Design</div>
        <q-btn color="secondary" icon="add" label="Gruppe hinzufügen" outline @click="addGroup" />
    </div>
    
    <div class="row q-col-gutter-md q-mt-sm">
        <div class="col-12" v-for="(group, index) in groups" :key="index">
            <q-card bordered flat class="bg-grey-1">
                <q-card-section class="row items-center q-pb-none">
                    <div class="text-subtitle1 text-weight-bold">Gruppe {{ index + 1 }}</div>
                    <q-space />
                    <q-btn v-if="groups.length > 2" icon="delete" color="negative" flat round dense @click="removeGroup(index)" />
                </q-card-section>
                
                <q-card-section class="row q-col-gutter-md">
                    <div class="col-12 col-md-4">
                        <q-input outlined v-model="group.name" label="Gruppenname (z.B. Ser_p38)" bg-color="white" />
                    </div>
                    <div class="col-12 col-md-8">
                        <q-input outlined v-model="group.samples" label="Samples (kommagetrennt)" bg-color="white" />
                    </div>
                </q-card-section>
            </q-card>
        </div>
    </div>

    <div class="q-mt-xl text-center">
        <q-btn 
            color="primary" icon="science" label="DESeq2 Analyse Starten" 
            @click="startAnalysis" :loading="analysisStore.isAnalyzing"
            :disable="!isReadyToStart" size="lg"
        />
    </div>
</q-card-section>
            </q-card>
        </div>
    </div>
</template>