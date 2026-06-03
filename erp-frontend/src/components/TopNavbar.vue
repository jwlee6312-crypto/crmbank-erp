<template>
  <nav class="navbar navbar-expand bg-color shadow-sm px-4">
    <!-- 로고 영역 -->
    <div class="logo-container" @click="handleLogoClick">
      <div class="smart-core-logo">
        <span class="text-smart">Smart</span><span class="text-core">Core</span>
      </div>
      <div class="logo-subtitle">AI + ERP + CRM Integration</div>
    </div>

    <!-- 상단 대메뉴 -->
    <ul class="navbar-nav ms-5 me-auto custom-menu">
      <li v-for="menu in headerMenus" :key="menu.codecd" class="nav-item">
        <a
          class="nav-link"
          :class="{ active: menuStore.activeCodecd === menu.codecd }"
          href="javascript:void(0)"
          @click="selectTopMenu(menu.codecd)"
        >
          {{ menu.codenm }}
        </a>
      </li>
    </ul>

    <!-- 사용자 정보 및 액션 영역 -->
    <div class="d-flex align-items-center gap-3">
      <div v-if="authStore.usernm" class="user-profile-box d-none d-lg-flex align-items-center">
        <div class="avatar-icon">
          <i class="bi bi-person-fill"></i>
        </div>
        <div class="user-text-info">
          <span class="user-name">{{ authStore.usernm }}님</span>
          <span class="user-dept">{{ authStore.deptnm }}</span>
        </div>
      </div>

      <button class="btn-logout-custom" @click="logout">
        <i class="bi bi-power"></i>
        <span>로그아웃</span>
      </button>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useMenuStore } from '@/stores/menuStore'
import { useAuthStore } from '@/stores/authStore'

const menuStore = useMenuStore()
const authStore = useAuthStore()
const router = useRouter()

const headerMenus = computed(() => {
  if (!menuStore.topMenuItems) return []
  return menuStore.topMenuItems.map((item: any) => ({
    codecd: item.codecd, // 💡 소문자 표준 적용
    codenm: item.codenm  // 💡 소문자 표준 적용
  }))
})

const selectTopMenu = async (codecd: string) => {
  await menuStore.selectTopMenu(codecd)
}

const handleLogoClick = () => { router.push('/') }
const logout = async () => { await authStore.logout() }
</script>

<style scoped>
.bg-color { background: #005a9f; height: 65px; border-bottom: 2px solid #ffc107; }
.logo-container { width: 180px; display: flex; flex-direction: column; cursor: pointer; }
.smart-core-logo { font-size: 1.6rem; font-weight: 800; line-height: 1; }
.text-smart { color: #ffffff; }
.text-core { color: #ffc107; }
.logo-subtitle { font-size: 0.55rem; color: rgba(255, 255, 255, 0.7); margin-top: 2px; font-weight: 600; }
.custom-menu .nav-link { color: rgba(255, 255, 255, 0.8) !important; font-size: 1rem; font-weight: 600; padding: 0.5rem 1.5rem !important; }
.custom-menu .nav-link:hover, .custom-menu .nav-link.active { color: #ffffff !important; }
.user-profile-box { background: rgba(255, 255, 255, 0.1); padding: 5px 12px; border-radius: 12px; border: 1px solid rgba(255, 255, 255, 0.2); gap: 10px; }
.avatar-icon { width: 32px; height: 32px; background: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #005a9f; }
.user-text-info { display: flex; flex-direction: column; line-height: 1.2; text-align: left; }
.user-name { color: #fff; font-size: 0.9rem; font-weight: 700; }
.user-dept { color: rgba(255, 255, 255, 0.7); font-size: 0.7rem; font-weight: 500; }
.btn-logout-custom { display: flex; align-items: center; gap: 8px; background: transparent; border: 1px solid #ffc107; color: #ffc107; padding: 6px 15px; border-radius: 20px; font-size: 0.85rem; font-weight: 700; cursor: pointer; }
</style>
