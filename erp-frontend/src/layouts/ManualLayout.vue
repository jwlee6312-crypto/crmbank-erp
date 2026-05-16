<template>
	<div class="empty-layout">
		<div class="d-flex justify-content-between">
			<p></p>
			<button type="button" class="btn-close" @click="close"></button>
		</div>
		<div v-for="img in images" :key="img">
			<img :src="img" style="max-width: 100%; margin-top: 10px" alt="manual" />
			<hr />
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useManualStore } from '@/stores/manualStore'
import { API_URL } from '@/config/api'

const manualStore = useManualStore()
const api = API_URL
const images = ref<string[]>([])

onMounted(async () => {
	const base = manualStore.fileName
	for (let i = 1; i <= 10; i++) {
		const num = String(i).padStart(2, '0') // 01, 02 형식
		const url = `${api}/manual/${base}_${num}`
		const res = await fetch(url, { credentials: 'include' })
		console.log(`${num}: `, res)
		if (res.status === 200) {
			images.value.push(`${api}/manual/${base}_${num}`)
		} else {
			break
		}
	}
})
function close() {
	manualStore.close()
}
</script>
<style>
.empty-layout {
	width: 1400px;
	height: 800px;
}
</style>
