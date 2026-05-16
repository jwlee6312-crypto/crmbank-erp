<!--
	=============================================================
	프로그램명	: 입고반품작업 (Purchase Return Entry) [디자인 원칙 13가지 완벽 준수]
	작성일자	: 25.02.24
	작성자	    : AI Assistant
	설명        : HSIP100U와 동일한 UI 표준 패턴 적용 (테두리, 정렬, 색상 통일)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="hsio190u-wrapper d-flex flex-column h-100 bg-white p-0">
		<!-- 🚀 1, 12. 상단 액션 바 (버튼 그룹 우측 상단 정렬 및 표준 색상) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-arrow-return-left me-2 text-danger" style="font-size: 18px;"></i>
				구매정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				입고관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">입고반품작업 (HSIO190U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="fetchOrder">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-save" @click="handleSave">
					<i class="bi bi-save"></i> 저장
				</button>
				<button class="btn-erp btn-delete" @click="handleDelete" :disabled="!formData.IONO || formData.IONO === ''">
					<i class="bi bi-trash"></i> 삭제
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
								<input v-model="formData.DEPTCD" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
								<input v-model="formData.DEPTNM" type="text" class="form-control" placeholder="부서 선택" />
								<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
							</div>
						</div>
						<div class="d-flex align-items-center gap-2">
							<span class="erp-label">반품번호</span>
							<div class="d-flex gap-1">
								<input v-model="uiIOYM" type="month" class="form-control form-control-sm text-center fw-bold" style="width: 130px;" />
								<input v-model="formData.IONO" type="text" class="form-control form-control-sm text-center fw-bold" style="width: 70px;" placeholder="번호" @keyup.enter="fetchOrder" />
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<!-- 📊 3. 메인 작업 영역 -->
		<div class="flex-grow-1 overflow-auto p-2 d-flex flex-column gap-2">
			<!-- 🅰️ 10. 입력 항목 영역 (마스터 정보) -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-body p-0">
					<table class="erp-table-full">
						<colgroup>
							<col style="width: 100px;"><col style="width: 25%">
							<col style="width: 100px;"><col style="width: 20%">
							<col style="width: 100px;"><col style="width: auto;">
						</colgroup>
						<tbody>
							<tr>
								<th class="required">반품일자</th>
								<td><input v-model="formData.IOYMD" type="date" class="form-control form-control-sm" /></td>
								<th class="required">매&nbsp;&nbsp;입&nbsp;&nbsp;처</th>
								<td>
									<div class="input-group input-group-sm">
										<input v-model="formData.CUSTCD" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 70px;" readonly />
										<input v-model="formData.CUSTNM" type="text" class="form-control form-control-sm bg-light" readonly />
										<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('CUST')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="required">출고창고</th>
								<td>
									<select v-model="formData.WHCD" class="form-select form-select-sm">
										<option v-for="opt in whOptions" :key="opt.CODE" :value="opt.CODE">{{ opt.CDNM }}</option>
									</select>
								</td>
							</tr>
							<tr>
								<th>작&nbsp;&nbsp;업&nbsp;&nbsp;자</th>
								<td><input v-model="formData.USERNM" type="text" class="form-control form-control-sm bg-light text-center" readonly /></td>
								<th>반품내역</th>
								<td colspan="3"><input v-model="formData.REMARK" type="text" class="form-control form-control-sm" placeholder="반품 사유 입력" /></td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 🅱️ 8. 디테일 그리드 영역 (중앙 정렬 표준 적용 및 HPIO200U 스타일 버튼) -->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
					<span class="fw-bold small text-dark d-flex align-items-center">
						<i class="bi bi-grid-3x3-gap-fill me-2 text-primary"></i> 반품 품목 상세 내역
					</span>
					<!-- 💎 HPIO200U 스타일로 행추가 버튼 통합 -->
					<button class="btn btn-sm btn-outline-primary px-3" style="height: 28px; font-size: 12px; font-weight: 600;" @click="addRow">
						<i class="bi bi-plus-circle me-1"></i> 행추가
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
				<div class="col-md-3 small">항목: <span class="fw-bold text-info">{{ activeItemCount }}</span> 건</div>
				<div class="col-md-9 text-end">
					<span class="me-4 small opacity-75">공급가액: <span class="fw-bold text-white ms-1">{{ formatNumber(totals.amt) }}</span></span>
					<span class="me-4 small opacity-75">부가세: <span class="fw-bold text-warning ms-1">{{ formatNumber(totals.vat) }}</span></span>
					<span class="fs-5 ms-2 fw-light">총 합계: <span class="fw-bold text-white ms-2">{{ formatNumber(totals.sum) }}</span> 원</span>
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

// 13. 모든 변수명 대문자 고수
const formData = reactive<any>({
	ACTKIND: 'S0', CMPYCD: authStore.CMPYCD, IOYM: new Date().toISOString().substring(0, 7).replace('-', ''), IONO: '',
	IOYMD: new Date().toISOString().substring(0, 10), DEPTCD: authStore.DEPTCD, DEPTNM: authStore.DEPTNM,
	CUSTCD: '', CUSTNM: '', WHCD: '100', REMARK: '', USERNM: authStore.USERNM, CLSYMD: '', SCLSYM: '', ASTKIND: '2'
})

const uiIOYM = computed({
	get: () => formData.IOYM ? `${formData.IOYM.substring(0, 4)}-${formData.IOYM.substring(4, 6)}` : '',
	set: (v) => formData.IOYM = v.replace('-', '')
})

const whOptions = ref<any[]>([]);
const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null
const activeItemCount = ref(0);
const totals = reactive({ amt: 0, vat: 0, sum: 0 })

const fetchOrder = async () => {
    if (!formData.IOYM || !formData.IONO) return vAlertError('반품번호를 입력하세요.')
	try {
		const res = await api.post('/api/hsio/HSIO_190U_STR', { ...formData, ACTKIND: 'S0' })
		if (res.data.data?.length > 0) {
			Object.assign(formData, res.data.data[0])
            const resItems = await api.post('/api/hsio/HSIO_191U_STR', { ...formData, ACTKIND: 'S0' })
            mainGrid?.setData(resItems.data.data.map((i: any) => ({ ...i, UPKIND: 'U' })))
            recalcTotals()
            vAlert('조회되었습니다.')
		} else {
            vAlertError('조회 결과가 없습니다.')
        }
	} catch (e) { vAlertError('조회 실패') }
}

const handleSave = async () => {
	if (!formData.CUSTCD) return vAlertError('매입처를 선택하세요.')
	const items = mainGrid?.getData();
	if (!items || items.length === 0) return vAlertError('반품 품목을 추가하세요.')

	try {
		const res = await api.post('/api/hsio/HSIO_190U_STR', { ...formData, ACTKIND: formData.IONO ? 'U0' : 'A0', ITEMS: items })
		vAlert('저장되었습니다.'); fetchOrder()
	} catch (e) { vAlertError('저장 실패') }
}

const handleDelete = async () => {
    if (!confirm('삭제하시겠습니까?')) return
    try {
        await api.post('/api/hsio/HSIO_190U_STR', { ...formData, ACTKIND: 'D0' })
        vAlert('삭제되었습니다.'); initialize()
    } catch (e) { vAlertError('삭제 실패') }
}

const handlePrint = () => { vAlert('인쇄 기능을 준비 중입니다.') }

const addRow = () => { mainGrid?.addRow({ UPKIND: 'A', IOQTY: 0, PRICE: 0, IOAMT: 0, IOVAT: 0, AMTSUM: 0, VATYN: 'Y' }, true); recalcTotals(); }

const recalcTotals = () => {
    const data = mainGrid?.getData() || []
    const active = data.filter((i: any) => i.UPKIND !== 'D')
    activeItemCount.value = active.length
    totals.amt = active.reduce((acc, cur: any) => acc + (Number(cur.IOAMT) || 0), 0)
    totals.vat = active.reduce((acc, cur: any) => acc + (Number(cur.IOVAT) || 0), 0)
    totals.sum = totals.amt + totals.vat
}

function initialize() {
	resetForm(formData);
    formData.IOYM = new Date().toISOString().substring(0, 7).replace('-', '');
    formData.IOYMD = new Date().toISOString().substring(0, 10);
    formData.DEPTCD = authStore.DEPTCD; formData.DEPTNM = authStore.DEPTNM;
    formData.ASTKIND = '2'; formData.USERNM = authStore.USERNM;
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
    VATYN: d.VATYN || 'Y',
    UPKIND: 'A'
  });
  itemHelpVisible.value = false; recalcTotals();
}

onMounted(async () => {
    const resWh = await api.get('/api/hs00/HS00_000S_STR', { params: { GUBUN: 'W0', CMPYCD: authStore.CMPYCD } })
    whOptions.value = resWh.data.map((i: any) => ({ CODE: i.WHCD || i.CODE, CDNM: i.WHNM || i.CDNM }))

	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
			columns: [
				{ title: "상태", field: "UPKIND", width: 60, formatter: (c) => {
                    const v = c.getValue();
                    if (v === 'A') return '<i class="bi bi-plus-circle-fill text-primary"></i>';
                    if (v === 'U') return '<i class="bi bi-pencil-square text-warning"></i>';
                    if (v === 'D') return '<i class="bi bi-dash-circle-fill text-danger"></i>';
                    return '';
                }},
                {
                    title: "품목코드", field: "ITEMCD", width: 140,
                    formatter: (cell) => {
                        return `<div class='d-flex align-items-center justify-content-between w-100 px-2 flex-nowrap'>
                                    <span>${cell.getValue() || ''}</span>
                                    <i class='bi bi-search text-primary cursor-pointer' style='font-size: 13px;'></i>
                                </div>`
                    },
                    cellClick: (e, cell) => { if ((e.target as HTMLElement).classList.contains('bi-search')) handleOpenHelp('ITEM', cell.getRow()) }
                },
				{ title: '품목명', field: 'ITEMNM', minWidth: 200, widthGrow: 2, cssClass: 'fw-bold', hozAlign: 'left' },
				{ title: '규격', field: 'ITSIZE', width: 150, hozAlign: 'left' },
				{ title: '단위', field: 'UNIT', width: 70 },
				{ title: '수량', field: 'IOQTY', hozAlign: 'right', width: 150, editor: 'number', formatter: 'money', formatterParams: { precision: 0 }, cssClass: 'bg-yellow fw-bold' },
				{ title: '단가', field: 'PRICE', hozAlign: 'right', width: 150, editor: 'number', formatter: 'money', formatterParams: { precision: 2 }, cssClass: 'bg-yellow fw-bold' },
				{ title: '금액', field: 'IOAMT', hozAlign: 'right', width: 150, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '부가세', field: 'IOVAT', hozAlign: 'right', width: 150, formatter: 'money', formatterParams: { precision: 0 } },
				{ title: '합계', field: 'AMTSUM', hozAlign: 'right', width: 150, formatter: 'money', cssClass: 'fw-bold text-primary', formatterParams: { precision: 0 } },
                {
                    title: "삭제", width: 60,
                    formatter: () => "<i class='bi bi-trash text-danger cursor-pointer' style='font-size: 16px;'></i>",
                    cellClick: (e, c) => {
                        if (c.getRow().getData().UPKIND === 'A') c.getRow().delete();
                        else {
                            c.getRow().update({ UPKIND: 'D' });
                            c.getRow().getElement().style.opacity = '0.5';
                        }
                        recalcTotals();
                    }
                }
			]
		})
        mainGrid.on("cellEdited", (cell) => {
            const field = cell.getField();
            if (['IOQTY', 'PRICE'].includes(field)) {
                const d = cell.getData();
                const amt = Number(d.IOQTY || 0) * Number(d.PRICE || 0);
                const vat = d.VATYN === 'Y' ? Math.floor(amt * 0.1) : 0;
                cell.getRow().update({ IOAMT: amt, IOVAT: vat, AMTSUM: amt + vat });
                if (d.UPKIND !== 'A') cell.getRow().update({ UPKIND: 'U' });
                recalcTotals();
            }
        })
	}
})

const formatNumber = (val: any) => Number(val || 0).toLocaleString()
</script>

<style scoped>
.hsio190u-wrapper { height: 100%; overflow: hidden; }
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