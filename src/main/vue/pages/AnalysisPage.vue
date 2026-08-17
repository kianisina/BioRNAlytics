<template>
  <q-page class="flex flex-center column q-pa-md">
    
    <q-card style="width: 400px; max-width: 90vw;" class="q-pa-md text-center">
      <h5 class="q-mt-none">Volcano Plot Generator</h5>
      
      <q-file 
        v-model="selectedFile" 
        label="Upload Count Data (.txt)" 
        accept=".txt,.csv"
        outlined
        class="q-mb-md"
      >
        <template v-slot:prepend>
          <q-icon name="attach_file" />
        </template>
      </q-file>

      <q-btn 
        color="primary" 
        label="Generate Plot" 
        @click="uploadAndGenerate" 
        :loading="isLoading"
        :disable="!selectedFile"
        class="full-width"
      />
    </q-card>

    <q-card v-if="plotImageUrl" class="q-mt-xl q-pa-md text-center" style="max-width: 800px;">
      <h6 class="q-mt-none">Your Analysis</h6>
      <img :src="plotImageUrl" style="max-width: 100%; height: auto;" alt="Volcano Plot" />
    </q-card>

  </q-page>
</template>

<script setup>
import { ref } from 'vue'



// State variables
const selectedFile = ref(null)
const plotImageUrl = ref(null)
const isLoading = ref(false)

const uploadAndGenerate = async () => {
  if (!selectedFile.value) return;

  isLoading.value = true;
  plotImageUrl.value = null;

  const formData = new FormData();
  formData.append('file', selectedFile.value);

  try {
    // Grab the token directly from Pinia! 
  
    const token = authStore.token || localStorage.getItem('token'); 
    
    console.log("Checking token before sending:", token); // <-- Look at this in your F12 Console!

    // const response = await axios.post('http://localhost:8080/api/analysis/volcano', formData, {
    //   headers: {
    //     'Content-Type': 'multipart/form-data',
    //     'Authorization': `Bearer ${token}` 
    //   },
    //   responseType: 'blob' 
    // });

    // plotImageUrl.value = URL.createObjectURL(response.data);

  } catch (error) {
    console.error("Upload failed!", error);
    alert("Failed to generate plot. Check console.");
  } finally {
    isLoading.value = false;
  }
}
</script>