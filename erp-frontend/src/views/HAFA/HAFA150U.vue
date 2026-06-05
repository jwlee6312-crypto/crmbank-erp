<!--	=============================================================
	프로그램명	: 감가상각전표 발행
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 감가상각 계산 내역을 바탕으로 회계 전표를 일괄 또는 선택 발행 (디자인 표준화 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 (표준 규격 및 함수명 통일) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-receipt-cutoff me-2 text-primary" style="font-size: 18px;"></i>
				고정자산 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">감가상각전표 (HAFA150U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="search">조회</button>
				<button class="btn-erp btn-save" @click="save">전표발행</button>
			</div>
		</div>

		<!-- 🔍 2. 검색 조건 영역 (표준 erp-table-full 적용) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-0 bg-light">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 80px;" /><col style="width: 250px;" />
							<col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required">대상연월</th>
								<td class="bg-white">
									<div class="d-flex align-items-center gap-1 px-1">
										<select v-model="searchForm.yy" class="form-select" style="width: 100px;">
											<option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
										</select>
										<select v-model="searchForm.mm" class="form-select" style="width: 80px;">
											<option v-for="m in monthOptions" :key="m" :value="m">{{ m }}월</option>
										</select>
										<span class="small fw-bold ms-2 text-secondary">분 감가상각 내역 조회</span>
									</div>
								</td>
								<td class="bg-white"></td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>

		<!-- 📝 3. 전표 발행 정보 설정 (HSOD100U 스타일 고밀도 테이블 적용) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
					<div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-primary"></i>전표 발행 정보 설정</div>
					<div class="text-danger small fw-bold" style="font-size: 11px;">※ 전표 발행 시 선택한 항목에 대해 전표가 자동 생성됩니다.</div>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 80px;" /><col style="width: 250px;" />
							<col style="width: 80px;" /><col style="width: 200px;" />
							<col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required">발행부서</th>
								<td>
									<div class="input-group">
										<input v-model="issuingForm.deptcd" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 60px;" readonly />
										<input v-model="issuingForm.deptnm" type="text" class="form-control" @keydown.enter="openHelp('DEPT')" />
										<button class="btn" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="required">발행일자</th>
								<td><input v-model="issuingForm.acctymd" type="date" class="form-control" /></td>
								<td class="px-3">
									<div class="d-flex justify-content-end gap-2">
										<div class="d-flex align-items-center bg-light border rounded px-2 py-1">
											<span class="text-muted small me-2">총 상각액:</span>
											<span class="fw-bold text-dark small">{{ formatMoney(summary.totalAmt) }}</span>
										</div>
										<div class="d-flex align-items-center bg-primary-subtle border border-primary-subtle rounded px-2 py-1">
											<span class="text-primary-emphasis small me-2 fw-bold">발행대상:</span>
											<span class="fw-bold text-primary">{{ formatMoney(summary.issuingAmt) }}</span>
										</div>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>

		<!-- 📊 4. 그리드 영역 (표준 통합CSS 적용) -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                  <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
                </div>
			</div>
		</div>
	</div>

	<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useTabStore } from '@/stores/tabStore'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const tabStore = useTabStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const currentYear = new Date().getFullYear()
const currentMonth = (new Date().getMonth() + 1).toString().padStart(2, '0')
const yearOptions = Array.from({ length: 5 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))

const searchForm = reactive({ yy: String(currentYear),.mm: currentMonth })
const issuingForm = reactive({ deptcd: authStore.deptcd || '', deptnm: authStore.deptnm || '', acctymd: new Date().toISOString().slice(0, 10) })
const summary = reactive({ totalAmt: 0, issuingAmt: 0 })

const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

const formatMoney = (val: any) => Number(val || 0).toLocaleString()

async function search() {
	try {
		const res = await api.post('/api/hafa/HAFA_150U_STR', { cmpycd: authStore.cmpycd, baseym: searchForm.yy + searchForm.mm })
		const data = (res.data || []).map((row: any) => ({ Udeptcd: row.col0, Udeptnm: row.col1, acctcd: row.col2, acctnm: row.col3, costtype: row.col4, costtypeNM: row.col5, dprsamt: Number(row.col6 || 0), Cacctcd: row.col7, Macctcd: row.COL9, Sacctcd: row.col11, slipymd: row.col13, slipno: row.col14, srowno: row.col15 }))
		summary.totalAmt = data.reduce((acc: number, cur: any) => acc + cur.dprsamt, 0)
		summary.issuingAmt = 0; mainGrid?.setData(data); vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

async function save() {
	const selectedRows = mainGrid?.getSelectedData() || []
	if (selectedRows.length === 0) return vAlertError('전표 발행할 대상을 선택하세요.')
	if (!issuingForm.deptcd) return vAlertError('발행부서를 선택하세요.')
	if (!confirm('선택한 항목들에 대해 감가상각 전표를 자동 발행하시겠습니까?')) return
	try {
		await api.post('/api/hafa/HAFA_150U_SAVE', { cmpycd: authStore.cmpycd, yy: searchForm.yy,.mm: searchForm.mm, deptcd: issuingForm.deptcd, acctymd: issuingForm.acctymd.replace(/-/g, ''), items: selectedRows, userid: authStore.userid })
		vAlert('전표가 정상적으로 발행되었습니다.'); search()
	} catch (e) { vAlertError('전표 발행 중 오류 발생') }
}

function initialize() {
	searchForm.yy = String(currentYear); searchForm.mm = currentMonth;
	issuingForm.deptcd = authStore.deptcd || ''; issuingForm.deptnm = authStore.deptnm || '';
	issuingForm.acctymd = new Date().toISOString().slice(0, 10);
	mainGrid?.clearData(); summary.totalAmt = 0; summary.issuingAmt = 0;
}

const modalVisible = ref(false); const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })
function openHelp(type: string) {
	if (type === 'DEPT') {
		Object.assign(modalProps, { title: '부서 선택', path: '/api/ha00/HA00_00P_STR', data: { gubun: 'D0', cmpycd: authStore.cmpycd, search: issuingForm.deptnm }, columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }],
			onConfirm: (d: any) => { issuingForm.deptcd = d.deptcd; issuingForm.deptnm = d.deptnm }
		})
	}
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: true,
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ formatter: "rowSelection", titleFormatter: "rowSelection", width: 40, hozAlign: "center", headerSort: false, cellClick: (e, cell) => cell.getRow().toggleSelect() },
				{ title: "부서코드", field: "Udeptcd", width: 80, hozAlign: "center" },
				{ title: "부서명", field: "Udeptnm", width: 150 },
				{ title: "계정코드", field: "acctcd", width: 90, hozAlign: "center" },
				{ title: "계정과목명", field: "acctnm", width: 150, cssClass: "fw-bold" },
				{ title: "비용구분", field: "costtypeNM", width: 100, hozAlign: "center" },
				{ title: "감가상각액", field: "dprsamt", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 }, cssClass: "text-primary fw-bold" },
				{ title: "전표번호", field: "slipno", width: 160, hozAlign: "center",
					formatter: (cell) => {
						const d = cell.getData(); if (!d.slipymd || d.slipymd === '00000000') return '';
						return `<span class="text-primary text-decoration-underline cursor-pointer">${d.slipymd}-${d.slipno}</span>`;
					},
					cellClick: (e, cell) => {
						const d = cell.getData(); if (!d.slipno) return;
						tabStore.addTab({ name: '일반전표관리', path: '/HASL/HASL110U', params: { slipymd: d.slipymd, slipno: d.slipno } });
					}
				}
			]
		})
		mainGrid.on("rowSelectionChanged", (data) => summary.issuingAmt = data.reduce((acc: number, cur: any) => acc + cur.dprsamt, 0))
	}
})
</script>
