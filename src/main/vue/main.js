import { createApp } from 'vue'
import { createPinia } from 'pinia'

// 1. Import Quasar, Notify, AND grab all the UI components
import { Quasar, Notify } from 'quasar'
import * as components from 'quasar'

// Import Quasar icons and CSS
import '@quasar/extras/material-icons/material-icons.css'
import 'quasar/dist/quasar.css'

import App from './App.vue'
import router from './router'

const app = createApp(App)


app.use(createPinia())
app.use(router)


app.use(Quasar, {
  components,
  plugins: {
    Notify 
  }
})


app.mount('#app')