<script setup>
import {ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {useUserStore} from "@/main/vue/stores/users";
import {useQuasar} from "quasar";
const userStore = useUserStore();
const router = useRouter();
const route = useRoute();
const $q = useQuasar();
let user = ref(null);
const company = ref([
    'Universität Bielefeld',
    'Hochschule Bielefeld'
]);

const newPassword = ref('');
const confirmPassword = ref('');
userStore.requestCurrentUser().then(res => {
    console.log(res, "asdfghj")
    console.log(userStore.findUserByUsername(res), "testtt")
    user.value = userStore.findUserByUsername(res)
})
function finishEditing() {
    if (newPassword.value !== confirmPassword.value) {
        $q.notify({
            type: 'negative',
            message: 'Passwörter stimmen nicht überein',
            caption: 'Bitte überprüfen Sie, dass die Passwörter übereinstimmen.'
        });
        return;
    }
    const updateData = {
        ...user.value,
        password: newPassword.value
    };
    console.log(updateData,"bnm,vcdc")
    userStore.requestUpdateUser(user.value.username, updateData).then(() => {
        $q.notify({
            type: 'positive',
            message: 'Speichern erfolgreich',
            caption: 'User konnte erfolgreich gespeichert werden.',
            timeout: 1000
        })
        //router.go(-1);
    }).catch(() => {
        $q.notify({
            type: 'negative',
            message: 'Speichern Fehlgeschlagen',
            caption: 'User konnte nicht gespeichert werden.',
            timeout: 1000
        });
    });
}
</script>
<template>
    <q-page>
        <q-card v-if="user">
            <q-card-section>
                <div class="text-h6">User bearbeiten</div>
            </q-card-section>
            <q-separator inset/>
            <q-card-section class="centered-username">
                <q-input v-model="user.username" label="Id" readonly class="readonly-input centered-input"/>
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
                <q-select outlined v-model="user.center" :options="company" label="Company Name" />
            </q-card-section>
            
            <q-card-section>
                <q-input placeholder="Age" v-model="user.age" label="Age"/>
            </q-card-section>
            <q-card-section class="readonly-input">
                <input type="checkbox" id="isAdmin" value="ROLE_admin" v-model="user.roles" disabled/>
                <label for="isAdmin" >Admin</label>
                <input type="checkbox" id="isProfessor" value="ROLE_professor" v-model="user.roles" disabled/>
                <label for="isProfessor">Professor</label>
                <input type="checkbox" id="isStudent" value="ROLE_student" v-model="user.roles" disabled/>
                <label for="isStudent">Student</label>
                <input type="checkbox" id="isEmployee" value="ROLE_employee" v-model="user.roles" disabled/>
                <label for="isEmployee">Employee</label>
            </q-card-section>
            <q-card-section>
                <q-input v-model="newPassword" label="Neues Passwort" type="password" />
            </q-card-section>
            <q-card-section>
                <q-input v-model="confirmPassword" label="Passwort bestätigen" type="password" />
            </q-card-section>
            <q-card-actions>
                <q-btn icon="save" label="Speichern" @click="finishEditing"/>
            </q-card-actions>
        </q-card>
        <div v-else>
            <q-spinner class="absolute-center" color="primary" size="3em"/>
        </div>
    </q-page>
</template>
<style scoped>
.readonly-input {
    background-color: #f0f0f0;
    color: #888;
    text-align: center; 
}
.centered-username {
    display: flex;
    justify-content: center;
    align-items: center; 
}
.centered-input {
    width: 100%; 
    text-align: center; 
    font-size: 2.2em; 
}

</style>