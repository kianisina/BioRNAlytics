<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'; // Added nextTick
import { useAnalysisStore } from '@/main/vue/stores/analysis';
import { useQuasar, date } from 'quasar';

const $q = useQuasar();
const analysisStore = useAnalysisStore();
let pollingInterval = null;

// --- Dialog States ---
const showResultsDialog = ref(false);
const availableComparisons = ref([]);
const activeJobId = ref(null);
const isFetchingComparisons = ref(false);

const showPlotDialog = ref(false);
const currentPlotTitle = ref('');

// --- Table Setup ---
const columns = [
    { name: 'date', label: 'Datum', align: 'left', field: 'createdAt', format: val => date.formatDate(val, 'DD.MM.YYYY HH:mm') },
    { name: 'groups', label: 'Vergleich', align: 'left', field: 'comparisonSummary' },
    { name: 'status', label: 'Status', align: 'center', field: 'status' },
    { name: 'actions', label: 'Ergebnisse', align: 'right' }
];

// --- Actions ---
function openResults(jobId) {
    activeJobId.value = jobId;
    showResultsDialog.value = true;
    isFetchingComparisons.value = true;
    availableComparisons.value = [];

    analysisStore.fetchComparisons(jobId)
        .then(comparisons => {
            availableComparisons.value = comparisons;
        })
        .catch(() => {
            $q.notify({ type: 'negative', message: 'Konnte Ergebnisse nicht laden.' });
            showResultsDialog.value = false;
        })
        .finally(() => {
            isFetchingComparisons.value = false;
        });
}

// --- NEW INTERACTIVE PLOT LOGIC ---
function viewPlot(comparison) {
    // $q.loading.show({ message: 'Lade Daten für interaktiven Plot...' });
    
    // Using the Store perfectly! No more direct Axios calls here.
    analysisStore.fetchCsvText(activeJobId.value, comparison)
        .then(csvText => {
            // Parse the CSV text into a JSON array using PapaParse
            window.Papa.parse(csvText, {
                header: true,
                dynamicTyping: true,
                skipEmptyLines: true,
                complete: function(results) {
                    currentPlotTitle.value = comparison;
                    showPlotDialog.value = true;
                    
                    nextTick(() => {
                        renderPlotly(results.data, comparison);
                    });
                }
            });
        })
        .catch((err) => {
            console.error(err);
            $q.notify({ type: 'negative', message: 'Plot-Daten konnten nicht geladen werden.' });
        })
        .finally(() => {
            // $q.loading.hide();
        });
}

function renderPlotly(data, comparison) {
    // 1. Filter out genes with missing or invalid p-values to prevent math errors
    const validData = data.filter(d => d.log2FoldChange !== null && d.padj !== null && d.padj > 0 && d.padj !== "NA");
    
    // 2. Helper function to build Traces (Layers) for Plotly
    // 2. Helper function to build Traces
    const createTrace = (sigFilter, name, color) => {
        const filtered = validData.filter(d => d.Significance === sigFilter);
        return {
            x: filtered.map(d => d.log2FoldChange),
            y: filtered.map(d => -Math.log10(d.padj)), 
            // UPDATED: Show the Symbol in bold, and the raw ID below it
            text: filtered.map(d => `<b>Gene:</b> ${d.Symbol || d.Gene}<br><b>ID:</b> ${d.Gene}<br><b>Log2FC:</b> ${Number(d.log2FoldChange).toFixed(2)}<br><b>p-adj:</b> ${Number(d.padj).toExponential(2)}`),
            mode: 'markers',
            type: 'scattergl',
            name: name,
            marker: { color: color, opacity: 0.7, size: 5 },
            hoverinfo: 'text'
        };
    };
    
    const layout = {
        title: `Volcano Plot: ${comparison}`,
        xaxis: { title: 'Log2 Fold Change' },
        yaxis: { title: '-Log10 Adjusted p-value' },
        hovermode: 'closest',
        shapes: [
            // Vertical dashed lines at -1 and 1
            { type: 'line', x0: -1, x1: -1, y0: 0, y1: 1, yref: 'paper', line: { color: 'black', dash: 'dash' } },
            { type: 'line', x0: 1, x1: 1, y0: 0, y1: 1, yref: 'paper', line: { color: 'black', dash: 'dash' } },
            // Horizontal dashed line at p-adj = 0.05
            { type: 'line', x0: 0, x1: 1, xref: 'paper', y0: -Math.log10(0.05), y1: -Math.log10(0.05), line: { color: 'black', dash: 'dash' } }
        ]
    };
    
    // 3. Draw the plot inside the div!
    window.Plotly.newPlot('plotly-container', [
        createTrace('Upregulated', 'Upregulated', 'red'),
        createTrace('Downregulated', 'Downregulated', 'blue'),
        createTrace('Not Significant', 'Not Significant', 'grey')
    ], layout);
}
// ----------------------------------
const showHeatmapDialog = ref(false);
const currentPlotTitleh = ref('');
const rawHeatmapData = ref([]); // Holds the raw CSV data
const heatmapColumns = ref([]); // Holds the current order of the columns
const draggedColIndex = ref(null); // Tracks which column is being dragged

function viewHeatmap(comparison) {
    analysisStore.fetchHeatmapText(activeJobId.value, comparison)
        .then(csvText => {
            window.Papa.parse(csvText, {
                header: true,
                dynamicTyping: true,
                skipEmptyLines: true,
                complete: function(results) {
                    currentPlotTitleh.value = comparison;
                    rawHeatmapData.value = results.data;
                    
                    // 1. Set the initial column order (excluding Gene and Symbol)
                    heatmapColumns.value = Object.keys(results.data[0]).filter(k => k !== 'Gene' && k !== 'Symbol');
                    
                    showHeatmapDialog.value = true;
                    
                    nextTick(() => {
                        renderHeatmap();
                    });
                }
            });
        })
        .catch(() => {
            $q.notify({ type: 'negative', message: 'Heatmap-Daten nicht gefunden.' });
        });
}

// --- DRAG AND DROP FUNCTIONS ---
function onDragStart(event, index) {
    draggedColIndex.value = index;
    event.dataTransfer.effectAllowed = 'move';
}

function onDrop(event, index) {
    // Remove the dragged column from its old position
    const draggedColName = heatmapColumns.value.splice(draggedColIndex.value, 1)[0];
    // Insert it into the new position
    heatmapColumns.value.splice(index, 0, draggedColName);
    draggedColIndex.value = null;
    
    renderHeatmap();
}
// -------------------------------

function renderHeatmap() {
    const data = rawHeatmapData.value;
    const activeCols = heatmapColumns.value; // The currently sorted columns!

    const geneNames = data.map(row => row.Symbol ? row.Symbol : row.Gene);
    
    // Build the Z-matrix using the active column order
    const zValues = data.map(row => {
        return activeCols.map(colName => row[colName]);
    });

    const trace = {
        z: zValues,
        x: activeCols, // X-axis now uses our draggable array
        y: geneNames,
        type: 'heatmap',
        colorscale: 'RdBu',
        reversescale: true,
        zmid: 0,
        colorbar: { title: 'Z-Score' }
    };

    const layout = {
        title: `Expression Pattern of Top 50 Genes (${currentPlotTitleh.value})`,
        xaxis: { tickangle: -45, automargin: true },
        yaxis: { automargin: true, tickfont: { size: 10 } },
        margin: { l: 150, r: 50, b: 100, t: 50 }
    };

    window.Plotly.react('heatmap-container', [trace], layout);
}

function handleDownload(comparison) {
    analysisStore.downloadResultCsv(activeJobId.value, comparison).catch(() => {
        $q.notify({ type: 'negative', message: 'Download fehlgeschlagen.' });
    });
}


onMounted(() => {
    analysisStore.fetchHistory();
    pollingInterval = setInterval(() => {
        analysisStore.fetchHistory();
    }, 3000);
});

onUnmounted(() => {
    clearInterval(pollingInterval);
});

const showPcaDialog = ref(false);
const currentPcaUrl = ref('');

function viewPca(jobId) {
    // $q.loading.show({ message: 'Lade PCA Plot...' });
    
    analysisStore.getPcaUrl(jobId).then(url => {
        currentPcaUrl.value = url;
        showPcaDialog.value = true;
    }).catch(() => {
        $q.notify({ type: 'negative', message: 'PCA konnte nicht geladen werden.' });
    }).finally(() => {
        // $q.loading.hide();
    });
}

const showLogDialog = ref(false);
const currentLogContent = ref('');

function viewLog(jobId) {
    // $q.loading.show({ message: 'Lade Log...' });
    
    analysisStore.fetchJobLog(jobId).then(logText => {
        currentLogContent.value = logText;
        showLogDialog.value = true;
    }).catch(() => {
        $q.notify({ type: 'negative', message: 'Log konnte nicht geladen werden.' });
    }).finally(() => {
        // $q.loading.hide();
    });
}
</script>

<template>
    <div class="full-height">
        <div class="row justify-center">
            <div class="col-12">
                <q-table
                    title="DESeq2 Verlauf"
                    :rows="analysisStore.historyJobs"
                    :columns="columns"
                    row-key="id"
                    :loading="analysisStore.isHistoryLoading"
                    flat bordered
                    class="shadow-2"
                >
                    <template v-slot:body-cell-status="props">
                        <q-td :props="props">
                            <q-chip 
                                :color="props.row.status === 'COMPLETED' ? 'positive' : (props.row.status === 'FAILED' ? 'negative' : 'warning')" 
                                text-color="white" class="text-weight-bold"
                            >
                                {{ props.row.status }}
                                <q-spinner-tail v-if="props.row.status === 'RUNNING' || props.row.status === 'QUEUED'" color="white" size="1em" class="q-ml-sm" />
                            </q-chip>
                        </q-td>
                    </template>

                    <template v-slot:body-cell-actions="props">
                        <q-td :props="props">
                            <div class="row q-gutter-xs justify-end">
                                <q-btn v-if="props.row.status === 'COMPLETED'" 
                                       flat round color="accent" icon="bubble_chart" 
                                       @click="viewPca(props.row.id)">
                                    <q-tooltip>Dataset Quality (PCA) ansehen</q-tooltip>
                                </q-btn>

                                <q-btn v-if="props.row.status === 'COMPLETED'" 
                                       outline color="primary" icon="folder_open" label="Vergleiche" 
                                       @click="openResults(props.row.id)" />

                                <q-btn v-if="props.row.status === 'FAILED'" 
                                       outline color="negative" icon="bug_report" label="Log ansehen" 
                                       @click="viewLog(props.row.id)" />
                            </div>
                        </q-td>
                    </template>
                </q-table>
            </div>
        </div>

        <q-dialog v-model="showResultsDialog">
            <q-card style="width: 600px; max-width: 90vw;">
                <q-card-section class="bg-primary text-white row items-center">
                    <div class="text-h6">Verfügbare Vergleiche</div>
                    <q-space />
                    <q-btn icon="close" flat round dense @click="showResultsDialog = false" />
                </q-card-section>

                <q-card-section v-if="isFetchingComparisons" class="text-center q-pa-xl">
                    <q-spinner color="primary" size="3em" />
                    <div class="q-mt-md text-grey">Lade Daten...</div>
                </q-card-section>

                <q-card-section v-else class="q-pa-none">
                    <q-list bordered separator>
                        <q-item v-for="comp in availableComparisons" :key="comp" class="q-py-md">
                            <q-item-section>
                                <q-item-label class="text-weight-bold text-subtitle1">{{ comp }}</q-item-label>
                            </q-item-section>
                            
                            <q-item-section side>
                                <div class="row q-gutter-sm">
                                    <q-btn outline color="warning" icon="grid_on" label="Heatmap" @click="viewHeatmap(comp)" />
                                    <q-btn outline color="primary" icon="image" label="Plot" @click="viewPlot(comp)" />
                                    <q-btn outline color="secondary" icon="download" label="CSV" @click="handleDownload(comp)" />
                                </div>
                            </q-item-section>
                        </q-item>
                        
                        <q-item v-if="availableComparisons.length === 0" class="text-center text-grey q-pa-md">
                            Keine Vergleiche gefunden.
                        </q-item>
                    </q-list>
                </q-card-section>
            </q-card>
        </q-dialog>

        <q-dialog v-model="showPlotDialog" maximized transition-show="slide-up" transition-hide="slide-down">
            <q-card class="bg-white text-dark">
                <q-card-section class="row items-center q-pb-none shadow-1 z-top">
                    <div class="text-h6">Interaktiver Volcano Plot: {{ currentPlotTitle }}</div>
                    <q-space />
                    <q-btn icon="close" flat round dense @click="showPlotDialog = false" />
                </q-card-section>

                <q-card-section class="q-pa-none" style="height: calc(100vh - 60px);">
                    <div id="plotly-container" style="width: 100%; height: 100%;"></div>
                </q-card-section>
            </q-card>
        </q-dialog>

        <q-dialog v-model="showHeatmapDialog" maximized transition-show="slide-up" transition-hide="slide-down">
            <q-card class="bg-white text-dark column">
        
                <!-- Header -->
                <q-card-section class="row items-center q-pb-none shadow-1 z-top col-auto">
                    <div class="text-h6">Top 50 Genes Heatmap: {{ currentPlotTitleh }}</div>
                    <q-space />
                    <q-btn icon="close" flat round dense @click="showHeatmapDialog = false" />
                </q-card-section>

                <!-- The Draggable Control Bar -->
                <q-card-section class="bg-grey-2 col-auto q-py-sm border-bottom">
                    <div class="text-caption text-grey-8 q-mb-xs">
                        <q-icon name="info" size="16px" class="q-mr-xs"/> 
                        Spalten neu anordnen (Drag & Drop)
                    </div>
                    <div class="row q-gutter-sm">
                        <!-- Native HTML5 Drag and Drop on Vue Chips -->
                        <q-chip 
                            v-for="(col, index) in heatmapColumns" :key="col"
                            draggable="true"
                            @dragstart="onDragStart($event, index)"
                            @dragover.prevent
                            @drop="onDrop($event, index)"
                            color="primary" text-color="white" icon="drag_indicator" 
                            class="cursor-pointer shadow-1"
                        >
                            {{ col }}
                        </q-chip>
                    </div>
                </q-card-section>

                <!-- The Plotly Canvas -->
                <q-card-section class="q-pa-none col" style="min-height: 0;">
                    <div id="heatmap-container" style="width: 100%; height: 100%;"></div>
                </q-card-section>
            </q-card>
        </q-dialog>

        <q-dialog v-model="showPcaDialog">
            <q-card style="width: 800px; max-width: 90vw;">
                <q-card-section class="bg-accent text-white row items-center">
                    <div class="text-h6">PCA: Sample Clustering</div>
                    <q-space />
                    <q-btn icon="close" flat round dense @click="showPcaDialog = false" />
                </q-card-section>
                <q-card-section class="text-center">
                    <img :src="currentPcaUrl" style="max-width: 100%; height: auto;" />
                    <div class="q-mt-md text-caption text-grey-7">
                        Dieser Plot zeigt, wie nah Ihre Samples beieinander liegen. 
                        Replikate sollten idealerweise eng beieinander gruppiert sein.
                    </div>
                </q-card-section>
            </q-card>
        </q-dialog>

        <q-dialog v-model="showLogDialog">
            <q-card style="width: 800px; max-width: 90vw;">
                <q-card-section class="bg-negative text-white row items-center">
                    <div class="text-h6">Fehlerprotokoll (R Execution Log)</div>
                    <q-space />
                    <q-btn icon="close" flat round dense @click="showLogDialog = false" />
                </q-card-section>

                <q-card-section class="q-pa-md">
                    <pre style="white-space: pre-wrap; word-wrap: break-word; background-color: #1e1e1e; color: #d4d4d4; padding: 15px; border-radius: 5px; max-height: 60vh; overflow-y: auto;">{{ currentLogContent }}</pre>
                </q-card-section>
            </q-card>
        </q-dialog>

    </div>
</template>