<!--
	=============================================================
	?„ë¡œê·¸ë¨ëª?: ë²•ì¸ì¹´ë“œê´€ë¦?(haba140u)
	?‘ì„±?¼ì	: 2025.03.14
	?¤ëª…        : ë²•ì¸ì¹´ë“œ ?•ë³´ ?ì„¸ ê´€ë¦?(?„ì „ ?Œë¬¸???ì¹™ ?ìš©)
	=============================================================
-->

<template>
	<AppAlert :show="showalert" :error="showerror" :message="alertmessage" />

	<div class="erp-container">
		<!-- ?? ?ë‹¨ ?¡ì…˜ ë°?-->
		<div class="erp-header d-flex justify-content-between align-items-center border-bottom bg-white py-2 px-3 sticky-top shadow-sm flex-shrink-0">
			<div class="fw-bold text-dark d-flex align-items-center" style="font-size: 14px;">
				<i class="bi bi-credit-card me-2 text-primary" style="font-size: 18px;"></i>
				ê¸°ë³¸?•ë³´ <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
				<span class="text-primary fw-bolder">ë²•ì¸ì¹´ë“œ (haba140u)</span>
			</div>
			<div class="btn-group-erp d-flex gap-1">
				<button class="btn-erp btn-init" @click="initialize">
					<i class="bi bi-plus-lg"></i> ? ê·œ
				</button>
				<button class="btn-erp btn-search" @click="search">
					<i class="bi bi-search"></i> ì¡°íšŒ
				</button>
				<button class="btn-erp btn-save" @click="save">
					<i class="bi bi-check-lg"></i> ?€??
				</button>
			</div>
		</div>

		<!-- ?” ê²€??ì¡°ê±´ ?ì—­ -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm overflow-hidden bg-light">
				<table class="erp-table-full" style="table-layout: fixed;">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 250px;" />
						<col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center border-end">ë°???ì²?/th>
							<td class="bg-white border-end px-2">
								<div class="input-group input-group-sm">
									<input v-model="searchform.bankcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="searchform.bankcd_t" type="text" class="form-control" placeholder="ë°œí–‰ì²?? íƒ" @keydown.enter="openhelp('search_bank')" />
									<button class="btn btn-outline-secondary px-2" @click="openhelp('search_bank')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<td class="bg-white px-3 text-muted small">
								<i class="bi bi-info-circle me-1"></i> ê²€?‰í•˜??ë°œí–‰ì²˜ë? ? íƒ??ì£¼ì‹­?œìš”.
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- ?“ ?ì„¸ ?•ë³´ ?…ë ¥ ?ì—­ -->
		<div class="p-2 pb-0 flex-shrink-0">
			<div class="card border shadow-sm bg-white overflow-hidden">
				<div class="card-header py-1 px-2 bg-light border-bottom">
					<span class="small fw-bold text-secondary"><i class="bi bi-pencil-square me-1"></i> ë²•ì¸ì¹´ë“œ ?ì„¸ ?•ë³´ [{{ masterform.actkind === 'i1' ? '? ê·œ' : '?˜ì •' }}]</span>
				</div>
				<table class="erp-table-full small border-0">
					<colgroup>
						<col style="width: 100px;" /><col style="width: 20%;" />
						<col style="width: 100px;" /><col style="width: 20%;" />
						<col style="width: 100px;" /><col />
					</colgroup>
					<tbody>
						<tr>
							<th class="text-center bg-light-subtle border-end">ì¹´ë“œë²ˆí˜¸</th>
							<td class="bg-white border-end px-2 py-1">
								<input v-model="masterform.cardno" type="text" class="form-control form-control-sm" maxlength="19" :readonly="masterform.actkind === 'u1'" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">ë°???ì²?/th>
							<td class="bg-white border-end border-top px-2 py-1">
								<div class="input-group input-group-sm">
									<input v-model="masterform.bankcd" type="text" class="form-control text-center bg-light" style="max-width: 60px;" readonly />
									<input v-model="masterform.bankcd_t" type="text" class="form-control" @keydown.enter="openhelp('bank')" />
									<button class="btn btn-outline-secondary px-2" @click="openhelp('bank')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end border-top">ì¹???ëª?/th>
							<td class="bg-white border-top px-2 py-1">
								<input v-model="masterform.cardnm" type="text" class="form-control form-control-sm" maxlength="50" />
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">ë°?????/th>
							<td class="bg-white border-end border-top px-2 py-1">
								<input v-model="masterform.stdymd" type="date" class="form-control form-control-sm" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">? íš¨?„ì›”</th>
							<td class="bg-white border-end border-top px-2 py-1">
								<div class="d-flex align-items-center gap-1">
									<input v-model="masterform.sndymd_yy" type="text" class="form-control form-control-sm text-center" maxlength="4" style="width: 60px;" placeholder="yyyy" />
									<span class="small">??/span>
									<select v-model="masterform.sndymd_mm" class="form-select form-select-sm" style="width: 70px;">
										<option value="00">? íƒ</option>
										<option v-for="m in 12" :key="m" :value="String(m).padStart(2, '0')">{{ String(m).padStart(2, '0') }}</option>
									</select>
									<span class="small">??/span>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end border-top">ê²?????/th>
							<td class="bg-white border-top px-2 py-1">
								<div class="d-flex align-items-center gap-1">
									<select v-model="masterform.chkdd" class="form-select form-select-sm" style="width: 80px;">
										<option value="00">? íƒ</option>
										<option v-for="d in 31" :key="d" :value="String(d).padStart(2, '0')">{{ String(d).padStart(2, '0') }}</option>
									</select>
									<span class="small">??/span>
								</div>
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">??????/th>
							<td class="bg-white border-end border-top px-2 py-1">
								<input v-model="masterform.soname" type="text" class="form-control form-control-sm" maxlength="20" />
							</td>
							<th class="text-center bg-light-subtle border-end border-top">ê²°ì œê³„ì¢Œ</th>
							<td class="bg-white border-end border-top px-2 py-1">
								<div class="input-group input-group-sm">
									<input v-model="masterform.gujano" type="text" class="form-control" @keydown.enter="openhelp('guja')" />
									<button class="btn btn-outline-secondary px-2" @click="openhelp('guja')"><i class="bi bi-search"></i></button>
								</div>
							</td>
							<th class="text-center bg-light-subtle border-end border-top">?¬ìš©?¬ë?</th>
							<td class="bg-white border-top px-3">
								<div class="form-check pt-1">
									<input v-model="masterform.useyn" class="form-check-input" type="checkbox" id="usecheck" true-value="Y" false-value="N">
									<label class="form-check-label small fw-bold" for="usecheck">?¬ìš©</label>
								</div>
							</td>
						</tr>
						<tr>
							<th class="text-center bg-light-subtle border-end border-top">ë¹?nbsp;&nbsp;&nbsp;&nbsp;ê³?/th>
							<td colspan="5" class="bg-white border-top px-2 py-1">
								<input v-model="masterform.remark" type="text" class="form-control form-control-sm" maxlength="50" />
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>

		<!-- ?“Š ê·¸ë¦¬???ì—­ -->
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

// ?” ê²€???°ì´??
const searchform = reactive({
	bankcd: '',
	bankcd_t: ''
})

// ?“ ë§ˆìŠ¤???°ì´??
const masterform = reactive({
	actkind: 'i1',
	cardno: '',
	bankcd: '',
	bankcd_t: '',
	cardnm: '',
	stdymd: '',
	sndymd_yy: '',
	sndymd_mm: '00',
	chkdd: '00',
	soname: '',
	gujano: '',
	remark: '',
	useyn: 'Y'
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
	try {
		const res = await api.post('/api/haba/HABA_140U_STR', {
			actkind: searchform.bankcd_t ? 'SR' : 'S1',
			cmpycd: authstore.cmpycd,
			bankcd: searchform.bankcd
		})

		const processeddata = (res.data || []).map((r: any) => {
			const n = normalizekeys(r);
			return {
				cardno: n.cardno,
				bankcd: n.bankcd,
				banknm: n.banknm,
				cardnm: n.cardnm,
				stdymd: formatymd(n.stdymd),
				sndymd_yy: n.sndymd_yy,
				sndymd_mm: n.sndymd_mm,
				chkdd: n.chkdd,
				soname: n.soname,
				gujano: n.gujano,
				remark: n.remark,
				useyn: n.useyn
			}
		})

		maingrid?.setData(processeddata)
		if (processeddata.length > 0) valert('ì¡°íšŒ?˜ì—ˆ?µë‹ˆ??')
		else valert('?°ì´?°ê? ì¡´ì¬?˜ì? ?ŠìŠµ?ˆë‹¤.')
	} catch (e) { valerterror('ì¡°íšŒ ì¤??¤ë¥˜ ë°œìƒ') }
}

const save = async () => {
	if (!masterform.cardno) return valert('ì¹´ë“œë²ˆí˜¸ë¥??•í™•???…ë ¥?˜ì„¸??')
	if (!masterform.bankcd) return valert('ë°œí–‰ì²˜ë? ê¸°ì¬??ì£¼ì‹­?œìš”.')
	if (!masterform.cardnm) return valert('ì¹´ë“œëª…ì„ ê¸°ì¬??ì£¼ì‹­?œìš”.')
	if (!masterform.stdymd) return valert('ë°œí–‰?¼ìë¥?ê¸°ì¬??ì£¼ì‹­?œìš”.')
	if (!masterform.sndymd_yy || isNaN(Number(masterform.sndymd_yy))) return valert('? íš¨?„ë„ë¥??«ìë¡?ê¸°ì¬??ì£¼ì‹­?œìš”.')
	if (masterform.sndymd_mm === '00') return valert('? íš¨?”ì„ ê¸°ì¬??ì£¼ì‹­?œìš”.')
	if (masterform.chkdd === '00') return valert('ê²°ì œ?¼ì„ ê¸°ì¬??ì£¼ì‹­?œìš”.')
	if (!masterform.soname) return valert('?Œìœ ?ë? ê¸°ì¬??ì£¼ì‹­?œìš”.')
	if (!masterform.gujano) return valert('ê²°ì œê³„ì¢Œë¥?ê¸°ì¬??ì£¼ì‹­?œìš”.')

	try {
		const payload = {
			...masterform,
			cmpycd: authstore.cmpycd,
			userid: authstore.userid,
			stdymd: masterform.stdymd.replace(/-/g, ''),
			sndymd: masterform.sndymd_yy + masterform.sndymd_mm
		}

		const res = await api.post('/api/haba/HABA_140U_STR', payload)
		const resdata = normalizekeys(res.data?.[0]);

		if (resdata.ret_yn === 'Y' || resdata.result === 'N') {
			valerterror(resdata.ret_msg || resdata.msg || '?€???¤íŒ¨')
		} else {
			valert('?•ìƒ?¼ë¡œ ?‘ì—…???˜ì—ˆ?µë‹ˆ??')
			search()
			initialize()
		}
	} catch (e) { valerterror('?€???¤íŒ¨') }
}

const initialize = () => {
	resetform(masterform)
	masterform.actkind = 'i1'
	masterform.sndymd_mm = '00'
	masterform.chkdd = '00'
	masterform.useyn = 'Y'
	masterform.cmpycd = authstore.cmpycd
}

// ?ì—… ?¤ì •
const modalvisible = ref(false)
const modalprops = reactive<ModalProps>({ title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table' })

function openhelp(type: string) {
	if (type === 'search_bank') {

		Object.assign(modalprops, {
			title: 'ë°œí–‰ì²?? íƒ', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'C3', cmpycd: authstore.cmpycd, code: searchform.bankcd_t },
			columns: [{ title: 'ì½”ë“œ', field: 'bankcd', width: 80 }, { title: 'ê±°ë˜ì²˜ëª…', field: 'banknm', width: 180 }],
			onConfirm: (d: any) => {
				const n = normalizekeys(d);
				searchform.bankcd = n.bankcd; searchform.bankcd_t = n.banknm
			}
		})
	} else if (type === 'bank') {
		Object.assign(modalprops, {
			title: 'ë°œí–‰ì²?? íƒ', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'C3', cmpycd: authstore.cmpycd, code: masterform.bankcd_t },
			columns: [{ title: 'ì½”ë“œ', field: 'bankcd', width: 80 }, { title: 'ê±°ë˜ì²˜ëª…', field: 'banknm', width: 180 }],
			onConfirm: (d: any) => {
				const n = normalizekeys(d);
				masterform.bankcd = n.bankcd; masterform.bankcd_t = n.banknm
			}
		})
	} else if (type === 'guja') {

		Object.assign(modalprops, {
			title: 'ê²°ì œê³„ì¢Œ ? íƒ', path: '/api/ha00/HA00_00P_STR',
			data: { gubun: 'M0', cmpycd: authstore.cmpycd, code: masterform.gujano, gbncd: '010', remark: '1120' },
			columns: [{ title: 'ê³„ì¢Œë²ˆí˜¸', field: 'mgtno', width: 150 }, { title: 'ê´€ë¦¬í•­ëª©ëª…', field: 'mgtnm', width: 150 }],
			onConfirm: (d: any) => {
				const n = normalizekeys(d);
				masterform.gujano = n.mgtno
			}
		})
	}
	modalvisible.value = true
}

onMounted(() => {
	if (maingridelement.value) {
		maingrid = new Tabulator(maingridelement.value, {
			layout: 'fitColumns',
			height: '100%',
			columnDefaults: { headerSort: false, vertAlign: "middle", headerHozAlign: "center", hozAlign: "center" },
			columns: [
				{ title: "ì¹´ë“œë²ˆí˜¸", field: "cardno", width: 200, hozAlign: "left" },
				{ title: "ë°œí–‰ì²?, field: "banknm", width: 250, hozAlign: "left" },
				{ title: "ë°œí–‰??, field: "stdymd", width: 150 },
				{
					title: "? íš¨?„ì›”", field: "sndymd", width: 150,
					formatter: (c) => {
						const d = c.getData()
						return `${d.sndymd_yy}-${d.sndymd_mm}`
					}
				},
				{ title: "ê²°ì œ??, field: "chkdd", width: 150, formatter: (c) => c.getValue() + "?? },
				{ title: "?Œìœ ??, field: "soname", width: 150 },
				{ title: "ê²°ì œê³„ì¢Œ", field: "gujano", width: 200 },
				{ title: "ë¹„ê³ ", field: "remark", minWidth: 150, hozAlign: "left" },
                { title: "?¬ìš©", field: "useyn", width: 80, hozAlign: "center",
                  formatter: (cell) => {
                    const val = String(cell.getValue() || '').trim().toUpperCase();
                    return val === 'Y' ? '<b class="text-primary">?¬ìš©</b>' : '';
                  }
                }
			]
		})
		maingrid.on('rowClick', (e, row) => {
			const d = normalizekeys(row.getData())
			Object.assign(masterform, d)
			masterform.actkind = 'u1'
			masterform.bankcd_t = d.banknm
		})
	}
	nextTick(search);
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
</style>
