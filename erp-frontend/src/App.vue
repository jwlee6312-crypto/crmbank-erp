<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { useManualStore } from '@/stores/manualStore'
import { useAuthStore } from '@/stores/authStore'
import { useCtiStore } from '@/stores/ctiStore'
import ManualPopup from '@/layouts/ManualLayout.vue'

const popup = useManualStore()
const authStore = useAuthStore()
const ctiStore = useCtiStore()

onMounted(() => {
	// 브라우저 알림 권한 요청
	if (Notification.permission === 'default') {
		Notification.requestPermission()
	}

	// 💡 초기 진입 시 이미 로그인 상태라면 CTI 연결
	if (authStore.isAuthenticated) {
		ctiStore.connect()
	}
})

// 💡 로그인 상태 변화 감시 (로그인 성공 시 즉시 CTI 연결)
watch(() => authStore.isAuthenticated, (isAuth) => {
	if (isAuth) {
		ctiStore.connect()
	} else {
		ctiStore.disconnect()
	}
})
</script>

<template>
	<router-view />

	<teleport to="body">
		<div v-if="popup.isOpen" class="popup-overlay" @click="popup.close()">
			<div class="popup-content" @click.stop>
				<ManualPopup :fileName="popup.fileName" @close="popup.close()" />
			</div>
		</div>
	</teleport>
</template>

<style>
.popup-overlay {
	position: fixed;
	inset: 0;
	background: rgba(0, 0, 0, 0.5);
	display: flex;
	justify-content: center;
	align-items: center;
	z-index: 9999;
}

.popup-content {
	background: #fff;
	padding: 20px;
	border-radius: 6px;
	max-height: 90vh;
	overflow: auto;
}
</style>
