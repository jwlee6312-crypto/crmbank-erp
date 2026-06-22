<!--기본정보/조직도관리 [ERP 프리미엄 고밀도 표준 적용] -->
<template>
	<appalert :show="showalert" :error="showerror" :message="alertmessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom shadow-sm bg-white py-2 sticky-top">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-diagram-3-fill me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">조직도 관리 (haba060u)</span>
			</div>
			<div class="btn-group-erp pe-3">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="fetchlist">조회</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
			</div>
		</div>

		<!-- 💡 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-hidden d-flex flex-column gap-3 p-3 bg-light">

			<!-- 🔍 2. 검색 바 섹션 -->
			<div class="card border-0 shadow-sm flex-shrink-0">
				<div class="card-body p-2 bg-white rounded">
					<div class="d-flex align-items-center gap-3">
						<div class="input-group input-group-sm flex-nowrap" style="width: 420px;">
							<span class="input-group-text bg-light fw-bold px-3 border-0">부서명 검색</span>
							<input v-model="searchform.deptnm_s" class="form-control border-light-subtle" placeholder="부서명을 입력하고 엔터..." @keyup.enter="fetchlist" />
							<button class="btn btn-dark px-3" @click="fetchlist"><i class="bi bi-search"></i></button>
						</div>
					</div>
				</div>
			</div>

			<!-- 💡 3. 상세 입수정 영역 -->
			<div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
					<div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-secondary"></i>부서 상세 정보 관리</div>
					<div v-if="formdata.actkind === 'U0'" class="badge bg-warning text-dark px-2">수정 중</div>
					<div v-else class="badge bg-primary text-white px-2">신규 등록</div>
				</div>
				<div class="card-body p-0 bg-white border-bottom">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 100px;" /><col />
							<col style="width: 100px;" /><col />
							<col style="width: 100px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required">부서코드</th>
								<td><input v-model="formdata.deptcd" type="text" class="form-control fw-bold text-primary text-center" maxlength="5" :disabled="formdata.actkind === 'U0'"/></td>
								<th class="required">부서명칭</th>
								<td><input v-model="formdata.deptnm" type="text" class="form-control" /></td>
								<th>영문명칭</th>
								<td><input v-model="formdata.deptenm" type="text" class="form-control" /></td>
							</tr>
							<tr>
								<th>상위부서</th>
								<td>
									<div class="input-group input-group-sm flex-nowrap">
										<input v-model="formdata.updept" type="text" class="form-control bg-light text-center fw-bold" style="max-width: 60px;" readonly />
										<input v-model="formdata.updeptnm" type="text" class="form-control border-start-0" placeholder="상위부서 검색" @keyup.enter="opendeptpopup" />
										<button class="btn btn-outline-secondary px-2" @click="opendeptpopup"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th>사업장</th>
								<td>
									<select v-model="formdata.taxunit" class="form-select">
										<option value="">-- 선택 --</option>
										<option v-for="opt in saoptions" :key="opt.taxunit" :value="opt.taxunit">{{ opt.unitnm }}</option>
									</select>
								</td>
								<th>비용구분</th>
								<td>
									<select v-model="formdata.costgbn" class="form-select">
										<option value="">-- 선택 --</option>
										<option value="100">관리비용</option>
										<option value="110">제조비용</option>
									</select>
								</td>
							</tr>
							<tr>
								<th class="required">적용일자</th>
								<td><input v-model="formdata.deptopn" type="date" class="form-control" /></td>
								<th>유효일자</th>
								<td><input v-model="formdata.deptcld" type="date" class="form-control" /></td>
								<th>사용여부</th>
								<td class="bg-light-subtle text-center">
									<div class="form-check form-switch m-0 d-inline-block">
										<input v-model="formdata.useyn" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="useyn060">
										<label class="form-check-label ms-2 small fw-bold" for="useyn060">{{ formdata.useyn === 'Y' ? '사용 중' : '중지' }}</label>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- 📊 4. 하단 그리드 영역 -->
			<div class="card border-0 shadow-sm flex-grow-1 overflow-hidden">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
					<i class="bi bi-table me-2 text-secondary"></i>
					<span class="fw-bold small text-dark">부서 목록</span>
				</div>
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                    <div ref="maingridelement" class="tabulator-instance flex-grow-1"></div>
                </div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useAlerts } from '@/composables/useAlerts'
import { useFormReset } from '@/composables/useFormReset'
import appalert from '@/components/AppAlert.vue'

const authstore = useAuthStore()
const { showAlert: showalert, showError: showerror, alertMessage: alertmessage, vAlert: valert, vAlertError: valerterror } = useAlerts()
const { resetForm: resetform } = useFormReset()

const searchform = reactive({ deptnm_s: '' })
const formdata = reactive({
	actkind: 'S0', cmpycd: authstore.cmpycd, deptcd: '', deptnm: '', deptenm: '', deptsnm: '', updept: '', updeptnm: '',
	deptdum: 'N', taxunit: '', costgbn: '', deptemp: '', deptempnm: '',
	deptopn: new Date().toISOString().substring(0, 10), deptcld: '', dspord: '1', useyn: 'Y'
})

const saoptions = ref<any[]>([])
const maingridelement = ref<HTMLDivElement | null>(null)
let maingrid: Tabulator | null = null

const normalizekeys = (obj: any) => {
  const n: any = {}; if (!obj) return n;
  Object.keys(obj).forEach(k => n[k.toLowerCase()] = typeof obj[k] === 'string' ? obj[k].trim() : obj[k]);
  return n;
}

async function fetchcodes() {
	try {
		const ressa = await api.get('/api/ha00/ha00_00p_str', { params: { gubun: 'sa', cmpycd: authstore.cmpycd } })
		const normalize = (data: any[]) => (data || []).map(i => {
			const n = normalizekeys(i);
			return {
				taxunit: String(n.taxunit || n.code || '').trim(),
				unitnm: String(n.unitnm || n.cdnm || '').trim()
			}
		})
		saoptions.value = normalize(ressa.data)
	} catch (e) { console.error('코드 로드 실패') }
}

async function fetchlist() {
	try {
		const res = await api.post('/api/haba/haba_060u_str', {
			actkind: 'S0',
			cmpycd: authstore.cmpycd,
			deptnm: searchform.deptnm_s
		})
		const processed = (res.data || []).map((i: any) => normalizekeys(i));
		maingrid?.setData(processed)
		valert('조회되었습니다.')
	} catch (e) { valerterror('조회 실패') }
}

async function save() {
	if (!formdata.deptcd || !formdata.deptnm) return valerterror('부서코드와 부서명칭은 필수입니다.')
	if (!confirm('저장하시겠습니까?')) return
	try {
		const act = formdata.actkind === 'S0' ? 'A0' : 'U0';
		const res = await api.post('/api/haba/haba_060u_str', { ...formdata, actkind: act, updemp: authstore.userid })
		const resdata = normalizekeys(res.data?.[0]);
		if (resdata.result === 'N') valerterror(resdata.msg || '저장 실패')
		else { valert('저장이 완료되었습니다.'); fetchlist() }
	} catch (e) { valerterror('저장 실패') }
}

function initialize() {
	resetform(formdata); Object.assign(formdata, { actkind: 'S0', useyn: 'Y', cmpycd: authstore.cmpycd, deptopn: new Date().toISOString().substring(0, 10) });
}

function opendeptpopup() { /* 팝업 로직 */ }

onMounted(() => {
	if (maingridelement.value) {
		maingrid = new Tabulator(maingridelement.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			placeholder: '표시할 데이터가 없습니다.',
			columnDefaults: { headerSort: false, headerHozAlign: 'center', hozAlign: 'center', vertAlign: 'middle' },
			columns: [
				{ title: '부서코드', field: 'deptcd', width: 100, cssClass: 'fw-bold text-primary border-end' },
				{ title: '부서명칭', field: 'deptnm', minWidth: 200, widthGrow: 1, cssClass: 'fw-bold', hozAlign: 'left' },
				{ title: '상위부서', field: 'updeptnm', minWidth: 150, widthGrow: 1, hozAlign: 'left' },
				{ title: '적용일', field: 'deptopn', hozAlign: 'center', width: 120 },
				{ title: '사용', field: 'useyn', hozAlign: 'center', width: 80, formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
			]
		})
		maingrid.on('rowClick', (e, row) => {
			const d = normalizekeys(row.getData());
			Object.assign(formdata, d);
			formdata.actkind = 'U0'
		})
	}
	fetchcodes().then(() => fetchlist())
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
