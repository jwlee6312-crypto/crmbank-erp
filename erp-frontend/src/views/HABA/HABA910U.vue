<!--기본정보/개인정보관리 [ERP 프리미엄 고밀도 표준 적용] -->
<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="haba910u-wrapper d-flex flex-column h-100 bg-light p-0">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom shadow-sm bg-white py-1">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 13px;">
				<i class="bi bi-person-badge-fill me-2 text-primary"></i>
				기본정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">개인정보 관리 (HABA910U)</span>
			</div>
			<div class="btn-group-erp pe-2">
				<button class="btn-erp btn-init" @click="fetchInfo">초기화</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
			</div>
		</div>

		<!-- 💡 메인 컨텐츠 영역 (섹션 간 확실한 간격 확보: gap-3) -->
		<div class="flex-grow-1 overflow-hidden d-flex flex-column gap-3 p-3">

			<!-- 🔍 2. 상단 검색/정보 바 섹션 (패턴 통일) -->


			<!-- 💡 3. 상세 정보 수정 카드 (2열 고밀도 밸런스 레이아웃) -->
			<div class="card border-0 shadow-sm overflow-hidden flex-shrink-0" style="max-width: 1000px;">
				<div class="card-header bg-white py-2 px-3 border-bottom d-flex align-items-center">
					<i class="bi bi-pencil-square me-2 text-secondary"></i>
					<span class="fw-bold small text-dark">사용자 프로필 수정</span>
				</div>
				<div class="card-body p-3 bg-white">
					<table class="erp-table-full border">
						<colgroup>
							<col style="width: 130px;" /><col />
							<col style="width: 130px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th>아이디</th>
								<td>
									<input v-model="formData.USERID" type="text" class="form-control bg-light fw-bold text-primary text-center" readonly />
								</td>
								<th class="required">성    명</th>
								<td>
									<input v-model="formData.USERNM" type="text" class="form-control" placeholder="성함 입력" />
								</td>
							</tr>
							<tr>
								<th class="required">비밀번호</th>
								<td>
									<input v-model="formData.PW" type="password" class="form-control" placeholder="변경할 비밀번호" />
								</td>
								<th class="required">비밀번호 확인</th>
								<td>
									<input v-model="formData.PW_C" type="password" class="form-control" placeholder="비밀번호 재입력" />
								</td>
							</tr>
							<tr>
								<th>연락처(TEL)</th>
								<td>
									<input v-model="formData.TELNO" type="text" class="form-control text-center" />
								</td>
								<th>핸드폰(HP)</th>
								<td>
									<input v-model="formData.HPNO" type="text" class="form-control text-center text-primary fw-bold" />
								</td>
							</tr>
							<tr>
								<th>이메일 주소</th>
								<td colspan="3">
									<input v-model="formData.EMAIL" type="email" class="form-control" placeholder="example@domain.com" />
								</td>
							</tr>
							<tr>
								<th class="required">소속부서</th>
								<td>
									<div class="input-group input-group-sm flex-nowrap">
										<input v-model="formData.DEPTCD" type="text" class="form-control bg-light text-center" style="max-width: 70px;" readonly />
										<input v-model="formData.DEPTNM" type="text" class="form-control" readonly />
										<button class="btn btn-outline-secondary px-2" disabled><i class="bi bi-lock-fill"></i></button>
									</div>
								</td>
								<th>단가관리 권한</th>
								<td>
									<select v-model="formData.PRICEGBN" class="form-select">
										<option value="1">공급가의 단가</option>
										<option value="2">(공급가+부가세)의 단가</option>
									</select>
								</td>
							</tr>
							<tr>
								<th class="border-bottom-0">사용여부</th>
								<td colspan="3" class="border-bottom-0">
									<div class="form-check form-switch m-0 d-flex align-items-center h-100">
										<input v-model="formData.USEYN" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" disabled>
										<label class="form-check-label ms-2 small fw-bold text-secondary">정상 사용 중 (관리자 설정)</label>
									</div>
								</td>
							</tr>
						</tbody>
					</table>

					<!-- 안내 영역 -->
					<div class="mt-4 p-3 rounded bg-light border-start border-4 border-primary">
						<h6 class="fw-bold small mb-2"><i class="bi bi-info-circle-fill me-1 text-primary"></i> 정보 수정 안내</h6>
						<ul class="mb-0 small text-secondary list-unstyled">
							<li class="mb-1">· 비밀번호는 영문, 숫자 조합으로 4자 이상 설정을 권장합니다.</li>
							<li class="mb-1">· 부서 정보 및 사용 여부는 시스템 관리자만 변경할 수 있습니다.</li>
							<li>· 변경된 정보는 <strong>다음 로그인 시점</strong>부터 모든 시스템에 적용됩니다.</li>
						</ul>
					</div>
				</div>
			</div>
			<div class="flex-grow-1"></div> <!-- 하단 여백용 -->
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const formData = reactive({
	ACTKIND: 'S1',
	USERID: authStore.USERID,
	USERNM: authStore.USERNM,
	PW: '',
	PW_C: '',
	TELNO: '',
	HPNO: '',
	EMAIL: '',
	DEPTCD: '',
	DEPTNM: '',
	PRICEGBN: '1',
	USEYN: 'Y'
})

async function fetchInfo() {
	try {
		const res = await api.post('/api/haba/HABA_910U_STR', {
			ACTKIND: 'S1',
			CMPYCD: authStore.CMPYCD,
			USERID: authStore.USERID
		})
		if (res.data && res.data.length > 0) {
			Object.assign(formData, res.data[0])
			formData.PW_C = formData.PW
		}
	} catch (e) { vAlertError('개인 정보 로드 실패') }
}

async function save() {
	if (!formData.USERNM) return vAlertError('성명을 입력하세요.')
	if (formData.PW !== formData.PW_C) return vAlertError('비밀번호 확인이 일치하지 않습니다.')

	try {
		await api.post('/api/haba/HABA_910U_STR', {
			...formData,
			ACTKIND: 'U1',
			CMPYCD: authStore.CMPYCD,
			USERID: authStore.USERID
		})
		vAlert('성공적으로 저장되었습니다.')
		fetchInfo()
	} catch (e) { vAlertError('저장 실패') }
}

onMounted(() => {
	fetchInfo()
})
</script>

<style scoped>
.haba910u-wrapper { height: 100%; overflow: hidden; }

/* 💎 표준 버튼 팔레트 */
.btn-group-erp { display: flex; gap: 4px; }
.btn-erp { padding: 4px 18px; border-radius: 4px; font-size: 13px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-init { background-color: #fff; color: #6c757d; border: 1px solid #6c757d; }
.btn-init:hover { background-color: #f8f9fa; }
.btn-save { background-color: #005a9f; color: #fff; border: none; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.btn-save:hover { background-color: #004a85; }

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

/* 💎 입력창 스타일 */
.form-control, .form-select { border-radius: 4px; border: 1px solid #cbd5e1; font-size: 13px; height: 32px; width: 100%; font-weight: 500; }
.form-control:focus, .form-select:focus { border-color: #005a9f; box-shadow: 0 0 0 2px rgba(0, 90, 159, 0.1); }
.bg-light-subtle { background-color: #f1f5f9 !important; }
</style>
