<!--
	=============================================================
	프로그램명	: 자산이력조회 (HAFA020S)
	작성일자	: 2025.02.24
	설명        : 개별 고정자산의 마스터 정보 및 변동 이력 통합 조회 (팝업 호출 제어 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

	<div class="erp-container d-flex flex-column h-100 bg-white">
		<!-- 🚀 1. 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
			<div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-journal-text me-2 text-primary" style="font-size: 18px;"></i>
				고정자산 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				이력조회 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">자산이력조회 (HAFA020S)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1 pe-3">
				<button class="btn-erp btn-search" @click="search">조회</button>
			</div>
		</div>

		<!-- 💡 2. 메인 컨텐츠 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

			<!-- [상단] 조회 필터 영역 -->
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
										<input v-model="searchForm.acctnm" type="text" class="form-control" @keydown.enter="openHelp('ACCT')" placeholder="계정 선택" />
										<button class="btn btn-outline-secondary" @click="openHelp('ACCT')"><i class="bi bi-search"></i></button>
									</div>
								</td>
								<th class="text-center bg-light">자산코드</th>
								<td>
									<div class="input-group input-group-sm" style="width: 350px;">
										<input v-model="searchForm.asetcd" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 80px;" readonly />
										<input v-model="searchForm.asetnm" type="text" class="form-control" @keydown.enter="openHelp('ASET')" placeholder="자산 선택" />
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

			<!-- 📄 3. 자산 마스터 정보 -->
			<div class="card border shadow-sm flex-shrink-0 overflow-hidden">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
					<span class="fw-bold small text-dark"><i class="bi bi-info-circle me-2 text-primary"></i>자산 마스터 요약 정보</span>
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
								<th class="bg-light">취득일</th>
								<td class="text-center">{{ masterData.pchymd }}</td>
								<th class="bg-light">취득수량</th>
								<td class="text-end px-2">{{ formatMoney(masterData.pchqty) }}</td>
								<th class="bg-light">취득가액</th>
								<td class="text-end px-2">{{ formatMoney(masterData.pchamt) }}</td>
								<th class="bg-light">보유부서</th>
								<td class="text-center small">{{ masterData.deptnm }}</td>
							</tr>
							<tr>
								<th class="bg-light">상각방법</th>
								<td class="text-center">{{ masterData.dprstypenm }}</td>
								<th class="bg-light">내용연수</th>
								<td class="text-center">{{ masterData.legalyy }}</td>
								<th class="bg-light">가감연수</th>
								<td class="text-center">{{ masterData.GAGAMyy }}</td>
								<th class="bg-light">상각율</th>
								<td class="text-center">{{ masterData.asetrate }}</td>
							</tr>
							<tr>
								<th class="bg-light text-danger">처분일</th>
								<td class="text-center text-danger">{{ masterData.dspymd }}</td>
								<th class="bg-light text-danger">처분수량</th>
								<td class="text-end px-2 text-danger">{{ masterData.dspymd ? formatMoney(masterData.dspqty) : '' }}</td>
								<th class="bg-light text-danger">처분가액</th>
								<td class="text-end px-2 text-danger">{{ masterData.dspymd ? formatMoney(masterData.dspamt) : '' }}</td>
								<th class="bg-light text-danger">처분손익</th>
								<td class="text-end px-2 text-danger">{{ masterData.dspymd ? formatMoney(masterData.DSPPROFIT) : '' }}</td>
							</tr>
							<tr>
								<th class="bg-light">기초가액</th>
								<td class="text-end px-2">{{ formatMoney(masterData.baseamt) }}</td>
								<th class="bg-light">당기증가</th>
								<td class="text-end px-2">{{ formatMoney(masterData.incramt) }}</td>
								<th class="bg-light">전기상각</th>
								<td class="text-end px-2">{{ formatMoney(masterData.dprssum) }}</td>
								<th class="bg-light">당기상각</th>
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

			<!-- 📊 4. 그리드 영역 -->
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
				<div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
					<span class="fw-bold small text-dark"><i class="bi bi-list-ul me-2 text-primary"></i>자산 변동 이력 리스트</span>
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
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()

const searchForm = reactive({ acctcd: '', acctnm: '', asetcd: '', asetnm: '' })
const masterData = reactive({ pchymd: '', pchqty: 0, pchamt: 0, deptnm: '', dprstypenm: '', legalyy: '', GAGAMyy: '', asetrate: 0, dspymd: '', dspqty: 0, dspamt: 0, DSPPROFIT: 0, baseamt: 0, incramt: 0, dprssum: 0, dprsamt: 0, NONdprssum: 0, costtypeNM: '', remark: '' })

const mainGridRef = ref<HTMLDivElement | null>(null); let mainGrid: Tabulator | null = null

// 데이터 키를 소문자로 정규화
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
	if (!searchForm.acctcd || !searchForm.asetcd) return vAlertError('계정과목과 자산코드를 선택하세요.')
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
		mainGrid?.setData(data); vAlert('조회되었습니다.')
	} catch (e) { vAlertError('조회 중 오류 발생') }
}

const modalVisible = ref(false); const modalProps = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })
function openHelp(type: string) {
	if (type === 'ACCT') {
		Object.assign(modalProps, { title: '계정과목 선택', path: '/api/ha00/HA00_00P_STR', data: { gubun: 'A8', cmpycd: authStore.cmpycd, gbncd: '020', code: '' }, columns: [{ title: '코드', field: 'acctcd', width: 80 }, { title: '계정명', field: 'acctnm', width: 200 }],
			onConfirm: (rawData: any) => {
        const d = normalizeKeys(rawData);
        searchForm.acctcd = d.acctcd || d.code;
        searchForm.acctnm = d.acctnm || d.cdnm || d.name;
        searchForm.asetcd = ''; searchForm.asetnm = ''
      }
		})
	} else if (type === 'ASET') {
		if (!searchForm.acctcd) {
			vAlertError('계정과목을 먼저 선택해 주세요.');
			return;
		}
		Object.assign(modalProps, { title: '자산 선택', path: '/api/ha00/HA00_00P_STR', data: { gubun: 'J0', cmpycd: authStore.cmpycd, gbncd: searchForm.acctcd, code: searchForm.asetnm }, columns: [{ title: '코드', field: 'asetcd', width: 100 }, { title: '자산명', field: 'asetnm', width: 200 }],
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
				{ title: "발생일", field: "procymd", width: 150, hozAlign: "center" },
				{ title: "구분", field: "procnm", width: 150, hozAlign: "center", cssClass: "text-primary fw-bold" },
				{ title: "수량", field: "procqty", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "금액", field: "procamt", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "보유부서", field: "deptnm", width: 200 },
				{ title: "비고", field: "remark", minWidth: 250 },
				{ title: "사용", field: "useyn", width: 100, hozAlign: "center", formatter: "tickCross" }
			]
		})
	}
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
