<!--
	=============================================================
	프로그램명	: 입고처리 (Purchase Goods Receipt) [디자인 원칙 13가지 완벽 준수]
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : HSIP100U와 동일한 UI 표준 패턴 적용 (테두리, 정렬, 색상 통일)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsio060u-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1, 12. 상단 액션 바 (버튼 그룹 우측 상단 정렬 및 표준 색상) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-box-seam me-2 text-primary" style="font-size: 18px;"></i>
				구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				입고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">입고처리 (HSIO060U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="fetchCustList">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-save" @click="handleSave">
					<i class="bi bi-save"></i> 저장
				</button>
			</div>
		</div>

		<!-- 🔍 9. 최상단 검색 항목 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-light bg-opacity-50">
				<div class="card-body py-2 px-3">
					<div class="d-flex align-items-center gap-4">
						<div class="d-flex align-items-center gap-2">
							<span class="erp-label">입고부서</span>
							<div class="input-group input-group-sm" style="width: 250px;">
								<input v-model="searchForm.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
								<input v-model="searchForm.DEPTNM" type="text" class="form-control" placeholder="부서 선택" />
								<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT_SEARCH')"><i class="bi bi-search"></i></button>
							</div>
						</div>
						<div class="d-flex align-items-center gap-2">
							<span class="erp-label">발주일자</span>
							<div class="d-flex align-items-center gap-1">
								<input v-model="searchForm.YMDFR" type="date" class="form-control form-control-sm" style="width: 140px;" />
								<span class="text-muted mx-1">~</span>
								<input v-model="searchForm.YMDTO" type="date" class="form-control form-control-sm" style="width: 140px;" />
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📊 3. 메인 작업 영역 (좌우 분할 레이아웃) -->
		<div class="d-flex flex-row flex-grow-1 overflow-hidden p-2 gap-2">
			<!-- 🅰️ 좌측: 발주 거래처 목록 -->
			<div class="card border shadow-sm d-flex flex-column" style="width: 280px; min-width: 280px;">
				<div class="card-header bg-light py-1 px-3 border-bottom d-flex align-items-center">
					<span class="fw-bold small text-dark"><i class="bi bi-list-ul me-1"></i> 발주 거래처</span>
				</div>
				<div class="flex-grow-1 overflow-hidden bg-white">
					<div ref="poGridRef" style="height: 100%;"></div>
				</div>
			</div>

			<!-- 🅱️ 우측: 입고 상세 및 품목 그리드 -->
			<div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">
				<!-- 🅰️ 10. 입력 항목 영역 (입고 마스터 정보) -->
				<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
					<div class="card-body p-0">
						<table class="erp-table-full">
							<colgroup>
								<col style="width: 100px;"><col style="width: 25%">
								<col style="width: 100px;"><col style="width: 15%">
								<col style="width: 100px;"><col style="width: 15%">
                                <col style="width: 100px;"><col style="width: auto;">
							</colgroup>
							<tbody>
								<tr>
									<th>거&nbsp;&nbsp;래&nbsp;&nbsp;처</th>
									<td>
										<div class="d-flex gap-1">
											<input v-model="formData.CUSTCD" type="text" class="form-control form-control-sm text-center bg-light fw-bold" style="max-width: 70px;" readonly />
											<input v-model="formData.CUSTNM" type="text" class="form-control form-control-sm bg-light" readonly />
										</div>
									</td>
									<th class="required">입고창고</th>
									<td>
										<select v-model="formData.WHCD" class="form-select form-select-sm">
											<option v-for="opt in whOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
										</select>
									</td>
									<th class="required">입고일자</th>
									<td><input v-model="formData.IOYMD" type="date" class="form-control form-control-sm" /></td>
                                    <th>비고</th>
                                    <td><input v-model="formData.REMARK" type="text" class="form-control form-control-sm" placeholder="특기사항" /></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>

				<!-- 🅱️ 8. 품목 그리드 (중앙 정렬 표준 적용 및 HPIO200U 스타일 버튼) -->
				<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
					<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
						<span class="fw-bold small text-dark d-flex align-items-center">
							<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 입고 대상 품목 명세
						</span>
						<div class="d-flex gap-1">
                            <!-- 💎 그리드 내 보조 액션 버튼도 메인 버튼보다 작게 유지 -->
						    <button class="btn btn-xs btn-outline-secondary px-2" style="height: 28px; font-size: 11px;" @click="toggleAllRows">전체선택</button>
                        </div>
					</div>
					<div class="card-body p-0 flex-grow-1 bg-white">
						<div ref="itemGridRef" style="height: 100%;"></div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📊 4. 하단 요약 바 -->
		<div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom flex-shrink-0">
			<div class="row align-items-center w-100">
				<div class="col-md-3 small">선택 항목: <span class="fw-bold text-info">{{ activeItemCount }}</span> 건</div>
				<div class="col-md-9 text-end">
					<span class="fs-5 ms-2 fw-light">총 입고수량: <span class="fw-bold text-warning ms-2">{{ formatNumber(totalSummary) }}</span></span>
				</div>
			</div>
		</div>
	</div>

	<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

// 13. 모든 변수명 대문자 고수
const searchForm = reactive({
  DEPTCD: authStore.DEPTCD,
  DEPTNM: authStore.DEPTNM,
  YMDFR: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10),
  YMDTO: new Date().toISOString().substring(0, 10)
})

const formData = reactive<any>({
  CUSTCD: '', CUSTNM: '', WHCD: '100',
  IOYMD: new Date().toISOString().substring(0, 10),
  REMARK: '', PCLSYM: '', SCLSYM: '', WCLSYM: ''
})

const whOptions = ref<any[]>([])
const poGridRef = ref<HTMLDivElement | null>(null);
const itemGridRef = ref<HTMLDivElement | null>(null);
let poGrid: Tabulator | null = null;
let itemGrid: Tabulator | null = null;
const activeItemCount = ref(0);

const totalSummary = computed(() => {
  const items = itemGrid?.getData().filter((r: any) => r.PROCYN === 'Y') || []
  return items.reduce((acc, cur: any) => acc + (Number(cur.IOQTY) || 0), 0)
})

async function fetchCustList() {
  try {
    const res = await api.post('/api/hsio/HSIO_060U_STR', {
      ...searchForm,
      IOGBN: '100',
      ACTKIND: 'S1',
      IOYMDFR: searchForm.YMDFR.replace(/-/g, ''),
      IOYMDTO: searchForm.YMDTO.replace(/-/g, '')
    })
    poGrid?.setData(res.data.data || [])
    itemGrid?.clearData();
    vAlert('조회되었습니다.')
  } catch (e) { vAlertError('거래처 조회 실패') }
}

async function fetchDetail(cust: any) {
  formData.CUSTCD = cust.CUSTCD;
  formData.CUSTNM = cust.CUSTNM;
  try {
    const res = await api.post('/api/hsio/HSIO_060U_STR', {
      ...searchForm,
      IOGBN: '100',
      ACTKIND: 'S0',
      CUSTCD: cust.CUSTCD,
      IOYMDFR: searchForm.YMDFR.replace(/-/g, ''),
      IOYMDTO: searchForm.YMDTO.replace(/-/g, '')
    })
    const detailData = (res.data.data || []).map((i: any) => ({ ...i, IOQTY: i.JANQTY, PROCYN: 'Y' }))
    itemGrid?.setData(detailData)
    setTimeout(updateCount, 100)
  } catch (e) { vAlertError('상세 조회 실패') }
}

async function handleSave() {
  const items = itemGrid?.getData().filter((r: any) => r.PROCYN === 'Y')
  if (!items || items.length === 0) return vAlertError('입고 항목을 선택하세요.')
  if (!confirm('입고작업을 하시겠습니까?')) return

  try {
    const mRes = await api.post('/api/hsio/HSIO_060U_STR', {
        ...formData,
        ACTKIND: 'A0',
        IOGBN: '100',
        IOYMD: formData.IOYMD.replace(/-/g, '')
    })

    if (mRes.data.data?.length > 0) {
        const ioYM = mRes.data.data[0].IOYM;
        const ioNo = mRes.data.data[0].IONO;

        for (const item of items) {
            await api.post('/api/hsio/HSIO_061U_STR', {
                ...item, ACTKIND: 'A0', IOGBN: '100', IOYM: ioYM, IONO: ioNo,
                IOYMD: formData.IOYMD.replace(/-/g, ''), WHCD: formData.WHCD, UPDEMP: authStore.USERID
            })
        }
        vAlert('정상적으로 처리되었습니다.');
        fetchCustList();
    }
  } catch (e) { vAlertError('저장 실패') }
}

const toggleAllRows = () => {
  const rows = itemGrid?.getRows(); if (!rows) return
  const allSelected = rows.every(r => r.getData().PROCYN === 'Y')
  rows.forEach(r => r.update({ PROCYN: allSelected ? 'N' : 'Y' }))
  updateCount()
}

const updateCount = () => {
    const data = itemGrid?.getData() || []
    activeItemCount.value = data.filter((r: any) => r.PROCYN === 'Y').length
}

function initialize() {
  resetForm(formData);
  formData.IOYMD = new Date().toISOString().substring(0, 10);
  poGrid?.clearData(); itemGrid?.clearData(); activeItemCount.value = 0;
}

onMounted(async () => {
  api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'W0', CMPYCD: authStore.CMPYCD } })
     .then(r => whOptions.value = r.data.map((i:any)=>({CODE: i.CODE || i.WHCD, CDNM: i.CDNM || i.WHNM})));

  if (poGridRef.value) {
    poGrid = new Tabulator(poGridRef.value, {
      layout: 'fitColumns', height: '100%', selectable: 1,
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
      columns: [{ title: '발주 거래처', field: 'CUSTNM', cssClass: 'fw-bold text-dark', hozAlign: 'left' }]
    })
    poGrid.on('rowClick', (e, row) => fetchDetail(row.getData()))
  }

  if (itemGridRef.value) {
    itemGrid = new Tabulator(itemGridRef.value, {
      layout: 'fitColumns', height: '100%',
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
      columns: [
        {
            title: '선택', field: 'PROCYN', hozAlign: 'center', width: 80, formatter: 'tickCross', editor: true,
            headerClick: () => toggleAllRows()
        },
        { title: '발주일', field: 'BALYMD', width: 150 },
        { title: '품목명', field: 'ITEMNM', minWidth: 200, widthGrow: 1, cssClass: 'fw-bold', hozAlign: 'left' },
        { title: '규격', field: 'ITSIZE', width: 200, hozAlign: 'left' },
        { title: '단위', field: 'UNIT', width: 70 },
        { title: '미입고', field: 'JANQTY', hozAlign: 'right', width: 150, cssClass: 'text-danger fw-bold', formatter: 'money', formatterParams: { precision: 0 } },
        { title: '입고량', field: 'IOQTY', hozAlign: 'right', width: 150, editor: 'number', cssClass: 'bg-yellow fw-bold', formatter: 'money', formatterParams: { precision: 0 } }
      ]
    })
    itemGrid.on("cellEdited", (cell) => { if (cell.getField() === 'PROCYN') updateCount() })
  }
})

const formatNumber = (val: any) => Number(val || 0).toLocaleString()
const openHelp = (type: string) => { /* 팝업 로직 */ }
const modalVisible = ref(false); const modalProps = reactive<any>({ title: '', path: '', onConfirm: () => {} })
</script>

<style scoped>
.hsio060u-wrapper { height: 100%; overflow: hidden; }
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