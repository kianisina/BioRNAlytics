<script setup>
import {ref, inject, onMounted, watch} from "vue";
import {useRoute, useRouter} from "vue-router";
import {useUserStore} from "@/main/vue/stores/users";
import {useQuasar} from "quasar";
const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const $q = useQuasar()
const id = decodeURIComponent(route.params.id)
const user = ref(null)
const currentUser = ref(null)
// Inject the switchTab method from the parent
const switchTab = inject('switchTab');
const navigateToUserManagement = () => {
    switchTab('userManagement');
};
const company = ref([
    'Universität Bielefeld',
    'Hochschule Bielefeld'
]);

userStore.requestUser(id).then(() => {
    user.value = userStore.findUserByUsername(id)
})
const fetchCurrentUser = async () => {
    await userStore.requestCurrentUser().then(res => {
        currentUser.value = userStore.findUserByUsername(res);
        console.log(currentUser.value, "fjoojfjoj");
    }).catch(err => {
        console.error("Error fetching current user:", err);
    });
};
const hasRole = (role) => {
    return currentUser.value && currentUser.value.roles.includes(role);
};
onMounted(() => {
    fetchCurrentUser();
});
console.log(currentUser.value,"eeeefjoojfjoj")
watch(currentUser, (newValue) => {
    if (newValue) {
        console.log(`User updated: ${newValue.email}`);
        // Now you can safely use hasRole
        if (hasRole('ROLE_admin')) {
            console.log('User has admin role');
        } else {
            console.log('User does not have admin role');
        }
    }
});
function finishEditing() {
    userStore.requestUpdateUser(id, user.value).then(() => {
        $q.notify({
            type: 'positive',
            message: 'Speichern erfolgreich',
            caption: 'User konnte erfolgreich gespeichert werden.',
            timeout: 1000
        })
        router.go(-1)
        //navigateToUserManagement();
        //router.push('/admin-settings')
    }).catch(() => {
        $q.notify({
            type: 'negative',
            message: 'Speichern Fehlgeschlagen',
            caption: 'User konnte nicht gespeichert werden.',
            timeout: 1000
        })
    })
}
function deleteUser() {
    userStore.deleteUser(id).then(() => {
        userStore.requestCurrentUser()
        router.go(-1)
    }).catch(() => {
        userStore.requestCurrentUser()
        $q.notify({
            type: 'negative',
            message: 'Löschen Fehlgeschlagen',
            caption: 'User konnte nicht gelöscht werden'
        })
    })
}
</script>
<template>
           <q-page>
               <q-card v-if="user">
                   <q-card-section>
                       <div class="text-h6">User bearbeiten</div>
                   </q-card-section>
                   <q-separator inset/>
                   <q-card-section>
                       <q-input v-model="user.username" label="Id"/>
                   </q-card-section>
                   <q-card-section>
                       <q-input placeholder="Vorname" v-model="user.firstname" label="Vorname"/>
                   </q-card-section>
                   <q-card-section>
                       <q-input placeholder="Nachname" v-model="user.lastname" label="Nachname"/>
                   </q-card-section>
                   <q-card-section>
                       <q-input placeholder="E-Mail" v-model="user.email" label="E-Mail"/>
                   </q-card-section>
                   <q-card-section>
                       <!-- -->
                       <q-select outlined v-model="user.center" :options="company" label="Company Name" />
                   </q-card-section>
                   
                   <q-card-section>
                       <q-input placeholder="Age" v-model="user.age" label="Age"/>
                   </q-card-section>
                   <q-card-section>
                       <div>Checked Roles: {{ user.roles }}</div>
                       <input type="checkbox" id="isAdmin" value="ROLE_admin" v-model="user.roles" :disabled="!hasRole('ROLE_admin')"/>
                       <label for="isAdmin">Admin </label>
                       <input type="checkbox" id="isProfessor" value="ROLE_professor" v-model="user.roles" :disabled="!hasRole('ROLE_admin')"/>
                       <label for="isProfessor">Professor </label>
                       <input type="checkbox" id="isStudent" value="ROLE_student" v-model="user.roles" :disabled="!hasRole('ROLE_admin')"/>
                       <label for="isStudent">Student </label>
                       <input type="checkbox" id="isEmployee" value="ROLE_employee" v-model="user.roles" :disabled="!hasRole('ROLE_admin')"/>
                       <label for="isEmployee">Employee </label>
                   </q-card-section>
                   <q-card-actions>
                       <q-btn icon="save" label="Speichern" @click="finishEditing"/>
                       <q-btn icon="delete" label="Löschen" @click="deleteUser" type="negative" :disabled="!hasRole('ROLE_admin')"/>
                   </q-card-actions>
               </q-card>
               <div v-else>
                   <q-spinner class="absolute-center" color="primary" size="3em"/>
               </div>
           </q-page>
   </template>
   <style scoped>
   </style>