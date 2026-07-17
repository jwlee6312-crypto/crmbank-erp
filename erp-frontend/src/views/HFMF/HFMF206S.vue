<!--
	=============================================================
	?ÑÎ°úÍ∑∏Îû®Î™?: Í≥µÏ†ïÎ≥ÑÏ†úÏ°∞ÎπÑ??Î∞∞Î???(HFMF206S)
	?ëÏÑ±?ºÏûê	: 2025.02.24
	?§Î™Ö        : Í≥ÑÏ†ïÍ≥ºÎ™©Î≥?Í≥µÏ†ï Î∞∞Î? ?ÑÌô© Ï∂úÎ†• (HSOD100U ?úÏ? UI ?ÅÏö©)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- ?? 1. ?ÅÎã® ?°ÏÖò Î∞?-->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-file-earmark-bar-graph me-2 text-primary" style="font-size: 18px;"></i>
        ?êÍ?Í¥ÄÎ¶?<i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        ?ÑÌô©Ï°∞Ìöå <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">Í≥µÏ†ïÎ≥ÑÏ†úÏ°∞ÎπÑ??Î∞∞Î???(HFMF206S)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-search" @click="handleSearch">Ï°∞Ìöå</button>
      </div>
    </div>

    <!-- ?í° 2. Î©îÏù∏ Ïª®ÌÖêÏ∏??ÅÏó≠ -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [?ÅÎã®] Ï°∞Ìöå ?ÑÌÑ∞ ?ÅÏó≠ -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
                <col style="width: 10%" /><col style="width: 40%" />
                <col style="width: 10%" /><col style="width: 40%" />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light required">Ï°∞Ìöå?ÑÏõî</th>
                <td class="d-flex align-items-center gap-1 border-0" style="height: 32px;">
                  <select v-model="searchForm.yyyy" class="form-select form-select-sm" style="width: 100px;">
                    <option v-for="year in yearOptions" :key="year" :value="year">{{ year }}??/option>
                  </select>
                  <select v-model="searchForm.mm" class="form-select form-select-sm" style="width: 80px;">
                    <option v-for="month in monthOptions" :key="month" :value="month">{{ month }}??/option>
                  </select>
                </td>
                <th class="text-center bg-light">ÎßàÍ∞ê?ÅÌÉú</th>
                <td>
                  <span v-if="clsInfo.wclsym" class="badge bg-danger-subtle text-danger border border-danger-subtle">ÎßàÍ∞ê?? {{ clsInfo.wclsym }}</span>
                  <span v-else class="text-muted small">ÎØ∏ÎßàÍ∞?/span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [?òÎã®] Î≥¥Í≥†???ÅÏó≠ (HTML Table) -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column bg-white">
        <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">
          <i class="bi bi-table me-2 text-primary"></i>Í≥µÏ†ïÎ≥??úÏ°∞ÎπÑÏö© Î∞∞Î? ?ÅÏÑ∏
        </div>
        <div class="card-body p-0 overflow-auto flex-grow-1">
          <table class="report-table">
            <thead class="sticky-top bg-light">
              <tr>
                <th style="width: 15%;">Í≥ÑÏ†ïÍ≥ºÎ™©</th>
                <th v-for="(h, idx) in processHeaders" :key="idx" style="width: 7.7%;">{{ h }}</th>
                <th v-for="n in (10 - processHeaders.length)" :key="'h-empty-'+n" style="width: 7.7%;"></th>
                <th style="width: 8%;">?©Í≥Ñ</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="reportData.length === 0">
                <td colspan="12" class="text-center py-5 text-muted">Ï°∞Ìöå???∞Ïù¥?∞Í? ?ÜÏäµ?àÎã§.</td>
              </tr>
              <tr v-for="(row, idx) in reportData" :key="idx" class="data-row">
                <td class="text-start ps-3 fw-bold" :class="{'bg-light-yellow': row.lev === '2'}" v-html="formatAcctNm(row)"></td>
                <td v-for="(amt, aIdx) in row.processamts" :key="aIdx" class="text-end pe-2">
                  {{ formatNumber(amt) }}
                </td>
                <td v-for="n in (10 - row.processamts.length)" :key="'empty-'+n" class="text-end pe-2"></td>
                <td class="text-end pe-2 fw-bold text-primary bg-primary-subtle">
                  {{ formatNumber(row.totalamt) }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useAlerts } from '@/composables/useAlerts'
import AppAlert from '@/components/AppAlert.vue'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
const { showAlert, showError, alertMessage, vAlertError } = useAlerts()

const currentYear = new Date().getFullYear()
const yearOptions = Array.from({ length: 6 }, (_, i) => String(currentYear - i))
const monthOptions = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0'))

const searchForm = reactive({
	yyyy: String(currentYear),
	mm: String(new Date().getMonth() + 1).padStart(2, '0')
})

const clsInfo = reactive({ wclsym: '' })
const reportData = ref<any[]>([])
const processHeaders = ref<string[]>([])

const formatNumber = (val: any) => {
	if (!val || isNaN(Number(val)) || Number(val) === 0) return ''
	return Number(val).toLocaleString()
}

const formatAcctNm = (row: any) => {
	const acct = row.acct || ''
	const lev = String(row.lev || '')
	let nm = row.acctnm || ''
	if (lev === '2' && acct === '412-0000') return `?? ${nm}`
	if (lev === '2' && acct === '413-0000') return `?? ${nm}`
	if (lev === '5' && acct === '414-0000') return nm
	return `&nbsp;&nbsp;&nbsp;&nbsp;${nm}`
}

const handleSearch = async () => {
	try {
		const ym = `${searchForm.yyyy}${searchForm.mm}`
		const { data } = await api.post('/api/hfmf/FMF2060R_STR', {
			cmpycd: authStore.cmpycd,
			actkind: 'S0',
			ym: ym,
			costcd: '10000'
		})

		const grouped: any[] = []
		let currentAcct = ''
		let currentRow: any = null
		const headerSet: Set<string> = new Set()

		data.forEach((item: any) => {
			if (item.prognm) headerSet.add(item.prognm)
			if (item.acct !== currentAcct) {
				if (currentRow) grouped.push(currentRow)
				currentAcct = item.acct
				currentRow = {
					acct: item.acct, acctnm: item.acctnm, lev: String(item.lev || ''),
					processamts: [], totalamt: 0
				}
			}
			const amt = Number(item.totamt || 0)
			currentRow.processamts.push(amt)
			currentRow.totalamt += amt
		})
		if (currentRow) grouped.push(currentRow)

		processHeaders.value = Array.from(headerSet).slice(0, 10)
		reportData.value = grouped
	} catch (e) {
		vAlertError('Ï°∞Ìöå Ï§??§Î•òÍ∞Ä Î∞úÏÉù?àÏäµ?àÎã§.')
	}
}

onMounted(async () => {
	try {
		const { data } = await api.get('/api/hp00/HP00_000S_STR', {
			params: { gubun: 'CL', cmpycd: authStore.cmpycd }
		})
		if (data?.length > 0) clsInfo.wclsym = data[0].wclsym || ''
	} catch (e) {}
	handleSearch()
})
</script>

<style scoped>
.report-table { width: 100%; border-collapse: collapse; font-size: 12px; }
.report-table th, .report-table td { border: 1px solid #dee2e6; padding: 8px 4px; height: 35px; vertical-align: middle; }
.report-table th { background-color: #f8f9fa; font-weight: bold; text-align: center; }
.data-row:hover { background-color: #f1f8ff; }
.bg-light-yellow { background-color: #fffde7 !important; }
</style>
