<script setup>
import { ref, computed } from 'vue';
import { useUserStore } from '@/main/vue/stores/users';
const userStore = useUserStore();
userStore.requestUsers();
const searchName = ref('');
const searchEmail = ref('');
const filterRoles = ref({
    admin: false,
    professor: false,
    student: false,
    employee: false,
});
const filterServices = ref({
    'Universität Bielefeld': false,
    'Hochschule Bielefeld': false,
});

const filteredUsers = computed(() => {
    return userStore.usersOverview.filter(user => {
        const matchesName = user.firstname.toLowerCase().includes(searchName.value.toLowerCase()) || user.lastname.toLowerCase().includes(searchName.value.toLowerCase());
        const matchesEmail = user.email.toLowerCase().includes(searchEmail.value.toLowerCase());
        const activeRoles = Object.keys(filterRoles.value).filter(role => filterRoles.value[role]);
        const matchesRole = activeRoles.length === 0 || activeRoles.some(role => user.roles.map(r => r.replace('ROLE_', '')).includes(role));
        const activeServices = Object.keys(filterServices.value).filter(service => filterServices.value[service]);
        const matchesService = activeServices.length === 0 || activeServices.includes(user.center);
        return matchesName && matchesEmail && matchesRole && matchesService;
    });
});
const columns = [
    { name: 'name', label: 'Name', align: 'left', field: row => `${row.firstname} ${row.lastname}`, sortable: true },
    { name: 'email', label: 'E-Mail', align: 'left', field: 'email', sortable: true },
    { name: 'roles', label: 'Roles', align: 'left', field: 'roles', format: val => val ? val.map(role => role.replace('ROLE_', '')).join(', ') : '', sortable: true },
    { name: 'company', label: 'Company Name', align: 'left', field: 'center', sortable: true },
    { name: 'age', label: 'Age', align: 'left', field: 'age', sortable: true },
    { name: 'isAccountNonLocked', label: 'Account gesperrt', align: 'left', field: 'isAccountNonLocked', sortable: true }
];
function getLockStatusLabel(isAccountNonLocked) {
    return isAccountNonLocked ? 'Konto entsperrt' : 'Konto gesperrt';
}
function toggleAccountStatus(user) {
    userStore.toggleAccountLockStatus(user)
}
</script>
<template>
    <q-page>
        <q-card>
            <q-card-section>
                <div class="text-h6">Benutzer filtern</div>
            </q-card-section>
            <q-separator/>
            <q-card-section class="row q-col-gutter-md">
                <q-input v-model="searchName" label="Name" class="col"/>
                <q-input v-model="searchEmail" label="E-Mail" class="col"/>
            </q-card-section>
            <q-card-section class="filter">
                <div class="q-mt-md">Roles</div>
                <q-toggle v-model="filterRoles.admin" label="Admin"/>
                <q-toggle v-model="filterRoles.professor" label="Professor"/>
                <q-toggle v-model="filterRoles.student" label="Student"/>
                <q-toggle v-model="filterRoles.employee" label="Employee"/>
                <div class="q-mt-md">Company Name</div>
                <q-toggle v-model="filterServices['Universität Bielefeld']" label="Universität Bielefeld"/>
                <q-toggle v-model="filterServices['Hochschule Bielefeld']" label="Hochschule Bielefeld"/>
                
            </q-card-section>
        </q-card>
        <q-table
            :rows="filteredUsers"
            :columns="columns"
            row-key="username"
        >
            <template v-slot:body-cell-name="props">
                <q-td :props="props">
                    <router-link :to="'/user/' + encodeURIComponent(props.row.username)">
                        {{ props.row.firstname }} {{ props.row.lastname }}
                    </router-link>
                </q-td>
            </template>
            <template v-slot:body-cell-isAccountNonLocked="props">
                <q-td :props="props">
                    <q-toggle
                        v-model="props.row.isAccountNonLocked"
                        @update:model-value="toggleAccountStatus(props.row.username)"
                        :label="getLockStatusLabel(props.row.isAccountNonLocked)"
                    />
                </q-td>
            </template>
        </q-table>
    </q-page>
</template>
<style scoped>
/* Add your styles here if necessary */
.filter {
    color: black;
}
</style>