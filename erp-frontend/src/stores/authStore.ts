import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '@/utils/axios'
import { useTabStore } from '@/stores/tabStore'
import { useMenuStore } from '@/stores/menuStore'

export const useAuthStore = defineStore(
	'auth',
	() => {
		const isAuthenticated = ref<boolean>(false)

		const cmpycd = ref<string>('')
		const userid = ref<string>('')
		const usernm = ref<string>('')
		const inner_no = ref<string>('')
		const deptcd = ref<string>('')
		const deptnm = ref<string>('')
		const usergrp = ref<string>('')
		const salsyn = ref<string>('')
		const email = ref<string>('')

		const tabStore = useTabStore()
		const menuStore = useMenuStore()

		function setUserInfo(data: any) {
			if (!data) return;
			cmpycd.value = data.cmpycd || ''
			userid.value = String(data.userid || '').trim()
			usernm.value = String(data.usernm || '').trim()
			inner_no.value = String(data.inner_no || '').trim()
			deptcd.value = data.deptcd || ''
			deptnm.value = data.deptnm || ''
			usergrp.value = data.usergrp || ''
			salsyn.value = data.salsyn || ''
			email.value = data.email || ''
			isAuthenticated.value = !!userid.value
		}

		async function checkSession() {
			try {
				const res = await api.get('/api/comm/session')
				if (res.data) {
					setUserInfo(res.data)
					await menuStore.fetchMenus()
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
					cmpycd: loginData.cmpycd,
					userid: loginData.userid,
					passwd: loginData.passwd
				})

				if (res.status === 200 && res.data) {
					setUserInfo(res.data)
					await menuStore.fetchMenus()
					return res.data
				}
			} catch (e: any) {
				console.error('로그인 실패:', e)
				throw new Error(e.response?.data?.message || '로그인 중 오류가 발생했습니다.')
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
			cmpycd.value = ''
			userid.value = ''
			usernm.value = ''
			inner_no.value = ''
			deptcd.value = ''
			deptnm.value = ''
			usergrp.value = ''
			salsyn.value = ''
			email.value = ''
		}

		return {
			isAuthenticated,
			cmpycd, userid, usernm, inner_no, deptcd, deptnm, usergrp, salsyn, email,
			login, logout, resetState, checkSession, setUserInfo
		}
	},
	{
		persist: { storage: sessionStorage },
	}
)
