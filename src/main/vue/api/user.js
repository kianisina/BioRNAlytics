import axios from 'axios';
export default {
    list() {
        return axios.get('/api/users/showUsers');
    },
    get(id) {
        return axios.get('/api/users/showUsers/' + id);
    },
    update(id, user) {
        let userCMD = {
            username: user.username,
            email: user.email,
            firstname: user.firstname,
            lastname: user.lastname,
            password: user.password,
            roles: user.roles,
            age: user.age,
            center: user.center,
            isAccountNonLocked: user.isAccountNonLocked
        };
        console.log(userCMD,"likeee")
        return axios.put('/api/users/updateUser/' + id, userCMD);
    },
    getCurrent() {
        return axios.get('/api/users/showCurrentUser');
    },
    delete(id) {
        return axios.delete('/api/users/deleteUser/' + id);
    },
    updateAccountLockStatus(username, isAccountNonLocked) {
        return axios.put(`/api/users/updateAccountLockStatus/${username}`, { isAccountNonLocked });
    }
};