<!--
	=============================================================
	프로그램명	  : 월별 제품 제조원가명세서
    프로그램 ID	: HFMF214S
	작성일자	    : 25.02.24
	작성자	      : AI Assistant
    설명         : 품목별 제조원가계산서(월별) (FMF2140R_STR 연결 - 전수조사 규격 반영)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	<!-- 💡 HSOD100U와 동일한 품목 팝업 적용 -->
	<ItemHelpModal :visible="itemHelpVisible" :cmpycd="authStore.CMPYCD" astKind="2" @close="itemHelpVisible = false" @confirm="onSelectItem" />

	<div class="hfmf214s-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-calendar3 me-2 text-primary" style="font-size: 18px;"></i>
				원가관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">월별 제품 제조원가명세서 (HFMF214S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-search" @click="handleSearch">조회</button>
				<button class="btn-erp btn-outline-success px-3" style="border: 1px solid #198754 !important;" @click="handleExport">엑셀저장</button>
			</div>
		</div>

		<!-- 💡 2. 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
			<!-- 🅰️ 조회 조건 영역 -->
			<div class="card border shadow-sm overflow-hidden">
				<div class="card-body p-0">
					<table class="erp-table-full">
						<colgroup>
							<col style="width: 100px"><col style="width: 150px">
							<col style="width: 100px"><col style="width: 150px">
							<col style="width: 100px"><col>
						</colgroup>
						<tbody>
							<tr>
								<th>조회년도</th>
								<td>
									<select v-model="searchForm.year" class="form-select form-select-sm">
										<option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
									</select>
								</td>
								<th>재고자산</th>
								<td>
									<select v-model="searchForm.ASTKIND" class="form-select form-select-sm">
										<option value="200">제품</option>
										<option value="210">반제품</option>
									</select>
								</td>
								<th>품목선택</th>
								<td>
									<div class="input-group input-group-sm w-50">
										<input v-model="searchForm.ITEMCD" class="form-control text-center bg-light" style="max-width: 100px;" placeholder="코드" readonly />
										<input v-model="searchForm.ITEMNM" class="form-control bg-light" placeholder="품목 선택" readonly @click="openHelp" />
										<button class="btn btn-outline-secondary" type="button" @click="openHelp"><i class="bi bi-search"></i></button>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 🅱️ 데이터 영역 -->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden border-top border-3 border-primary">
				<div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">
					<span>월별 제조원가 추이 (1월 ~ 12월)</span>
				</div>
				<div class="card-body p-0 flex-grow-1 bg-white">
					<div ref="mainGridRef" class="tabulator-full-height" />
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import AppAlert from '@/components/AppAlert.vue'
import ItemHelpModal from '@/components/ItemHelpModal.vue'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

// 상태 관리
const yearOptions = ref<string[]>([])
const searchForm = reactive({
	year: new Date().getFullYear().toString(),
	ASTKIND: '200',
	ITEMCD: '',
	ITEMNM: ''
})

const itemHelpVisible = ref(false)
const mainGridRef = ref<HTMLElement | null>(null)
let mainGrid: Tabulator | null = null

const openHelp = () => {
    itemHelpVisible.value = true
}

const onSelectItem = (data: any) => {
    searchForm.ITEMCD = String(data.ITEMCD || '').trim()
    searchForm.ITEMNM = String(data.ITEMNM || '').trim()
    itemHelpVisible.value = false
    handleSearch()
}

const handleSearch = async () => {
    if(!searchForm.ITEMCD) return vAlertError('품목을 먼저 선택하세요.');
	try {
		const ym = searchForm.year + '01';
		console.log(`📋 [SSMS Query] EXEC FMF2140R_STR '${authStore.CMPYCD}', '${ym}', '${searchForm.ITEMCD}'`)

		// 💡 수정한 XML 규격: CMPYCD, YYMM, ITEMCD (LIMIT 제거)
		const { data } = await api.post('/api/hfmf/FMF2140R_STR', {
            CMPYCD: String(authStore.CMPYCD),
            YYMM: String(ym),
            ITEMCD: String(searchForm.ITEMCD)
        })
		mainGrid?.setData(data)
	} catch (e) {
        vAlertError('조회 실패')
    }
}

const handleExport = () => {
	mainGrid?.download('xlsx', `월별제조원가명세서_${searchForm.year}_${searchForm.ITEMNM}.xlsx`)
}

onMounted(() => {
    const curYear = new Date().getFullYear();
    for(let i=0; i<6; i++) yearOptions.value.push((curYear - i).toString());

	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerHozAlign: 'center', headerSort: false },
			columns: [
				{ title: '과목', field: 'ACCTNM', width: 200, frozen: true, hozAlign: 'left' },
				{ title: '1월', field: 'M01', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '2월', field: 'M02', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '3월', field: 'M03', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '4월', field: 'M04', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '5월', field: 'M05', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '6월', field: 'M06', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '7월', field: 'M07', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '8월', field: 'M08', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '9월', field: 'M09', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '10월', field: 'M10', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '11월', field: 'M11', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '12월', field: 'M12', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } }
			]
		})
	}
})
</script>

<style scoped>
.hfmf214s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.tabulator-full-height { height: 100% !important; min-height: 400px; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; border: none; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 100px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 6px 12px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
.required::after { content: ' *'; color: #dc3545; }
</style>
