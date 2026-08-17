import axios from 'axios';
export default {
    login(username, password) {
        const credentials = new URLSearchParams();
        credentials.append('username', username);
        credentials.append('password', password);
        return axios.post('/api/authenticate', credentials)
            .then(response => {
                // Check the response from the server
                if (response.data && response.data.allowed === false) {
                    // If login is not allowed, handle it here
                    throw new Error('Login not allowed');
                } else {
                    // If login is allowed, return the response
                    return response;
                }
            })
            .catch(error => {
                // Handle errors here (e.g., network error, server error)
                throw error; // Propagate the error for further handling
            });
    },
    register(username, password, email, firstname, lastname, age , center) {
        return axios.post('/api/users/register', {
            username,
            password,
            email,
            firstname,
            lastname,
            age,
            center
        });
    },
    forgotPassword(email) {
        return axios.post('/api/users/forgot-password', { email });
    },
    resetPassword(token, password) {
        return axios.post('/api/users/reset-password', { token, password });
    }
}