<!--
	=============================================================
	프로그램명	: 유형별 출고현황 [디자인 표준화 버전]
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : erp-container 표준 레이아웃 적용 (global.css 기반)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-grid-3x3-gap-fill me-2 text-primary" style="font-size: 18px;"></i>
				영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				출고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">유형별 출고현황 (HSIO610S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-outline-success border" style="font-size: 12px; height: 28px;" @click="excel">
					<i class="bi bi-file-earmark-excel"></i> 엑셀
				</button>
			</div>
		</div>

		<!-- 🔍 2. 최상단 검색 항목 영역 (20~25% 균등 배분) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 20%;" />
						<col style="width: 20%;" />
						<col style="width: 25%;" />
						<col style="width: 17.5%;" />
						<col style="width: 17.5%;" />
					</colgroup>
					<tbody>
						<tr>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label">출고창고</span>
									<select v-model="searchForm.whcd" class="form-select">
										<option value="000">전체</option>
										<option v-for="opt in whOptions" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
									</select>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label">출고유형</span>
									<select v-model="searchForm.iotype" class="form-select">
										<option value="000">전체</option>
										<option v-for="opt in ioTypeOptions" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
									</select>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label">출고일자</span>
									<div class="d-flex align-items-center gap-1 flex-grow-1">
										<input v-model="searchForm.frymd" type="date" class="form-control" />
										<span class="text-muted">~</span>
										<input v-model="searchForm.toymd" type="date" class="form-control" />
									</div>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label">거래처</span>
									<div class="input-group flex-nowrap">
										<input v-model="searchForm.custnm" type="text" class="form-control" placeholder="거래처" @keyup.enter="handleOpenHelp('CUST')" />
										<button class="btn btn-outline-secondary" @click="handleOpenHelp('CUST')"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label">품목명</span>
									<div class="input-group flex-nowrap">
										<input v-model="searchForm.itemnm" type="text" class="form-control" placeholder="품목명" @keyup.enter="handleOpenHelp('ITEM')" />
										<button class="btn btn-outline-secondary" @click="handleOpenHelp('ITEM')"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 3. 중앙 그리드 영역 -->
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
import { ref, reactive, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

const searchForm = reactive({
	whcd: '000', iotype: '000',
	frymd: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10),
	toymd: new Date().toISOString().substring(0, 10),
	custcd: '', custnm: '', itemcd: '', itemnm: ''
})

const whOptions = ref<any[]>([]); const ioTypeOptions = ref<any[]>([]);
const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

const search = async () => {
	try {
		const res = await api.post('/api/hsio/HSIO_610S_STR', {
			...searchForm, cmpycd: authStore.cmpycd,
			frymd: searchForm.frymd.replace(/-/g, ''),
			toymd: searchForm.toymd.replace(/-/g, '')
		})
		mainGrid?.setData(res.data || [])
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const initialize = () => {
	resetForm(searchForm);
	searchForm.whcd = '000'; searchForm.iotype = '000';
	searchForm.frymd = new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10);
	searchForm.toymd = new Date().toISOString().substring(0, 10);
	mainGrid?.clearData();
}

const excel = () => mainGrid?.download("xlsx", "유형별출고현황.xlsx")

function handleOpenHelp(type: string) {
	if (type === 'CUST') {
		openHelp('CUST', (d: any) => {
			searchForm.custcd = d.custcd;
			searchForm.custnm = d.custnm;
		});
	} else if (type === 'ITEM') {
		openHelp('ITEM', (d: any) => {
			searchForm.itemcd = d.itemcd;
			searchForm.itemnm = d.itemnm;
		});
	}
}

onMounted(async () => {
	try {
		const resWh = await api.post('/api/hs00/HS00_000S_STR', { gubun: 'W0', cmpycd: authStore.cmpycd })
		whOptions.value = resWh.data.map((i: any) => ({ code: i.code || i.whcd, cdnm: i.cdnm || i.whnm }))
		const resType = await api.post('/api/hs00/HS00_000S_STR', { gubun: 'E0', cmpycd: authStore.cmpycd, gbncd: '130' })
		ioTypeOptions.value = resType.data.map((i: any) => ({ code: i.code || i.codecd, cdnm: i.cdnm || i.codenm }))
	} catch (e) {}

	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
			columns: [
				{ title: "출고일", field: "ioymd", width: 110, formatter: (c) => { const v = c.getValue(); return v ? `${v.substring(0,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : '' } },
				{ title: "거래처", field: "custnm", minWidth: 180, hozAlign: "left", cssClass: "fw-bold" },
				{ title: "품목명", field: "itemnm", minWidth: 200, widthGrow: 2, hozAlign: "left" },
				{ title: "규격", field: "itsize", width: 150, hozAlign: "left" },
				{ title: "단위", field: "unit", width: 70 },
				{ title: "수량", field: "ioqty", hozAlign: "right", width: 100, formatter: "money", formatterParams: { precision: 0 } },
				{ title: "단가", field: "price", hozAlign: "right", width: 110, formatter: "money", mutatorData: (v,d) => Number(d.jsanamt)/(Number(d.ioqty)||1) },
				{ title: "공급가", field: "jsanamt", hozAlign: "right", width: 120, formatter: "money" },
				{ title: "부가세", field: "jsanvat", hozAlign: "right", width: 110, formatter: "money" },
				{ title: "합계", field: "amtsum", hozAlign: "right", width: 130, formatter: "money", cssClass: "text-primary fw-bold", mutatorData: (v,d) => Number(d.jsanamt||0) + Number(d.jsanvat||0) },
				{ title: "특기사항", field: "remark", minWidth: 200, hozAlign: "left" }
			]
		})
	}
})
</script>

<style scoped>
/* 🎨 개별 스타일 제거 완료. global.css 표준을 따릅니다. */
</style>
