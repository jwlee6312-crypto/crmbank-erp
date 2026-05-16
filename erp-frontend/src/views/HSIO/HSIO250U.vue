<!--
	=============================================================
	프로그램명	: 덤입고작업 (Bonus Goods Receipt)
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : [디자인정상화] 라벨 겹침 및 글자 일그러짐 해결, 고밀도 표준 적용
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsio250u-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1. 상단 액션 바 (표준 버튼 배치 및 색상) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-gift-fill me-2 text-primary" style="font-size: 18px;"></i>
				구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				입고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">덤입고작업 (HSIO250U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchOrder">조회</button>
				<button class="btn-erp btn-save" @click="handleSave">저장</button>
				<button class="btn-erp btn-danger" @click="handleDelete" :disabled="!formData.IONO || formData.IONO === ''">삭제</button>
				<button class="btn-erp btn-print" @click="handlePrint">인쇄</button>
			</div>
		</div>

		<!-- 💡 2. 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
			<!-- 🅰️ 마스터 정보 (라벨 폭 고정으로 일그러짐 해결) -->
			<div class="card border shadow-sm overflow-hidden flex-shrink-0">
				<div class="card-body p-0">
					<table class="erp-table-full">
						<colgroup>
							<col style="width: 100px;"><col style="width: 23%">
							<col style="width: 100px;"><col style="width: 23%">
							<col style="width: 100px;"><col style="width: 24%">
						</colgroup>
						<tbody>
							<tr>
								<th class="required">입고부서</th>
								<td>
									<div class="input-group input-group-sm">
										<input v-model="formData.DEPTCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
										<input v-model="formData.DEPTNM" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="handleOpenHelp('DEPT')" />
										<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="required">입고번호</th>
								<td>
									<div class="d-flex gap-1">
										<input v-model="uiIOYM" type="month" class="form-control form-control-sm text-center fw-bold" style="width: 120px;" />
										<input v-model="formData.IONO" type="text" class="form-control form-control-sm text-center bg-light fw-bold" style="width: 60px;" readonly placeholder="0000" />
									</div>
								</td>
								<th class="required">입고일자</th>
								<td><input v-model="formData.IOYMD" type="date" class="form-control form-control-sm" /></td>
							</tr>
							<tr>
								<th class="required">매&nbsp;&nbsp;입&nbsp;&nbsp;처</th>
								<td>
									<div class="input-group input-group-sm">
										<input v-model="formData.CUSTCD" type="text" class="form-control text-center bg-light" style="max-width: 70px;" readonly />
										<input v-model="formData.CUSTNM" type="text" class="form-control" placeholder="매입처 선택" @keyup.enter="handleOpenHelp('CUST')" />
										<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('CUST')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="required">입고창고</th>
								<td>
									<select v-model="formData.WHCD" class="form-select form-select-sm">
										<option v-for="opt in whOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
									</select>
								</td>
								<th>작&nbsp;&nbsp;업&nbsp;&nbsp;자</th>
								<td><input v-model="formData.USERNM" type="text" class="form-control form-control-sm bg-light text-center" readonly /></td>
							</tr>
							<tr>
								<th>특기사항</th>
								<td colspan="5"><input v-model="formData.REMARK" type="text" class="form-control form-control-sm" placeholder="덤입고 사유 및 참고사항 입력" /></td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 🅱️ 디테일 품목 그리드 (무한 확장 및 상하중앙 정렬) -->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between" style="height: 40px;">
					<span class="fw-bold small text-dark d-flex align-items-center">
						<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 덤입고 품목 상세 내역
					</span>
					<button class="btn btn-grid-row-add" @click="addGridRow" :disabled="formData.STS === 'Y'">
                      <i class="bi bi-plus-circle"></i> 행추가
                    </button>
				</div>
				<div class="card-body p-0 flex-grow-1 bg-white">
					<div ref="mainGridRef" style="height: 100%;"></div>
				</div>
			</div>
		</div>

		<!-- 📊 4. 하단 요약 바 -->
		<div class="erp-footer bg-dark text-white py-2 px-4 shadow-lg sticky-bottom flex-shrink-0">
			<div class="row align-items-center w-100">
				<div class="col-md-3 small">항목: <span class="fw-bold text-white">{{ activeItemCount }}</span> 건</div>
				<div class="col-md-9 text-end">
					<span class="me-4 small opacity-75">총 입고수량: <span class="fw-bold text-info ms-1">{{ formatNumber(totals.qty) }}</span></span>
					<span class="fs-5 ms-2 fw-light">총 공급가액: <span class="fw-bold text-white ms-2">{{ formatNumber(totals.amt) }}</span></span>
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

const formData = reactive<any>({
	ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, IOYM: new Date().toISOString().substring(0, 7).replace('-', ''), IONO: '',
	IOYMD: new Date().toISOString().substring(0, 10), DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
	CUSTCD: '', CUSTNM: '', WHCD: '100', REMARK: '', USERNM: authStore.USERNM, CLSYMD: '', SCLSYM: '', PKUNITYN: 'N', ASTKIND: '2'
})

const uiIOYM = computed({
	get: () => formData.IOYM ? `${formData.IOYM.substring(0, 4)}-${formData.IOYM.substring(4, 6)}` : '',
	set: (v) => formData.IOYM = v.replace('-', '')
})

const whOptions = ref<any[]>([]);
const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null
const activeItemCount = ref(0);
const totals = reactive({ qty: 0, amt: 0 })

const fetchOrder = async () => {
    if (!formData.IOYM || !formData.IONO) return vAlertError('입고번호를 입력하세요.')
	try {
		const res = await api.post('/api/hsio/HSIO_250U_STR', { ...formData, ACTKIND: 'S0' })
		if (res.data.data?.length > 0) {
			Object.assign(formData, res.data.data[0])
            const resItems = await api.post('/api/hsio/HSIO_251U_STR', { ...formData, ACTKIND: 'S0' })
            mainGrid?.setData(resItems.data.data.map((i: any) => ({ ...i, UPKIND: 'U' })))
            recalcTotals()
		}
	} catch (e) { vAlertError('조회 실패') }
}

const handleSave = async () => {
	if (!formData.CUSTCD) return vAlertError('매입처를 선택하세요.')
	const items = mainGrid?.getData();
	if (!items || items.length === 0) return vAlertError('입고 품목을 추가하세요.')

	try {
		const res = await api.post('/api/hsio/HSIO_250U_STR', { ...formData, ACTKIND: formData.IONO ? 'U0' : 'A0', ITEMS: items })
		vAlert('저장되었습니다.'); fetchOrder()
	} catch (e) { vAlertError('저장 실패') }
}

const handleDelete = async () => {
    if (!confirm('해당 자료를 삭제하시겠습니까?')) return
    try {
        await api.post('/api/hsio/HSIO_250U_STR', { ...formData, ACTKIND: 'D0' })
        vAlert('삭제되었습니다.'); initialize()
    } catch (e) { vAlertError('삭제 실패') }
}
const addGridRow = () => { mainGrid?.addRow({ UPKIND: 'A', IOQTY: 0, PRICE: 0, IOAMT: 0 }); recalcTotals();}

const recalcTotals = () => {
    const data = mainGrid?.getData() || []
    const active = data.filter((i: any) => i.UPKIND !== 'D')
    activeItemCount.value = active.length
    totals.qty = active.reduce((acc, cur: any) => acc + (Number(cur.IOQTY) || 0), 0)
    totals.amt = active.reduce((acc, cur: any) => acc + (Number(cur.IOAMT) || 0), 0)
}

function initialize() {
	resetForm(formData);
    formData.IOYM = new Date().toISOString().substring(0, 7).replace('-', '');
    formData.IOYMD = new Date().toISOString().substring(0, 10);
    formData.ASTKIND = '2';
    mainGrid?.clearData(); recalcTotals();
}

function handleOpenHelp(type: string, target?: any) {
  if (type === 'DEPT') {
    openHelp('DEPT', (d) => { formData.DEPTCD = d.DEPTCD; formData.DEPTNM = d.DEPTNM });
  } else if (type === 'CUST') {
    openHelp('CUST', (d) => { formData.CUSTCD = d.CUSTCD; formData.CUSTNM = d.CUSTNM });
  } else if (type === 'ITEM') {
    if (!formData.CUSTCD) return vAlertError('매입처를 먼저 선택하세요.');
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
  itemHelpVisible.value = false; recalcTotals();
}

onMounted(async () => {
    api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'W0', CMPYCD: authStore.CMPYCD } })
       .then(r => whOptions.value = r.data.map((i: any) => ({ CODE: i.WHCD || Object.values(i)[0], CDNM: i.WHNM || Object.values(i)[1] })));

    api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'U2', CMPYCD: authStore.CMPYCD } })
       .then(r => formData.PKUNITYN = r.data?.length ? 'Y' : 'N');

	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: "center", minWidth: 80 },
			columns: [
				{ title: "상태", field: "UPKIND", width: 50, hozAlign: "center", formatter: (c) => {
                    const v = c.getValue();
                    if (v === 'A') return '<i class="bi bi-plus-circle-fill text-primary"></i>';
                    if (v === 'U') return '<i class="bi bi-pencil-square text-warning"></i>';
                    if (v === 'D') return '<i class="bi bi-dash-circle-fill text-danger"></i>';
                    return '';
                }},
                  { title: "품목코드", field: "ITEMCD", width: 150, hozAlign: "center"},
                  {
                    title: "품목명",
                    field: "ITEMNM",
                    minWidth: 100,
                    widthGrow: 1,
                    cssClass: "fw-bold",
                    formatter: (cell) => `
                    <div class="d-flex justify-content-between align-items-center w-100">
                        <span>${cell.getValue() || ''}</span>
                        <i class="bi bi-search text-primary ms-1 cursor-pointer"></i>
                    </div>
                    `,
                    cellClick: (e, cell) => handleOpenHelp('ITEM', cell.getRow()) },
				{ title: '규격', field: 'ITSIZE', width: 200 },
				{ title: '단위', field: 'UNIT', width: 80, hozAlign: 'center' },
				{ title: '수량', field: 'IOQTY', hozAlign: 'right', width: 150, editor: 'number' },
				{ title: '단가', field: 'PRICE', hozAlign: 'right', width: 150, editor: 'number' },
				{ title: '금액', field: 'IOAMT', hozAlign: 'right', width: 150, formatter: 'money' },
                { title: '포장용기', field: 'PKUNITNM', width: 120, visible: formData.PKUNITYN === 'Y' },
                { title: "", width: 40, hozAlign: "center", formatter: () => "<i class='bi bi-trash text-danger cursor-pointer'></i>", cellClick: (e, c) => { if (c.getRow().getData().UPKIND === 'A') c.getRow().delete(); else { c.getRow().update({ UPKIND: 'D' }); c.getRow().getElement().style.opacity = '0.5' }; recalcTotals(); } }
			]
		})
        mainGrid.on("cellEdited", (cell) => {
            const field = cell.getField();
            if (['IOQTY', 'PRICE'].includes(field)) {
                const d = cell.getData();
                const amt = Number(d.IOQTY || 0) * Number(d.PRICE || 0);
                cell.getRow().update({ IOAMT: amt });
                if (d.UPKIND !== 'A') cell.getRow().update({ UPKIND: 'U' });
                recalcTotals();
            }
        })
	}
})

const formatNumber = (val: any) => Number(val || 0).toLocaleString()
const handlePrint = () => { vAlert('인쇄 준비 중') }
</script>

 <style scoped>
.hsio250u-wrapper { height: 100%; overflow: hidden; }
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