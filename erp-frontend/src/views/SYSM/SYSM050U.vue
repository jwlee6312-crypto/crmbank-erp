<!--
	=============================================================
	프로그램명	: 매뉴얼 관리 (SYSM_050U) - 데이터 무결성 보장 버전
	작성일자	: 2025.02.24
	설명        : 워드 복사본 입력 -> 저장 -> 데이터 유실 방지
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center">
				<i class="bi bi-journal-check me-2 text-primary" style="font-size: 18px;"></i>
				시스템관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">매뉴얼 마스터 (SYSM_050U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1 pe-2">
				<button class="btn-erp btn-search" @click="search">조회</button>
				<button class="btn-erp btn-save" @click="save">저장하기</button>
			</div>
		</div>

		<div class="flex-grow-1 overflow-hidden p-3 d-flex gap-3 bg-light">
			<!-- 좌측: 프로그램 목록 -->
			<div class="card border shadow-sm d-flex flex-column" style="width: 250px;">
				<div class="card-header bg-white py-2 fw-bold small">대상 프로그램</div>
				<div class="p-2 border-bottom bg-white">
					<div class="input-group input-group-sm">
						<span class="input-group-text bg-light border-end-0"><i class="bi bi-search small"></i></span>
						<input v-model="searchPgmId" type="text" class="form-control border-start-0 ps-0" placeholder="ID 검색" @keyup.enter="search" />
					</div>
				</div>
				<div class="card-body p-0 overflow-hidden d-flex flex-column">
					<div ref="gridRef" class="tabulator-instance flex-grow-1"></div>
				</div>
			</div>

			<!-- 우측: 편집 공간 -->
			<div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
				<div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden bg-white">
					<div class="card-header bg-white py-2 d-flex justify-content-between align-items-center border-bottom">
						<span class="fw-bold text-dark small">[{{ selectedProg.id }}] {{ selectedProg.nm }} 매뉴얼 편집</span>
						<div class="btn-group">
							<button class="btn btn-xs btn-outline-primary" @click="applyTemplate">디자인 입히기</button>
							<button class="btn btn-xs btn-outline-dark" @click="toggleHtmlMode">HTML 보기</button>
						</div>
					</div>
					<div class="card-body p-0 flex-grow-1 d-flex flex-column overflow-hidden">
						<div v-show="!isHtmlMode" id="editor-container" class="flex-grow-1"></div>
						<textarea v-show="isHtmlMode" v-model="htmlContent" class="form-control p-3 font-monospace flex-grow-1 border-0" style="resize: none; background: #2d2d2d; color: #ccc;"></textarea>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import Quill from 'quill'
import 'quill/dist/quill.snow.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import AppAlert from '@/components/AppAlert.vue'
import { useAuthStore } from '@/stores/authStore'

const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const authStore = useAuthStore()

const gridRef = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null
let quill: Quill | null = null

const selectedProg = reactive({ id: '', nm: '' })
const searchPgmId = ref('')
const isHtmlMode = ref(false)
const htmlContent = ref('')

const toggleHtmlMode = () => {
	if (isHtmlMode.value) {
		if (quill) quill.root.innerHTML = htmlContent.value
	} else {
		htmlContent.value = quill ? quill.root.innerHTML : ''
	}
	isHtmlMode.value = !isHtmlMode.value
}

onMounted(() => {
	quill = new Quill('#editor-container', {
		theme: 'snow',
		modules: {
			toolbar: [[{ 'header': [1, 2, 3, false] }], ['bold', 'italic', 'underline', { 'color': [] }], [{ 'list': 'ordered' }, { 'list': 'bullet' }], ['image', 'link', 'clean']]
		}
	})

	grid = new Tabulator(gridRef.value!, {
		layout: 'fitColumns', height: '100%',
		columns: [{ title: "ID", field: "pgmid", width: 100 }, { title: "명칭", field: "pgmnm", hozAlign: "left" }]
	})

	grid.on("rowClick", (e, row) => {
		const d = row.getData()
		selectedProg.id = d.pgmid; selectedProg.nm = d.pgmnm
		loadManual(d.pgmid)
	})
	search()
})

const search = async () => {
	try {
		const res = await api.post('/api/comm/getProgramList', { pgmnm: '' })
		const query = searchPgmId.value.toUpperCase()
		const filtered = res.data.filter((i: any) => (i.pgmid || '').endsWith('U') && (query ? i.pgmid.includes(query) : true))
		grid?.setData(filtered)
	} catch (e) { console.error(e) }
}

const loadManual = async (progid: string) => {
	try {
		const res = await api.get(`/api/manual/db/${progid}`)
		const content = res.data?.content || ''
		if (quill) quill.root.innerHTML = content
		htmlContent.value = content
	} catch (e) {
		if (quill) quill.root.innerHTML = ''
		htmlContent.value = ''
	}
}

const save = async () => {
	if (!selectedProg.id) return vAlertError('프로그램을 선택하세요.')

	// 모드 상관없이 현재 에디터 내용 추출
	const content = isHtmlMode.value ? htmlContent.value : (quill ? quill.root.innerHTML : '')

	try {
		await api.post('/api/manual/db/save', {
			progid: selectedProg.id,
			prognm: selectedProg.nm,
			content: content,
			upd_id: authStore.userid
		})
		vAlert('성공적으로 저장되었습니다.')
		loadManual(selectedProg.id) // 저장 후 즉시 다시 조회하여 확인
	} catch (e) { vAlertError('저장 오류 발생') }
}

const applyTemplate = () => {
	const current = isHtmlMode.value ? htmlContent.value : (quill ? quill.root.innerHTML : '')
	const res = `
		<div style="border: 2px solid #005a9f; padding: 20px; border-radius: 10px;">
			<h2 style="color: #005a9f; border-bottom: 2px solid #005a9f; padding-bottom: 10px;">[${selectedProg.id}] ${selectedProg.nm} 가이드</h2>
			<div style="margin-top: 20px; line-height: 1.8;">${current}</div>
		</div>
	`
	if (quill) quill.root.innerHTML = res
	htmlContent.value = res
	vAlert('디자인이 적용되었습니다. 다시 저장 버튼을 눌러주세요.')
}
</script>

<style scoped>
#editor-container :deep(.ql-editor) { min-height: 300px; font-size: 14px; }
.btn-xs { padding: 0.1rem 0.4rem; font-size: 0.75rem; }
</style>
