<!--관리정보/기본정보/회사정보관리(시스템마스터) [ERP 프리미엄 고밀도 표준 - 3열 배치] -->
<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 shadow-sm sticky-top">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-gear-fill me-2 text-primary" style="font-size: 18px;"></i>
				시스템 관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">회사정보관리 (HAAA100U)</span>
			</div>
			<div class="btn-group-erp pe-3">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="search">조회</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
			</div>
		</div>

		<!-- 💡 메인 컨텐츠 영역 (섹션 간 확실한 간격 확보: gap-3) -->
		<div class="flex-grow-1 overflow-auto d-flex flex-column gap-3 p-3">

			<!-- 🔍 2. 상단 검색 바 섹션 (통합 표준 패턴) -->
			<div class="card border-0 shadow-sm flex-shrink-0">
				<div class="card-body p-2 bg-white rounded">
					<div class="d-flex align-items-center gap-3">
						<div class="input-group input-group-sm flex-nowrap" style="width: 420px;">
							<span class="input-group-text bg-light fw-bold px-3 border-0">회사명 검색</span>
							<input v-model="searchForm.sch_cmpynm" class="form-control border-light-subtle" placeholder="회사명 또는 상호를 입력하세요..." @keyup.enter="search" />
							<button class="btn btn-dark px-3" @click="search"><i class="bi bi-search"></i></button>
						</div>
						<div class="small text-muted border-start ps-3"><i class="bi bi-info-circle me-1"></i> 목록에서 회사를 선택하면 상세 마스터 정보를 수정할 수 있습니다.</div>
					</div>
				</div>
			</div>

			<!-- 💡 3. 상세 입수정 영역 (3열 밸런스 레이아웃) -->
			<div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
					<div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-secondary"></i>회사 마스터 상세 정보</div>
					<div v-if="formData.actkind === 'U0'" class="badge bg-primary text-white px-2">수정 중</div>
					<div v-else class="badge bg-success text-white px-2">신규 등록</div>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 110px;" /><col />
							<col style="width: 110px;" /><col />
							<col style="width: 110px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required text-center">회사코드</th>
								<td><input v-model="formData.cmpycd" type="text" class="form-control fw-bold text-primary text-center" maxlength="10" :disabled="formData.actkind === 'U0'" /></td>
								<th class="required">회사명(상호)</th>
								<td><input v-model="formData.CMPYNM" type="text" class="form-control" /></td>
								<th>영문상호</th>
								<td><input v-model="formData.CMPYENM" type="text" class="form-control" /></td>
							</tr>
							<tr>
								<th class="required">사업자번호</th>
								<td><input v-model="formData.saupno" type="text" class="form-control text-center" maxlength="12" /></td>
								<th>대표자명</th>
								<td><input v-model="formData.bossnm" type="text" class="form-control" /></td>
								<th>법인번호</th>
								<td><input v-model="formData.legalno" type="text" class="form-control text-center" maxlength="14" /></td>
							</tr>
							<tr>
								<th>업    태</th>
								<td><input v-model="formData.uptae" type="text" class="form-control" /></td>
								<th>종    목</th>
								<td><input v-model="formData.upjong" type="text" class="form-control" /></td>
								<th>대표전화</th>
								<td><input v-model="formData.telno" type="text" class="form-control text-center" /></td>
							</tr>
							<tr>
								<th class="required">본사주소</th>
								<td colspan="3">
									<div class="d-flex gap-1 flex-nowrap">
										<input v-model="formData.postno" type="text" class="form-control bg-light text-center" style="max-width: 80px; flex-shrink: 0;" readonly />
										<button class="btn btn-sm btn-outline-secondary px-2 flex-nowrap" style="flex-shrink: 0;" @click="execDaumPostcode"><i class="bi bi-search me-1"></i>우편번호</button>
										<input v-model="formData.address" type="text" class="form-control flex-grow-1" />
									</div>
								</td>
								<th>도메인/메일</th>
								<td><input v-model="formData.domain" type="text" class="form-control" /></td>
							</tr>
							<tr>
								<th>결산월</th>
								<td>
									<select v-model="formData.clsmm" class="form-select">
										<option v-for="m in 12" :key="m" :value="String(m).padStart(2, '0')">{{ m }}월</option>
									</select>
								</td>
								<th>설립일자</th>
								<td><input v-model="formData.fondymd" type="date" class="form-control" /></td>
								<th>사용여부</th>
								<td class="bg-light-subtle">
									<div class="form-check form-switch m-0 d-flex align-items-center justify-content-center h-100">
										<input v-model="formData.useyn" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="useYn100">
										<label class="form-check-label ms-2 small fw-bold" for="useYn100">{{ formData.useyn === 'Y' ? '정상' : '중지' }}</label>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 📊 4. 하단 그리드 영역 (전체 폭 복구 & 자동 높이 확보) -->
			<div class="card border-0 shadow-sm flex-grow-1 overflow-hidden">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
					<i class="bi bi-table me-2 text-secondary"></i>
					<span class="fw-bold small text-dark">등록 회사 목록</span>
				</div>
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                    <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
                </div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const searchForm = reactive({ sch_cmpynm: '' })
const formData = reactive({
	actkind: 'S0',
	cmpycd: '', CMPYNM: '', CMPYENM: '', saupno: '', bossnm: '', legalno: '',
	postno: '', address: '', uptae: '', upjong: '', telno: '', clsmm: '12',
	fondymd: '', domain: '', useyn: 'Y', userid: authStore.user_id
})

const mainGridRef = ref<HTMLDivElement | null>(null)
let mainGrid: Tabulator | null = null

async function search() {
	try {
		const res = await api.post('/api/haba/HABA_900U_STR', {
			actkind: 'S1',
			cmpycd: '',
			ltdnm: searchForm.sch_cmpynm
		})
		mainGrid?.setData(res.data || [])
		vAlert('회사 정보 조회가 완료되었습니다.')
	} catch (e) { vAlertError('데이터 로드 실패') }
}

async function save() {
	if (!formData.cmpycd || !formData.CMPYNM) return vAlertError('회사코드와 상호는 필수 입력사항입니다.')
	try {
		const act = formData.actkind === 'S0' ? 'A0' : 'U0';
		const param = {
			...formData,
			actkind: act,
			fondymd: formData.fondymd ? formData.fondymd.replace(/-/g, '') : '',
			userid: authStore.user_id
		}
		await api.post('/api/haba/HABA_900U_STR', param)
		vAlert('성공적으로 저장되었습니다.')
		search()
	} catch (e) { vAlertError('저장 실패') }
}

function initialize() {
	resetForm(formData); formData.actkind = 'S0'; formData.useyn = 'Y'; formData.clsmm = '12';
}

const execDaumPostcode = () => {
	new (window as any).daum.Postcode({
		oncomplete: (data: any) => {
			formData.postno = data.zonecode
			formData.address = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress
		}
	}).open()
}

onMounted(async () => {
	if (!(window as any).daum) {
		const script = document.createElement('script')
		script.src = '//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js'
		document.head.appendChild(script)
	}

	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			placeholder: '표시할 회사 정보가 없습니다.',
			columnDefaults: { headerSort: false, headerHozAlign: 'center' },
			columns: [
				{ title: '코드', field: 'cmpycd', hozAlign: 'center', width: 100, cssClass: 'fw-bold text-primary border-end' },
				{ title: '회사명(상호)', field: 'CMPYNM', minWidth: 200, widthGrow: 1, cssClass: 'fw-bold' },
				{ title: '사업자번호', field: 'saupno', hozAlign: 'center', width: 130 },
				{ title: '대표자', field: 'bossnm', hozAlign: 'center', width: 110 },
				{ title: '연락처', field: 'telno', width: 150 },
				{ title: '주소', field: 'address', minWidth: 300, widthGrow: 1 }
			]
		})
		mainGrid.on('rowClick', (e, row) => {
			const data = row.getData(); Object.assign(formData, data); formData.actkind = 'U0'
			if (formData.fondymd && formData.fondymd.length === 8) {
				formData.fondymd = `${formData.fondymd.substring(0, 4)}-${formData.fondymd.substring(4, 6)}-${formData.fondymd.substring(6, 8)}`
			}
		})
	}
	search()
})
</script>
