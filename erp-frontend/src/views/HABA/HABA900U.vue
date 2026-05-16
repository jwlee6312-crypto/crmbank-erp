<!--기본정보/회사정보관리 [ERP 프리미엄 고밀도 표준 적용] -->
<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="haba900u-wrapper d-flex flex-column h-100 bg-light p-0">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom shadow-sm bg-white py-2 sticky-top">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-building me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">회사정보관리 (HABA900U)</span>
			</div>
			<div class="btn-group-erp pe-3">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchCompanyInfo">조회</button>
				<button class="btn-erp btn-save" @click="handleSave">저장</button>
			</div>
		</div>

		<!-- 💡 메인 컨텐츠 영역 (섹션 간 간격 확보: gap-3) -->
		<div class="flex-grow-1 overflow-auto d-flex flex-column gap-3 p-3">

			<!-- 💡 2. 회사 상세 정보 카드 (3열 고밀도 밸런스 레이아웃) -->
			<div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
				<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center">
					<i class="bi bi-pencil-square me-2 text-secondary"></i>
					<span class="fw-bold small text-dark">회사 기본 정보 상세 관리</span>
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
								<th>회사코드</th>
								<td><input v-model="formData.CMPYCD" type="text" class="form-control bg-light fw-bold text-primary text-center" readonly /></td>
								<th class="required">회사명(상호)</th>
								<td><input v-model="formData.LTDNM" type="text" class="form-control" placeholder="상호 입력" /></td>
								<th>영문상호</th>
								<td><input v-model="formData.LTDENM" type="text" class="form-control" /></td>
							</tr>
							<tr>
								<th class="required">사업자번호</th>
								<td><input v-model="formData.SAUPNO" type="text" class="form-control text-center" maxlength="10" /></td>
								<th>대표자명</th>
								<td><input v-model="formData.BOSSNM" type="text" class="form-control" /></td>
								<th class="required">법인번호</th>
								<td><input v-model="formData.LEGALNO" type="text" class="form-control text-center" maxlength="13" /></td>
							</tr>
							<tr>
								<th>업태</th>
								<td><input v-model="formData.UPTAE" type="text" class="form-control" /></td>
								<th>종목</th>
								<td><input v-model="formData.UPJONG" type="text" class="form-control" /></td>
								<th>연락처</th>
								<td><input v-model="formData.TELNO" type="text" class="form-control text-center" /></td>
							</tr>
							<tr>
								<th class="required">본사주소</th>
								<td colspan="3">
									<div class="d-flex gap-1 flex-nowrap">
										<input v-model="formData.POSTNO" type="text" class="form-control bg-light text-center" style="max-width: 80px; flex-shrink: 0;" readonly />
										<button class="btn btn-sm btn-outline-secondary px-2 flex-nowrap" style="flex-shrink: 0;" @click="execDaumPostcode"><i class="bi bi-search me-1"></i>우편번호</button>
										<input v-model="formData.ADDRESS" type="text" class="form-control flex-grow-1" placeholder="기본 주소 입력" />
									</div>
								</td>
								<th>도메인</th>
								<td><input v-model="formData.DOMAIN" type="text" class="form-control" /></td>
							</tr>
							<tr>
								<th class="required">결산월</th>
								<td>
									<select v-model="formData.CLSMM" class="form-select">
										<option v-for="m in 12" :key="m" :value="String(m).padStart(2, '0')">{{ m }}월</option>
									</select>
								</td>
								<th>설립일자</th>
								<td><input v-model="formData.FONDYMD" type="date" class="form-control" /></td>
								<th>사용여부</th>
								<td class="bg-light-subtle">
									<div class="form-check form-switch m-0 d-flex align-items-center justify-content-center h-100">
										<input v-model="formData.USEYN" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="useYnSwitch900">
										<label class="form-check-label ms-2 small fw-bold" for="useYnSwitch900">{{ formData.USEYN === 'Y' ? '사용 중' : '중지' }}</label>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<div class="flex-grow-1"></div> <!-- 하단 공간 확보용 -->
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import AppAlert from '@/components/AppAlert.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()

const formData = reactive({
	ACTKIND: 'S0',
	CMPYCD: authStore.CMPYCD,
	LTDNM: '', LTDENM: '', SAUPNO: '', BOSSNM: '', LEGALNO: '',
	POSTNO: '', ADDRESS: '', UPTAE: '', UPJONG: '', TELNO: '',
	FONDYMD: '', CLSMM: '12', DOMAIN: '', USEYN: 'Y'
})

async function fetchCompanyInfo() {
	try {
		// 💡 신규 API 경로 및 프로시저 호출 (/api/haba/HABA_900U_STR)
		const res = await api.post('/api/haba/HABA_900U_STR', {
			ACTKIND: 'S0',
			CMPYCD: authStore.CMPYCD
		})
		if (res.data && res.data.length > 0) {
			Object.assign(formData, res.data[0])
			if (formData.FONDYMD && formData.FONDYMD.length === 8) {
				formData.FONDYMD = `${formData.FONDYMD.substring(0, 4)}-${formData.FONDYMD.substring(4, 6)}-${formData.FONDYMD.substring(6, 8)}`
			}
		}
	} catch (e) { vAlertError('회사 정보 로드 실패') }
}

async function handleSave() {
	if (!formData.LTDNM || !formData.SAUPNO) return vAlertError('상호와 사업자번호는 필수입니다.')
	try {
		const param = {
			...formData,
			ACTKIND: 'U0',
			SAUPNO: formData.SAUPNO.replace(/-/g, ''),
			LEGALNO: formData.LEGALNO.replace(/-/g, ''),
			FONDYMD: formData.FONDYMD ? formData.FONDYMD.replace(/-/g, '') : '',
			USERID: authStore.USERID
		}
		await api.post('/api/haba/HABA_900U_STR', param)
		vAlert('저장되었습니다.')
		fetchCompanyInfo()
	} catch (e) { vAlertError('저장 실패') }
}

function initialize() {
	resetForm(formData)
	formData.ACTKIND = 'S0'; formData.CMPYCD = authStore.CMPYCD; formData.CLSMM = '12';
}

const execDaumPostcode = () => {
	new (window as any).daum.Postcode({
		oncomplete: (data: any) => {
			formData.POSTNO = data.zonecode
			formData.ADDRESS = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress
		}
	}).open()
}

onMounted(() => {
	if (!(window as any).daum) {
		const script = document.createElement('script')
		script.src = '//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js'
		document.head.appendChild(script)
	}
	fetchCompanyInfo()
})
</script>

<style scoped>
.haba900u-wrapper { height: 100%; overflow: hidden; }

/* 💎 표준 버튼 시스템 */
.btn-group-erp { display: flex; gap: 4px; }
.btn-erp { padding: 4px 18px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff; color: #6c757d; border: 1px solid #6c757d; }
.btn-init:hover { background-color: #f8f9fa; }
.btn-search { background-color: #2d3748; color: #fff; border: none; }
.btn-save { background-color: #005a9f; color: #fff; border: none; }

/* 💎 프리미엄 테이블 스타일 (줄바꿈 방지) */
.erp-table-full { table-layout: fixed; border-collapse: collapse !important; width: 100%; border-style: hidden; }
.erp-table-full th {
	background-color: #f8fafc; border: 1px solid #e2e8f0 !important;
	text-align: center; font-weight: 800; font-size: 12.5px; color: #475569;
	vertical-align: middle; padding: 10px 15px !important;
	white-space: nowrap; overflow: hidden;
}
.erp-table-full td { vertical-align: middle; padding: 8px 12px !important; border: 1px solid #e2e8f0 !important; background-color: #fff; }
.required::after { content: ' *'; color: #ef4444; }

.form-control, .form-select { border-radius: 4px; border: 1px solid #cbd5e1; font-size: 13px; height: 32px; width: 100%; font-weight: 500; }
.form-control:focus, .form-select:focus { border-color: #005a9f; box-shadow: 0 0 0 2px rgba(0, 90, 159, 0.1); }
</style>
