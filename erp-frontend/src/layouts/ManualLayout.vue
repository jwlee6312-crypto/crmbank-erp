<template>
	<div class="manual-container">
		<div class="d-flex justify-content-between align-items-center mb-3 border-bottom pb-2">
			<h5 class="mb-0 fw-bold text-primary">
				<i class="bi bi-book me-2"></i>시스템 도움말
			</h5>
			<button type="button" class="btn-close" @click="close"></button>
		</div>

		<!-- [1] DB 기반 HTML 매뉴얼 출력 -->
		<div v-if="dbContent" class="manual-html-content ql-editor" v-html="dbContent"></div>

		<!-- [2] 기존 이미지 기반 매뉴얼 출력 (DB 내용 없을 경우) -->
		<div v-else-if="images.length > 0">
			<div v-for="img in images" :key="img" class="mb-3 text-center">
				<img :src="img" class="img-fluid rounded shadow-sm border" alt="manual" />
			</div>
		</div>

		<!-- [3] 내용이 없을 경우 -->
		<div v-else class="text-center py-5 text-muted">
			<i class="bi bi-info-circle display-4"></i>
			<p class="mt-3">등록된 매뉴얼이 없습니다.</p>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useManualStore } from '@/stores/manualStore'
import { API_URL } from '@/config/api'
import 'quill/dist/quill.snow.css' // 에디터 스타일과 맞추기 위해 추가

const manualStore = useManualStore()
const api = API_URL
const images = ref<string[]>([])
const dbContent = ref('')

onMounted(async () => {
	const base = manualStore.fileName
	if (!base) return

	// 🚀 [1] DB 기반 매뉴얼 먼저 조회
	try {
		const resDb = await fetch(`${api}/api/manual/db/${base}`, { credentials: 'include' })
		if (resDb.ok) {
			const data = await resDb.json()
			if (data && data.content) {
				dbContent.value = data.content
				return // DB에 내용이 있으면 여기서 중단
			}
		}
	} catch (e) {
		console.warn("DB 매뉴얼 조회 실패, 이미지 방식을 시도합니다.", e)
	}

	// 🚀 [2] DB에 없으면 기존 이미지 방식 시도
	for (let i = 1; i <= 10; i++) {
		const num = String(i).padStart(2, '0')
		const url = `${api}/manual/${base}_${num}`
		const res = await fetch(url, { credentials: 'include' })
		if (res.ok) {
			images.value.push(url)
		} else {
			break
		}
	}
})

function close() {
	manualStore.close()
}
</script>

<style scoped>
.manual-container {
	width: 1000px;
	min-height: 400px;
	max-height: 80vh;
	padding: 10px;
}
.manual-html-content {
	font-size: 14px;
	line-height: 1.6;
	color: #333;
}
/* Quill 에디터 스타일 유지 */
:deep(.ql-editor img) {
	max-width: 100%;
	height: auto;
	display: block;
	margin: 10px auto;
}
</style>
