import { createI18n } from 'vue-i18n';
import messages from 'src/i18n';

export default ({ app }) => {
    // 1. Check if the user already chose a language in a previous visit
    let savedLang = localStorage.getItem('user-lang');
    let startingLang = 'en'; // Default fallback

    if (savedLang) {
        startingLang = savedLang;
    } else {
        // 2. Sniff the browser language (e.g., 'en-US' or 'de-DE')
        let browserLang = navigator.language || navigator.userLanguage;
        
        // Extract just the first two letters ('en' or 'de')
        let shortLang = browserLang.toLowerCase().split('-')[0];

        // 3. The Restrictor: Only allow 'de' or 'en'
        if (shortLang === 'de' || shortLang === 'en') {
            startingLang = shortLang;
        }
    }

    // 4. Initialize i18n with our smart starting language
    const i18n = createI18n({
        locale: startingLang,
        fallbackLocale: 'en',
        messages,
        legacy: false // Required for Vue 3 Composition API!
    });

    // Tell Vue to use it
    app.use(i18n);
};