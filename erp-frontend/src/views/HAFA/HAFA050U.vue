<!--	=============================================================
	프로그램명	: 고정자산 마스터 관리
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 고정자산 마스터 정보(취득, 상각방법, 부서 등) 등록 및 관리 (디자인 표준화 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 1. 상단 액션 바 (표준 규격 및 함수명 통일) -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-box-seam me-2 text-primary" style="font-size: 18px;"></i>
				고정자산 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">고정자산관리 (HAFA050U)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="search">조회</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
			</div>
		</div>

		<!-- 🔍 2. 검색 조건 영역 (표준 erp-table-full 적용) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-body p-0 bg-light">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 80px;" /><col style="width: 250px;" />
							<col style="width: 80px;" /><col style="width: 250px;" />
							<col />
						</colgroup>
						<tbody>
							<tr>
								<th class="required">계정과목</th>
								<td>
									<div class="input-group">
										<input v-model="searchForm.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
										<input v-model="searchForm.acctnm" type="text" class="form-control" @keydown.enter="openHelp('ACCT', 'search')" placeholder="계정 선택" />
										<button class="btn" @click="openHelp('ACCT', 'search')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th>기준연월</th>
								<td>
									<div class="d-flex align-items-center gap-1 px-1">
										<select v-model="searchForm.yy" class="form-select" style="width: 100px;">
											<option v-for="y in yearOptions" :key="y" :value="y">{{ y }}년</option>
										</select>
										<input v-model="searchForm.mm" type="text" class="form-control text-center" style="width: 50px;" maxlength="2" />
										<span class="small fw-bold ms-1 text-secondary">월 현재</span>
									</div>
								</td>
								<td class="bg-white"></td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>

		<!-- 📝 3. 입력/상세 정보 영역 (표준 erp-table-full 및 고밀도 레이아웃) -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
					<div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-primary"></i>자산 상세 정보 입력 [{{ formData.actkind === 'A' ? '신규등록' : '정보수정' }}]</div>
					<div class="form-check form-switch m-0">
						<input v-model="formData.useyn" class="form-check-input" type="checkbox" id="useynCheck050" true-value="N" false-value="Y">
						<label class="form-check-label text-danger fw-bold small" for="useynCheck050">데이터 삭제</label>
					</div>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full border-0">
						<colgroup>
							<col style="width: 80px;" /><col />
							<col style="width: 80px;" /><col />
							<col style="width: 80px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th>자산코드</th>
								<td><input v-model="formData.asetcd" type="text" class="form-control text-center bg-light fw-bold" readonly placeholder="자동부여" /></td>
								<th class="required">자산명</th>
								<td><input v-model="formData.asetnm" type="text" class="form-control" placeholder="자산 명칭 입력" /></td>
								<th class="required">계정과목</th>
								<td>
									<div class="input-group">
										<input v-model="formData.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
										<input v-model="formData.acctnm" type="text" class="form-control" @keydown.enter="openHelp('ACCT', 'form')" />
										<button class="btn" @click="openHelp('ACCT', 'form')"><i class="bi bi-search"></i></button>
									</div>
								</td>
							</tr>
							<tr>
								<th class="required">취득일</th>
								<td><input v-model="formData.pchymd" type="date" class="form-control" /></td>
								<th class="required">취득수량</th>
								<td><input v-model="formData.pchqty" type="number" class="form-control text-end" /></td>
								<th class="required">취득가액</th>
								<td><input v-model="formData.pchamt" type="number" class="form-control text-end fw-bold text-primary" placeholder="0" /></td>
							</tr>
							<tr>
								<th class="required">상각방법</th>
								<td>
									<select v-model="formData.dprstype" class="form-select">
										<option v-for="item in dprstypeOptions" :key="item.code" :value="item.code">{{ item.name }}</option>
									</select>
								</td>
								<th class="required">내용연수</th>
								<td>
									<div class="input-group">
										<input v-model="formData.legalyy" type="number" class="form-control text-end" />
										<button class="btn" @click="openHelp('rate')"><i class="bi bi-list-ul"></i></button>
									</div>
								</td>
								<th class="required">상각율</th>
								<td>
									<div class="d-flex gap-1 align-items-center">
										<input v-model="formData.asetrate" type="number" step="0.001" class="form-control text-end fw-bold" style="width: 100px;" />
										<span class="small text-muted ms-1">가감:</span>
										<input v-model="formData.GAGAMyy" type="number" class="form-control text-end" style="width: 50px;" />
									</div>
								</td>
							</tr>
							<tr>
								<th class="required">비용구분</th>
								<td>
									<select v-model="formData.costtype" class="form-select">
										<option v-for="item in costtypeOptions" :key="item.code" :value="item.code">{{ item.name }}</option>
									</select>
								</td>
								<th class="required">보유부서</th>
								<td>
									<div class="input-group">
										<input v-model="formData.deptcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
										<input v-model="formData.deptnm" type="text" class="form-control" @keydown.enter="openHelp('DEPT')" />
										<button class="btn" @click="openHelp('DEPT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th>비 고</th>
								<td><input v-model="formData.remark" type="text" class="form-control" placeholder="참조 사항 입력" /></td>
							</tr>
							<tr>
								<th class="required">기초가액</th>
								<td><input v-model="formData.baseamt" type="number" class="form-control text-end" /></td>
								<th class="required">전기상각액</th>
								<td><input v-model="formData.dprssum" type="number" class="form-control text-end" /></td>
								<td colspan="2" class="bg-white"></td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>

		<!-- 📊 4. 그리드 영역 -->
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
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const today = new Date().toISOString().slice(0, 10)
const currentYear = new Date().getFullYear()
const currentMonth = (new Date().getMonth() + 1).toString().padStart(2, '0')
const yearOptions = Array.from({ length: 5 }, (_, i) => String(currentYear - i))

const searchForm = reactive({ acctcd: '', acctnm: '', yy: String(currentYear),.mm: currentMonth })
const formData = reactive({ actkind: 'A', asetcd: '', asetnm: '', acctcd: '', acctnm: '', pchymd: today, pchqty: 0, pchamt: 0, dprstype: '010', legalyy: 0, GAGAMyy: 0, asetrate: 0, costtype: '010', deptcd: '', deptnm: '', remark: '', baseamt: 0, dprssum: 0, useyn: 'Y' })

const dprstypeOptions = ref<any[]>([]); const costtypeOptions = ref<any[]>([])
const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

function initialize() {
	Object.assign(formData, { actkind: 'A', asetcd: '', asetnm: '', acctcd: searchForm.acctcd, acctnm: searchForm.acctnm, pchymd: today, pchqty: 0, pchamt: 0, dprstype: '010', legalyy: 0, GAGAMyy: 0, asetrate: 0, costtype: '010', deptcd: '', deptnm: '', remark: '', baseamt: 0, dprssum: 0, useyn: 'Y' })
}

const fetchOptions = async () => {
	try {
		const [resD, resC] = await Promise.all([
			api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', search: '250' }),
			api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', search: '110' })
		])
		dprstypeOptions.value = resD.data; costtypeOptions.value = resC.data
	} catch (e) { console.error(e) }
}

async function search() {
	if (!searchForm.acctcd) return vAlertError('계정과목을 선택하세요.')
	try {
		const res = await api.post('/api/hafa/HAFA_050U_STR', { actkind: 'S', cmpycd: authStore.cmpycd, yy: searchForm.yy,.mm: searchForm.mm, acctcd: searchForm.acctcd })
		const data = (res.data || []).map((r: any) => ({ ...r, pchymd: r.pchymd ? `${r.pchymd.slice(0,4)}-${r.pchymd.slice(4,6)}-${r.pchymd.slice(6,8)}` : '' }))
		mainGrid?.setData(data); vAlert('조회되었습니다.'); initialize()
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

async function save() {
	if (!formData.acctcd) return vAlertError('계정과목을 선택하세요.')
	if (!formData.asetnm) return vAlertError('자산명을 입력하세요.')
	try {
		await api.post('/api/hafa/HAFA_050U_STR', { ...formData, cmpycd: authStore.cmpycd, yy: searchForm.yy,.mm: searchForm.mm, pchymd: formData.pchymd.replace(/-/g, ''), userid: authStore.userid })
		vAlert('저장되었습니다.'); search()
	} catch (e) { vAlertError('저장 중 오류 발생') }
}

const modalVisible = ref(false); const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })
function openHelp(type: string, target?: string) {
	if (type === 'ACCT') {
		Object.assign(modalProps, { title: '계정과목 선택', path: '/api/ha00/HELP_acctcd_LTD_ETC_STR', data: { acctgbn: '020', search: target === 'search' ? searchForm.acctnm : formData.acctnm }, columns: [{ title: '코드', field: 'acctcd', width: 80 }, { title: '계정명', field: 'acctnm', width: 200 }],
			onConfirm: (d: any) => { if (target === 'search') { searchForm.acctcd = d.acctcd; searchForm.acctnm = d.acctnm; search() } else { formData.acctcd = d.acctcd; formData.acctnm = d.acctnm } }
		})
	} else if (type === 'DEPT') {
		Object.assign(modalProps, { title: '부서 선택', path: '/api/ha00/HA00_00P_STR', data: { gubun: 'D0', cmpycd: authStore.cmpycd, search: formData.deptnm }, columns: [{ title: '코드', field: 'deptcd', width: 80 }, { title: '부서명', field: 'deptnm', width: 180 }],
			onConfirm: (d: any) => { formData.deptcd = d.deptcd; formData.deptnm = d.deptnm }
		})
	} else if (type === 'rate') {
		Object.assign(modalProps, { title: '상각율 선택', path: '/api/hafa/HELP_asetrate_LTD_STR', data: { dprstype: formData.dprstype, legalyy: formData.legalyy }, columns: [{ title: '내용연수', field: 'legalyy', width: 100 }, { title: '상각율', field: 'asetrate', width: 100 }],
			onConfirm: (d: any) => { formData.legalyy = d.legalyy; formData.asetrate = d.asetrate }
		})
	}
	modalVisible.value = true
}

onMounted(() => {
	fetchOptions()
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "자산코드", field: "asetcd", width: 90, hozAlign: "center", cssClass: "fw-bold text-primary" },
				{ title: "자산명", field: "asetnm", width: 180, cssClass: "fw-bold" },
				{ title: "취득일", field: "pchymd", width: 100, hozAlign: "center" },
				{ title: "취득가액", field: "pchamt", width: 120, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "상각방법", field: "dprstypeNM", width: 100, hozAlign: "center" },
				{ title: "내용연수", field: "legalyy", width: 80, hozAlign: "center" },
				{ title: "상각율", field: "asetrate", width: 80, hozAlign: "center" },
				{ title: "보유부서", field: "deptnm", width: 150 },
				{ title: "삭제", field: "useyn", width: 60, hozAlign: "center", formatter: (c) => c.getValue() === 'N' ? 'V' : '' }
			]
		})
		mainGrid.on("rowClick", (e, row) => { Object.assign(formData, row.getData()); formData.actkind = 'U' })
	}
})
</script>
