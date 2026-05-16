import axios from 'axios'
import { API_URL } from '@/config/api'
import router from '@/router'
import { useAuthStore } from '@/stores/authStore'
import { checkVer } from '@/composables/useVersionCheck'

export const api = axios.create({
	baseURL: API_URL,
	withCredentials: true,
	timeout: 1200000,
})

api.interceptors.request.use(
	async (config) => {
		if (config.url?.includes('version.json')) return config
		const ok = await checkVer()
		if (!ok) {
			const authStore = useAuthStore()
			alert('버전이 업데이트 되었습니다. 로그인 페이지로 이동합니다.')
			await authStore.logout()
			await router.push('/auth/login')
			return Promise.reject(new Error('APP_VERSION_CHANGED'))
		}
		return config
	},
	(error) => Promise.reject(error)
)

api.interceptors.response.use(
	(response) => response,
	async (error) => {
		const status = error.response?.status
		const configUrl = error.config?.url || ''

		if (status === 401) {
			const authStore = useAuthStore()

			// 💡 [확인사살] 세션 만료 메시지 표시
			if (!configUrl.includes('/comm/session')) {
				alert('세션이 만료되었습니다. 다시 로그인해 주십시오.')
			}

			authStore.resetState()
			sessionStorage.clear()
			if (!router.currentRoute.value.path.includes('/login')) {
				await router.push('/auth/login')
			}
		}
		return Promise.reject(error)
	}
)
