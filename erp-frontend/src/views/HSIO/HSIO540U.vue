<!--
	=============================================================
	프로그램명	: 매출전표취소 (HSIO540U)
	작성일자	: 2025.02.24
	설명        : 발행된 매출 전표 조회 및 취소 처리 (ASP 패턴 기반 순차 저장 로직 및 소문자 통일)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-file-earmark-x-fill me-2 text-danger" style="font-size: 18px;"></i>
				영업정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				매출관리 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">매출전표취소 (HSIO540U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-arrow-clockwise"></i> 초기화
				</button>
				<button class="btn-erp btn-search" @click="fetchIssuedList">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-delete" @click="handleCancelSlip">
					<i class="bi bi-x-circle"></i> 전표 취소
				</button>
			</div>
		</div>

		<!-- 🔍 2. 최상단 검색 항목 영역 -->
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
									<span class="erp-label me-2">발행일자</span>
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

		<!-- 📊 3. 중앙: 취소 대상 그리드 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
					<span class="fw-bold small text-dark d-flex align-items-center">
						<i class="bi bi-list-check me-2 text-primary"></i> 전표 취소 대상 목록
					</span>
				</div>
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
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

// 상태 관리 (소문자 통일)
const searchForm = reactive({
	deptcd: authStore.deptcd, deptnm: authStore.deptnm,
	ioymdfr: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().substring(0, 10),
	ioymdto: new Date().toISOString().substring(0, 10)
})

const closingInfo = reactive({ clsymd: '', sclsym: '' })
const activeItemCount = ref(0)
const totals = reactive({ sum: 0 })

const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

const fetchIssuedList = async () => {
	try {
		const res = await api.post('/api/hsio/HSIO_540U_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd,
            gubun: '200',
			ioymdfr: searchForm.ioymdfr.replace(/-/g, ''),
			ioymdto: searchForm.ioymdto.replace(/-/g, ''),
            deptcd: searchForm.deptcd
		})
		mainGrid?.setData(res.data.map((i: any) => ({
            ...i,
            slipymd: i.slipymd,
            slipno: i.slipno,
            deptcd: i.deptcd,
            spyamt: i.spyamt,
            vatamt: i.vatamt,
            custcd: i.custcd,
            custnm: i.custnm,
            vattypenm: i.vattypenm
        })))
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 실패') }
}

/**
 * 🚀 전표 취소 처리 (ASP 로직 반영)
 */
const handleCancelSlip = async () => {
	const selected = mainGrid?.getSelectedData()
	if (!selected || selected.length === 0) return vAlertError('취소할 전표를 선택하세요.')

	if (!confirm('선택한 전표를 삭제(취소) 하시겠습니까?')) return
	try {
        // 1. 자동전표 여부 확인 (gbn: 'p1')
        const resset = await api.post('/api/ha00/HA00_010S_STR', { cmpycd: authStore.cmpycd, gubun: 'p1' })
        const autoslip = (resset.data?.[0]?.slipyn || 'N').toLowerCase()

		for (const item of selected) {
            const slipymd = (item.slipymd || '').replace(/-/g, '')
            const slipno = item.slipno
            const deptcd = item.deptcd

            if (!slipymd || slipymd === '00000000') continue;

            // 2. 자동전표인 경우 확정 취소 (ASP: HASL_020U_STR 'A0')
            if (autoslip === 'Y') {
                await api.post('/api/hasl/HASL_020U_STR', {
                    actkind: 'A0',
                    cmpycd: authStore.cmpycd,
                    slipymd: slipymd,
                    acctymd: slipymd,
                    slipno: slipno,
                    deptcd: deptcd,
                    slipkind: '040',
                    slipyn: 'Y',
                    cofmyn: 'N',
                    empnm: authStore.usernm,
                    updemp: authStore.userid
                })
            }

            // 3. 정산 내역 전표 정보 삭제 (ASP: HSIO_540U_STR 'D0')
			const res = await api.post('/api/hsio/HSIO_540U_STR', {
				actkind: 'D0',
				cmpycd: authStore.cmpycd,
                gubun: '200',
				ioymdfr: searchForm.ioymdfr.replace(/-/g, ''),
				ioymdto: searchForm.ioymdto.replace(/-/g, ''),
                deptcd: deptcd,
                slipymd: slipymd,
                slipno: slipno,
				userid: authStore.userid
			})

            const resData = res.data?.[0]
            if (resData && (resData.result === 'Y' || resData.erryn === 'Y' || resData.RESULT === 'Y' || resData.ERRYN === 'Y')) {
                throw new Error(resData.msg || resData.MSG || '전표 취소 중 업무 오류 발생')
            }
		}

		vAlert('정상적으로 처리되었습니다.')
		fetchIssuedList()
	} catch (e: any) {
        vAlertError(e.message || '취소 실패')
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
	mainGrid?.clearData(); activeItemCount.value = 0; totals.sum = 0;
}

const modalVisible = ref(false);
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	if (type === 'DEPT') {
		Object.assign(modalProps, {
			title: '부서 선택', path: '/api/ha00/HA00_00P_STR', defaultField: 'deptnm',
			data: { gubun: 'D0', cmpycd: authStore.cmpycd },
			columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }],
			onConfirm: (d: any) => { searchForm.deptcd = d.deptcd; searchForm.deptnm = d.deptnm }
		})
	}
	modalVisible.value = true
}

onMounted(async () => {
	api.get('/api/hs00/HS00_000S_STR', { params: { gubun: 'CL', cmpycd: authStore.cmpycd } }).then(r => {
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
					title: "선택", formatter: "rowSelection", titleFormatter: "rowSelection", width: 80, hozAlign: "center",
					headerClick: () => toggleAllRows()
				},
				{ title: "전표번호", field: "slip_full", width: 160, cssClass: "fw-bold text-primary",
				  formatter: (cell) => {
					  const d = cell.getData();
                      const slipymd = d.slipymd || d.SLIPYMD;
                      const slipno = d.slipno || d.slipno;
					  return slipymd && slipno ? `${slipymd}-${slipno}` : '';
				  }
				},
				{ title: "발행부서", field: "deptnm", width: 250 },
				{ title: "거래처", field: "custnm", minWidth: 200, hozAlign: "left", cssClass: "fw-bold" },
				{ title: "유형", field: "vattypenm", width: 150 },
				{ title: "공급가액", field: "spyamt", hozAlign: "right", width: 150, formatter: "money", formatterParams: { precision: 0 } },
				{ title: "부가세", field: "vatamt", hozAlign: "right", width: 150, formatter: "money", formatterParams: { precision: 0 } },
				{ title: "합계금액", field: "jsansum", hozAlign: "right", width: 150, formatter: "money", cssClass: "text-danger fw-bold",
				  mutatorData: (v,d) => Number(d.spyamt||0) + Number(d.vatamt||0)
				}
			]
		})
		mainGrid.on('rowSelectionChanged', (data) => {
			activeItemCount.value = data.length
			totals.sum = data.reduce((acc, cur: any) => acc + (Number(cur.spyamt||0) + Number(cur.vatamt||0)), 0)
		})
	}
})
</script>
