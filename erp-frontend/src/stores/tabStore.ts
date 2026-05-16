import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

export interface Tab {
  pgmId: string
  pgmNm: string
  path: string
}

export const useTabStore = defineStore('tab', () => {
  const router = useRouter()
  const tabs = ref<Tab[]>([])
  const activeTabId = ref<string>('')

  // 탭 추가
  const addTab = (tab: Tab) => {
    const exists = tabs.value.find(t => t.pgmId === tab.pgmId)
    if (!exists) {
      tabs.value.push(tab)
    }
    activeTabId.value = tab.pgmId
    router.push(tab.path)
  }

  // 탭 닫기
  const closeTab = (pgmId: string) => {
    const index = tabs.value.findIndex(t => t.pgmId === pgmId)
    if (index !== -1) {
      const isRemovingActive = activeTabId.value === pgmId
      tabs.value.splice(index, 1)

      if (isRemovingActive && tabs.value.length > 0) {
        // 마지막 탭이 아니면 다음 탭, 마지막 탭이면 이전 탭 활성화
        const nextTab = tabs.value[index] || tabs.value[index - 1]
        activeTabId.value = nextTab.pgmId
        router.push(nextTab.path)
      } else if (tabs.value.length === 0) {
        activeTabId.value = ''
        router.push('/')
      }
    }
  }

  // 탭 선택
  const selectTab = (tab: Tab) => {
    activeTabId.value = tab.pgmId
    router.push(tab.path)
  }

  return { tabs, activeTabId, addTab, closeTab, selectTab }
})
