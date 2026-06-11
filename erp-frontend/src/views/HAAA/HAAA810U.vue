<!--관리정보/시스템관리/분류코드관리 [ERP 프리미엄 고밀도 표준 - 1열 배치] -->
<template>
	<appalert :show="showalert" :error="showerror" :message="alertmessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 shadow-sm sticky-top">
			<div class="fw-bold ps-3 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-folder-fill me-2 text-primary" style="font-size: 18px;"></i>
				시스템 관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">분류코드 관리 (haaa810u)</span>
			</div>
			<div class="btn-group-erp pe-3">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="search">조회</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
				<button v-if="formdata.actkind === 'U0'" class="btn-erp btn-delete" @click="deletedata">삭제</button>
			</div>
		</div>

		<!-- 💡 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-hidden d-flex flex-column gap-3 p-3 bg-light">

			<!-- 🔍 2. 검색 바 섹션 -->
			<div class="card border-0 shadow-sm flex-shrink-0">
				<div class="card-body p-2 bg-white rounded">
					<div class="d-flex align-items-center gap-3">
						<div class="input-group input-group-sm flex-nowrap" style="width: 400px;">
							<span class="input-group-text bg-light fw-bold px-3 border-0">업무(대분류) 선택</span>
							<select v-model="searchform.upmucd" class="form-select border-light-subtle" @change="search">
								<option v-for="opt in upmuoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
							</select>
						</div>
					</div>
				</div>
			</div>

			<!-- 💡 3. 상세 입수정 영역 -->
			<div class="card border-0 shadow-sm overflow-hidden flex-shrink-0">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
					<div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-secondary"></i>분류 상세 정보</div>
					<div v-if="formdata.actkind === 'U0'" class="badge bg-warning text-dark px-2">수정 중</div>
					<div v-else class="badge bg-primary text-white px-2">신규 등록</div>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 80px;" /><col />
							<col style="width: 80px;" /><col />
							<col style="width: 80px;" /><col />
							<col style="width: 80px;" /><col />
							<col style="width: 80px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required">업    무</th>
								<td>
									<select v-model="formdata.upmucd" class="form-select bg-light" disabled>
										<option v-for="opt in upmuoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
									</select>
								</td>
								<th class="required">분류코드</th>
								<td><input v-model="formdata.grpcd" type="text" class="form-control fw-bold text-primary text-center" maxlength="3" :disabled="formdata.actkind === 'U0'" /></td>
								<th class="required">분 류 명</th>
								<td><input v-model="formdata.grpnm" type="text" class="form-control" placeholder="분류명칭" /></td>
								<th class="required">정렬순서</th>
								<td><input v-model="formdata.dspord" type="number" class="form-control text-end" /></td>
								<th>사용여부</th>
								<td>
									<div class="form-check form-switch m-0 d-flex align-items-center justify-content-center h-100">
										<input v-model="formdata.useyn" class="form-check-input mt-0" type="checkbox" true-value="y" false-value="n" id="useyn810">
										<label class="form-check-label ms-2 small fw-bold" for="useyn810">{{ formdata.useyn === 'Y' ? '사용' : '중지' }}</label>
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
					<span class="fw-bold small text-dark">메뉴 그룹 목록</span>
				</div>
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                    <div ref="maingridelement" class="tabulator-instance flex-grow-1"></div>
                </div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import appalert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'

const authstore = useAuthStore()
const { showAlert: showalert, showError: showerror, alertMessage: alertmessage, vAlert: valert, vAlertError: valerterror } = useAlerts()
const { resetForm: resetform } = useFormReset()

const searchform = reactive({ upmucd: '' })
const formdata = reactive({
	actkind: 'S0', upmucd: '', grpcd: '', grpnm: '', dspord: '1', useyn: 'Y', cmpycd: authstore.cmpycd, userid: authstore.userid
})

const upmuoptions = ref<any[]>([])
const maingridelement = ref<HTMLDivElement | null>(null)
let maingrid: Tabulator | null = null

const normalizekeys = (obj: any) => {
  const n: any = {};
  if (!obj) return n;
  Object.keys(obj).forEach(k => n[k.toLowerCase()] = typeof obj[k] === 'string' ? obj[k].trim() : obj[k]);
  return n;
}

async function fetchupmu() {
	try {
		const res = await api.post('/api/ha00/ha00_00p_str', { gubun: 'e0', gbncd: '320', cmpycd: authstore.cmpycd })
		if (res.data) {
			const processed = (res.data || []).map((i: any) => normalizekeys(i));
			upmuoptions.value = processed.map((n: any) => ({ codecd: n.codecd || n.code, codenm: n.codenm || n.cdnm }))
			if (upmuoptions.value.length > 0) {
				searchform.upmucd = upmuoptions.value[0].codecd;
				formdata.upmucd = upmuoptions.value[0].codecd;
			}
		}
	} catch (e) { console.error('업무코드 로드 실패') }
}

async function search() {
	if (!searchform.upmucd) return
	try {
		const res = await api.post('/api/haaa/haaa_810u_str', { actkind: 'S0', upmucd: searchform.upmucd, cmpycd: authstore.cmpycd })
		const processed = (res.data || []).map((i: any) => normalizekeys(i));
		maingrid?.setData(processed)
		valert('조회되었습니다.')
	} catch (e) { valerterror('조회 실패') }
}

async function save() {
	if (!formdata.grpcd || !formdata.grpnm) return valerterror('필수값 확인')
	if (!confirm('저장하시겠습니까?')) return
	try {
		const act = formdata.actkind === 'S0' ? 'A0' : 'U0';
		const res = await api.post('/api/haaa/haaa_810u_str', { ...formdata, actkind: act })
		const resdata = normalizekeys(res.data?.[0]);
		if (resdata.result === 'N') return valerterror(resdata.msg || '저장 실패')

		valert('저장되었습니다.'); search()
	} catch (e) { valerterror('저장 실패') }
}

async function deletedata() {
	if (!confirm('삭제하시겠습니까?')) return
	try {
		await api.post('/api/haaa/haaa_810u_str', { ...formdata, actkind: 'D0' })
		valert('삭제되었습니다.'); search(); initialize()
	} catch (e) { valerterror('삭제 실패') }
}

function initialize() {
	resetform(formdata); formdata.upmucd = searchform.upmucd; formdata.actkind = 'S0'; formdata.useyn = 'Y'; formdata.dspord = '1'; formdata.cmpycd = authstore.cmpycd;
}

watch(() => searchform.upmucd, (newval) => { formdata.upmucd = newval })

onMounted(async () => {
	if (maingridelement.value) {
		maingrid = new Tabulator(maingridelement.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			placeholder: '데이터가 없습니다.',
			columnDefaults: { headerSort: false, headerHozAlign: 'center', vertAlign: "middle" },
			columns: [
				{ title: '분류코드', field: 'grpcd', hozAlign: 'center', width: 110, cssClass: 'fw-bold text-primary border-end' },
				{ title: '분류 명칭', field: 'grpnm', minWidth: 250, widthGrow: 1, cssClass: 'fw-bold', hozAlign: 'left' },
				{ title: '순서', field: 'dspord', hozAlign: 'center', width: 100 },
				{ title: '사용', field: 'useyn', hozAlign: 'center', width: 80, formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
			]
		})
		maingrid.on('rowClick', (e, row) => {
			const d = normalizekeys(row.getData());
			Object.assign(formdata, d);
			formdata.actkind = 'U0'
		})
	}
	await fetchupmu()
	nextTick(search)
})
</script>
