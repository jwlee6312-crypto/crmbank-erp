<!--
	=============================================================
	프로그램명	: 지급어음장 관리
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 지급어음장 등록 및 관리
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-journal-text me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">지급어음장 관리 (HABA170U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-plus-lg"></i> 신규
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> 조회
				</button>
				<button class="btn-erp btn-save" @click="save">
					<i class="bi bi-check-lg"></i> 저장
				</button>
			</div>
		</div>

		<!-- 🔍 검색 조건 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden bg-light">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 350px;" />
						<col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center border-end">어음번호</th>
							<td class="bg-white border-end px-2">
								<div class="d-flex align-items-center gap-2">
									<input v-model="searchForm.billno" type="text" class="form-control form-control-sm" maxlength="12" placeholder="시작 번호" @keydown.enter="search" />
									<span class="text-muted small">~</span>
									<input v-model="searchForm.billno_TO" type="text" class="form-control form-control-sm" maxlength="12" placeholder="종료 번호" @keydown.enter="search" />
								</div>
							</td>
							<td class="bg-white px-3 text-muted small">
								<i class="bi bi-info-circle me-1"></i> 조회하고자 하는 어음번호 범위를 입력하세요.
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📝 상세 정보 입력 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-header py-1 px-2 bg-light border-bottom">
					<span class="small fw-bold text-secondary"><i class="bi bi-pencil-square me-1"></i> 어음 상세 정보</span>
				</div>
				<table class="erp-table-full small">
					<colgroup>
						<col style="width: 80px;" /><col style="width: 18%;" />
						<col style="width: 80px;" /><col style="width: 100px;" />
						<col style="width: 80px;" /><col style="width: 25%;" />
						<col style="width: 80px;" /><col style="width: 15%;" />
						<col style="width: 80px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center bg-light-subtle border-end">어음번호</th>
							<td class="bg-white border-end px-2 py-1">
								<input v-model="masterForm.billno" type="text" class="form-control form-control-sm" maxlength="12" placeholder="번호 입력" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">등록매수</th>
							<td class="bg-white border-end border-top px-2 py-1">
								<div class="d-flex align-items-center gap-1">
									<input v-model="masterForm.billcnt" type="number" class="form-control form-control-sm text-end" />
									<span class="fw-bold small">매</span>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end border-top">발행은행</th>
							<td class="bg-white border-end border-top px-2 py-1">
								<div class="input-group input-group-sm">
									<input v-model="masterForm.bankcd" type="text" class="form-control text-center bg-light" style="max-width: 50px;" readonly />
									<input v-model="masterForm.banknm" type="text" class="form-control" placeholder="은행 검색" @keydown.enter="openHelp('BANK')" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('BANK')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end border-top">발행인</th>
							<td class="bg-white border-end border-top px-2 py-1">
								<input v-model="masterForm.issuman" type="text" class="form-control form-control-sm" maxlength="20" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">상태</th>
							<td class="bg-white border-top px-2 py-1">
								<div class="form-check form-check-inline m-0 pt-1">
									<input v-model="masterForm.useyn" class="form-check-input" type="checkbox" id="deleteCheck" true-value="N" false-value="Y">
									<label class="form-check-label small fw-bold text-danger" for="deleteCheck">삭제</label>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 그리드 영역 -->
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
import { ref, reactive, onMounted } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const normalizekeys = (obj: any) => {
  const n: any = {};
  if (!obj) return n;
  Object.keys(obj).forEach(k => n[k.toLowerCase()] = typeof obj[k] === 'string' ? obj[k].trim() : obj[k]);
  return n;
}

// 🔍 검색 데이터
const searchForm = reactive({
	billno: '',
	billno_TO: ''
})

// 📝 마스터 데이터
const masterForm = reactive({
	actkind: '',
	BILLGU: '100',
	billno: '',
	billno_TO: '', // Hidden in ASP detail but used in logic
	billcnt: 1,
	bankcd: '',
	banknm: '',
	issuman: '',
	useyn: 'Y'
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

const formatYmd = (v: string) => v && v.length === 8 ? `${v.substring(2, 4)}.${v.substring(4, 6)}.${v.substring(6, 8)}` : v

const search = async () => {
	try {
		const res = await api.post('/api/haba/HABA_170U_STR', {
			actkind: 'S1',
			cmpycd: authStore.cmpycd,
			BILLGU: masterForm.BILLGU,
			billno: searchForm.billno,
			billno_TO: searchForm.billno_TO
		})

		const processedData = (res.data || []).map((r: any) => {
			const n = normalizekeys(r);
			return {
				billno: n.col0 || n.billno,
				bankcd: n.col1 || n.bankcd,
				banknm: n.col2 || n.banknm,
				issuman: n.col3 || n.issuman,
				regdate: formatYmd(n.col4 || n.regdate),
				wonamt: Number(n.col5 || n.wonamt || 0),
				billgu_nm: n.col7 || n.billgu_nm,
				useyn: n.col8 || n.useyn
			}
		})

		mainGrid?.setData(processedData)
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

const save = async () => {
	if (!masterForm.billno) return vAlert('어음번호를 정확히 입력하세요.')
	if (isNaN(masterForm.billcnt) || masterForm.billcnt < 1) return vAlert('등록매수를 확인해 주십시요.')
	if (!masterForm.banknm) return vAlert('발행은행을 기재해 주십시요.')
	if (!masterForm.issuman) return vAlert('발행인을 기재해 주십시요.')

	try {
		const payload = {
			...masterForm,
			actkind: masterForm.actkind || 'U1',
			cmpycd: authStore.cmpycd,
			userid: authStore.userid
		}

		const res = await api.post('/api/haba/HABA_170U_STR', payload)

		if (res.data?.[0]?.ret_yn === 'Y') {
			vAlertError(res.data[0].ret_msg)
		} else {
			vAlert('정상으로 작업이 되었습니다.')
			search()
			initialize()
		}
	} catch (e) { vAlertError('저장 실패') }
}

const initialize = () => {
	resetForm(masterForm)
	masterForm.actkind = ''
	masterForm.BILLGU = '100'
	masterForm.billcnt = 1
	masterForm.useyn = 'Y'
}

// 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openHelp(type: string) {
	if (type === 'BANK') {
		Object.assign(modalProps, {
			title: '은행 선택', path: '/api/ha00/ha00_00p_str',
			data: { gubun: 'C3', cmpycd: authStore.cmpycd, gbncd: '', code: masterForm.banknm, remark: '' },
			columns: [
				{ title: '코드', field: 'CUSTCD', width: 80 },
				{ title: '은행명', field: 'CUSTNM', width: 180 }
			],
			onConfirm: (d: any) => {
				const n = normalizekeys(d);
				masterForm.bankcd = n.custcd;
				masterForm.banknm = n.custnm
			}
		})
	}
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "어음번호", field: "billno", width: 180, hozAlign: "center" },
				{ title: "발행은행", field: "banknm", minWidth: 200 },
				{ title: "발행인", field: "issuman", width: 120, hozAlign: "center" },
				{ title: "등록일", field: "regdate", width: 100, hozAlign: "center" },
				{ title: "금 액", field: "wonamt", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "형태", field: "billgu_nm", width: 100, hozAlign: "center" },
                { title: "삭제", field: "useyn", width: 80, hozAlign: "center",
                  formatter: (cell) => {
                    const val = String(cell.getValue() || '').trim().toUpperCase();
                    return val === 'Y' ? '<b class="text-primary">사용</b>' : '<span class="text-danger">삭제</span>';
                  }
                }
			],
			rowClick: (e, row) => {
				const d = row.getData()
				masterForm.actkind = 'U1'
				masterForm.billno = d.billno
				masterForm.bankcd = d.bankcd
				masterForm.banknm = d.banknm
				masterForm.issuman = d.issuman
				masterForm.useyn = d.useyn
				masterForm.billcnt = 1
			}
		})
	}
})
</script>

<style scoped>
.bg-light-subtle { background-color: #f8f9fa !important; }
:deep(.tabulator-row) { cursor: pointer; }
:deep(.tabulator-row:hover) { background-color: #f0f7ff !important; }
</style>
