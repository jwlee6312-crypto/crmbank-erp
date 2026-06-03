<!--
	=============================================================
	프로그램명	: 매출전표발행 [디자인 원칙 13가지 + 검색영역 단일행 균등배분]
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 매출 확정 내역에 대한 전표 생성 및 표준 UI 적용 (ASP 패턴 기반 순차 저장 로직 및 소문자 통일)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-check-fill me-2 text-primary" style="font-size: 18px;"></i>
				영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				매출관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">매출전표발행 (HSIO530U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="fetchUnissuedList">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-save" @click="handleGenerateSlip">
					<i class="bi bi-file-earmark-check"></i> 전표 발행
				</button>
			</div>
		</div>

		<!-- 🔍 2. 최상단 검색 항목 영역 (단일행 균등 배분 적용) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 50%;" />
						<col style="width: 50%;" />
					</colgroup>
					<tbody>
						<tr>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">판매부서</span>
									<div class="input-group input-group-sm flex-nowrap" style="max-width: 300px;">
										<input v-model="searchForm.deptcd" type="text" class="form-control text-center bg-white" style="max-width: 60px;" readonly />
										<input v-model="searchForm.deptnm" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="openHelp('DEPT')" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
									</div>
								</div>
							</td>
							<td>
								<div class="d-flex align-items-center px-2">
									<span class="erp-label me-2">정산일자</span>
									<div class="d-flex align-items-center gap-1 flex-grow-1" style="max-width: 320px;">
										<input v-model="searchForm.ioymdfr" type="date" class="form-control form-control-sm" />
										<span class="text-muted">~</span>
										<input v-model="searchForm.ioymdto" type="date" class="form-control form-control-sm" />
									</div>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 3. 중앙: 발행 대상 그리드 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
					<span class="fw-bold small text-dark d-flex align-items-center">
						<i class="bi bi-list-check me-2 text-primary"></i> 전표 발행 대기 목록
					</span>
				</div>
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                  <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
                </div>
			</div>

			<!-- 💰 4. 하단: 발행 정보 입력 영역 (표준 폼 적용) -->
			<div class="card border shadow-sm overflow-hidden flex-shrink-0">
				<div class="card-header bg-light py-1 px-3 border-bottom fw-bold small text-secondary">
					<i class="bi bi-pencil-square me-1"></i> 전표 발행 설정
				</div>
				<div class="card-body p-0">
					<table class="erp-table-full">
						<colgroup>
							<col style="width: 10%"><col style="width: 23%">
							<col style="width: 10%"><col style="width: 23%">
							<col style="width: 10%"><col style="width: 24%">
						</colgroup>
						<tbody>
							<tr>
								<th class="required">전표일자</th>
								<td><input v-model="slipForm.slipymd" type="date" class="form-control form-control-sm" /></td>
								<th class="required">발행부서</th>
								<td>
									<div class="input-group input-group-sm">
										<input v-model="formData.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
										<input v-model="formData.deptnm" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="openHelp('ISSUE_DEPT')" />
										<button class="btn btn-outline-secondary px-2" @click="openHelp('ISSUE_DEPT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th>정산마감</th>
								<td>
									<div class="d-flex gap-2">
										<input :value="closingInfo.clsymd" class="form-control form-control-sm text-center bg-light" readonly title="회계마감" />
										<input :value="closingInfo.sclsym" class="form-control form-control-sm text-center bg-light" readonly title="영업마감" />
									</div>
								</td>
							</tr>
						</tbody>
					</table>
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
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

// 상태 관리
const searchForm = reactive({
	deptcd: authStore.deptcd, deptnm: authStore.deptnm,
	ioymdfr: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10),
	ioymdto: new Date().toISOString().substring(0, 10)
})

const slipForm = reactive({ slipymd: new Date().toISOString().substring(0, 10) })
const formData = reactive({ deptcd: authStore.deptcd, deptnm: authStore.deptnm })
const closingInfo = reactive({ clsymd: '', sclsym: '' })
const activeItemCount = ref(0)
const totals = reactive({ spyamt: 0, vatamt: 0, sum: 0 })

const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

const fetchUnissuedList = async () => {
	try {
		const res = await api.post('/api/hsio/HSIO_530U_STR', {
			actkind: 's0',
			cmpycd: authStore.cmpycd,
			ioymdfr: searchForm.ioymdfr.replace(/-/g, ''),
			ioymdto: searchForm.ioymdto.replace(/-/g, ''),
			deptcd: searchForm.deptcd
		})
		mainGrid?.setData(res.data || [])
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

/**
 * 🚀 전표 발행 처리 (ASP 패턴 반영)
 */
const handleGenerateSlip = async () => {
	const selected = mainGrid?.getSelectedData()
	if (!selected || selected.length === 0) return vAlertError('발행할 항목을 선택하세요.')

	const slipymd = slipForm.slipymd.replace(/-/g, '')
	if (slipymd <= closingInfo.clsymd) return vAlertError('회계 마감된 일자입니다.')
	if (slipymd.substring(0,6) <= closingInfo.sclsym) return vAlertError('영업 마감된 월입니다.')

	if (!confirm('전표를 발행하시겠습니까?')) return
	try {
        // 1. 자동전표 여부 확인 (gbn: 'p1')
        const resset = await api.post('/api/comm/HA00_010S_STR', { cmpycd: authStore.cmpycd, gubun: 'p1' })
        const autoslip = (resset.data?.[0]?.slipyn || 'n').toLowerCase()
        const acctymd = autoslip === 'y' ? slipymd : ''
        const business = `${slipymd.substring(0, 4)}년 ${slipymd.substring(4, 6)}월 상품 매출 건`

        // 2. 전표 마스터 생성 (ASP: HASL_010U_STR 'A')
		const resmst = await api.post('/api/hasl/HASL_010U_STR', {
			actkind: 'a',
			cmpycd: authStore.cmpycd,
			slipymd: slipymd,
            slipno: '',
            acctymd: acctymd,
			deptcd: formData.deptcd,
            empnm: authStore.usernm,
			slipkind: '040',
            business: business,
			updemp: authStore.userid
		})

        const slipno = resmst.data?.[0]?.slipno
        if (!slipno || slipno === '000000') throw new Error('전표 마스터 생성 실패')

        // 3. 정산 내역 순차 업데이트 (ASP: HSIO_530U_STR 'U0')
        for (const item of selected) {
            const params = {
                actkind: 'u0',
                cmpycd: authStore.cmpycd,
                gubun: '200',
                ioymdfr: searchForm.ioymdfr.replace(/-/g, ''),
                ioymdto: searchForm.ioymdto.replace(/-/g, ''),
                udeptcd: item.udeptcd || item.deptcd || formData.deptcd,
                jsanym: item.jsanym,
                jsanno: item.jsanno,
                jsanymd: (item.jsanymd || item.jsanymD || '').replace(/-/g, ''),
                spyamt: String(item.spyamt || '0').replace(/,/g, ''),
                vatamt: String(item.vatamt || '0').replace(/,/g, ''),
                custcd: item.custcd,
                taxunit: item.taxunit,
                vattype: item.vattype,
                slipymd: slipymd,
                slipno: slipno,
                updemp: authStore.userid
            }
            const resdet = await api.post('/api/hsio/HSIO_530U_STR', params)
            const resdata = resdet.data?.[0]
            if (resdata && (resdata.status === 'y' || resdata.erryn === 'y' || resdata.STATUS === 'Y' || resdata.ERRYN === 'Y')) {
                throw new Error(resdata.msg || resdata.MSG || '정산 내역 처리 중 업무 오류 발생')
            }
        }

        // 4. 자동전표 확정 (autoslip === 'y')
        if (autoslip === 'y') {
            await api.post('/api/hasl/HASL_020U_STR', {
                actkind: 'a0',
                cmpycd: authStore.cmpycd,
                slipymd: slipymd,
                acctymd: acctymd,
                slipno: slipno,
                deptcd: formData.deptcd,
                slipkind: '040',
                slipyn: 'n',
                cofmyn: 'y',
                empnm: authStore.usernm,
                updemp: authStore.userid
            })
        }

		vAlert('전표가 발행되었습니다.')

        // 전표 인쇄 팝업 (ASP 로직 반영)
        const printUrl = `../HASL/HASL_SLIP_PRINT.asp?SLIPGU=010&SLIPYMD=${slipymd}&SLIPNO=${slipno}&DEPTCD=${formData.deptcd}`
        window.open(printUrl, '전표인쇄', 'left=10,top=10,width=700,height=650,scrollbars=yes')

		fetchUnissuedList()
	} catch (e: any) {
        vAlertError(e.message || '발행 실패')
    }
}

const toggleAllRows = () => {
	if (!mainGrid) return
	const rows = mainGrid.getRows()
	const allSelected = mainGrid.getSelectedRows().length === rows.length
	if (allSelected) mainGrid.deselectRow()
	else mainGrid.selectRow()
}

const initialize = () => {
	resetForm(searchForm);
	searchForm.deptcd = authStore.deptcd; searchForm.deptnm = authStore.deptnm;
	searchForm.ioymdfr = new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10);
	searchForm.ioymdto = new Date().toISOString().substring(0, 10);
	slipForm.slipymd = new Date().toISOString().substring(0, 10);
	mainGrid?.clearData(); activeItemCount.value = 0;
	totals.spyamt = 0; totals.vatamt = 0; totals.sum = 0;
}

const modalVisible = ref(false);
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	if (type === 'DEPT' || type === 'ISSUE_DEPT') {
		Object.assign(modalProps, {
			title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'deptnm',
			data: { gubun: 'd0', cmpycd: authStore.cmpycd },
			columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }],
			onConfirm: (d: any) => {
				if (type === 'DEPT') { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm }
				else { formData.deptcd = d.deptcd; formData.deptnm = d.deptnm }
			}
		})
	}
	modalVisible.value = true
}

onMounted(async () => {
	api.get('/api/hs00/HS00_000S_STR', { params: { gubun: 'cl', cmpycd: authStore.cmpycd } }).then(r => {
		if (r.data?.length) {
            const d = r.data[0]
			closingInfo.clsymd = String(d.clsymd || d.CLSYMD || '');
			closingInfo.sclsym = String(d.sclsym || d.SCLSYM || '');
		}
	});

	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: true,
			columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle", minWidth: 100 },
			columns: [
				{
					title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 60, hozAlign: "center",
					headerClick: () => toggleAllRows()
				},
				{ title: "정산일", field: "jsanymd", width: 150, formatter: (c) => {
                    const v = c.getValue() || c.getData().jsanymD;
                    return v && v.length === 8 ? `${v.substring(0,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : v;
                }},
				{ title: "정산부서", field: "deptnm", width: 250 },
				{ title: "거래처", field: "custnm", minWidth: 200, hozAlign: "left", cssClass: "fw-bold" },
				{ title: "사업장", field: "unitnm", width: 200 },
				{ title: "유형", field: "vattypenm", width: 150 },
				{ title: "공급가액", field: "spyamt", hozAlign: "right", width: 150, formatter: "money", formatterParams: { precision: 0 } },
				{ title: "부가세", field: "vatamt", hozAlign: "right", width: 150, formatter: "money", formatterParams: { precision: 0 } },
				{ title: "합계금액", field: "jsansum", hozAlign: "right", width: 150, formatter: "money", cssClass: "text-primary fw-bold" }
			]
		})
		mainGrid.on('rowSelectionChanged', (data) => {
			activeItemCount.value = data.length
			totals.spyamt = data.reduce((acc, cur: any) => acc + (Number(cur.spyamt) || 0), 0)
			totals.vatamt = data.reduce((acc, cur: any) => acc + (Number(cur.vatamt) || 0), 0)
			totals.sum = totals.spyamt + totals.vatamt
		})
	}
})

const formatNumber = (val: any) => Number(val || 0).toLocaleString()
</script>
