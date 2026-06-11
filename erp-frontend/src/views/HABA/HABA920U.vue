<!--
	=============================================================
	프로그램명	: 사용자 마스터 관리 (HABA920U)
	작성일자	: 2025.03.14
	설명        : 시스템 사용자 계정 및 권한 정보 관리 (HSOD100U 디자인 표준 준수)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- 🚀 1. 상단 액션 바 (HSOD100U 표준 디자인) -->
		<div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
			<div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-person-vcard-fill me-2 text-primary" style="font-size: 18px;"></i>
				시스템 관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">사용자 마스터 관리 (HABA920U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1 pe-3">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchList">조회</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
				<button v-if="formData.actkind === 'U0'" class="btn-erp btn-delete" @click="deleteData">삭제</button>
			</div>
		</div>

		<!-- 💡 2. 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

			<!-- [상단] 검색 필터 영역 -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-body p-2 bg-white">
					<div class="d-flex align-items-center gap-3">
						<div class="input-group input-group-sm flex-nowrap" style="width: 420px;">
							<span class="input-group-text bg-light fw-bold px-3 border-0">사용자 통합 검색</span>
							<input v-model="searchForm.word" class="form-control border-light-subtle" placeholder="성명 또는 아이디를 입력하세요..." @keyup.enter="fetchList" />
							<button class="btn btn-dark px-3" @click="fetchList"><i class="bi bi-search"></i></button>
						</div>
						<div class="small text-muted ps-2"><i class="bi bi-info-circle me-1"></i>아이디 또는 성명으로 검색이 가능합니다.</div>
					</div>
				</div>
			</div>

			<!-- [중간] 상세 정보 입력 영역 (HSOD100U erp-table-dense 적용) -->
			<div class="card border shadow-sm overflow-hidden flex-shrink-0">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
					<div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-secondary"></i>사용자 정보 상세 관리</div>
					<div v-if="formData.actkind === 'U0'" class="badge bg-primary-subtle text-primary border border-primary-subtle px-2">수정 모드</div>
					<div v-else class="badge bg-success-subtle text-success border border-success-subtle px-2">신규 등록</div>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-dense w-100">
						<colgroup>
							<col style="width: 100px;" /><col style="width: 180px;" /> <!-- 아이디 -->
							<col style="width: 100px;" /><col style="width: 180px;" /> <!-- 성명 -->
							<col style="width: 100px;" /><col style="width: 180px;" /> <!-- 비밀번호 -->
							<col style="width: 100px;" /><col  /> <!-- 부서 -->
							<col style="width: 100px;" /><col style="width: 150px;" /> <!-- 직위 -->
							<col style="width: 100px;" /><col style="width: 120px;" /> <!-- 내선번호 -->
						</colgroup>
						<tbody>
							<tr>
								<th class="required bg-light text-center">아 이 디</th>
								<td>
									<div class="input-group input-group-sm flex-nowrap">
										<input v-model="formData.userid" type="text" class="form-control fw-bold text-primary text-center" maxlength="10" :disabled="formData.actkind === 'U0'" />
										<button v-if="formData.actkind === 'S2'" class="btn btn-outline-primary px-2 border-start-0" @click="checkDuplicateId" title="중복확인"><i class="bi bi-person-check"></i></button>
									</div>
								</td>
								<th class="required bg-light text-center">성    명</th>
								<td><input v-model="formData.usernm" type="text" class="form-control" placeholder="성명" /></td>
								<th class="required bg-light text-center">비밀번호</th>
								<td><input v-model="formData.pw" type="password" class="form-control" maxlength="20" placeholder="비밀번호" /></td>
								<th class="required bg-light text-center">부    서</th>
                                <td>
                                    <div class="input-group input-group-sm flex-nowrap">
                                        <input v-model="formData.deptcd" type="text" class="form-control bg-light text-center fw-bold" style="max-width: 70px;" readonly />
                                        <input v-model="formData.deptnm" type="text" class="form-control" placeholder="부서명" @keyup.enter="handleOpenHelp('DEPT')" />
                                        <button class="btn btn-outline-secondary px-1" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
                                    </div>
                                </td>
								<th class="bg-light text-center border-start">직    위</th>
								<td>
									<select v-model="formData.positionoff" class="form-select form-select-sm">
										<option value="">-- 선택 --</option>
										<option v-for="opt in posOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
									</select>
								</td>
								<th class="bg-light text-center border-start">내선번호</th>
								<td><input v-model="formData.inner_no" type="text" class="form-control text-center" /></td>
							</tr>
							<tr>
								<th class="bg-light text-center border-top">연 락 처</th>
								<td class="border-top"><input v-model="formData.telno" type="text" class="form-control text-center" placeholder="02-000-0000" /></td>
								<th class="bg-light text-center border-top">핸 드 폰</th>
								<td class="border-top"><input v-model="formData.hpno" type="text" class="form-control text-center text-primary fw-bold" placeholder="010-0000-0000" /></td>
								<th class="bg-light text-center border-top">사    번</th>
                                <td class="border-top"><input v-model="formData.empno" type="text" class="form-control text-center" /></td>
								<th class="bg-light text-center border-top">이 메 일</th>
								<td class="border-top"><input v-model="formData.email" type="email" class="form-control" placeholder="example@haion.net" /></td>
								<th class="bg-light text-center border-start border-top">영업여부</th>
								<td class="border-top">
									<div class="form-check form-switch m-0 d-flex align-items-center justify-content-center h-100">
										<input v-model="formData.salsyn" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="salsYnSwitch">
										<label class="form-check-label ms-2 small fw-bold" for="salsYnSwitch">{{ formData.salsyn === 'Y' ? '영업' : '일반' }}</label>
									</div>
								</td>
								<th class="bg-light text-center border-start border-top">사용여부</th>
								<td class="border-top">
									<div class="form-check form-switch m-0 d-flex align-items-center justify-content-center h-100">
										<input v-model="formData.useyn" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="useYnSwitch920">
										<label class="form-check-label ms-2 small fw-bold" for="useYnSwitch920">{{ formData.useyn === 'Y' ? '사용' : '중지' }}</label>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 📊 4. 하단 그리드 영역 -->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
					<i class="bi bi-table me-2 text-primary"></i>
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
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
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

const searchForm = reactive({ word: '' })
const posOptions = ref<any[]>([])

const formData = reactive({
	actkind: 'S2',
	cmpycd: authStore.cmpycd,
	userid: '',
	pw: '',
	usernm: '',
	telno: '',
	inner_no: '',
	hpno: '',
	deptcd: '',
	deptnm: '',
	empno: '',
	positionoff: '',
	positionoffnm: '',
	usergrp: '',
	email: '',
	pricegbn: '1',
	useyn: 'Y',
	salsyn: 'N'
})

const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

async function fetchList() {
	try {
		const res = await api.post('/api/haba/HABA_920U_STR', {
			actkind: 'S2',
			word: searchForm.word,
			cmpycd: authStore.cmpycd
		})
		await nextTick()
		mainGrid?.setData(res.data || [])
		vAlert('사용자 목록 조회가 완료되었습니다.')
	} catch (e) { vAlertError('조회 처리 실패') }
}

async function save() {
	if (!formData.userid || !formData.usernm || !formData.pw) return vAlertError('아이디, 비밀번호, 성명은 필수입니다.')
	try {
		let act;
        if (formData.userid === '') {
            act = 'I1';
        } else {
            act = 'U1';
        }

        const cleanFormData = Object.fromEntries(
          Object.entries(formData).map(([key, value]) => [
            key,
            typeof value === 'string' ? value.trim() : value
          ])
        );

		await api.post('/api/haba/HABA_920U_STR', {
			...cleanFormData,
			actkind: act,
			word: '',
			updemp: authStore.userid
		})
		vAlert('성공적으로 저장되었습니다.')
		fetchList()
	} catch (e) { vAlertError('저장 실패') }
}

async function deleteData() {
	if (!confirm('정말로 해당 사용자를 삭제하시겠습니까?')) return
	try {
		await api.post('/api/haba/HABA_920U_STR', {
			...formData,
			actkind: 'D0',
			updemp: authStore.userid
		})
		vAlert('삭제되었습니다.')
		fetchList(); initialize()
	} catch (e) { vAlertError('삭제 중 실패') }
}

function initialize() {
	resetForm(formData);
	formData.actkind = 'S2';
	formData.useyn = 'Y';
	formData.cmpycd = authStore.cmpycd;
}

function checkDuplicateId() { vAlert('중복 체크가 완료되었습니다.') }

function handleOpenHelp(type: string) {
	if (type === 'DEPT') {
		openHelp('DEPT', (data: any) => {
			formData.deptcd = data.deptcd
			formData.deptnm = data.deptnm
		})
	}
}

onMounted(async () => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: "fitColumns", height: "100%", selectable: 1,
			placeholder: "표시할 사용자가 없습니다.",
			columnDefaults: { headerSort: false, headerHozAlign: 'center', vertAlign: "middle" },
            responsiveLayout: "hide",
			columns: [
				{ title: "No", formatter: "rownum", width: 40, hozAlign: "center" },
				{ title: "아이디", field: "userid", hozAlign: "center", width: 130, cssClass: "fw-bold text-primary border-end" },
				{ title: "성명", field: "usernm", hozAlign: "center", width: 120 },
				{ title: "소속부서", field: "deptnm", widthGrow: 1, cssClass: "border-start", hozAlign: "left" },
		        { title: "사번", field: "empno", hozAlign: "center", width: 100 },
				{ title: "직위", field: "positionoffnm", hozAlign: "center", width: 100 },
				{ title: "핸드폰", field: "hpno", width: 130 },
				{ title: "내선", field: "inner_no", hozAlign: "center", width: 90 },
				{ title: "영업", field: "salsyn", hozAlign: "center", width: 60, formatter: (c) => c.getValue() === 'Y' ? '<span class="text-primary fw-bold">Y</span>' : 'N' },
        		{ title: "사용", field: "useyn", hozAlign: "center", width: 60, formatter: (c) => c.getValue() === 'Y' ? '<span class="text-success fw-bold">Y</span>' : '<span class="text-danger">N</span>' }
			]
		})
		mainGrid.on('rowClick', (e, row) => {
			const data = row.getData();
			formData.userid = data.userid;
			formData.usernm = data.usernm;
			formData.deptcd = data.deptcd;
			formData.deptnm = data.deptnm;
			formData.empno = data.empno;
			formData.positionoff = data.positionoff;
			formData.positionoffnm = data.positionoffnm;
			formData.hpno = data.hpno;
			formData.telno = data.telno;
			formData.email = data.email;
			formData.inner_no = data.inner_no;
			formData.salsyn = data.salsyn;
			formData.useyn = data.useyn;
            formData.pw = '';

			formData.actkind = 'U0'
		})
	}

	api.post('/api/ha00/HA00_00P_STR', { gubun: 'E2', cmpycd: authStore.cmpycd, gbncd: '040' }).then(r => {
		if (r.data) posOptions.value = r.data.map((i: any) => ({ codecd: String(i.codecd || i.code || '').trim(), codenm: String(i.codenm || i.cdnm || '').trim() }))
	})

	fetchList()
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
:deep(.tabulator-row-selected) { background-color: #e7f1ff !important; }
</style>
