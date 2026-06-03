<!--
	=============================================================
	프로그램명	: 프로젝트관리
	작성일자	: 2025.02.24
	작성자	    : AI Assistant
	설명        : 프로젝트 기본 정보 및 투입 인원 관리 (HABA070U)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-folder2-open me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">프로젝트관리 (HABA070U)</span>
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
				<button v-if="masterForm.actkind === 'U'" class="btn-erp btn-danger" @click="deleteData">
					<i class="bi bi-trash"></i> 삭제
				</button>
			</div>
		</div>

		<!-- 🔍 검색 조건 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden bg-light">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 300px;" />
						<col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center border-end">프로젝트</th>
							<td class="bg-white border-end px-2">
								<div class="input-group input-group-sm">
									<input v-model="searchForm.PRJCD" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="searchForm.PRJNM" type="text" class="form-control" placeholder="프로젝트 선택" @keydown.enter="openHelp('search_PRJ')" />
									<button class="btn btn-outline-secondary px-2" @click="openHelp('search_PRJ')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<td class="bg-white px-3 text-muted small">
								<i class="bi bi-info-circle me-1"></i> 관리하실 프로젝트를 선택해 주십시요.
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📝 마스터 정보 입력 영역 -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-header py-1 px-2 bg-light border-bottom">
					<span class="small fw-bold text-secondary"><i class="bi bi-pencil-square me-1"></i> 프로젝트 기본 정보</span>
				</div>
				<table class="erp-table-full small">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 20%;" />
						<col style="width: 100px;" /><col style="width: 25%;" />
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center bg-light-subtle border-end">프로젝트</th>
							<td class="bg-white border-end px-2 py-1">
								<input v-model="masterForm.PRJCD" type="text" class="form-control form-control-sm text-center" maxlength="4" :readonly="masterForm.actkind === 'U'" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">프로젝트명</th>
							<td class="bg-white border-end border-top px-2 py-1">
								<input v-model="masterForm.PRJNM" type="text" class="form-control form-control-sm" maxlength="50" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">완료여부</th>
							<td class="bg-white border-top px-3">
								<div class="form-check pt-1">
									<input v-model="masterForm.useyn" class="form-check-input" type="checkbox" id="prjUseYn" true-value="N" false-value="Y">
									<label class="form-check-label small fw-bold" for="prjUseYn">완료</label>
								</div>
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">시&nbsp;작&nbsp;일</th>
							<td class="bg-white border-end border-top px-2 py-1">
								<input v-model="masterForm.frymd" type="date" class="form-control form-control-sm" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">종&nbsp;료&nbsp;일</th>
							<td class="bg-white border-end border-top px-2 py-1">
								<input v-model="masterForm.toymd" type="date" class="form-control form-control-sm" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">출현순서</th>
							<td class="bg-white border-top px-2 py-1">
								<input v-model="masterForm.dspord" type="number" class="form-control form-control-sm text-end" style="max-width: 80px;" />
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">개&nbsp;&nbsp;&nbsp;&nbsp;요</th>
							<td colspan="5" class="bg-white border-top px-2 py-1">
								<textarea v-model="masterForm.remark" class="form-control form-control-sm" rows="2" maxlength="200"></textarea>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 투입 인원 그리드 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header py-1 px-2 bg-light border-bottom d-flex justify-content-between align-items-center">
					<span class="small fw-bold text-secondary"><i class="bi bi-people me-1"></i> 투입 인원 상세</span>
					<div class="btn-group btn-group-sm">
						<button class="btn btn-outline-primary py-0" @click="addRow"><i class="bi bi-plus"></i> 추가</button>
					</div>
				</div>
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                  <div ref="detailGridRef" class="tabulator-instance flex-grow-1"></div>
                </div>
			</div>
		</div>
	</div>

	<Modal v-model:visible="modalVisible" :modalProps="modalProps" />
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
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

const today = new Date().toISOString().substring(0, 10)

// 🔍 검색 데이터
const searchForm = reactive({
	PRJCD: '',
	PRJNM: ''
})

// 📝 마스터 데이터
const masterForm = reactive({
	actkind: 'A',
	PRJCD: '',
	PRJNM: '',
	frymd: today,
	toymd: today,
	remark: '',
	dspord: 0,
	useyn: 'Y'
})

const detailGridRef = ref<HTMLDivElement | null>(null)
let detailGrid: Tabulator | null = null

const formatYmd = (v: string) => v && v.length === 8 ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v

const search = async () => {
	if (!searchForm.PRJCD) return vAlert('프로젝트를 선택하시기 바랍니다.')
	try {
		// 1. 마스터 조회
		const resMaster = await api.post('/api/haba/HABA_070U_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd,
			PRJCD: searchForm.PRJCD
		})

		if (resMaster.data && resMaster.data.length > 0) {
			const m = resMaster.data[0]
			Object.assign(masterForm, {
				actkind: 'U',
				PRJCD: m.PRJCD,
				PRJNM: m.PRJNM,
				frymd: formatYmd(m.frymd),
				toymd: formatYmd(m.toymd),
				remark: m.bigo,
				dspord: Number(m.dspord || 0),
				useyn: m.useyn
			})
		}

		// 2. 디테일(투입인원) 조회
		const resDetail = await api.post('/api/haba/HABA_071U_STR', {
			actkind: 'S0',
			cmpycd: authStore.cmpycd,
			PRJCD: searchForm.PRJCD
		})

		const processedDetails = (resDetail.data || []).map((r: any) => ({
			upkind: '',
			rowno: r.rowno,
			userid: r.userid,
			usernm: r.usernm,
			Sfrymd: formatYmd(r.frymd),
			Stoymd: formatYmd(r.toymd),
			Sbigo: r.bigo
		}))

		detailGrid?.setData(processedDetails)
		vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

const save = async () => {
	if (!masterForm.PRJCD) return vAlert('프로젝트 코드를 입력하시기 바랍니다.')
	if (!masterForm.PRJNM) return vAlert('프로젝트 명을 입력하시기 바랍니다.')
	if (!masterForm.frymd || !masterForm.toymd) return vAlert('일자를 입력하시기 바랍니다.')

	try {
		// 1. 마스터 저장 (actkind + 0)
		const masterPayload = {
			...masterForm,
			actkind: masterForm.actkind + '0',
			cmpycd: authStore.cmpycd,
			userid: authStore.userid,
			frymd: masterForm.frymd.replace(/-/g, ''),
			toymd: masterForm.toymd.replace(/-/g, '')
		}

		const resMaster = await api.post('/api/haba/HABA_070U_STR', masterPayload)
		if (resMaster.data?.[0]?.ret_yn === 'Y') return vAlertError(resMaster.data[0].RET_MSG)

		// 2. 디테일 저장
		const gridData = detailGrid?.getData() || []
		for (const row of gridData) {
			let kind = row.upkind || masterForm.actkind
			if (!row.userid) continue

			const detailPayload = {
				actkind: kind + '0',
				cmpycd: authStore.cmpycd,
				PRJCD: masterForm.PRJCD,
				rowno: row.rowno || '',
				userid: row.userid,
				Sfrymd: (row.Sfrymd || '').replace(/-/g, ''),
				Stoymd: (row.Stoymd || '').replace(/-/g, ''),
				Sbigo: row.Sbigo || '',
				MGR_userid: authStore.userid
			}
			await api.post('/api/haba/HABA_071U_STR', detailPayload)
		}

		vAlert('정상으로 작업이 되었습니다.')
		search()
	} catch (e) { vAlertError('저장 실패') }
}

const deleteData = async () => {
	if (!confirm('해당 자료를 삭제하시겠습니까?')) return
	try {
		const res = await api.post('/api/haba/HABA_070U_STR', {
			actkind: 'D0',
			cmpycd: authStore.cmpycd,
			PRJCD: masterForm.PRJCD
		})
		vAlert('삭제되었습니다.')
		initialize()
	} catch (e) { vAlertError('삭제 실패') }
}

const initialize = () => {
	resetForm(masterForm)
	masterForm.actkind = 'A'
	masterForm.frymd = today
	masterForm.toymd = today
	masterForm.useyn = 'Y'
	detailGrid?.setData([])
	searchForm.PRJCD = ''
	searchForm.PRJNM = ''
}

const addRow = () => {
	detailGrid?.addRow({ upkind: 'A', Sfrymd: masterForm.frymd, Stoymd: masterForm.toymd }, true)
}

// 팝업 설정
const modalVisible = ref(false)
const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

let activeRow: any = null

function openHelp(type: string, row?: any) {
	if (type === 'search_prj') {
		Object.assign(modalProps, {
			title: '프로젝트 선택', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'P0', cmpycd: authStore.cmpycd, search: searchForm.PRJNM },
			columns: [{ title: '코드', field: 'PRJCD', width: 100 }, { title: '프로젝트명', field: 'PRJNM', width: 250 }],
			onConfirm: (d: any) => {
				searchForm.PRJCD = d.PRJCD;
				searchForm.PRJNM = d.PRJNM;
				search();
			}
		})
	} else if (type === 'USER') {
		activeRow = row;
		Object.assign(modalProps, {
			title: '사용자 선택', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'U0', cmpycd: authStore.cmpycd, search: row.getData().usernm },
			columns: [{ title: '사번', field: 'userid', width: 100 }, { title: '성명', field: 'usernm', width: 150 }],
			onConfirm: (d: any) => {
				activeRow.update({ userid: d.userid, usernm: d.usernm, upkind: activeRow.getData().rowno ? 'U' : 'A' });
			}
		})
	}
	modalVisible.value = true
}

onMounted(() => {
	if (detailGridRef.value) {
		detailGrid = new Tabulator(detailGridRef.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "No", field: "rowno", width: 60, hozAlign: "center" },
				{
					title: "투입인원", field: "usernm", width: 250,
					formatter: (cell) => {
						const d = cell.getData()
						return `<div class="d-flex align-items-center gap-1">
									<span class="badge bg-light text-dark border">${d.userid || ''}</span>
									<span>${d.usernm || ''}</span>
									<i class="bi bi-search ms-auto text-primary cursor-pointer"></i>
								</div>`
					},
					cellClick: (e, cell) => {
						if ((e.target as HTMLElement).classList.contains('bi-search')) {
							openHelp('USER', cell.getRow())
						}
					}
				},
				{ title: "투입일", field: "Sfrymd", width: 130, editor: "date" },
				{ title: "종료일", field: "Stoymd", width: 130, editor: "date" },
				{ title: "비고", field: "Sbigo", editor: "input" },
				{
					title: "삭제", width: 60, hozAlign: "center",
					formatter: () => '<i class="bi bi-x-circle text-danger cursor-pointer"></i>',
					cellClick: async (e, cell) => {
						const d = cell.getData()
						if (d.rowno) {
							if (confirm('이 인원을 프로젝트에서 삭제하시겠습니까?')) {
								try {
									await api.post('/api/haba/HABA_071U_STR', {
										actkind: 'D0',
										cmpycd: authStore.cmpycd,
										PRJCD: masterForm.PRJCD,
										rowno: d.rowno,
										MGR_userid: authStore.userid
									})
									cell.getRow().delete()
									vAlert('삭제되었습니다.')
								} catch (e) { vAlertError('삭제 실패') }
							}
						} else {
							cell.getRow().delete()
						}
					}
				}
			],
			dataChanged: (data) => {
				// 데이터 변경 시 upkind 처리 (필요시)
			}
		})
	}
})
</script>

<style scoped>
.bg-light-subtle { background-color: #f8f9fa !important; }
.cursor-pointer { cursor: pointer; }
</style>
