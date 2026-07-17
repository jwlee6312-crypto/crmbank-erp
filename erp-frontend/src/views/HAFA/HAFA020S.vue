<!--
	=============================================================
	?�로그램�?: ?�산?�력조회 (HAFA020S)
	?�성?�자	: 2025.02.24
	?�명        : 개별 고정?�산??마스???�보 �?변???�력 ?�합 조회 (?�업 ?�출 ?�어 ?�용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- ?? 1. ?�단 ?�션 �?-->
		<div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
			<div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-journal-text me-2 text-primary" style="font-size: 18px;"></i>
				고정?�산 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				?�력조회 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">?�산?�력조회 (HAFA020S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1 pe-3">
				<button class="btn-erp btn-search" @click="search">조회</button>
			</div>
		</div>

		<!-- ?�� 2. 메인 컨텐�??�역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

			<!-- [?�단] 조회 ?�터 ?�역 -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-body p-0 bg-white">
					<table class="erp-table-dense" width="100%">
						<colgroup>
							<col style="width: 10%" /><col style="width: 40%" />
							<col style="width: 10%" /><col style="width: 40%" />
						</colgroup>
						<tbody>
							<tr>
								<th class="text-center bg-light">계정과목</th>
								<td>
									<div class="input-group input-group-sm" style="width: 300px;">
										<input v-model="searchForm.acctcd" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 65px;" readonly />
										<input v-model="searchForm.acctnm" type="text" class="form-control" @keydown.enter="openHelp('ACCT')" placeholder="계정 ?�택" />
										<button class="btn btn-outline-secondary" @click="openHelp('ACCT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="text-center bg-light">?�산코드</th>
								<td>
									<div class="input-group input-group-sm" style="width: 350px;">
										<input v-model="searchForm.asetcd" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 80px;" readonly />
										<input v-model="searchForm.asetnm" type="text" class="form-control" @keydown.enter="openHelp('ASET')" placeholder="?�산 ?�택" />
										<button class="btn btn-outline-secondary" @click="openHelp('ASET')" :disabled="!searchForm.acctcd">
											<i class="bi bi-search"></i>
										</button>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- ?�� 3. ?�산 마스???�보 -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
					<span class="fw-bold small text-dark"><i class="bi bi-info-circle me-2 text-primary"></i>?�산 마스???�약 ?�보</span>
				</div>
				<div class="card-body p-0 bg-white">
					<table class="erp-table-full">
						<colgroup>
							<col style="width: 100px;" /><col />
							<col style="width: 100px;" /><col />
							<col style="width: 100px;" /><col />
							<col style="width: 100px;" /><col />
						</colgroup>
						<tbody>
							<tr>
								<th class="bg-light">취득??/th>
								<td class="text-center">{{ masterData.pchymd }}</td>
								<th class="bg-light">취득?�량</th>
								<td class="text-end px-2">{{ formatMoney(masterData.pchqty) }}</td>
								<th class="bg-light">취득가??/th>
								<td class="text-end px-2">{{ formatMoney(masterData.pchamt) }}</td>
								<th class="bg-light">보유부??/th>
								<td class="text-center small">{{ masterData.deptnm }}</td>
							</tr>
							<tr>
								<th class="bg-light">?�각방법</th>
								<td class="text-center">{{ masterData.dprstypenm }}</td>
								<th class="bg-light">?�용?�수</th>
								<td class="text-center">{{ masterData.legalyy }}</td>
								<th class="bg-light">가감연??/th>
								<td class="text-center">{{ masterData.GAGAMyy }}</td>
								<th class="bg-light">?�각??/th>
								<td class="text-center">{{ masterData.asetrate }}</td>
							</tr>
							<tr>
								<th class="bg-light text-danger">처분??/th>
								<td class="text-center text-danger">{{ masterData.dspymd }}</td>
								<th class="bg-light text-danger">처분?�량</th>
								<td class="text-end px-2 text-danger">{{ masterData.dspymd ? formatMoney(masterData.dspqty) : '' }}</td>
								<th class="bg-light text-danger">처분가??/th>
								<td class="text-end px-2 text-danger">{{ masterData.dspymd ? formatMoney(masterData.dspamt) : '' }}</td>
								<th class="bg-light text-danger">처분?�익</th>
								<td class="text-end px-2 text-danger">{{ masterData.dspymd ? formatMoney(masterData.DSPPROFIT) : '' }}</td>
							</tr>
							<tr>
								<th class="bg-light">기초가??/th>
								<td class="text-end px-2">{{ formatMoney(masterData.baseamt) }}</td>
								<th class="bg-light">?�기증�?</th>
								<td class="text-end px-2">{{ formatMoney(masterData.incramt) }}</td>
								<th class="bg-light">?�기?�각</th>
								<td class="text-end px-2">{{ formatMoney(masterData.dprssum) }}</td>
								<th class="bg-light">?�기?�각</th>
								<td class="text-end px-2">{{ formatMoney(masterData.dprsamt) }}</td>
							</tr>
							<tr>
								<th class="bg-light text-primary fw-bolder">미상각액</th>
								<td class="text-end px-2 fw-bold text-primary">{{ formatMoney(masterData.NONdprssum) }}</td>
								<th class="bg-light">비용구분</th>
								<td class="text-center">{{ masterData.costtypeNM }}</td>
								<th class="bg-light">비고</th>
								<td colspan="3" class="px-2 small">{{ masterData.remark }}</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>

			<!-- ?�� 4. 그리???�역 -->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
					<span class="fw-bold small text-dark"><i class="bi bi-list-ul me-2 text-primary"></i>?�산 변???�력 리스??/span>
				</div>
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
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const searchForm = reactive({ acctcd: '', acctnm: '', asetcd: '', asetnm: '' })
const masterData = reactive({ pchymd: '', pchqty: 0, pchamt: 0, deptnm: '', dprstypenm: '', legalyy: '', GAGAMyy: '', asetrate: 0, dspymd: '', dspqty: 0, dspamt: 0, DSPPROFIT: 0, baseamt: 0, incramt: 0, dprssum: 0, dprsamt: 0, NONdprssum: 0, costtypeNM: '', remark: '' })

const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

// ?�이???��? ?�문?�로 ?�규??
const normalizeKeys = (data: any): any => {
  if (!data) return data;
  if (Array.isArray(data)) return data.map(item => normalizeKeys(item));
  if (typeof data !== 'object' || data instanceof Date) return data;
  const normalized: any = {};
  Object.keys(data).forEach(key => { normalized[key.toLowerCase().trim()] = normalizeKeys(data[key]); });
  return normalized;
};

const formatMoney = (val: any) => (val || val === 0) ? Number(val).toLocaleString() : ''

async function search() {
	if (!searchForm.acctcd || !searchForm.asetcd) return vAlertError('계정과목�??�산코드�??�택?�세??')
	try {
		const resMaster = await api.post('/api/hafa/HAFA_020S_STR', { actkind: 'S0', cmpycd: authStore.cmpycd, acctcd: searchForm.acctcd, asetcd: searchForm.asetcd })
		if (resMaster.data && resMaster.data.length > 0) {
			const m = normalizeKeys(resMaster.data[0]);
			Object.assign(masterData, {
        ...m,
        pchymd: m.pchymd ? `${m.pchymd.slice(0,4)}.${m.pchymd.slice(4,6)}.${m.pchymd.slice(6,8)}` : '',
        dspymd: (m.dspymd && m.dspymd > '00000000') ? `${m.dspymd.slice(0,4)}.${m.dspymd.slice(4,6)}.${m.dspymd.slice(6,8)}` : '',
        nondprssum: (m.baseamt || 0) + (m.incramt || 0) - (m.dprssum || 0) - (m.dprsamt || 0),
        dspprofit: (m.dspamt || 0) - ((m.baseamt || m.pchamt || 0) + (m.incramt || 0) - (m.dprssum || 0) - (m.dprsamt || 0))
      })
		} else { Object.keys(masterData).forEach(key => (masterData as any)[key] = '') }

		const resList = await api.post('/api/hafa/HAFA_020S_STR', { actkind: 'S1', cmpycd: authStore.cmpycd, acctcd: searchForm.acctcd, asetcd: searchForm.asetcd })
		const data = normalizeKeys(resList.data || []).map((r: any) => ({ ...r, procymd: r.procymd ? `${r.procymd.slice(0,4)}-${r.procymd.slice(4,6)}-${r.procymd.slice(6,8)}` : '' }))
		mainGrid?.setData(data); vAlert('조회?�었?�니??')
	} catch (e) { vAlertError('조회 �??�류 발생') }
}

const modalVisible = ref(false); const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })
function openHelp(type: string) {
	if (type === 'ACCT') {
		Object.assign(modalProps, { title: '계정과목 ?�택', path: '/api/ha00/HA00_00P_STR', data: { gubun: 'A8', cmpycd: authStore.cmpycd, gbncd: '020', code: '' }, columns: [{ title: '코드', field: 'acctcd', width: 80 }, { title: '계정�?, field: 'acctnm', width: 200 }],
			onConfirm: (rawData: any) => {
        const d = normalizeKeys(rawData);
        searchForm.acctcd = d.acctcd || d.code;
        searchForm.acctnm = d.acctnm || d.cdnm || d.name;
        searchForm.asetcd = ''; searchForm.asetnm = ''
      }
		})
	} else if (type === 'ASET') {
		if (!searchForm.acctcd) {
			vAlertError('계정과목??먼�? ?�택??주세??');
			return;
		}
		Object.assign(modalProps, { title: '?�산 ?�택', path: '/api/ha00/HA00_00P_STR', data: { gubun: 'J0', cmpycd: authStore.cmpycd, gbncd: searchForm.acctcd, code: searchForm.asetnm }, columns: [{ title: '코드', field: 'asetcd', width: 100 }, { title: '?�산�?, field: 'asetnm', width: 200 }],
			onConfirm: (rawData: any) => {
        const d = normalizeKeys(rawData);
        searchForm.asetcd = d.asetcd || d.code;
        searchForm.asetnm = d.asetnm || d.cdnm || d.name;
        search()
      }
		})
	}
	modalVisible.value = true
}

onMounted(() => {
	if (mainGridRef.value) {
		mainGrid = new Tabulator(mainGridRef.value, {
			layout: 'fitColumns', height: '100%', columnDefaults: { headerSort: false, vertAlign: "middle" },
			columns: [
				{ title: "발생??, field: "procymd", width: 150, hozAlign: "center" },
				{ title: "구분", field: "procnm", width: 150, hozAlign: "center", cssClass: "text-primary fw-bold" },
				{ title: "?�량", field: "procqty", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "금액", field: "procamt", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "보유부??, field: "deptnm", width: 200 },
				{ title: "비고", field: "remark", minWidth: 250 },
                { title: "?�용", field: "useyn", width: 80, hozAlign: "center",
                  formatter: (cell) => {
                    const val = String(cell.getValue() || '').trim().toUpperCase();
                    return val === 'Y' ? '<b class="text-primary">?�용</b>' : '';
                  }
                }			]
		})
	}
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
