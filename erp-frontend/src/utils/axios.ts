import axios from 'axios'
import { API_URL } from '@/config/api'
import router from '@/router'
import { useAuthStore } from '@/stores/authStore'
import { checkVer } from '@/composables/useVersionCheck'

/**
 * 💡 객체의 키를 소문자로 변환하고 DB 별칭(A.)을 완벽히 제거하는 유틸리티
 * 원칙: 모든 프론트엔드 코드는 소문자 키로 통일한다.
 * 문제 해결: DB가 LINECD 혹은 A.LINECD로 주더라도 JS에서는 linecd로 접근하게 함.
 */
const standardizeKeys = (obj: any): any => {
	if (obj === null || typeof obj !== 'object' || obj instanceof FormData) return obj
	if (Array.isArray(obj)) return obj.map(standardizeKeys)
	const newObj: any = {}
	for (const key in obj) {
		if (Object.prototype.hasOwnProperty.call(obj, key)) {
			// 1. 소문자화 (A.LINECD -> a.linecd)
			let newKey = key.toLowerCase()
			// 2. 별칭 제거 (a.linecd -> linecd)
			if (newKey.includes('.')) {
				newKey = newKey.split('.').pop() || newKey
			}
			newObj[newKey] = standardizeKeys(obj[key])
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
		if (config.url?.includes('version.json')) return config
		const ok = await checkVer()
		if (!ok) {
			const authStore = useAuthStore()
			alert('버전이 업데이트 되었습니다. 로그인 페이지로 이동합니다.')
			await authStore.logout()
			await router.push('/auth/login')
			return Promise.reject(new Error('APP_VERSION_CHANGED'))
		}
		// 보낼 때 키 소문자 정규화
		if (config.data && !(config.data instanceof FormData)) config.data = standardizeKeys(config.data)
		if (config.params) config.params = standardizeKeys(config.params)
		return config
	},
	(error) => Promise.reject(error)
)

api.interceptors.response.use(
	(response) => {
		const resData = response.data
		let content = resData

		/**
		 * 🚀 [전역 표준화 로직]
		 * 어떤 형태의 응답이 오더라도 최종적으로는 "소문자 알맹이"만 반환한다.
		 */

		// 1. ApiResponse 껍데기 제거 로직 ({ status, data, message } 구조인 경우)
		if (resData && typeof resData === 'object' && 'status' in resData && 'data' in resData) {
			content = resData.data
		}

		// 2. 모든 데이터의 키를 소문자로 변환하고 별칭 제거 (A.LINECD -> linecd)
		const finalData = standardizeKeys(content)

		// 🚀 [결과] 이제 모든 Vue 컴포넌트에서는 res.data를 "알맹이 데이터"로 즉시 사용 가능
		return { ...response, data: finalData }
	},
	async (error) => {
		// 🚀 세션 유실(401) 또는 권한 없음(403) 발생 시 자동 로그아웃 처리
		if (error.response?.status === 401 || error.response?.status === 403) {
			const authStore = useAuthStore()

			// 무한 루프 방지: 현재 페이지가 로그인 페이지가 아닐 때만 처리
			if (!router.currentRoute.value.path.includes('/login')) {
				alert('세션이 만료되었거나 연결 정보가 유실되었습니다.\n로그인 페이지로 이동합니다.')
				authStore.resetState()
				sessionStorage.clear()
				await router.push('/auth/login')
			}
		}
		return Promise.reject(error)
	}
)
