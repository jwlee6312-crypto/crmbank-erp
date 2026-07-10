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
			<div class="card border shadow-sm d-flex flex-column" style="width: 400px;">
				<div class="card-header bg-white py-2 fw-bold small">대상 프로그램</div>
				<div class="p-2 border-bottom bg-white">
					<div class="input-group input-group-sm">
						<span class="input-group-text bg-light border-end-0"><i class="bi bi-search small"></i></span>
						<input v-model="searchPgmId" type="text" class="form-control border-start-0 ps-0" placeholder="ID 또는 명칭 검색" @keyup.enter="search" />
					</div>
				</div>
				<div class="card-body p-0 overflow-hidden d-flex flex-column">
					<div ref="gridRef" class="tabulator-instance flex-grow-1"></div>
				</div>
			</div>

			<!-- 우측: 편집 공간 -->
			<div class="flex-grow-1 d-flex flex-row gap-2 overflow-hidden">
				<!-- 편집창 -->
				<div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden bg-white" style="flex: 1;">
					<div class="card-header bg-white py-2 d-flex justify-content-between align-items-center border-bottom">
						<div class="d-flex align-items-center gap-2">
							<span class="fw-bold text-dark small">[{{ selectedProg.id }}] {{ selectedProg.nm }} 편집</span>
							<span v-if="lastSavedTime" class="badge bg-success" style="font-size: 10px;">
								<i class="bi bi-check-all me-1"></i>저장완료 ({{ lastSavedTime }})
							</span>
						</div>
						<span class="badge bg-info text-white" style="font-size: 10px;">텍스트 모드</span>
					</div>
					<div class="card-body p-0 flex-grow-1 d-flex flex-column overflow-hidden">
						<textarea v-model="textContent"
							class="form-control p-3 flex-grow-1 border-0 custom-textarea"
							placeholder="매뉴얼 내용을 입력하세요.
주요 제목 앞에는 ■ 또는 ● 를 붙이면 멋지게 표시됩니다.
[안내] 처럼 대괄호를 사용하면 강조 박스로 표시됩니다."
							@input="lastSavedTime = ''"
							style="resize: none; font-family: 'Pretendard', 'Malgun Gothic', sans-serif; font-size: 15px; line-height: 1.6;"></textarea>
					</div>
				</div>

				<!-- 미리보기창 (멋있게 편집된 결과) -->
				<div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden bg-light" style="flex: 1;">
					<div class="card-header bg-white py-2 d-flex justify-content-between align-items-center border-bottom">
						<span class="fw-bold text-primary small">실시간 미리보기</span>
						<span v-if="lastSavedTime" class="text-success fw-bold" style="font-size: 11px;">
							<i class="bi bi-cloud-check-fill me-1"></i>서버 저장 상태
						</span>
						<span v-else-if="textContent" class="text-warning fw-bold" style="font-size: 11px;">
							<i class="bi bi-pencil-square me-1"></i>편집 중 (저장 필요)
						</span>
					</div>
					<div class="card-body p-4 flex-grow-1 overflow-auto bg-white manual-preview-container">
						<div class="manual-content-view">
							<h3 class="manual-title" v-if="selectedProg.id">
								<i class="bi bi-file-earmark-text me-2"></i>{{ selectedProg.nm }}
								<small class="text-muted ms-2" style="font-size: 12px;">({{ selectedProg.id }})</small>
							</h3>
							<hr v-if="selectedProg.id" />
							<div v-html="formattedContent"></div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import AppAlert from '@/components/AppAlert.vue'
import { useAuthStore } from '@/stores/authStore'

const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const authStore = useAuthStore()

const gridRef = ref<HTMLElement | null>(null)
let grid: Tabulator | null = null

const selectedProg = reactive({ id: '', nm: '' })
const searchPgmId = ref('')
const textContent = ref('')
const lastSavedTime = ref('')

// 멋있게 텍스트 포맷팅하는 로직
const formattedContent = computed(() => {
	if (!textContent.value) return '<p class="text-muted">내용이 없습니다.</p>'

	const lines = textContent.value.split('\n')
	return lines.map(line => {
		let trimmed = line.trim()
		if (!trimmed) return '<br/>'

		// 1. ■ 제목 스타일
		if (trimmed.startsWith('■')) {
			return `<h5 class="mt-4 mb-2 fw-bold text-primary border-bottom pb-2"><i class="bi bi-check-square-fill me-2"></i>${trimmed.substring(1)}</h5>`
		}
		// 2. ● 소제목 스타일
		if (trimmed.startsWith('●')) {
			return `<h6 class="mt-3 mb-2 fw-bold text-dark"><i class="bi bi-dot me-1"></i>${trimmed.substring(1)}</h6>`
		}
		// 3. [ ] 강조 박스 스타일
		if (trimmed.startsWith('[') && trimmed.includes(']')) {
			const endIdx = trimmed.indexOf(']')
			const tag = trimmed.substring(1, endIdx)
			const content = trimmed.substring(endIdx + 1)
			return `<div class="alert alert-info py-2 px-3 my-2 shadow-sm border-start border-4 border-info"><strong class="me-2">${tag}</strong> ${content}</div>`
		}
		// 4. 일반 텍스트
		return `<p class="mb-1 ps-1">${line}</p>`
	}).join('')
})

onMounted(() => {
	grid = new Tabulator(gridRef.value!, {
		layout: 'fitColumns', height: '100%',
		columns: [
			{ title: "ID", field: "pgmid", width: 100, headerHozAlign: 'center' },
			{ title: "프로그램명", field: "pgmnm", hozAlign: "left", headerHozAlign: 'center', minWidth: 150 },
			{ title: "등록", field: "reg_yn", width: 60, hozAlign: "center", headerHozAlign: 'center',
				formatter: (cell) => {
					const val = cell.getValue();
					return val === 'Y' ? '<span class="badge bg-primary">등록</span>' : '<span class="text-muted small">미등록</span>';
				}
			}
		]
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
		// 🚀 ID 또는 명칭으로 검색 가능하게 변경
		const filtered = res.data.filter((i: any) =>
			(query ? (i.pgmid || '').includes(query) || (i.pgmnm || '').includes(query) : true)
		)
		grid?.setData(filtered)
	} catch (e) { console.error(e) }
}

const loadManual = async (progid: string) => {
	try {
		// 🚀 조회 시에도 혹시 모를 언더바 제거 처리 (표준화)
		const cleanId = progid.replace(/_/g, '')
		const res = await api.get(`/api/manual/db/${cleanId}`)
		textContent.value = res.data?.content || ''
		lastSavedTime.value = textContent.value ? '방금 조회됨' : ''
	} catch (e) {
		textContent.value = ''
		lastSavedTime.value = ''
	}
}

const save = async () => {
	if (!selectedProg.id) return vAlertError('프로그램을 선택하세요.')

	try {
		const cleanId = selectedProg.id.replace(/_/g, '')
		await api.post('/api/manual/db/save', {
			progid: cleanId,
			prognm: selectedProg.nm,
			content: textContent.value,
			upd_id: authStore.userid
		})
		vAlert('성공적으로 저장되었습니다.')

		// 저장 시각 표시
		const now = new Date()
		lastSavedTime.value = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`

		// 저장 후 최신 상태 로드
		loadManual(cleanId)
	} catch (e) { vAlertError('저장 오류 발생') }
}
</script>

<style scoped>
.custom-textarea {
	background-color: #fdfdfd;
	outline: none;
	transition: background-color 0.2s;
}
.custom-textarea:focus {
	background-color: #fff;
	box-shadow: none;
}
.manual-preview-container {
	font-family: 'Pretendard', 'Malgun Gothic', sans-serif;
	line-height: 1.7;
	color: #333;
}
.manual-title {
	color: #005a9f;
	font-weight: 800;
	margin-bottom: 20px;
}
.manual-content-view :deep(h5) { color: #005a9f; }
.manual-content-view :deep(.alert) { font-size: 0.95rem; }
</style>
