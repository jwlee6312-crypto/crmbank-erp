<!--
	=============================================================
	프로그램명	  : 품목별 제조원가 계산서
    프로그램 ID	: HFMF212S
	작성일자	    : 25.02.24
	작성자	      : AI Assistant
    설명         : 품목별 제조원가 계산서 (FMF2120R_STR 연결 - ItemHelpModal 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	<!-- 💡 HSOD100U 스타일의 세련된 품목 팝업 적용 -->
	<ItemHelpModal :visible="itemHelpVisible" :cmpycd="authStore.CMPYCD" astKind="2" @close="itemHelpVisible = false" @confirm="onSelectItem" />

	<div class="hfmf212s-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-medical me-2 text-primary" style="font-size: 18px;"></i>
				원가관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">품목별 제조원가 계산서 (HFMF212S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-search" @click="handleSearch">조회</button>
			</div>
		</div>

		<!-- 💡 2. 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
			<!-- 🅰️ 조회 조건 영역 -->
			<div class="card border shadow-sm overflow-hidden">
				<div class="card-body p-0">
					<table class="erp-table-full">
						<colgroup>
							<col style="width: 100px"><col style="width: 250px">
							<col style="width: 100px"><col>
						</colgroup>
						<tbody>
							<tr>
								<th>마감년월</th>
								<td>
									<input v-model="searchForm.YM" type="month" class="form-control form-control-sm w-75" @change="handleSearch" />
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

			<!-- 🅱️ 데이터 영역 (좌우 분할) -->
			<div class="row g-2 flex-grow-1 overflow-hidden">
				<!-- 좌측: 품목 목록 -->
				<div class="col-md-4 d-flex flex-column">
					<div class="card border shadow-sm h-100 overflow-hidden">
						<div class="card-header bg-light py-1 px-3 border-bottom fw-bold small text-dark">대상 품목 리스트</div>
						<div class="card-body p-0 flex-grow-1 bg-white">
							<div ref="leftGridRef" style="height: 100%;"></div>
						</div>
					</div>
				</div>
				<!-- 우측: 상세 제조원가 계산서 -->
				<div class="col-md-8 d-flex flex-column">
					<div class="card border shadow-sm h-100 overflow-hidden border-top border-3 border-primary">
						<div class="card-header bg-white py-1 px-3 border-bottom d-flex justify-content-between align-items-center">
							<span class="fw-bold small text-dark">
								<i class="bi bi-list-check me-1"></i>
								<span v-if="selectedItemNm" class="text-primary fw-bold">[품목: {{ selectedItemNm }}]</span>
								제조원가 상세 명세
							</span>
							<div class="btn-group">
								<button class="btn btn-xs btn-outline-success px-2" @click="handlePrint('Excel')"><i class="bi bi-file-earmark-excel me-1"></i>Excel</button>
								<button class="btn btn-xs btn-outline-dark px-2" @click="handlePrint('Print')"><i class="bi bi-printer me-1"></i>인쇄</button>
							</div>
						</div>
						<div class="card-body p-0 flex-grow-1 bg-white">
							<div ref="mainGridRef" style="height: 100%;"></div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import AppAlert from '@/components/AppAlert.vue'
import ItemHelpModal from '@/components/ItemHelpModal.vue'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlertError } = useAlerts()

// 상태 관리
const searchForm = reactive({
	YM: new Date().toISOString().substring(0, 7),
	ITEMCD: '',
	ITEMNM: ''
})

const selectedItemNm = ref('')
const itemHelpVisible = ref(false)
const leftGridRef = ref<HTMLElement | null>(null)
const mainGridRef = ref<HTMLElement | null>(null)
let leftGrid: Tabulator | null = null
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

// 💡 조회 (XML 규격 준수: LIMIT 파라미터 삭제 및 전체 문자열 처리)
const handleSearch = async () => {
	try {
		const ym = searchForm.YM.replace('-', '')
		// 📋 SSMS Query 로그
		console.log(`📋 [SSMS Query] EXEC FMF2120R_STR '${authStore.CMPYCD}', '${ym}', '${searchForm.ITEMCD}'`)

		const { data } = await api.post('/api/hfmf/FMF2120R_STR', {
			CMPYCD: String(authStore.CMPYCD),
			YYMM: String(ym),
			ITEMCD: String(searchForm.ITEMCD)
		})
		leftGrid?.setData(data)
		mainGrid?.clearData()
		selectedItemNm.value = ''

		if (data && data.length > 0) {
			nextTick(() => leftGrid?.selectRow(leftGrid.getRows()[0]))
		}
	} catch (e) {
        vAlertError('조회 실패')
    }
}

const selectDetails = async (itemcd: string) => {
	try {
		const ym = searchForm.YM.replace('-', '')
		const { data } = await api.post('/api/hfmf/FMF2120R_STR', {
			CMPYCD: String(authStore.CMPYCD),
			YYMM: String(ym),
			ITEMCD: String(itemcd)
		})
		mainGrid?.setData(data)
	} catch (e) {
        vAlertError('상세 명세 조회 실패')
    }
}

const handlePrint = (type: 'Excel' | 'Print') => {
    if(!selectedItemNm.value) return vAlertError('품목을 선택하세요.');
    const fileName = `품목별제조원가계산서_${selectedItemNm.value}_${searchForm.YM}`;
    if (type === 'Excel') {
        mainGrid?.download('xlsx', `${fileName}.xlsx`, { sheetName: '원가계산서' });
    } else {
        mainGrid?.print(false, true);
    }
}

onMounted(() => {
	if (leftGridRef.value) {
		leftGrid = new Tabulator(leftGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			columnDefaults: { minWidth: 100, headerSort: false },
			columns: [
				{ title: "No", formatter: "rownum", width: 40, hozAlign: "center" },
				{ title: '구분', field: 'GUBUN', width: 80, hozAlign: 'center' },
				{ title: '품목코드', field: 'ITEMCD', width: 120, hozAlign: 'center' },
				{ title: '품목명', field: 'ITEMNM', widthGrow: 2, hozAlign: 'left' }
			]
		})
		leftGrid.on('rowSelected', (row) => {
			const data = row.getData()
			selectedItemNm.value = data.ITEMNM
			selectDetails(data.ITEMCD)
		})
	}

	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerHozAlign: 'center', headerSort: false },
			columns: [
				{
                    title: '계정과목 명', field: 'ACCTNM', widthGrow: 3, hozAlign: 'left', frozen: true,
                    formatter: (cell) => {
                        const row = cell.getData();
                        const acct = String(row.ACCT || '');
                        const acctnm = cell.getValue();
                        if (acct === '411-0000') return `&nbsp;&nbsp;${acctnm}`;
                        if (acct === '412-0000') return `<b>Ⅱ. ${acctnm}</b>`;
                        if (acct === '413-0000') return `<b>Ⅲ. ${acctnm}</b>`;
                        if (acct === '419-0000') return `&nbsp;${acctnm}`;
                        return `&nbsp;&nbsp;&nbsp;&nbsp;${acctnm}`;
                    }
                },
				{ title: '수량', field: 'PRQQTY', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '금액', field: 'TOTAMT', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '단위당원가', field: 'PRICE', hozAlign: 'right', formatter: 'money', formatterParams: { precision: 2 }, cssClass: 'fw-bold text-primary bg-light' }
			]
		})
	}
	handleSearch()
})
</script>

<style scoped>
.hfmf212s-wrapper { height: 100%; overflow: hidden; font-family: 'Pretendard', sans-serif; }
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 12.5px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-xs { padding: 1px 5px; font-size: 0.75rem; }

.erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th { width: 100px; background-color: #f8f9fa; border: 1px solid #dee2e6; text-align: center; font-weight: 700; font-size: 12px; padding: 6px 12px !important; color: #495057; }
.erp-table-full td { border: 1px solid #dee2e6; padding: 4px 8px !important; background-color: #fff; vertical-align: middle; }
</style>
