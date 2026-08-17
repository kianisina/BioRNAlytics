import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import axios from "axios";
import api from "../api";
export const useUserStore = defineStore('users', () => {
    const users = ref([]);
    const authenticated = ref(null);
    function authenticate(token) {
        if (token !== null) {
            authenticated.value = true;
            localStorage.setItem('token', token);
            axios.defaults.headers['Authorization'] = token;
        } else {
            authenticated.value = false;
            localStorage.removeItem('token');
            delete axios.defaults.headers['Authorization'];
        }
    }
    const savedToken = localStorage.getItem('token');
    if (savedToken) {
        authenticate(savedToken);
    } else {
        authenticate(null);
    }
    function requestToken(credentials) {
        return new Promise((resolve, reject) => {
            api.auth.login(credentials.username, credentials.password).then(res => {
                authenticate(res.headers.authorization);
                resolve();
            }).catch(() => {
                authenticate(null);
                reject();
            });
        });
    }
    async function registerUser(userData) {
        try {
            const response = await api.auth.register(userData.username, userData.password, userData.email,
                userData.firstname, userData.lastname, userData.age, userData.center);
            console.log(userData.password,"bezabsb")
            if (response.status === 200) {
                authenticate(response.headers.authorization);
            } else if (response.status === 409) {
                throw new Error('User already exists');
            } else {
                throw new Error('Registration failed');
            }
        } catch (error) {
            authenticate(null);
            throw error;
        }
    }
    async function resetPassword(token, password) {
        try {
            const response = await api.auth.resetPassword(token, password);
            if (response.status === 200) {
                console.log('Password reset successful');
            } else {
                throw new Error('Password reset failed');
            }
        } catch (error) {
            throw error;
        }
    }
    function forgotPassword(email) {
        return new Promise((resolve, reject) => {
            api.auth.forgotPassword(email).then(() => {
                resolve();
            }).catch(error => {
                reject(error);
            });
        });
    }
    function logout() {
        authenticated.value = false;
        localStorage.removeItem('token');
        delete axios.defaults.headers['Authorization'];
    }
    const usersOverview = computed(() => users.value.map(({ username, password, email, firstname, lastname, roles, age, center, isAccountNonLocked }) => {
        return {
            username,
            password,
            email,
            firstname,
            lastname,
            roles,
            age,
            center,
            isAccountNonLocked
        };
    }));
    function requestUpdateUser(id, user) {
        return new Promise((resolve, reject) => {
            api.user.update(id, user).then(res => {
                user = res.data;
                updateUser(id, user);
                resolve();
            }).catch(() => {
                reject();
            });
        });
    }
    function deleteUser(id) {
        return new Promise((resolve, reject) => {
            api.user.delete(id).then(() => {
                resolve();
            }).catch(() => {
                reject();
            });
        });
    }
    function findUserByUsername(username) {
        return users.value.find(user => user.username === username);
    }
    function requestUser(id) {
        return new Promise((resolve, reject) => {
            api.user.get(id).then(res => {
                updateUser(id, res.data);
                resolve();
            }).catch(() => {
                reject();
            });
        });
    }
    let currentUser = ref('');
    function requestCurrentUser() {
        return new Promise((resolve, reject) => {
            api.user.getCurrent().then(res => {
                currentUser = res.data;
                resolve(res.data);
                isAdmin();
            }).catch(() => {
                reject();
            });
        });
    }
    async function isAdmin() {
        try {
            await requestUsers();
            const currentUserr = currentUser;
            const s = ref(null);
            await requestUser(currentUserr);
            s.value = findUserByUsername(currentUserr);
            console.log(s.value.roles,"pas injaaaa")
            if (s.value) {
                return s.value.roles && s.value.roles.includes('admin');
            } else {
                return false;
            }
        } catch (error) {
            return false;
        }
    }
    function requestUsers() {
        return new Promise((resolve, reject) => {
            api.user.list().then(res => {
                console.log(res.data, "pooooooor")
                users.value = res.data;
                resolve();
            }).catch(() => {
                users.value = [];
                reject();
            });
        });
    }
    function updateUser(username, refreshedUser) {
        let index = users.value.indexOf(users.value.find(user => user.username === username));
        users.value[index] = refreshedUser;
    }
    function toggleAccountLockStatus(username) {
        const user = findUserByUsername(username);
        console.log(user,"kjkjjlkk")
        if (user) {
            user.isAccountNonLocked = !user.isAccountNonLocked;
            return api.user.updateAccountLockStatus(username, user.isAccountNonLocked).then(() => {
                updateUser(username, user);
            }).catch(() => {
                user.isAccountNonLocked = !user.isAccountNonLocked; // revert on error
            });
        }
    }
    return { authenticated, isAdmin, currentUser, authenticate, requestToken, registerUser, forgotPassword, resetPassword, logout, users, usersOverview, requestUpdateUser, findUserByUsername, requestUsers, requestUser, updateUser, deleteUser, requestCurrentUser, toggleAccountLockStatus  };
});
axios.defaults.headers['Authorization'] = localStorage.getItem('token');