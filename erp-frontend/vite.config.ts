import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
	plugins: [
		vue(),
		vueDevTools(),
		VitePWA({
			registerType: 'autoUpdate',
			manifest: {
				name: 'SmartCore',
				short_name: 'ERP',
				start_url: '/',
				display: 'standalone',
				theme_color: '#ffffff',
				background_color: '#ffffff',
				icons: [],
			},
			workbox: {
				cleanupOutdatedCaches: true,
				clientsClaim: true,
				skipWaiting: true,
			},
		}),
	],
	resolve: {
		alias: {
			'@': fileURLToPath(new URL('./src', import.meta.url)),
		},
	},
	server: {
		host: '0.0.0.0',
		port: 5173,
		fs: {
			strict: false,
		},
		proxy: {
			// 💡 /api/crm 으로 시작하는 요청은 /api 를 제거하고 백엔드로 전달
			// (백엔드 컨트롤러가 /crm 으로 시작하기 때문)
			'/api/crm': {
				target: 'http://localhost:8080',
				changeOrigin: true,
				secure: false,
				rewrite: (path) => path.replace(/^\/api/, ''),
			},
			// 💡 나머지 /api 요청은 그대로 백엔드로 전달
			'/api': {
				target: 'http://localhost:8080',
				changeOrigin: true,
				secure: false,
			},
		},
	},
})
