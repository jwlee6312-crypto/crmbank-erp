<template>
	<div class="main-content p-2 w-100 d-flex flex-column" style="height: 100vh; overflow: hidden;">
		<!-- 탭 UI (표준화 대상) -->
		<div class="tabs-container d-flex mb-1 flex-shrink-0" style="height: 32px; align-items: center;">
			<div class="tabs d-flex overflow-auto flex-grow-1" style="scrollbar-width: none; -ms-overflow-style: none;">
				<div
					v-for="(tab, index) in tabs"
					:key="index"
					class="tab d-flex align-items-center px-3 py-1 me-1 border rounded-top cursor-pointer position-relative"
					:class="activeTab === tab.route ? 'bg-primary text-white border-primary' : 'bg-light text-secondary'"
					style="font-size: 12px; white-space: nowrap; height: 30px;"
					@click="switchTab(tab.route)"
				>
					{{ tab.name }}
					<span class="ms-2 cursor-pointer" @click.stop="closeTab(index)">
						<i class="bi bi-x-circle-fill" :class="activeTab === tab.route ? 'text-white' : 'text-muted'"></i>
					</span>
				</div>
			</div>
			<div v-if="tabs.length > 0" class="tab-buttons d-flex ms-2">
				<button class="btn btn-sm btn-outline-secondary py-0" style="font-size: 11px; height: 26px;" @click="closeAllTabs">전체 닫기</button>
			</div>
		</div>

		<!-- 컨텐츠 영역 -->
		<div v-if="tabs.length > 0" class="flex-grow-1 overflow-hidden border rounded bg-white">
			<router-view v-slot="{ Component }">
				<keep-alive>
					<component :is="Component" :key="$route.name" />
				</keep-alive>
			</router-view>
		</div>
		<!-- 초기 화면 -->
		<DefaultPage v-else class="flex-grow-1" />
	</div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { Ref } from 'vue'
import DefaultPage from '@/views/DefaultPage.vue'

interface Tab {
	name: string
	route: string
}

const router = useRouter()
const route = useRoute()
const tabs: Ref<Tab[]> = ref([])
const activeTab = ref<string>(route.path)

router.afterEach((to) => {
	if (to.path === '/' || !to.name) return
	const existingTab = tabs.value.find((tab) => tab.route === to.path)
	if (!existingTab) {
		tabs.value.push({ name: to.name as string, route: to.path })
	}
	activeTab.value = to.path
})

function switchTab(route: string) {
	activeTab.value = route
	router.push(route)
}

function closeTab(index: number) {
	const closedTab = tabs.value[index]
	tabs.value.splice(index, 1)
	if (activeTab.value === closedTab.route) {
		if (tabs.length > 0) {
			const newTab = tabs.value[tabs.value.length - 1]
			activeTab.value = newTab.route
			router.push(newTab.route)
		} else {
			activeTab.value = ''
		}
	}
}

function closeAllTabs(): void {
	tabs.value = []
	activeTab.value = ''
	router.push('/')
}

watch(() => tabs.value.length, async () => {
	await nextTick()
	const tabsEl = document.querySelector('.tabs') as HTMLElement | null
	if (tabsEl) tabsEl.scrollLeft = tabsEl.scrollWidth
})

let tabsEl: HTMLDivElement | null = null
function handleWheel(e: WheelEvent) {
	if (!tabsEl) return
	e.preventDefault()
	tabsEl.scrollLeft += e.deltaY
}

onMounted(() => {
	tabsEl = document.querySelector('.tabs') as HTMLDivElement | null
	if (tabsEl) tabsEl.addEventListener('wheel', handleWheel as EventListener, { passive: false })
})

onBeforeUnmount(() => {
	if (tabsEl) tabsEl.removeEventListener('wheel', handleWheel as EventListener)
})
</script>
