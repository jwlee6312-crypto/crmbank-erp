<template>
  <div class="side-bar shadow-sm" :class="{ 'is-collapsed': isCollapsed }">
    <!-- 유저 프로필 카드 -->
    <div v-if="!isCollapsed" class="profile-card">
      <div class="user-details text-center">
        <div class="avatar-area mb-3">
          <div class="avatar-placeholder">
            <i class="bi bi-person-fill"></i>
          </div>
        </div>
        <div class="user-name">
          {{ authStore.USERNM }}님
        </div>
        <div class="user-id-text mb-1">
          <span class="user-id-small">{{ authStore.USERID }}</span>
        </div>
        <!-- 💡 이메일 정보를 이름 아래로 이동 -->
        <div v-if="authStore.EMAIL" class="user-email-text mb-2">
          {{ authStore.EMAIL }}
        </div>
        <div class="user-info-row d-flex justify-content-center gap-1 mb-2">
          <span class="badge bg-white text-dark border shadow-sm py-1 px-2" style="font-size: 10px;">
            내선: {{ authStore.INNER_NO || '-' }}
          </span>
          <span class="badge bg-light text-muted border py-1 px-2" style="font-size: 10px;">
            {{ authStore.DEPTNM }}
          </span>
        </div>
      </div>
    </div>

    <!-- 메뉴 리스트 -->
    <div id="accordionMenu" class="menu-list">
      <div v-for="group in groupedItems" :key="group.GRPCD" class="w-100">
        <a
          class="nav-link group-title"
          :class="{ 'collapsed': !isGroupOpen(group.GRPCD) }"
          href="javascript:void(0)"
          @click="toggleGroup(group.GRPCD)"
        >
          <i class="bi bi-folder2-open me-2"></i>
          <span v-if="!isCollapsed">{{ group.GRPNM }}</span>
        </a>

        <div class="collapse-content" v-show="isGroupOpen(group.GRPCD) && !isCollapsed">
          <nav class="nav d-flex flex-column">
            <a
              v-for="item in group.items"
              :key="item.PGMID"
              class="sb-nav-link"
              :class="{ 'is-active': tabStore.activeTabId === item.PGMID }"
              href="javascript:void(0)"
              :title="item.PGMID"
              @click="goPage(item.PGMID, item.PGMNM, item.GRPCD)"
            >
              <i class="bi bi-chevron-right sub-icon"></i>
              <span>{{ item.PGMNM }}</span>
            </a>
          </nav>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useMenuStore } from '@/stores/menuStore'
import { useTabStore } from '@/stores/tabStore'
import { useAuthStore } from '@/stores/authStore'
import { addDynamicRoute } from '@/router/dynamicRoute'

const props = defineProps({
  isCollapsed: Boolean
})

const authStore = useAuthStore()
const menuStore = useMenuStore()
const tabStore = useTabStore()
const openGroupId = ref<string | null>(null)

const isGroupOpen = (grpcd: string) => openGroupId.value === grpcd
const toggleGroup = (grpcd: string) => {
  openGroupId.value = openGroupId.value === grpcd ? null : grpcd
}

const groupedItems = computed(() => menuStore.groupedSidebarItems)

function goPage(pgmid: string, pgmnm: string, grpcd: string) {
  addDynamicRoute(pgmid, pgmnm, grpcd)
  tabStore.addTab({ pgmId: pgmid, pgmNm: pgmnm, path: `/${pgmid}` })
}
</script>

<style scoped>
.side-bar { background-color: #fff; border-right: 1px solid #dcdfe6; display: flex; flex-direction: column; height: 100%; overflow: hidden; }
.profile-card { padding: 20px 10px; border-bottom: 1px solid #ebeef5; flex-shrink: 0; background: #f8f9fa; }
.avatar-placeholder { width: 45px; height: 45px; background-color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24px; color: #005a9f; margin: 0 auto; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
.user-name { font-weight: 800; color: #2c3e50; font-size: 15px; }
.user-id-small { font-size: 12px; color: #909399; }
.user-email-text { font-size: 11px; color: #409eff; word-break: break-all; padding: 0 5px; }
.menu-list { flex: 1; overflow-y: auto; }
.group-title { display: flex; align-items: center; padding: 10px 15px; font-size: 13px; font-weight: 600; color: #303133; text-decoration: none; }
.sb-nav-link { padding: 8px 15px 8px 35px; font-size: 12px; color: #606266; text-decoration: none; display: flex; align-items: center; }
.sb-nav-link.is-active { background-color: #ecf5ff; color: #409eff; font-weight: bold; }
</style>
