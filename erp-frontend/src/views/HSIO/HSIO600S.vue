<!--
	=============================================================
	프로그램명	: 기간별 출고현황 [디자인 원칙 13가지 + 검색영역 단일행 균등배분]
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : 기간별/창고별 출고 내역 조회 및 상세 이동 기능
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-calendar-check me-2 text-primary" style="font-size: 18px;"></i>
				영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				출고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">기간별 출고현황 (HSIO600S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-outline-success border" @click="excel">
					<i class="bi bi-file-earmark-excel"></i> 엑셀
				</button>
				<button class="btn-erp btn-outline-secondary border" @click="print">
					<i class="bi bi-printer"></i> 인쇄
				</button>
			</div>
		</div>

		<!-- 🔍 2. 최상단 검색 항목 영역 (단일행 25% 균등 배분 적용) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 25%;" />
						<col style="width: 25%;" />
						<col style="width: 25%;" />
						<col style="width: 25%;" />
					</colgroup>
					<tbody>
						<tr>
							<td>
								<div class="d-flex align-items-center px-2 text-nowrap">
									<span class="erp-label me-2" style="width: 80px; flex-shrink: 0;">출고창고</span>
									<select v-model="searchForm.whcd" class="form-select form-select-sm" style="flex-grow: 1;">
										<option value="000">전체</option>
										<option v-for="opt in whOptions" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
									</select>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2 text-nowrap">
									<span class="erp-label me-2" style="width: 80px; flex-shrink: 0;">출고일자</span>
									<div class="d-flex align-items-center gap-1 flex-grow-1">
										<input v-model="searchForm.frymd" type="date" class="form-control form-control-sm" style="flex-grow: 1;" />
										<span class="text-muted">~</span>
										<input v-model="searchForm.toymd" type="date" class="form-control form-control-sm" style="flex-grow: 1;" />
									</div>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2 text-nowrap">
									<span class="erp-label me-2" style="width: 80px; flex-shrink: 0;">출고유형</span>
									<select v-model="searchForm.iotype" class="form-select form-select-sm" style="flex-grow: 1;">
										<option value="000">전체</option>
										<option v-for="opt in ioTypeOptions" :key="opt.code" :value="opt.code">{{ opt.cdnm }}</option>
									</select>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2 text-nowrap">
									<span class="erp-label me-2" style="width: 80px; flex-shrink: 0;">거 래 처</span>
									<div class="input-group input-group-sm flex-nowrap flex-grow-1">
										<input v-model="searchForm.custnm" type="text" class="form-control" placeholder="거래처 선택" @keyup.enter="handleOpenHelp('CUST')" />
										<button class="btn btn-erp btn-outline-secondary px-2" @click="handleOpenHelp('CUST')">
											<i class="bi bi-search"></i>
										</button>
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
import { useRouter } from 'vue-router'
import { useCommonHelp } from '@/composables/useCommonHelp'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const router = useRouter()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

const now = new Date();
const firstDay = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10);
const today = now.toISOString().substring(0, 10);

const searchForm = reactive({
	whcd: '000',
	frymd: firstDay,
	toymd: today,
	iotype: '000',
	custcd: '',
	custnm: ''
})

const whOptions = ref<any[]>([])
const ioTypeOptions = ref<any[]>([])

const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

const search = async () => {
	try {
		const res = await api.post('/api/hsio/HSIO_600S_STR', {
			...searchForm,
			cmpycd: authStore.cmpycd,
			frymd: searchForm.frymd.replace(/-/g, ''),
			toymd: searchForm.toymd.replace(/-/g, '')
		})
		mainGrid?.setData(res.data || [])
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

const excel = () => mainGrid?.download("xlsx", "기간별출고현황.xlsx")
const print = () => vAlert('인쇄 기능을 준비 중입니다.')

const initialize = () => {
	resetForm(searchForm);
	searchForm.whcd = '000'; searchForm.iotype = '000';
	searchForm.frymd = firstDay; searchForm.toymd = today;
	mainGrid?.clearData();
}

function handleOpenHelp(type: string) {
	if (type === 'CUST') {
		openHelp('CUST', (d: any) => {
			searchForm.custcd = d.custcd;
			searchForm.custnm = d.custnm;
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
				{
					title: "출고번호", field: "iono_full", width: 140, cssClass: "fw-bold text-primary cursor-pointer",
					formatter: (cell) => `${cell.getData().ioym}-${cell.getData().iono}`,
					cellClick: (e, cell) => {
						const d = cell.getData();
						if (d.giotype === "100" && d.gubun === "1" && d.spyamt >= 0) router.push({ path: '/HSIO/HSIO500U', query: { ioym: d.ioym, iono: d.iono, deptcd: d.deptcd } });
						else if (d.giotype === "100" && d.gubun === "1" && d.spyamt < 0) router.push({ path: '/HSIO/HSIO490U', query: { ioym: d.ioym, iono: d.iono, deptcd: d.deptcd } });
						else if (d.giotype === "100" && d.gubun === "2") router.push({ path: '/HSOD/HSOD200S', query: { ioym: d.ioym, iono: d.iono, deptcd: d.deptcd } });
						else if (d.giotype === "200") router.push({ path: '/HSIO/HSIO580U', query: { ioym: d.ioym, iono: d.iono, deptcd: d.deptcd } });
						else if (d.giotype === "390") router.push({ path: d.ingbn === "100" ? '/HSIO/HSIO720U' : '/HSIO/HSIO730U', query: { ioym: d.ioym, iono: d.iono, deptcd: d.deptcd } });
						else if (d.giotype >= "300") router.push({ path: '/HSIO/HSIO570U', query: { ioym: d.ioym, iono: d.iono, deptcd: d.deptcd } });
					}
				},
				{ title: "출고유형", field: "iotypenm", width: 120 },
				{ title: "출고일자", field: "ioymd", width: 110, formatter: (c) => { const v = c.getValue(); return v ? `${v.substring(0,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : '' } },
				{ title: "품목명", field: "itemnm", minWidth: 200, widthGrow: 2, hozAlign: "left", cssClass: "fw-bold" },
				{ title: "수량", field: "qty", hozAlign: "right", width: 90, formatter: "money", formatterParams: { precision: 0 } },
				{ title: "단가", field: "price", hozAlign: "right", width: 110, formatter: "money", formatterParams: { precision: 0 } },
				{ title: "공급가", field: "spyamt", hozAlign: "right", width: 130, formatter: "money", formatterParams: { precision: 0 } },
				{ title: "부가세", field: "vatamt", hozAlign: "right", width: 110, formatter: "money", formatterParams: { precision: 0 } },
				{ title: "합계액", field: "amtsum", hozAlign: "right", width: 130, formatter: "money", cssClass: "text-primary fw-bold", mutatorData: (v,d) => Number(d.spyamt||0) + Number(d.vatamt||0) },
				{ title: "특기사항", field: "remark", minWidth: 200, hozAlign: "left" }
			]
		})
	}
})
</script>

<style scoped>
.erp-label { font-weight: 700; font-size: 12px; color: #475569; text-align: center; }
</style>
