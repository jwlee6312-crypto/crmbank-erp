import axios from 'axios'
import { API_URL } from '@/config/api'
import router from '@/router'
import { useAuthStore } from '@/stores/authStore'
import { checkVer } from '@/composables/useVersionCheck'

/**
 * 💡 모든 전송 데이터(Body, Params)의 문자열 앞뒤 공백을 제거하는 유틸리티
 */
const deepTrim = (obj: any): any => {
	if (obj === null || typeof obj !== 'object') {
		return typeof obj === 'string' ? obj.trim() : obj
	}

	if (Array.isArray(obj)) {
		return obj.map((v) => deepTrim(v))
	}

	const newObj: any = {}
	for (const key in obj) {
		if (Object.prototype.hasOwnProperty.call(obj, key)) {
			newObj[key] = deepTrim(obj[key])
		}
	}
	return newObj
}

export const api = axios.create({
	baseURL: API_URL,
	withCredentials: true,
	timeout: 1200000,
})

api.interceptors.request.use(
	async (config) => {
		// 1. 버전 체크
		if (config.url?.includes('version.json')) return config
		const ok = await checkVer()
		if (!ok) {
			const authStore = useAuthStore()
			alert('버전이 업데이트 되었습니다. 로그인 페이지로 이동합니다.')
			await authStore.logout()
			await router.push('/auth/login')
			return Promise.reject(new Error('APP_VERSION_CHANGED'))
		}

		// 2. 💡 모든 데이터(JSON Body, Query Params) 자동 Trim 처리 (백엔드 Truncation 에러 방지)
		if (config.data && !(config.data instanceof FormData)) {
			config.data = deepTrim(config.data)
		}
		if (config.params) {
			config.params = deepTrim(config.params)
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
