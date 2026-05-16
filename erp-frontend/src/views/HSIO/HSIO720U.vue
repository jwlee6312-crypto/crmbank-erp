<!--
	=============================================================
	프로그램명	: 세트상품입고작업 (Set Goods Receipt)
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : [최종완성] HSIO550U(레이아웃) + HSOD100U(스타일/정렬) 표준 적용
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsio720u-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 (우측 상단 버튼 배치 표준) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-box-seam-fill me-2 text-primary" style="font-size: 18px;"></i>
				구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				입고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">세트상품입고작업 (HSIO720U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchList">조회</button>
				<button class="btn-erp btn-save" @click="handleSave">저장</button>
				<button class="btn-erp btn-danger" @click="handleDelete" :disabled="!formData.IONO || formData.IONO === ''">삭제</button>
			</div>
		</div>

		<!-- 🔍 2. 최상단 검색 조건 바 (HSIO550U 스타일) -->
		<div class="p-2 pb-0">
			<div class="card border shadow-sm bg-light bg-opacity-50">
				<div class="card-body py-2 px-3">
					<div class="d-flex align-items-center gap-4">
						<div class="d-flex align-items-center gap-2">
							<span class="fw-bold small text-dark" style="min-width: 60px;">입고부서</span>
							<div class="input-group input-group-sm" style="width: 250px;">
								<input v-model="searchForm.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
								<input v-model="searchForm.DEPTNM" type="text" class="form-control" @keyup.enter="handleOpenHelp('DEPT_SEARCH')" />
								<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('DEPT_SEARCH')"><i class="bi bi-search"></i></button>
							</div>
						</div>
						<div class="d-flex align-items-center gap-2">
							<span class="fw-bold small text-dark" style="min-width: 60px;">입고연월</span>
							<input v-model="uiSearchYM" type="month" class="form-control form-control-sm" style="width: 150px;" />
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📊 3. 메인 작업 영역 (좌우 분할 레이아웃) -->
		<div class="d-flex flex-row flex-grow-1 overflow-hidden p-2 gap-2">
			<!-- 🅰️ 좌측: 세트 입고 목록 -->
			<div class="card border shadow-sm d-flex flex-column" style="width: 280px; min-width: 280px;">
				<div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center">
					<span class="fw-bold small text-dark"><i class="bi bi-list-ul me-1"></i> 세트 입고 목록</span>
				</div>
				<div class="flex-grow-1 overflow-auto bg-white p-0">
					<div ref="poGridRef" style="height: 100%;"></div>
				</div>
			</div>

			<!-- 🅱️ 우측: 상세 정보 및 품목 그리드 -->
			<div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
				<!-- 마스터 정보 (2행 3열 준수) -->
				<div class="card border shadow-sm overflow-hidden">
					<div class="card-body p-0">
						<table class="erp-table-full">
							<colgroup>
								<col style="width: 100px;"><col />
								<col style="width: 100px;"><col />
								<col style="width: 100px;"><col />
							</colgroup>
							<tbody>
								<tr>
									<th>입고부서</th>
									<td>
										<div class="input-group input-group-sm">
											<input v-model="formData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
											<input v-model="formData.DEPTNM" type="text" class="form-control bg-light" readonly />
										</div>
									</td>
									<th class="required">입고번호</th>
									<td>
										<div class="d-flex gap-1">
											<input v-model="uiIOYM" type="month" class="form-control form-control-sm text-center fw-bold" style="width: 120px;" />
											<input v-model="formData.IONO" type="text" class="form-control form-control-sm text-center bg-light fw-bold text-primary" style="width: 60px;" readonly placeholder="0000" />
										</div>
									</td>
									<th class="required">입고일자</th>
									<td><input v-model="formData.IOYMD" type="date" class="form-control form-control-sm" /></td>
								</tr>
								<tr>
									<th class="required">입고창고</th>
									<td>
										<select v-model="formData.WHCD" class="form-select form-select-sm">
											<option v-for="opt in whOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
										</select>
									</td>
									<th>Lot No.</th>
									<td><input v-model="formData.LOTNO" type="text" class="form-control form-control-sm" /></td>
									<th>특기사항</th>
									<td><input v-model="formData.REMARK" type="text" class="form-control form-control-sm" /></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>

				<!-- 그리드 영역 (정중앙 정렬) -->
				<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
					<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="height: 40px;">
						<span class="fw-bold small text-dark d-flex align-items-center">
							<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 세트 구성 품목 명세
						</span>
						<button class="btn btn-xs btn-primary fw-bold" @click="addRow"><i class="bi bi-plus-lg me-1"></i> 행추가</button>
					</div>
					<div class="card-body p-0 flex-grow-1 bg-white">
						<div ref="itemGridRef" style="height: 100%;"></div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📊 4. 하단 요약 바 -->
		<div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom">
			<div class="row align-items-center w-100">
				<div class="col-md-3 small">항목: <span class="fw-bold text-warning">{{ activeItemCount }}</span> 건</div>
				<div class="col-md-9 text-end">
					<span class="fs-5 ms-2 fw-light">선택 총 수량: <span class="fw-bold text-white ms-2">{{ formatNumber(totalSummary) }}</span></span>
				</div>
			</div>
		</div>
	</div>

	<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
	<ItemHelpModal :visible="itemHelpVisible" :cmpycd="authStore.CMPYCD" :astKind="String(formData.ASTKIND || '2')" @close="itemHelpVisible = false" @confirm="onSelectItem" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import ItemHelpModal from '@/components/ItemHelpModal.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

const itemHelpVisible = ref(false)
const currentTargetRow = ref<any>(null)

const searchForm = reactive({
  DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  IOYM: new Date().toISOString().substring(0, 7).replace('-', '')
})

const formData = reactive<any>({
  ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, IOYM: new Date().toISOString().substring(0, 7).replace('-', ''), IONO: '',
  IOYMD: new Date().toISOString().substring(0, 10), DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
  WHCD: '100', LOTNO: '', REMARK: '', USERNM: authStore.USERNM, ASTKIND: '2'
})

const uiSearchYM = computed({ get: () => `${searchForm.IOYM.substring(0, 4)}-${searchForm.IOYM.substring(4, 6)}`, set: (v) => searchForm.IOYM = v.replace('-', '') })
const uiIOYM = computed({ get: () => `${formData.IOYM.substring(0, 4)}-${formData.IOYM.substring(4, 6)}`, set: (v) => formData.IOYM = v.replace('-', '') })

const whOptions = ref<any[]>([]);
const poGridRef = ref<HTMLDivElement | null>(null); const itemGridRef = ref<HTMLDivElement | null>(null)
let poGrid: Tabulator | null = null; let itemGrid: Tabulator | null = null
const activeItemCount = ref(0)

const totalSummary = computed(() => {
  const items = itemGrid?.getData() || []
  const active = items.filter((i: any) => i.UPKIND !== 'D')
  return active.reduce((acc, cur: any) => acc + (Number(cur.IOQTY) || 0), 0)
})

async function fetchList() {
  try {
    const res = await api.post('/api/hsio/HSIO_720U_STR', { ...searchForm, ACTKIND: 'S1', CMPYCD: authStore.CMPYCD })
    poGrid?.setData(res.data.data || []); itemGrid?.clearData(); vAlert('조회되었습니다.')
  } catch (e) { vAlertError('조회 실패') }
}

async function fetchDetail(row: any) {
  const d = row.getData();
  try {
    const res = await api.post('/api/hsio/HSIO_720U_STR', { IOYM: d.IOYM, IONO: d.IONO, ACTKIND: 'S0', CMPYCD: authStore.CMPYCD })
    if (res.data.data?.length) {
      Object.assign(formData, res.data.data[0])
      const resItems = await api.post('/api/hsio/HSIO_721U_STR', { IOYM: d.IOYM, IONO: d.IONO, ACTKIND: 'S0', CMPYCD: authStore.CMPYCD })
      itemGrid?.setData(resItems.data.data?.map((i: any) => ({ ...i, UPKIND: 'U' })) || [])
      activeItemCount.value = resItems.data.data?.length || 0
    }
  } catch (e) { vAlertError('상세 조회 실패') }
}

async function handleSave() {
  const items = itemGrid?.getData();
  if (!items || items.length === 0) return vAlertError('입고 품목을 추가하세요.')
  try {
    await api.post('/api/hsio/HSIO_720U_STR', { ...formData, ACTKIND: formData.IONO ? 'U0' : 'A0', ITEMS: items })
    vAlert('정상으로 작업이 되었습니다.'); fetchList()
  } catch (e) { vAlertError('저장 실패') }
}

async function handleDelete() {
    if(!confirm('삭제하시겠습니까?')) return
    try {
        await api.post('/api/hsio/HSIO_720U_STR', { ...formData, ACTKIND: 'D0' })
        vAlert('삭제되었습니다.'); initialize(); fetchList();
    } catch (e) { vAlertError('삭제 실패') }
}

function handleOpenHelp(type: string, target?: any) {
  if (type === 'DEPT_SEARCH') {
    openHelp('DEPT', (d) => { searchForm.DEPTCD = d.DEPTCD; searchForm.DEPTNM = d.DEPTNM });
  } else if (type === 'ITEM') {
    currentTargetRow.value = target;
    itemHelpVisible.value = true;
  }
}

const onSelectItem = (d: any) => {
  if (!currentTargetRow.value) return
  currentTargetRow.value.update({
    ITEMCD: d.ITEMCD,
    ITEMNM: d.ITEMNM,
    ITSIZE: d.ITSIZE,
    UNIT: d.UNIT || d.UNITNM,
    PRICE: d.INCOST || d.PRICE || 0,
    UPKIND: 'A'
  });
  itemHelpVisible.value = false;
  activeItemCount.value = itemGrid?.getData().filter((i: any) => i.UPKIND !== 'D').length || 0
}

const addRow = () => {
    itemGrid?.addRow({ IOQTY: 0, PRICE: 0, IOAMT: 0, UPKIND: 'A' });
    activeItemCount.value = itemGrid?.getData().filter((i: any) => i.UPKIND !== 'D').length || 0
}

function initialize() {
  resetForm(formData);
  formData.IOYM = new Date().toISOString().substring(0, 7).replace('-', '');
  formData.IOYMD = new Date().toISOString().substring(0, 10);
  formData.ASTKIND = '2';
  itemGrid?.clearData(); poGrid?.deselectRow(); activeItemCount.value = 0;
}

onMounted(async () => {
  api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'W0', CMPYCD: authStore.CMPYCD } })
     .then(r => whOptions.value = r.data.map((i:any)=>({CODE: i.CODE || i.WHCD, CDNM: i.CDNM || i.WHNM})));

  if (poGridRef.value) {
    poGrid = new Tabulator(poGridRef.value, {
      layout: 'fitColumns', height: '100%', selectable: 1,
      columns: [{ title: '입고번호', field: 'IONO_DISP', hozAlign: 'center', cssClass: 'fw-bold text-primary', mutatorData: (v, d) => `${d.IOYM}-${d.IONO}` }]
    })
    poGrid.on('rowClick', (e, row) => fetchDetail(row))
  }

  if (itemGridRef.value) {
    itemGrid = new Tabulator(itemGridRef.value, {
      layout: 'fitColumns', height: '100%',
      columnDefaults: { headerSort: false, headerHozAlign: "center", minWidth: 100 },
      columns: [
        { title: "상태", field: "UPKIND", width: 50, hozAlign: "center", formatter: (c) => {
            const v = c.getValue();
            if (v === 'A') return '<i class="bi bi-plus-circle-fill text-primary"></i>';
            if (v === 'U') return '<i class="bi bi-pencil-square text-warning"></i>';
            if (v === 'D') return '<i class="bi bi-dash-circle-fill text-danger"></i>';
            return '';
        }},
        { title: "품목코드", field: "ITEMCD", width: 120, formatter: (cell) => (cell.getValue() || '') + " <i class='bi bi-search text-primary ms-1 cursor-pointer'></i>", cellClick: (e, cell) => { if ((e.target as HTMLElement).classList.contains('bi-search')) handleOpenHelp('ITEM', cell.getRow()) } },
        { title: '품목명', field: 'ITEMNM', minWidth: 200, widthGrow: 1, cssClass: 'fw-bold' },
        { title: '규격', field: 'ITSIZE', width: 150 },
        { title: '단위', field: 'UNIT', width: 60, hozAlign: 'center' },
        { title: '수량', field: 'IOQTY', hozAlign: 'right', width: 100, editor: 'number' },
        { title: '단가', field: 'PRICE', hozAlign: 'right', width: 110, editor: 'number' },
        { title: '금액', field: 'IOAMT', hozAlign: 'right', width: 130, formatter: 'money' },
        { title: "", width: 40, hozAlign: "center", formatter: () => "<i class='bi bi-trash text-danger cursor-pointer'></i>", cellClick: (e, c) => { if (c.getRow().getData().UPKIND === 'A') c.getRow().delete(); else { c.getRow().update({ UPKIND: 'D' }); c.getRow().getElement().style.opacity = '0.5' }; activeItemCount.value = itemGrid?.getData().filter((i: any) => i.UPKIND !== 'D').length || 0 } }
      ]
    })
    itemGrid.on("cellEdited", (cell) => {
        const field = cell.getField();
        if (['IOQTY', 'PRICE'].includes(field)) {
            const d = cell.getData();
            const amt = Number(d.IOQTY || 0) * Number(d.PRICE || 0);
            cell.getRow().update({ IOAMT: amt });
            if (d.UPKIND !== 'A') cell.getRow().update({ UPKIND: 'U' });
        }
    })
  }
})

const formatNumber = (val: any) => Number(val || 0).toLocaleString()
</script>

<style scoped>
.hsio720u-wrapper { height: 100%; overflow: hidden; }
.tabulator-full-height { height: 100% !important; border: none; }

.erp-header { background-color: #ffffff !important; }

/* 💎 개별 파일의 스타일을 삭제하여 global.css 표준 디자인이 적용되도록 함 */
.btn-erp { padding: 4px 16px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff !important; color: #6c757d !important; border: 1px solid #6c757d !important; }
.btn-search { background-color: #2d3748 !important; color: #fff !important; border: none !important; }
.btn-save { background-color: #005a9f !important; color: #fff !important; border: none !important; }
.btn-danger { background-color: #d32f2f !important; color: #fff !important; border: none !important; }

erp-table-full { width: 100%; border-collapse: collapse; table-layout: fixed !important; border: 1px solid #dee2e6; }
.erp-table-full th {
  width: 1%; white-space: nowrap;
  background-color: #f8fafc; border: 1px solid #dee2e6;
  text-align: center; font-weight: 800; font-size: 12.5px; padding: 10px 15px !important; color: #495057;
}
.erp-table-full td { border: 1px solid #dee2e6; padding: 6px 10px !important; vertical-align: middle; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }
.bg-yellow { background-color: #fffde7 !important; }
</style>
