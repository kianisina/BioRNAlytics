import {fileURLToPath, URL} from 'url'

import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import {quasar, transformAssetUrls} from '@quasar/vite-plugin'
import {VitePWA} from "vite-plugin-pwa";

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [
        vue({
                template: transformAssetUrls
            }
        ), VitePWA({
            registerType: "autoUpdate",
            manifest: {
                name: "example-application",
                short_name: "example",
                description: "This is an example application",
                theme_color: "#ffffff",
                icons: [
                    {
                        src: "android-chrome-512x512.png",
                        sizes: "512x512",
                        type: "image/png",
                        purpose: "any maskable"
                    }
                ]
            },
            devOptions: {
                enabled: true
            }
        })]
    , resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    }, server: {
        proxy: {
            /*
                proxy all webpack dev-server requests starting with /api
                to our Spring Boot backend (localhost:8088) using http-proxy-middleware
                see https://vitejs.dev/config/#server-proxy
            */
            '/api': {
                target: 'http://localhost:8080', ws: true, changeOrigin: true
            }
        }
    }, build: {
        outDir: 'src/main/resources/public', assetsDir: 'static'
    }
})