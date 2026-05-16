import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/utils/axios'
import { useTabStore } from '@/stores/tabStore'
import { useMenuStore } from '@/stores/menuStore'

export const useAuthStore = defineStore(
	'auth',
	() => {
		const isAuthenticated = ref<boolean>(false)

		const CMPYCD = ref<string>('')
		const USERID = ref<string>('')
		const USERNM = ref<string>('')
		const INNER_NO = ref<string>('')
		const DEPTCD = ref<string>('')
		const DEPTNM = ref<string>('')
		const USERGRP = ref<string>('')
		const SALSYN = ref<string>('')
		const EMAIL = ref<string>('') // 💡 이메일 필드 추가

		const tabStore = useTabStore()
		const menuStore = useMenuStore()

		function setUserInfo(data: any) {
			if (!data) return;
			CMPYCD.value = data.CMPYCD || data.cmpycd || ''
			USERID.value = String(data.USERID || data.USER_ID || data.userId || data.userid || '').trim()
			USERNM.value = String(data.USERNM || data.USER_NAME || data.userName || data.usernm || '').trim()
			INNER_NO.value = String(data.INNER_NO || data.inner_no || '').trim()
			DEPTCD.value = data.DEPTCD || data.deptcd || ''
			DEPTNM.value = data.DEPTNM || data.deptnm || ''
			USERGRP.value = data.USERGRP || data.usergrp || ''
			SALSYN.value = data.SALSYN || data.salsyn || ''
			EMAIL.value = data.EMAIL || data.email || '' // 💡 이메일 세팅
			isAuthenticated.value = !!USERID.value
		}

		async function checkSession() {
			try {
				const res = await api.get('/api/comm/session')
				if (res.data) {
					setUserInfo(res.data)
					return true
				}
			} catch (e) {
				console.warn("세션 정보 없음");
				return false
			}
			return false
		}

		async function login(loginData: any) {
			try {
				const res = await api.post('/api/comm/login', {
					CMPYCD: loginData.CMPYCD,
					USERID: loginData.USERID,
					PASSWD: loginData.PASSWD
				})

				if (res.status === 200 && res.data) {
					setUserInfo(res.data)
					await menuStore.fetchMenus()
					return res.data
				}
			} catch (e: any) {
				console.error('로그인 실패:', e)
				throw new Error(e.response?.data || '로그인 중 오류가 발생했습니다.')
			}
		}

		async function logout() {
			try {
				await api.post('/api/comm/logout')
			} catch (e) {
				console.warn('로그아웃 API 호출 실패:', e)
			} finally {
				resetState()
				tabStore.tabs = []
				menuStore.clearMenus()
				sessionStorage.clear()
				localStorage.clear()
				window.location.href = '/auth/login'
			}
		}

		function resetState() {
			isAuthenticated.value = false
			CMPYCD.value = ''
			USERID.value = ''
			USERNM.value = ''
			INNER_NO.value = ''
			DEPTCD.value = ''
			DEPTNM.value = ''
			USERGRP.value = ''
			SALSYN.value = ''
			EMAIL.value = ''
		}

		return {
			isAuthenticated,
			CMPYCD, USERID, USERNM, INNER_NO, DEPTCD, DEPTNM, USERGRP, SALSYN, EMAIL,
			userId: USERID, userName: USERNM,
			login, logout, resetState, checkSession, setUserInfo
		}
	},
	{
		persist: { storage: sessionStorage },
	}
)
