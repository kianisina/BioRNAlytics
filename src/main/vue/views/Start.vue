<script setup>
import {RouterView} from 'vue-router'
</script>
<template>
    <q-layout view="hHh lpR fFf">
        <q-page-container>
            <router-view/>
        </q-page-container>
        <q-footer elevated class="bg-secondary text-white">
            <q-toolbar>
                <q-toolbar-title>
                    <div>GPSE</div>
                </q-toolbar-title>
            </q-toolbar>
        </q-footer>
    </q-layout>
</template>
<style scoped>
</style>