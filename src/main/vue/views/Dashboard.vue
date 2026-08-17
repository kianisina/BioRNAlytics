<template>
    <q-page padding class="bg-grey-1">
        
        <div class="row q-mb-lg">
            <div class="col-12 text-center">
                <div class="text-h4 text-primary text-weight-bold">RNA-Seq Analysis Dashboard</div>
                <div class="text-subtitle1 text-grey-7">Laden Sie Ihre Daten hoch und verwalten Sie Ihre DESeq2 Analysen</div>
            </div>
        </div>

        <div class="row q-col-gutter-lg">
            
            <div class="col-12 col-md-4">
                <q-card flat bordered class="shadow-2 bg-white">
                    <q-card-section class="bg-primary text-white">
                        <div class="text-h6">1. Neue Analyse</div>
                    </q-card-section>
                    <q-card-section>
                        <HomePanel />
                    </q-card-section>
                </q-card>
            </div>

            <div class="col-12 col-md-8">
                <q-card flat bordered class="shadow-2 bg-white">
                    <q-card-section class="bg-accent text-white">
                        <div class="text-h6">2. Ergebnisse & Verlauf</div>
                    </q-card-section>
                    <q-card-section>
                        <HistoryPanel />
                    </q-card-section>
                </q-card>
            </div>

        </div>
    </q-page>
</template>

<script setup>
import HomePanel from './Home.vue';
import HistoryPanel from '../pages/History.vue';
</script>