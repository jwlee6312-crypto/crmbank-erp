<template>
	<div class="main-content p-4 w-100">
		<!-- 탭 UI -->
		<div class="tabs-container d-flex mb-2">
			<div class="tabs d-flex">
				<div
					v-for="(tab, index) in tabs"
					:key="index"
					class="tab"
					:class="{ active: activeTab === tab.route }"
					@click="switchTab(tab.route)"
				>
					{{ tab.name }}
					<span class="close-btn" @click.stop="closeTab(index)">
						<i class="bi bi-x-circle-fill"></i>
					</span>
				</div>
			</div>
			<div v-if="tabs.length > 0" class="tab-buttons d-flex">
				<button class="btn btn-outline-custom" @click="closeAllTabs">전체 닫기</button>
			</div>
		</div>
		<!-- 탭이 있을 경우 -->
		<div v-if="tabs.length > 0" class="d-flex flex-column" style="flex: 1; overflow: auto">
			<router-view v-slot="{ Component }">
				<keep-alive>
					<component :is="Component" :key="$route.name" />
				</keep-alive>
			</router-view>
		</div>
		<!-- 탭 없을 때 -->
		<DefaultPage v-else class="p-2" />
	</div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { Ref } from 'vue'
import DefaultPage from '@/views/DefaultPage.vue'

// 타입 정의
interface Tab {
	name: string
	route: string
}

// 반응형 상태
const router = useRouter()
const route = useRoute()
const tabs: Ref<Tab[]> = ref([])
const activeTab = ref<string>(route.path)

// 라우터 변경 감지해서 탭 추가
router.afterEach((to) => {
	if (to.path === '/') return
	if (!to.name) return

	// 탭 존재 확인
	const existingTab = tabs.value.find((tab) => tab.route === to.path)
	const tabName = to.name as string
	if (!existingTab) {
		tabs.value.push({ name: tabName, route: to.path })
	}

	console.log(tabs.value)
	activeTab.value = to.path
})

// 탭 전환
function switchTab(route: string) {
	activeTab.value = route
	router.push(route)
}

// 탭 닫기
function closeTab(index: number) {
	const closedTab = tabs.value[index]
	tabs.value.splice(index, 1)

	if (activeTab.value === closedTab.route) {
		if (tabs.value.length > 0) {
			const newTab = tabs.value[tabs.value.length - 1]
			activeTab.value = newTab.route
			router.push(newTab.route)
		} else {
			activeTab.value = ''
		}
	}
}

// 전체 닫기
function closeAllTabs(): void {
	tabs.value = []
	activeTab.value = ''
}

// 자동으로 탭 오른쪽이동
watch(
	() => tabs.value.length,
	async () => {
		await nextTick() // DOM 렌더 완료 후 실행

		const tabsEl = document.querySelector('.tabs') as HTMLElement | null
		if (!tabsEl) return

		// 끝으로 이동
		tabsEl.scrollLeft = tabsEl.scrollWidth
	}
)

let tabsEl: HTMLDivElement | null = null

function handleWheel(e: WheelEvent) {
	e.preventDefault()
	if (!tabsEl) return
	tabsEl.scrollLeft += e.deltaY
}

onMounted(() => {
	tabsEl = document.querySelector('.tabs') as HTMLDivElement | null
	if (!tabsEl) return

	tabsEl.addEventListener('wheel', handleWheel as EventListener, { passive: false })
})

onBeforeUnmount(() => {
	if (!tabsEl) return
	tabsEl.removeEventListener('wheel', handleWheel as EventListener)
})
</script>

<style scoped>
.tabs-container {
	width: 100%;
	height: 44px;
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.tabs {
	display: flex;
	overflow-x: auto;
	flex-grow: 1;
}

.tabs::-webkit-scrollbar {
	display: none;
}

.tab {
	margin-right: 5px;
	padding: 5px 30px 5px 10px;
	border-radius: 7px;
	cursor: pointer;
	position: relative;
	white-space: nowrap;
	transition: background 0.2s;
	background-color: rgba(255, 255, 255, 0);
	border: rgba(187, 187, 187, 0.45) 1px solid;
}

.tab.active {
	color: #ffffff;
	background: rgba(0, 90, 159, 0.85);
	border: rgba(255, 255, 255, 0.09) 1px solid;
}

.tab.active .close-btn {
	right: 8px;
	position: absolute;
	color: #ffffff;
	cursor: pointer;
}

.close-btn {
	right: 8px;
	position: absolute;
	color: rgba(0, 90, 159, 0.85);
	cursor: pointer;
}

.tab-buttons {
	flex-shrink: 0;
	margin-left: 10px;
}

.tab-buttons button {
	margin-left: 5px;
}
</style>
