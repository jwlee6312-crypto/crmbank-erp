<template>
  <div class="erp-container">
    <!-- 1. 상단 네비바 -->
    <header class="erp-header">
      <TopNavbar />
    </header>

    <div class="erp-main-wrapper">
      <!-- 2. 좌측 사이드바 -->
      <aside class="erp-sidebar" :class="{ 'is-collapsed': isSidebarCollapsed }">
        <SideMenu :is-collapsed="isSidebarCollapsed" @toggle="toggleSidebar" />
      </aside>

      <!-- 3. 본문 영역 (탭 + 컨텐츠) -->
      <main class="erp-content">
        <!-- 💡 탭 바 디자인 고도화 (한 줄 유지 & 우측 스크롤 적용) -->
        <nav class="erp-tab-bar custom-scrollbar">
          <div
            v-for="tab in tabStore.tabs"
            :key="tab.pgmId"
            class="erp-tab-item"
            :class="{ 'is-active': tabStore.activeTabId === tab.pgmId }"
            @click="tabStore.selectTab(tab)"
          >
            <i class="bi bi-file-earmark-text me-2 tab-icon"></i>
            <span class="tab-title">{{ tab.pgmNm }}</span>
            <span class="tab-close" @click.stop="tabStore.closeTab(tab.pgmId)">
              <i class="bi bi-x-lg"></i>
            </span>
          </div>
        </nav>

        <!-- 3-2. 프로그램 컨텐츠 영역 -->
        <section class="erp-page-container">
          <router-view v-slot="{ Component }">
            <keep-alive>
              <component :is="Component" :key="$route.fullPath" />
            </keep-alive>
          </router-view>
        </section>
      </main>
    </div>

    <!-- 로딩 오버레이 -->
    <div v-if="loadingStore.isLoading" class="erp-loading">
      <AppSpinner />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import TopNavbar from '@/components/TopNavbar.vue'
import SideMenu from '@/components/SideMenu.vue'
import { useTabStore } from '@/stores/tabStore'
import { useLoadingStore } from '@/stores/loadingStore'
import AppSpinner from '@/components/AppSpinner.vue'

const tabStore = useTabStore()
const loadingStore = useLoadingStore()
const isSidebarCollapsed = ref(false)

const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}
</script>

<style scoped>
.erp-container { display: flex; flex-direction: column; height: 100vh; width: 100vw; overflow: hidden; background-color: #f0f2f5; }
.erp-header { height: 65px; flex-shrink: 0; z-index: 1000; }
.erp-main-wrapper { display: flex; flex: 1; overflow: hidden; }

.erp-sidebar { width: 240px; background: #fff; border-right: 1px solid #dcdfe6; transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1); flex-shrink: 0; display: flex; flex-direction: column; }
.erp-sidebar.is-collapsed { width: 64px; }

.erp-content { flex: 1; display: flex; flex-direction: column; overflow: hidden; }

/* 💡 탭 바 스타일 (한 줄 유지 및 가로 스크롤 적용) */
.erp-tab-bar {
  height: 40px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: flex-end;
  padding-left: 10px;
  flex-shrink: 0;
  gap: 2px;
  overflow-x: auto; /* 💡 가로 스크롤 활성화 */
  overflow-y: hidden;
  white-space: nowrap; /* 💡 텍스트 줄바꿈 방지 */
}

/* 스크롤바 디자인 (얇게 조정) */
.custom-scrollbar::-webkit-scrollbar { height: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: #f1f1f1; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #ccc; border-radius: 2px; }
.custom-scrollbar::-webkit-scrollbar-thumb:hover { background: #999; }

.erp-tab-item {
	height: 34px;
	padding: 0 15px;
	background: #f5f7fa;
	border: 1px solid #e4e7ed;
	border-bottom: none;
	margin-right: 2px;
	border-top-left-radius: 6px;
	border-top-right-radius: 6px;
	display: flex;
	align-items: center;
	font-size: 13px;
	cursor: pointer;
	color: #606266;
	transition: all 0.2s;
	position: relative;
	font-weight: 700;
	flex-shrink: 0; /* 💡 탭이 많아져도 너비가 줄어들지 않도록 설정 */
    white-space: nowrap;
}

.erp-tab-item:hover { background: #ebf5ff; color: #005a9f; }

/* 💡 현재 실행 중인 탭: 파란색(#005a9f) 및 더 강조된 Bold */
.erp-tab-item.is-active {
	background: #fff;
	color: #005a9f;
	font-weight: 800;
	height: 36px;
	border-top: 3px solid #005a9f;
	border-bottom: 2px solid #fff;
	margin-bottom: -1px;
	z-index: 2;
	box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
}

.tab-title {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tab-icon { font-size: 14px; opacity: 0.7; }
.erp-tab-item.is-active .tab-icon { color: #005a9f; opacity: 1; }

.tab-close { margin-left: 10px; width: 18px; height: 18px; border-radius: 50%; display: flex; align-items: center; justify-content: center; transition: all 0.2s; }
.tab-close i { font-size: 10px; }
.tab-close:hover { background-color: #f56c6c; color: #fff; }

.erp-page-container { flex: 1; overflow: hidden; background-color: #fff; padding: 0; }

.erp-loading { position: fixed; inset: 0; background: rgba(255,255,255,0.7); display: flex; justify-content: center; align-items: center; z-index: 9999; }
</style>
