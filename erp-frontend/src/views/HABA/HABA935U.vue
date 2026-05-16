<!--시스템관리/사용자그룹 프로그램 권한관리 [ERP 프리미엄 고밀도 표준 - 간격 확보] -->
<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="haba935u-wrapper d-flex flex-column h-100 bg-light p-0">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 shadow-sm sticky-top">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-shield-lock-fill me-2 text-primary" style="font-size: 18px;"></i>
				시스템 관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">사용자 그룹 프로그램 권한관리 (HABA935U)</span>
			</div>
			<div class="btn-group-erp pe-3">
				<button class="btn-erp btn-search" @click="fetchCategories">조회</button>
				<button class="btn-erp btn-save" @click="saveAll">일괄 저장</button>
			</div>
		</div>

		<!-- 💡 메인 컨텐츠 영역 (섹션 간 확실한 간격 확보: gap-3) -->
		<div class="flex-grow-1 overflow-hidden d-flex flex-column gap-3 p-3">

			<!-- 🔍 2. 검색 조건 섹션 -->
			<div class="card border-0 shadow-sm flex-shrink-0">
				<div class="card-body p-2 bg-white rounded">
					<div class="d-flex align-items-center gap-3">
						<div class="input-group input-group-sm flex-nowrap" style="width: 350px;">
							<span class="input-group-text bg-light fw-bold px-3 border-0">사용자그룹</span>
							<select v-model="searchForm.USERGRP" class="form-select border-light-subtle">
								<option v-for="opt in groupOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
							</select>
						</div>
						<div class="input-group input-group-sm flex-nowrap" style="width: 300px;">
							<span class="input-group-text bg-light fw-bold px-3 border-0">업무분류</span>
							<select v-model="searchForm.UPMUCD" class="form-select border-light-subtle" @change="fetchCategories">
								<option v-for="opt in upmuOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
							</select>
						</div>
						<div class="small text-muted border-start ps-3"><i class="bi bi-info-circle me-1"></i> 좌측 분류를 선택하면 우측에 해당 프로그램 리스트가 나타납니다.</div>
					</div>
				</div>
			</div>

			<!-- 📊 3. 하단 분할 영역 (카드로 명확히 분리 및 간격 확보) -->
			<div class="d-flex flex-row gap-3 flex-grow-1 overflow-hidden">

				<!-- 🅰️ 좌측: 프로그램 분류 리스트 -->
				<div class="card border-0 shadow-sm d-flex flex-column" style="width: 350px;">
					<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center">
						<i class="bi bi-list-task me-2 text-secondary"></i>
						<span class="fw-bold small text-dark">메뉴 그룹 분류</span>
					</div>
					<div class="card-body p-0 flex-grow-1 overflow-hidden bg-white">
						<div ref="catGridRef" class="tabulator-full-height" />
					</div>
				</div>

				<!-- 🅱️ 우측: 상세 권한 설정 -->
				<div class="card border-0 shadow-sm d-flex flex-column flex-grow-1">
					<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
						<div class="fw-bold small text-dark"><i class="bi bi-check2-circle me-2 text-secondary"></i>상세 프로그램 권한 설정</div>
						<button class="btn btn-xs btn-outline-primary fw-bold" @click="toggleSelectAll">전체선택 / 해제</button>
					</div>
					<div class="card-body p-0 flex-grow-1 overflow-hidden bg-white">
						<div ref="pgmGridRef" class="tabulator-full-height" />
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const searchForm = reactive({ USERGRP: '', UPMUCD: '', GRPCD: '' })
const groupOptions = ref<any[]>([])
const upmuOptions = ref<any[]>([])

const catGridRef = ref<HTMLElement | null>(null)
const pgmGridRef = ref<HTMLElement | null>(null)
let catGrid: Tabulator | null = null
let pgmGrid: Tabulator | null = null
//HS00_000S_STR 'GB','" & strCustId & "','600',''
//HA00_00P_STR 'E0','','320',''
async function fetchGrp() {
	try {
		// 💡 SELGBN 대신 GBNCD 사용
		const res = await api.post('/api/hs00/HS00_000S_STR', { GUBUN: 'GB',  CMPYCD: authStore.CMPYCD, GBNCD: '600' })
		console.log(res.data)
		if (res.data) {
			groupOptions.value = res.data.map((i: any) => ({ CODECD: String(i.CODE || i.CODE).trim(), CODENM: String(i.CDNM || i.CDNM).trim() }))
			if (groupOptions.value.length > 0) {
				searchForm.USERGRP = groupOptions.value[0].CODECD;

				await fetchUpmu();
			}
		}
	} catch (e) { console.error('사용자그룹 로드 실패') }
}

async function fetchUpmu() {
	try {
		// 💡 SELGBN 대신 GBNCD 사용
		const res = await api.post('/api/ha00/HA00_00P_STR', { GUBUN: 'E0', GBNCD: '320', CMPYCD: authStore.CMPYCD })
		if (res.data) {
			upmuOptions.value = res.data.map((i: any) => ({ CODECD: String(i.CODECD || i.CODE).trim(), CODENM: String(i.CODENM || i.CDNM).trim() }))
			if (upmuOptions.value.length > 0) {
				searchForm.UPMUCD = upmuOptions.value[0].CODECD;

				await fetchCategories()
			}
		}
	} catch (e) { console.error('업무코드 로드 실패') }
}

async function fetchCategories() {
	if (!searchForm.USERGRP || !searchForm.UPMUCD) return
	try {
		const res = await api.post('/api/haba/HABA_935U_STR', { ACTKIND: 'S1', USERGRP: searchForm.USERGRP, UPMUCD: searchForm.UPMUCD, CMPYCD: authStore.CMPYCD })
		catGrid?.setData(res.data || [])
		pgmGrid?.clearData()
	} catch (e) { vAlertError('분류 조회 실패') }
}

async function fetchPermissions(grpCd: string) {
	searchForm.GRPCD = grpCd
	try {
		const res = await api.post('/api/haba/HABA_935U_STR', { ACTKIND: 'S0', USERGRP: searchForm.USERGRP, UPMUCD: searchForm.UPMUCD, GRPCD: grpCd, CMPYCD: authStore.CMPYCD })
		pgmGrid?.setData(res.data || [])
	} catch (e) { vAlertError('권한 로드 실패') }
}

async function saveAll() {
	const rows = pgmGrid?.getData()
	if (!rows || rows.length === 0) return vAlertError('저장할 데이터가 없습니다.')
	try {
		await api.post('/api/haba/HABA_935U_STR', { ACTKIND: 'U0', USERGRP: searchForm.USERGRP, UPMUCD: searchForm.UPMUCD, GRPCD: searchForm.GRPCD, ITEMS: rows, CMPYCD: authStore.CMPYCD, USERID: authStore.USERID })
		vAlert('성공적으로 저장되었습니다.')
		fetchPermissions(searchForm.GRPCD)
	} catch (e) { vAlertError('저장 실패') }
}

const toggleSelectAll = () => {
	const rows = pgmGrid?.getRows(); if (!rows) return
	const allChecked = rows.every(r => r.getData().USEYN === 'Y')
	rows.forEach(r => r.update({ USEYN: allChecked ? 'N' : 'Y' }))
}

onMounted(async () => {
	if (catGridRef.value) {
		catGrid = new Tabulator(catGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			columns: [
				{ title: '코드', field: 'GRPCD', width: 70, hozAlign: 'center' },
				{ title: '분류 명칭', field: 'GRPNM', headerSort: false, cssClass: 'fw-bold text-primary' }
			]
		})
		catGrid.on('rowClick', (e, row) => fetchPermissions(row.getData().GRPCD))
	}
	if (pgmGridRef.value) {
		pgmGrid = new Tabulator(pgmGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: 'center' },
			columns: [
				{ title: '프로그램ID', field: 'PGMID', width: 130, hozAlign: 'center', cssClass: 'fw-bold border-end' },
				{ title: '프로그램 명칭', field: 'PGMNM', minWidth: 200, widthGrow: 1, cssClass: 'fw-bold' },
				{
					title: '사용', field: 'USEYN', hozAlign: 'center', width: 80,
					formatter: (c) => c.getValue() === 'Y' ? '<span class="text-primary fw-bold">O</span>' : '<span class="text-danger fw-bold">X</span>',
					cellClick: (e, cell) => cell.setValue(cell.getValue() === 'Y' ? 'N' : 'Y')
				},
				{
					title: '즐겨찾기', field: 'MYPGM', hozAlign: 'center', width: 90,
					formatter: (c) => c.getValue() === 'Y' ? '<span class="text-warning">★</span>' : '<span class="text-secondary">☆</span>',
					cellClick: (e, cell) => cell.setValue(cell.getValue() === 'Y' ? 'N' : 'Y')
				}
			]
		})
	}

	await fetchGrp()

})
</script>

<style scoped>
.haba935u-wrapper { height: 100%; overflow: hidden; }
.tabulator-full-height { height: 100% !important; border: none; }
.btn-erp { padding: 4px 18px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-search { background-color: #2d3748; color: #fff; border: none; }
.btn-save { background-color: #005a9f; color: #fff; border: none; }
.btn-xs { padding: 2px 10px; font-size: 11px; }
:deep(.tabulator) { border: none; font-size: 12.5px; background-color: transparent !important; }
:deep(.tabulator-header) { background-color: #f1f5f9 !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; font-weight: 800; color: #334155; }
:deep(.tabulator-row.tabulator-selected) { background-color: #e2e8f0 !important; border-left: 4px solid #005a9f !important; }
:deep(.tabulator-cell) { border-right: 1px solid #f1f5f9 !important; padding: 8px 4px !important; }
</style>
