import router from './index'
import { useMenuStore } from '@/stores/menuStore'

// Program     : 동적 라우팅 유틸리티
// Create-Date : 25.04.16
// Modify-Date : 26.04.21
// History     : PGMID 8자리 규칙에 맞춰 폴더 추출 로직 수정 (앞 3자리 -> 앞 4자리)

export function addDynamicRoute(pgmid: string, pgmnm: string, grpcd: string) {
	if (router.hasRoute(pgmnm)) return
	const menuStore = useMenuStore()

	// 💡 사용자님의 8자리 PGMID 규칙에 따라 앞 4자리를 폴더명으로 사용합니다.
	// 예: HABA900U -> HABA 폴더
	const folder = pgmid.slice(0, 4)

	const component = () =>
		import(`@/views/${folder}/${pgmid}.vue`).catch((err) => {
			alert(`[${pgmid}] 페이지가 존재하지 않습니다.\n경로: @/views/${folder}/${pgmid}.vue`)
			console.error(err)
			return import('@/views/NotFound.vue') // 에러 시 404 페이지로 대체
		})

	// 탭 활성화 시 메뉴버튼 CSS 활성화 위해서
	// 메타 데이터에 상단메뉴, 사이드바 메뉴 데이터 저장합니다.
	router.addRoute('DefaultLayout', {
		path: `/${pgmid}`,
		name: pgmnm,
		component,
		meta: {
			topMenuCode: menuStore.activeCodecd,
			sideMenuCode: grpcd,
			title: pgmid,
		},
	})
}
