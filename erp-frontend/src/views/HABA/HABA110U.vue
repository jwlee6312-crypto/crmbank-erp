<!--
	=============================================================
	?ÑÎ°úÍ∑∏Îû®Î™?: Íµ¨Ï¢å?ïÎ≥¥Í¥ÄÎ¶?(haba110u)
	?ëÏÑ±?ºÏûê	: 2025.03.14
	?§Î™Ö        : Í≥ÑÏ†ïÍ≥ºÎ™©Î≥?Í∏àÏúµ Í≥ÑÏ¢å(Íµ¨Ï¢å) ?ÅÏÑ∏ ?ïÎ≥¥ Í¥ÄÎ¶?(?úÏ? ?åÎ¨∏?????ÅÏö©)
	=============================================================
-->

<template>
	<AppAlert :show="showalert" :error="showerror" :message="alertmessage" />

	<div class="erp-container">
		<!-- ?? ?ÅÎã® ?°ÏÖò Î∞?-->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-wallet2 me-2 text-primary" style="font-size: 18px;"></i>
				Í∏∞Î≥∏?ïÎ≥¥ <i class="bi bi-chevron-right mx-2 small opacity-50"></i>
				<span class="text-primary fw-bolder">Íµ¨Ï¢å?ïÎ≥¥ (haba110u)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">Ï¥àÍ∏∞??/button>
				<button class="btn-erp btn-search" @click="search">Ï°∞Ìöå</button>
				<button class="btn-erp btn-save" @click="save">?Ä??/button>
			</div>
		</div>

		<!-- ?îç Í≤Ä??Ï°∞Í±¥ ?ÅÏó≠ -->
		<div class="p-2 pb-0 flex-shrink-0 bg-light">
			<div class="card border shadow-sm overflow-hidden bg-white">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 250px;" />
						<col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center border-end bg-light">Í≥ÑÏ†ïÍ≥ºÎ™©</th>
							<td class="px-2">
								<div class="input-group input-group-sm">
									<input v-model="searchform.acctcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="searchform.acctcd_t" type="text" class="form-control" placeholder="Í≥ÑÏ†ï ?†ÌÉù" @keydown.enter="openhelp('search_acct')" />
									<button class="btn btn-outline-secondary px-2" @click="openhelp('search_acct')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<td class="px-3 text-muted small">
								<i class="bi bi-info-circle me-1"></i> Í≤Ä?âÌïò??Í≥ÑÏ†ï???†ÌÉù??Ï£ºÏã≠?úÏöî.
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- ?ìù ?ÅÏÑ∏ ?ïÎ≥¥ ?ÖÎ†• ?ÅÏó≠ -->
		<div class="p-2 pb-0 flex-shrink-0 bg-light">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-header py-1 px-2 bg-light border-bottom">
					<span class="small fw-bold text-secondary"><i class="bi bi-pencil-square me-1"></i> Íµ¨Ï¢å ?ÅÏÑ∏ ?ïÎ≥¥ [{{ masterdata.actkind === 'I1' ? '?†Í∑ú' : '?òÏ†ï' }}]</span>
				</div>
				<table class="erp-table-full border-0 small">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 20%;" />
						<col style="width: 100px;" /><col style="width: 20%;" />
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center bg-light-subtle border-end">Í≥ÑÏ†ïÍ≥ºÎ™©</th>
							<td class="bg-white border-end px-2 py-1">
								<input v-model="masterdata.acctcd_t" type="text" class="form-control form-control-sm bg-light" readonly />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">Í≥ÑÏ¢åÎ≤àÌò∏</th>
							<td class="bg-white border-end border-top px-2 py-1">
								<input v-model="masterdata.gujano" type="text" class="form-control form-control-sm" maxlength="20" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">Í∞úÏÑ§?Ä??/th>
							<td class="bg-white border-top px-2 py-1">
								<div class="input-group input-group-sm">
									<input v-model="masterdata.bankcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="masterdata.bankcd_t" type="text" class="form-control" @keydown.enter="openhelp('bank')" />
									<button class="btn btn-outline-secondary px-2" @click="openhelp('bank')"><i class="bi bi-search"></i></button>
								</div>
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">Í∞?nbsp;??nbsp;??/th>
							<td class="bg-white border-end border-top px-2 py-1">
								<input v-model="masterdata.stdymd" type="date" class="form-control form-control-sm" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">Îß?nbsp;Í∏?nbsp;??/th>
							<td class="bg-white border-end border-top px-2 py-1">
								<input v-model="masterdata.endymd" type="date" class="form-control form-control-sm" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">Í≥?nbsp;??nbsp;Í∏?/th>
							<td class="bg-white border-top px-2 py-1">
								<div class="input-group input-group-sm">
									<input v-model="masterdata.wonamt" type="number" class="form-control text-end" />
									<span class="input-group-text">??/span>
								</div>
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">??nbsp;&nbsp;&nbsp;&nbsp;??/th>
							<td class="bg-white border-end border-top px-2 py-1">
								<div class="input-group input-group-sm">
									<input v-model="masterdata.rate" type="number" class="form-control text-end" step="0.01" />
									<span class="input-group-text">%</span>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end border-top">?îÎ∂à?ÖÍ∏à</th>
							<td class="bg-white border-end border-top px-2 py-1">
								<div class="input-group input-group-sm">
									<input v-model="masterdata.payamt" type="number" class="form-control text-end" />
									<span class="input-group-text">??/span>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end border-top">Î∂?nbsp;??nbsp;??/th>
							<td class="bg-white border-top px-2 py-1">
								<div class="d-flex align-items-center gap-2">
									<input v-model="masterdata.paydd" type="text" class="form-control form-control-sm text-end" style="max-width: 60px;" maxlength="2" />
									<span class="fw-bold">??/span>
								</div>
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">Îπ?nbsp;&nbsp;&nbsp;&nbsp;Í≥?/th>
							<td colspan="3" class="bg-white border-end border-top px-2 py-1">
								<input v-model="masterdata.remark" type="text" class="form-control form-control-sm" maxlength="40" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">?¨Ïö©?¨Î?</th>
							<td class="bg-white border-top px-3">
								<div class="form-check form-switch m-0 pt-1">
									<input v-model="masterdata.useyn" class="form-check-input" type="checkbox" id="usecheck" true-value="Y" false-value="N">
									<label class="form-check-label small fw-bold" for="usecheck">?¨Ïö©</label>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- ?ìä Í∑∏Î¶¨???ÅÏó≠ -->
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
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import Modal from '@/components/Modal.vue'
import type { ModalProps } from '@/types/modal'

const authstore = useAuthStore()
const { showAlert: showalert, showError: showerror, alertMessage: alertmessage, vAlert: valert, vAlertError: valerterror } = useAlerts()
const { resetForm: resetform } = useFormReset()

// 1. ?ÅÌÉú Í¥ÄÎ¶?(???åÎ¨∏?? Í∞??ÄÎ¨∏Ïûê ?êÏπô)
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
	if (!searchform.acctcd_t) return valert('Í≤Ä?âÌïò??Í≥ÑÏ†ï???†ÌÉù??Ï£ºÏã≠?úÏöî.')
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
		valert('Ï°∞Ìöå?òÏóà?µÎãà??')
	} catch (e) { valerterror('Ï°∞Ìöå Ï§??§Î•ò Î∞úÏÉù') }
}

const save = async () => {
	if (!masterdata.acctcd) return valert('Í≥ÑÏ†ïÏΩîÎìúÎ•?Í∏∞Ïû¨??Ï£ºÏã≠?úÏöî.')
	if (!masterdata.gujano) return valert('Í≥ÑÏ¢åÎ≤àÌò∏Î•?Í∏∞Ïû¨??Ï£ºÏã≠?úÏöî.')
	if (!masterdata.bankcd) return valert('Í∞úÏÑ§?Ä?âÏùÑ Í∏∞Ïû¨??Ï£ºÏã≠?úÏöî.')

	if (!confirm('?Ä?•Ìïò?úÍ≤†?µÎãàÍπ?')) return
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
		if (resdata.ret_yn === 'Y' || resdata.result === 'N') valerterror(resdata.ret_msg || resdata.msg || '?Ä???§Ìå®')
		else { valert('?ïÏÉÅ Ï≤òÎ¶¨?òÏóà?µÎãà??'); search(); initialize() }
	} catch (e) { valerterror('?Ä???§Ìå®') }
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
			title: 'Í≥ÑÏ†ïÍ≥ºÎ™© ?†ÌÉù', path: '/api/ha00/ha00_00p_str',
			data: { gubun: 'A6', cmpycd: authstore.cmpycd, gbncd:'023' },
			columns: [{ title: 'ÏΩîÎìú', field: 'acctcd', width: 80 }, { title: 'Í≥ÑÏ†ïÎ™?, field: 'acctnm', width: 180 }],
			onConfirm: (d: any) => {
				const n = normalizekeys(d);
				searchform.acctcd = n.acctcd; searchform.acctcd_t = n.acctnm;
				initialize();
			}
		})
	} else if (type === 'bank') {
		if (!masterdata.acctcd) return valert('Ï°∞Ìöå ???ÖÎ†•?òÏãúÍ∏?Î∞îÎûç?àÎã§.')
		Object.assign(modalprops, {
			title: '?Ä???†ÌÉù', path: '/api/ha00/ha00_00p_str',
			data: { gubun: 'C3', cmpycd: authstore.cmpycd },
			columns: [{ title: 'ÏΩîÎìú', field: 'custcd', width: 80 }, { title: '?Ä?âÎ™Ö', field: 'custnm', width: 180 }],
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
				{ title: "Í≥ÑÏ¢åÎ≤àÌò∏", field: "gujano", width: 200, hozAlign: "center" },
				{ title: "Í∞úÏÑ§?Ä??, field: "banknm", minWidth: 180, hozAlign: "left" },
				{ title: "Í∞úÏÑ§??, field: "stdymd", width: 150, hozAlign: "center" },
				{ title: "ÎßåÍ∏∞??, field: "endymd", width: 150, hozAlign: "center" },
				{ title: "Í≥ÑÏïΩÍ∏?, field: "wonamt", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "?îÎ∂à?ÖÏï°", field: "payamt", width: 150, hozAlign: "right", formatter: "money", formatterParams: { precision: 0 } },
				{ title: "?¥Ïú®", field: "rate", width: 150, hozAlign: "right", formatter: (c) => c.getValue() + "%" },
                { title: "?¨Ïö©", field: "useyn", width: 80, hozAlign: "center",
                  formatter: (cell) => {
                    const val = String(cell.getValue() || '').trim().toUpperCase();
                    return val === 'Y' ? '<b class="text-primary">?¨Ïö©</b>' : '';
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
