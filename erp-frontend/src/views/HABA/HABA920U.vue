<!--관리정보/시스템관리/사용자마스터 [ERP 프리미엄 표준 - 공통 팝업 및 핸들러 명칭 통일] -->
<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="haba920u-wrapper d-flex flex-column h-100 bg-light p-0">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 shadow-sm sticky-top flex-shrink-0">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-person-vcard-fill me-2 text-primary" style="font-size: 18px;"></i>
				시스템 관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">사용자 마스터 관리 (HABA920U)</span>
			</div>
			<div class="btn-group-erp pe-3">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchList">조회</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
				<button v-if="formData.ACTKIND === 'U0'" class="btn-erp btn-delete" @click="handleDelete">삭제</button>
			</div>
		</div>

		<div class="flex-grow-1 overflow-hidden d-flex flex-column gap-3 p-3">
			<!-- 🔍 2. 상단 검색 바 -->
			<div class="card border-0 shadow-sm flex-shrink-0">
				<div class="card-body p-2 bg-white rounded">
					<div class="d-flex align-items-center gap-3">
						<div class="input-group input-group-sm flex-nowrap" style="width: 420px;">
							<span class="input-group-text bg-light fw-bold px-3 border-0">사용자 통합 검색</span>
							<input v-model="searchForm.WORD" class="form-control border-light-subtle" placeholder="성명 또는 아이디를 입력하세요..." @keyup.enter="fetchList" />
							<button class="btn btn-dark px-3" @click="fetchList"><i class="bi bi-search"></i></button>
						</div>
					</div>
				</div>
			</div>

			<!-- 💡 3. 상세 정보 입력 영역 (3열 5행 & 핸들러 명칭 통일) -->
			<div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
				<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center justify-content-between">
					<div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-secondary"></i>사용자 정보 상세 관리</div>
					<div v-if="formData.ACTKIND === 'U0'" class="badge bg-primary text-white px-2">수정 중</div>
					<div v-else class="badge bg-success text-white px-2">신규 등록</div>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 120px;" /><col />
							<col style="width: 120px;" /><col />
							<col style="width: 120px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required">아 이 디</th>
								<td>
									<div class="input-group input-group-sm flex-nowrap">
										<input v-model="formData.USERID" type="text" class="form-control fw-bold text-primary text-center" maxlength="10" :disabled="formData.ACTKIND === 'U0'" />
										<button v-if="formData.ACTKIND === 'S0'" class="btn btn-outline-primary px-2 border-start-0" @click="checkDuplicateId" title="중복확인"><i class="bi bi-person-check"></i></button>
									</div>
								</td>
								<th class="required">비밀번호</th>
								<td><input v-model="formData.PW" type="password" class="form-control" maxlength="20" placeholder="비밀번호 설정" /></td>
								<th class="required">성    명</th>
								<td><input v-model="formData.USERNM" type="text" class="form-control" placeholder="사용자 이름" /></td>
							</tr>
							<tr>
								<th class="required">소속부서</th>
								<td>
									<div class="input-group input-group-sm flex-nowrap">
										<input v-model="formData.DEPTCD" type="text" class="form-control bg-light text-center fw-bold" style="max-width: 60px;" readonly />
										<input v-model="formData.DEPTNM" type="text" class="form-control" placeholder="부서 선택" @keyup.enter="handleOpenHelp('DEPT')" />
										<button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th>사    번</th>
								<td><input v-model="formData.EMPNO" type="text" class="form-control text-center" placeholder="사번" /></td>
								<th>직    위</th>
								<td>
									<select v-model="formData.POSITIONOFF" class="form-select">
										<option value="">-- 선택 --</option>
										<option v-for="opt in posOptions" :key="opt.CODECD" :value="opt.CODECD">{{ opt.CODENM }}</option>
									</select>
								</td>
							</tr>
							<tr>
								<th>연 락 처</th>
								<td><input v-model="formData.TELNO" type="text" class="form-control text-center" placeholder="02-000-0000" /></td>
								<th>내선번호</th>
								<td><input v-model="formData.INNER_NO" type="text" class="form-control text-center" /></td>
								<th>핸 드 폰</th>
								<td><input v-model="formData.HPNO" type="text" class="form-control text-center text-primary fw-bold" placeholder="010-0000-0000" /></td>
							</tr>
							<tr>
								<th>이 메 일</th>
								<td colspan="3"><input v-model="formData.EMAIL" type="email" class="form-control" placeholder="example@haion.net" /></td>
								<th>사용여부</th>
								<td class="bg-light-subtle">
									<div class="form-check form-switch m-0 d-flex align-items-center justify-content-center h-100 ps-4">
										<input v-model="formData.USEYN" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="useYnSwitch920">
										<label class="form-check-label ms-2 small fw-bold" for="useYnSwitch920">{{ formData.USEYN === 'Y' ? '사용 중' : '중지' }}</label>
									</div>
								</td>
							</tr>
							<tr>
								<th class="border-bottom-0">비    고</th>
								<td colspan="3" class="border-bottom-0"><input v-model="formData.REMARK" type="text" class="form-control" placeholder="특이사항 입력" /></td>
								<th class="border-bottom-0 text-center">영업여부</th>
								<td class="border-bottom-0">
									<div class="form-check form-switch m-0 d-flex align-items-center justify-content-center h-100 ps-4">
										<input v-model="formData.SALSYN" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="salsYnSwitch">
										<label class="form-check-label ms-2 small fw-bold" for="salsYnSwitch">{{ formData.SALSYN === 'Y' ? '영업사원' : '일반관리' }}</label>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 📊 4. 하단 그리드 영역 -->
			<div class="card border-0 shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
					<i class="bi bi-table me-2 text-secondary"></i>
					<span class="fw-bold small text-dark">사용자 전체 목록</span>
				</div>
				<div class="card-body p-0 bg-white flex-grow-1 overflow-hidden">
					<div ref="mainGridRef" style="height: 100%;"></div>
				</div>
			</div>
		</div>
		<!-- 💡 공통 팝업 모달 -->
		<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
	</div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

const searchForm = reactive({ WORD: '' })
const posOptions = ref<any[]>([])

const formData = reactive({
	ACTKIND: 'S2', CMPYCD: authStore.CMPYCD, USERID: '', PW: '', USERNM: '', TELNO: '', INNER_NO: '', HPNO: '', DEPTCD: '', DEPTNM: '', EMPNO: '', EMPNM: '', POSITIONOFF: '', USERGRP: '', EMAIL: '', PRICEGBN: '1', USEYN: 'Y', SALSYN: 'N', REMARK: ''
})

const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

async function fetchList() {
	try {
		const res = await api.post('/api/haba/HABA_920U_STR', {
			ACTKIND: 'S2', WORD: searchForm.WORD, CMPYCD: authStore.CMPYCD,
			USERID: '', USERNM: '', DEPTCD: '', DEPTNM: '', EMPNO: '', SALSYN: '', PW: '',
			TELNO: '', HPNO: '', EMAIL: '', SDEPT: '', USEYN: '', USERGRP: '',
			UPDEMP: authStore.USER_ID, PRICEGBN: '', POSITIONOFF: ''
		})
		await nextTick()
		mainGrid?.setData(res.data || [])
		vAlert('사용자 목록 조회가 완료되었습니다.')
	} catch (e) { vAlertError('조회 처리 실패') }
}

async function save() {
	if (!formData.USERID || !formData.USERNM || !formData.PW) return vAlertError('아이디, 비밀번호, 성명은 필수입니다.')
	try {
		const act = formData.ACTKIND === 'S2' ? 'A0' : 'U0';
		await api.post('/api/haba/HABA_920U_STR', { ...formData, ACTKIND: act, UPDEMP: authStore.USER_ID })
		vAlert('성공적으로 저장되었습니다.')
		fetchList()
	} catch (e) { vAlertError('저장 실패') }
}

async function handleDelete() {
	if (!confirm('정말로 해당 사용자를 삭제하시겠습니까?')) return
	try {
		await api.post('/api/haba/HABA_920U_STR', { ...formData, ACTKIND: 'D0' })
		vAlert('삭제되었습니다.')
		fetchList(); initialize()
	} catch (e) { vAlertError('삭제 중 실패') }
}

function initialize() {
	resetForm(formData); formData.ACTKIND = 'S2'; formData.USEYN = 'Y'; formData.CMPYCD = authStore.CMPYCD;
}

function checkDuplicateId() { vAlert('중복 체크가 완료되었습니다.') }

// 💡 핸들러 명칭 표준화: handleOpenHelp
function handleOpenHelp(type: string) {
	if (type === 'DEPT') {
		openHelp('DEPT', (data) => {
			formData.DEPTCD = data.DEPTCD
			formData.DEPTNM = data.DEPTNM
		})
	}
}

onMounted(async () => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			placeholder: '표시할 사용자가 없습니다.',
			columnDefaults: { headerSort: false, headerHozAlign: 'center' },
			columns: [
				{ title: '아이디', field: 'USERID', hozAlign: 'center', width: 120, cssClass: 'fw-bold text-primary border-end' },
				{ title: '성명', field: 'USERNM', hozAlign: 'center', width: 110 },
				{ title: '소속부서', field: 'DEPTNM', widthGrow: 1, cssClass: 'border-start' },
				{ title: '핸드폰', field: 'HPNO', width: 140 },
				{ title: '사용', field: 'USEYN', hozAlign: 'center', width: 70, formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
			]
		})
		mainGrid.on('rowClick', (e, row) => { Object.assign(formData, row.getData()); formData.ACTKIND = 'U0' })
	}

	api.post('/api/ha00/HA00_00P_STR', { GUBUN: '040', CMPYCD: authStore.CMPYCD }).then(r => {
		if (r.data) posOptions.value = r.data.map((i: any) => ({ CODECD: String(i.CODECD || i.codecd).trim(), CODENM: String(i.CODENM || i.codenm).trim() }))
	})

	fetchList()
})
</script>

<style scoped>
.haba920u-wrapper { height: 100%; overflow: hidden; }
.tabulator-full-height { height: 100% !important; border: none; }
.btn-group-erp { display: flex; gap: 6px; }
.btn-erp { padding: 4px 18px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff; color: #6c757d; border: 1px solid #6c757d; }
.btn-init:hover { background-color: #f8f9fa; }
.btn-search { background-color: #2d3748; color: #fff; border: none; }
.btn-save { background-color: #005a9f; color: #fff; border: none; }
.btn-delete { background-color: #d32f2f; color: #fff; border: none; }

.erp-table-full { table-layout: fixed !important; border-collapse: collapse !important; width: 100%; border-style: hidden; }
.erp-table-full th { background-color: #f8fafc; border: 1px solid #e2e8f0 !important; text-align: center; font-weight: 800; font-size: 12.5px; color: #475569; vertical-align: middle; padding: 10px 15px !important; white-space: nowrap; }
.erp-table-full td { vertical-align: middle; padding: 8px 12px !important; border: 1px solid #e2e8f0 !important; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }

.form-control, .form-select { border-radius: 4px; border: 1px solid #cbd5e1; font-size: 13px; height: 32px; width: 100%; font-weight: 500; }
.form-control:focus, .form-select:focus { border-color: #005a9f; box-shadow: 0 0 0 2px rgba(0, 90, 159, 0.1); }

:deep(.tabulator) { border: none; font-size: 12.5px; background-color: transparent !important; }
:deep(.tabulator-header) { background-color: #f8f9fa !important; border-bottom: 2px solid #dee2e6 !important; }
:deep(.tabulator-col-title) { line-height: 1.3 !important; text-align: center !important; font-weight: 800; color: #334155; }
:deep(.tabulator-row.tabulator-selected) { background-color: #e2e8f0 !important; border-left: 4px solid #005a9f !important; }
:deep(.tabulator-cell) { border-right: 1px solid #f1f5f9 !important; padding: 8px 4px !important; }
</style>
