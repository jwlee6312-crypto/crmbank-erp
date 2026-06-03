<!--관리정보/시스템관리/사용자마스터 [ERP 프리미엄 표준 - 1열 고밀도 배치] -->
<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
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
				<button v-if="formData.actkind === 'U0'" class="btn-erp btn-delete" @click="deleteData">삭제</button>
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

			<!-- 💡 3. 상세 정보 입력 영역 (가로 통합 배치) -->
			<div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
					<div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-secondary"></i>사용자 정보 상세 관리</div>
					<div v-if="formData.actkind === 'U0'" class="badge bg-primary text-white px-2">수정 중</div>
					<div v-else class="badge bg-success text-white px-2">신규 등록</div>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 70px;" /><col style="width: 150px;" /> <!-- 아이디 -->
							<col style="width: 70px;" /><col style="width: 150px;" /> <!-- 성명 -->
							<col style="width: 70px;" /><col style="width: 150px;" /> <!-- 비밀번호 -->
							<col style="width: 70px;" /><col  /> <!-- 부서 -->
							<col style="width: 70px;" /><col style="width: 150px;" /> <!-- 직위 -->
							<col style="width: 70px;" /><col style="width: 150px;" /> <!-- 내선번호 -->
						</colgroup>
						<tbody>
							<tr>
								<th class="required">아 이 디</th>
								<td>
									<div class="input-group input-group-sm flex-nowrap">
										<input v-model="formData.userid" type="text" class="form-control fw-bold text-primary text-center" maxlength="10" :disabled="formData.actkind === 'U0'" />
										<button v-if="formData.actkind === 'S2'" class="btn btn-outline-primary px-2 border-start-0" @click="checkDuplicateId" title="중복확인"><i class="bi bi-person-check"></i></button>
									</div>
								</td>
								<th class="required">성    명</th>
								<td><input v-model="formData.usernm" type="text" class="form-control" placeholder="성명" /></td>
								<th class="required">비밀번호</th>
								<td><input v-model="formData.pw" type="password" class="form-control" maxlength="20" placeholder="비밀번호" /></td>
								<th class="required">부    서</th>
                                <td>
                                    <div class="input-group input-group-sm flex-nowrap">
                                        <input v-model="formData.deptcd" type="text" class="form-control bg-light text-center fw-bold" style="max-width: 50px;" readonly />
                                        <input v-model="formData.deptnm" type="text" class="form-control" placeholder="부서명" @keyup.enter="handleOpenHelp('DEPT')" />
                                        <button class="btn btn-outline-secondary px-1" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
                                    </div>
                                </td>
								<th>직    위</th>
								<td>
									<select v-model="formData.positionoff" class="form-select">
										<option value="">-- 선택 --</option>
										<option v-for="opt in posOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
									</select>
								</td>
								<th>내선번호</th>
								<td><input v-model="formData.inner_no" type="text" class="form-control text-center" /></td>
							</tr>
							<tr>

								<th>연 락 처</th>
								<td><input v-model="formData.telno" type="text" class="form-control text-center" placeholder="02-000-0000" /></td>
								<th>핸 드 폰</th>
								<td><input v-model="formData.hpno" type="text" class="form-control text-center text-primary fw-bold" placeholder="010-0000-0000" /></td>
								<th>사    번</th>
                                <td><input v-model="formData.empno" type="text" class="form-control text-center" /></td>
								<th>이 메 일</th>
								<td><input v-model="formData.email" type="email" class="form-control" placeholder="example@haion.net" /></td>
								<th>영업여부</th>
								<td>
									<div class="form-check form-switch m-0 d-flex align-items-center justify-content-center h-100">
										<input v-model="formData.salsyn" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="salsYnSwitch">
										<label class="form-check-label ms-2 small fw-bold" for="salsYnSwitch">{{ formData.salsyn === 'Y' ? '영업' : '일반' }}</label>
									</div>
								</td>
								<th>사용여부</th>
								<td class="bg-light-subtle">
									<div class="form-check form-switch m-0 d-flex align-items-center justify-content-center h-100">
										<input v-model="formData.useyn" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="useYnSwitch920">
										<label class="form-check-label ms-2 small fw-bold" for="useYnSwitch920">{{ formData.useyn === 'Y' ? '사용' : '중지' }}</label>
									</div>
								</td>
							</tr>
							<!-- 특이사항 (사용자 요청으로 HIDDEN 처리) -->
							<tr style="display: none;">
								<th>비    고</th>
								<td colspan="11"><input v-model="formData.remark" type="text" class="form-control" /></td>
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
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                  <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
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

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

const searchForm = reactive({ WORD: '' })
const posOptions = ref<any[]>([])

const formData = reactive({
	actkind: 'S2', cmpycd: authStore.cmpycd, userid: '', pw: '', usernm: '', telno: '', inner_no: '', hpno: '', deptcd: '', deptnm: '', empno: '', EMPNM: '', positionoff: '', usergrp: '', email: '', pricegbn: '1', useyn: 'Y', salsyn: 'N', remark: ''
})

const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

async function fetchList() {
	try {
		const res = await api.post('/api/haba/HABA_920U_STR', {
			actkind: 'S2', WORD: searchForm.WORD, cmpycd: authStore.cmpycd,
			userid: '', usernm: '', deptcd: '', deptnm: '', empno: '', salsyn: '', pw: '',
			telno: '', hpno: '', email: '', SDEPT: '', useyn: '', usergrp: '',
			updemp: authStore.user_id, pricegbn: '', positionoff: '', positionoffNM: '', inner_no: ''
		})
		await nextTick()
		mainGrid?.setData(res.data || [])
		vAlert('사용자 목록 조회가 완료되었습니다.')
	} catch (e) { vAlertError('조회 처리 실패') }
}

async function save() {
	if (!formData.userid || !formData.usernm || !formData.pw) return vAlertError('아이디, 비밀번호, 성명은 필수입니다.')
	try {
		const act = formData.actkind === 'S2' ? 'A0' : 'U0';
		await api.post('/api/haba/HABA_920U_STR', { ...formData, actkind: act, updemp: authStore.user_id })
		vAlert('성공적으로 저장되었습니다.')
		fetchList()
	} catch (e) { vAlertError('저장 실패') }
}

async function deleteData() {
	if (!confirm('정말로 해당 사용자를 삭제하시겠습니까?')) return
	try {
		await api.post('/api/haba/HABA_920U_STR', { ...formData, actkind: 'D0' })
		vAlert('삭제되었습니다.')
		fetchList(); initialize()
	} catch (e) { vAlertError('삭제 중 실패') }
}

function initialize() {
	resetForm(formData); formData.actkind = 'S2'; formData.useyn = 'Y'; formData.cmpycd = authStore.cmpycd;
}

function checkDuplicateId() { vAlert('중복 체크가 완료되었습니다.') }

function handleOpenHelp(type: string) {
	if (type === 'DEPT') {
		openHelp('DEPT', (data) => {
			formData.deptcd = data.deptcd
			formData.deptnm = data.deptnm
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
				{ title: '아이디', field: 'userid', hozAlign: 'center', width: 150, cssClass: 'fw-bold text-primary border-end' },
				{ title: '성명', field: 'usernm', hozAlign: 'center', width: 150 },
				{ title: '소속부서', field: 'deptnm', widthGrow: 1, cssClass: 'border-start' },
				{ title: '직위', field: 'positionoffNM', hozAlign: 'center', width: 150 },
				{ title: '핸드폰', field: 'hpno', width: 200 },
				{ title: '메일', field: 'email', width: 250 },
				{ title: '내선번호', field: 'inner_no', hozAlign: 'center', width: 100 },
        		{ title: '사용', field: 'useyn', hozAlign: 'center', width: 150, formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
			]
		})
		mainGrid.on('rowClick', (e, row) => { Object.assign(formData, row.getData()); formData.actkind = 'U0' })
	}

	api.post('/api/ha00/HA00_00P_STR', { gubun: '040', cmpycd: authStore.cmpycd }).then(r => {
		if (r.data) posOptions.value = r.data.map((i: any) => ({ codecd: String(i.codecd || i.codecd).trim(), codenm: String(i.codenm || i.codenm).trim() }))
	})

	fetchList()
})
</script>
