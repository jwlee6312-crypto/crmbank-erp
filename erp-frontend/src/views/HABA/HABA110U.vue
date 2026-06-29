<!--
	=============================================================
	프로그램명	: 구좌정보관리 (haba110u)
	작성일자	: 2025.03.14
	설명        : 계정과목별 금융 계좌(구좌) 상세 정보 관리 (표준 소문자 키 적용)
	=============================================================
-->

<template>
	<AppAlert :show="showalert" :error="showerror" :message="alertmessage" />

	<div class="erp-container">
		<!-- 🚀 상단 액션 바 -->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-wallet2 me-2 text-primary" style="font-size: 18px;"></i>
				기본정보 <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">구좌정보 (haba110u)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">초기화</button>
				<button class="btn-erp btn-search" @click="search">조회</button>
				<button class="btn-erp btn-save" @click="save">저장</button>
			</div>
		</div>

		<!-- 🔍 검색 조건 영역 -->
		<div class="p-2 pb-0 flex-shrink-0 bg-light">
			<div class="card border shadow-sm overflow-hidden bg-white">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 250px;" />
						<col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center border-end bg-light">계정과목</th>
							<td class="px-2">
								<div class="input-group input-group-sm">
									<input v-model="searchform.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="searchform.acctcd_t" type="text" class="form-control" placeholder="계정 선택" @keydown.enter="openhelp('search_acct')" />
									<button class="btn btn-outline-secondary px-2" @click="openhelp('search_acct')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<td class="px-3 text-muted small">
								<i class="bi bi-info-circle me-1"></i> 검색하실 계정을 선택해 주십시요.
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📝 상세 정보 입력 영역 -->
		<div class="p-2 pb-0 flex-shrink-0 bg-light">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-header py-1 px-2 bg-light border-bottom">
					<span class="small fw-bold text-secondary"><i class="bi bi-pencil-square me-1"></i> 구좌 상세 정보 [{{ masterdata.actkind === 'I1' ? '신규' : '수정' }}]</span>
				</div>
				<table class="erp-table-full border-0 small">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 20%;" />
						<col style="width: 100px;" /><col style="width: 20%;" />
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center bg-light-subtle border-end">계정과목</th>
							<td class="bg-white border-end px-2 py-1">
								<input v-model="masterdata.acctcd_t" type="text" class="form-control form-control-sm bg-light" readonly />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">계좌번호</th>
							<td class="bg-white border-end border-top px-2 py-1">
								<input v-model="masterdata.gujano" type="text" class="form-control form-control-sm" maxlength="20" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">개설은행</th>
							<td class="bg-white border-top px-2 py-1">
								<div class="input-group input-group-sm">
									<input v-model="masterdata.bankcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="masterdata.bankcd_t" type="text" class="form-control" @keydown.enter="openhelp('bank')" />
									<button class="btn btn-outline-secondary px-2" @click="openhelp('bank')"><i class="bi bi-search"></i></button>
								</div>
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">개&nbsp;설&nbsp;일</th>
							<td class="bg-white border-end border-top px-2 py-1">
								<input v-model="masterdata.stdymd" type="date" class="form-control form-control-sm" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">만&nbsp;기&nbsp;일</th>
							<td class="bg-white border-end border-top px-2 py-1">
								<input v-model="masterdata.endymd" type="date" class="form-control form-control-sm" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">계&nbsp;약&nbsp;금</th>
							<td class="bg-white border-top px-2 py-1">
								<div class="input-group input-group-sm">
									<input v-model="masterdata.wonamt" type="number" class="form-control text-end" />
									<span class="input-group-text">원</span>
								</div>
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">이&nbsp;&nbsp;&nbsp;&nbsp;율</th>
							<td class="bg-white border-end border-top px-2 py-1">
								<div class="input-group input-group-sm">
									<input v-model="masterdata.rate" type="number" class="form-control text-end" step="0.01" />
									<span class="input-group-text">%</span>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end border-top">월불입금</th>
							<td class="bg-white border-end border-top px-2 py-1">
								<div class="input-group input-group-sm">
									<input v-model="masterdata.payamt" type="number" class="form-control text-end" />
									<span class="input-group-text">원</span>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end border-top">불&nbsp;입&nbsp;일</th>
							<td class="bg-white border-top px-2 py-1">
								<div class="d-flex align-items-center gap-2">
									<input v-model="masterdata.paydd" type="text" class="form-control form-control-sm text-end" style="max-width: 60px;" maxlength="2" />
									<span class="fw-bold">일</span>
								</div>
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">비&nbsp;&nbsp;&nbsp;&nbsp;고</th>
							<td colspan="3" class="bg-white border-end border-top px-2 py-1">
								<input v-model="masterdata.remark" type="text" class="form-control form-control-sm" maxlength="40" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">사용여부</th>
							<td class="bg-white border-top px-3">
								<div class="form-check form-switch m-0 pt-1">
									<input v-model="masterdata.useyn" class="form-check-input" type="checkbox" id="usecheck" true-value="Y" false-value="N">
									<label class="form-check-label small fw-bold" for="usecheck">사용</label>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- 📊 그리드 영역 -->
		<div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column bg-light">
			<div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
                <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                  <div ref="maingridelement" class="tabulator-instance flex-grow-1"></div>
                </div>
			</div>
		</div>
	</div>

	<Modal v-model:visible="modalvisible" :modalProps="modalprops" />
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

const authstore = useAuthStore()
const { showAlert: showalert, showError: showerror, alertMessage: alertmessage, vAlert: valert, vAlertError: valerterror } = useAlerts()
const { resetForm: resetform } = useFormReset()

// 1. 상태 관리 (키 소문자, 값 대문자 원칙)
const searchform = reactive({ acctcd: '', acctcd_t: '', custgbn: '' })
const masterdata = reactive<any>({
	actkind: 'I1', acctcd: '', acctcd_t: '', custgbn: '', gujano: '', bankcd: '', bankcd_t: '',
	stdymd: '', endymd: '', wonamt: 0, rate: 0, payamt: 0, paydd: '', remark: '', useyn: 'Y', cmpycd: authstore.cmpycd
})

const maingridelement = ref<HTMLDivElement | null>(null)
let maingrid: Tabulator | null = null

const formatymd = (v: string) => v && v.length === 8 ? `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}` : v

const normalizekeys = (obj: any) => {
  const n: any = {}; if (!obj) return n;
  Object.keys(obj).forEach(k => n[k.toLowerCase()] = typeof obj[k] === 'string' ? obj[k].trim() : obj[k]);
  return n;
}

const search = async () => {
	if (!searchform.acctcd_t) return valert('검색하실 계정을 선택해 주십시요.')
	try {
		const res = await api.post('/api/haba/haba_110u_str', {
			actkind: 'S1', cmpycd: authstore.cmpycd, acctcd: searchform.acctcd,
			wonamt: 0, rate: 0, payamt: 0
		})
		const processed = (res.data || []).map((r: any) => {
			const n = normalizekeys(r);
			return {
				...n,
				stdymd_fmt: formatymd(n.stdymd),
				endymd_fmt: formatymd(n.endymd)
			}
		})
		maingrid?.setData(processed)
		valert('조회되었습니다.')
	} catch (e) { valerterror('조회 중 오류 발생') }
}

const save = async () => {
	if (!masterdata.acctcd) return valert('계정코드를 기재해 주십시요.')
	if (!masterdata.gujano) return valert('계좌번호를 기재해 주십시요.')
	if (!masterdata.bankcd) return valert('개설은행을 기재해 주십시요.')

	if (!confirm('저장하시겠습니까?')) return
	try {
		const payload = {
			...masterdata,
			cmpycd: authstore.cmpycd,
			userid: authstore.userid,
			stdymd: (masterdata.stdymd || '').replace(/-/g, ''),
			endymd: (masterdata.endymd || '').replace(/-/g, '')
		}
		const res = await api.post('/api/haba/haba_110u_str', payload)
		const resdata = normalizekeys(res.data?.[0]);
		if (resdata.ret_yn === 'Y' || resdata.result === 'N') valerterror(resdata.ret_msg || resdata.msg || '저장 실패')
		else { valert('정상 처리되었습니다.'); search(); initialize() }
	} catch (e) { valerterror('저장 실패') }
}

const initialize = () => {
	resetform(masterdata)
	Object.assign(masterdata, { actkind: 'I1', cmpycd: authstore.cmpycd, useyn: 'Y', acctcd: searchform.acctcd, acctcd_t: searchform.acctcd_t, custgbn: searchform.custgbn })
}

const modalvisible = ref(false)
const modalprops = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openhelp(type: string) {
	if (type === 'search_acct') {
		Object.assign(modalprops, {
			title: '계정과목 선택', path: '/api/ha00/ha00_00p_str',
			data: { gubun: 'A6', cmpycd: authstore.cmpycd, gbncd:'023' },
			columns: [{ title: '코드', field: 'acctcd', width: 80 }, { title: '계정명', field: 'acctnm', width: 180 }],
			onConfirm: (d: any) => {
				const n = normalizekeys(d);
				searchform.acctcd = n.acctcd; searchform.acctcd_t = n.acctnm;
				initialize();
			}
		})
	} else if (type === 'bank') {
		if (!masterdata.acctcd) return valert('조회 후 입력하시기 바랍니다.')
		Object.assign(modalprops, {
			title: '은행 선택', path: '/api/ha00/ha00_00p_str',
			data: { gubun: 'C3', cmpycd: authstore.cmpycd },
			columns: [{ title: '코드', field: 'custcd', width: 80 }, { title: '은행명', field: 'custnm', width: 180 }],
			onConfirm: (d: any) => {
				const n = normalizekeys(d);
				masterdata.bankcd = n.custcd; masterdata.bankcd_t = n.custnm
			}
		})
	}
	modalvisible.value = true
}

onMounted(() => {
	if (maingridelement.value) {
		maingrid = new Tabulator(maingridelement.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			columnDefaults: { headerSort: false, vertAlign: "middle", headerHozAlign: "center", hozAlign: "center" },
			columns: [
				{ title: "계좌번호", field: "gujano", width: 200, hozAlign: "center" },
				{ title: "개설은행", field: "banknm", minWidth: 180, hozAlign: "left" },
				{ title: "개설일", field: "stdymd", width: 150, hozAlign: "center" },
				{ title: "만기일", field: "endymd", width: 150, hozAlign: "center" },
				{ title: "계약금", field: "wonamt", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "월불입액", field: "payamt", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "이율", field: "rate", width: 150, hozAlign: "right", formatter: (c) => c.getValue() + "%" },
                { title: "사용", field: "useyn", width: 80, hozAlign: "center",
                  formatter: (cell) => {
                    const val = String(cell.getValue() || '').trim().toUpperCase();
                    return val === 'Y' ? '<b class="text-primary">사용</b>' : '';
                  }
                }
			]
		})
		maingrid.on("rowClick", (e, row) => {
			const d = normalizekeys(row.getData())
			Object.assign(masterdata, d);
			masterdata.actkind = 'U1';
			masterdata.acctcd_t = d.acctnm; masterdata.bankcd_t = d.banknm;
			masterdata.stdymd = d.stdymd; masterdata.endymd = d.endymd;
		})
	}
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
