<!--
	=============================================================
	프로그램명	: 재무제표 차감수식 (haba022u)
	작성일자	: 2025.03.14
	설명        : 재무제표 충당금 계정의 차감 연산 수식 설정 및 관리 (완전 소문자 원칙 적용)
	=============================================================
-->

<template>
  <appalert :show="showalert" :error="showerror" :message="alertmessage" />
  <modal v-model:visible="modalvisible" :modalprops="modalprops" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom bg-white py-2 px-3 sticky-top shadow-sm">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-calculator-fill me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        장부관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">재무제표 차감수식 (haba022u)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">신규</button>
        <button class="btn-erp btn-search" @click="fetchleftgrid">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [상단] 조회 필터 영역 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense" width="100%">
            <colgroup>
                <col style="width: 100px;" /><col style="width: 150px;" />
                <col style="width: 100px;" /><col style="width: 250px;" />
                <col />
            </colgroup>
            <tbody>
              <tr>
                <th class="text-center bg-light">적용연도</th>
                <td>
                  <select v-model="searchform.yyyy" class="form-select form-select-sm" @change="fetchleftgrid">
                    <option v-for="y in yearoptions" :key="y" :value="y">{{ y }}년</option>
                  </select>
                </td>
                <th class="text-center bg-light">재무제표</th>
                <td>
                  <select v-model="searchform.gubun" class="form-select form-select-sm" @change="fetchleftgrid">
                    <option v-for="opt in statementoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <td class="px-3 text-muted small border-0 text-end pe-4">
                    <i class="bi bi-info-circle me-1"></i> 충당금 계정을 선택하여 수식을 설정하십시오.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 레이아웃 영역 (2분할) -->
      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">

        <!-- ⬅️ 좌측: 집계계정 목록 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 450px; min-width: 450px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">재무제표 집계항목</div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="leftgridref" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <!-- ➡️ 우측: 수식 편집 폼 + 상세 리스트 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">

          <!-- 수식 입력 폼 -->
          <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
            <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
                <span class="fw-bold small text-dark">
                    <i class="bi bi-pencil-square me-1 text-primary"></i> 수식 상세 설정
                    <span v-if="masterdata.upacct" class="ms-2 text-primary fw-bolder">[{{ masterdata.upacct_t }}]</span>
                </span>
            </div>
            <div class="card-body p-0 bg-white">
              <table class="erp-table-dense w-100">
                <colgroup>
                  <col style="width: 100px;" /><col style="width: 300px;" />
                  <col style="width: 100px;" /><col style="width: 180px;" />
                  <col style="width: 100px;" /><col style="width: 180px;" />
                  <col style="width: 100px;" /><col />
                </colgroup>
                <tbody>
                  <tr>
                    <th class="required bg-light">계정과목</th>
                    <td>
                      <div class="input-group input-group-sm">
                        <input v-model="masterdata.acctcd" type="text" class="form-control text-center bg-light fw-bold" style="max-width: 80px;" readonly />
                        <input v-model="masterdata.acctcd_t" type="text" class="form-control" placeholder="계정 선택" @keydown.enter="openhelp('acct')" />
                        <button class="btn btn-outline-secondary" @click="openhelp('acct')"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="required bg-light">연산대상</th>
                    <td>
                      <select v-model="masterdata.caltype" class="form-select form-select-sm">
                        <option value="000">선택하세요</option>
                        <option v-for="opt in calctypeoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                      </select>
                    </td>
                    <th class="required bg-light">연산수식</th>
                    <td>
                      <select v-model="masterdata.calgagam" class="form-select form-select-sm">
                        <option value="000">선택하세요</option>
                        <option v-for="opt in formulaoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                      </select>
                    </td>
                    <th class="bg-light">사용</th>
                    <td>
                      <div class="d-flex align-items-center justify-content-center h-100">
                        <div class="form-check form-switch m-0 ms-2">
                            <input v-model="masterdata.useyn" class="form-check-input" type="checkbox" id="usecheck" true-value="y" false-value="n">
                        </div>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 상세 리스트 그리드 -->
          <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden grid-container-right">
            <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">세부 구성 수식 리스트</div>
            <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
              <div ref="rightgridref" class="tabulator-instance flex-grow-1"></div>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import Modal from '@/components/Modal.vue'
import AppAlert from '@/components/AppAlert.vue'
import { useCommonHelp } from '@/composables/useCommonHelp'
import type { ModalProps } from '@/types/modal'

const authstore = useAuthStore()
const { showAlert: showalert, showError: showerror, alertMessage: alertmessage, vAlert: valert, vAlertError: valerterror } = useAlerts()
const { resetForm: resetform } = useFormReset()
const { modalVisible: modalvisible, modalProps: modalprops, openHelp: commonopenhelp } = useCommonHelp()

const yearoptions = ref<string[]>([])
const searchform = reactive({ yyyy: new Date().getFullYear().toString(), gubun: '010' })
const masterdata = reactive<any>({
    actkind: 'i1', yyyy: '', gubun: '', upacct: '', upacct_t: '',
    acctcd: '', acctcd_t: '', arowno: '', caltype: '000', calgagam: '000', useyn: 'Y', updyn: 'Y', rstyn: 'N'
})

const statementoptions = ref<any[]>([]); const calctypeoptions = ref<any[]>([]); const formulaoptions = ref<any[]>([])
const leftgridref = ref<HTMLDivElement | null>(null); const rightgridref = ref<HTMLDivElement | null>(null)
let leftgrid: Tabulator | null = null; let rightgrid: Tabulator | null = null

const normalizekeys = (obj: any) => {
  const n: any = {}; if (!obj) return n;
  Object.keys(obj).forEach(k => n[k.toLowerCase()] = typeof obj[k] === 'string' ? obj[k].trim() : obj[k]);
  return n;
}

const loadinitdata = async () => {
	try {
        const resyear = await api.post('/api/ha00/ha00_00p_str', { gubun: 'y0', cmpycd: authstore.cmpycd })
        yearoptions.value = (resyear.data || []).map((r: any) => {
            const n = normalizekeys(r);
            return (n.yyyy || n.col0 || '').toString()
        }).filter(y => y !== '')
        if (yearoptions.value.length > 0) searchform.yyyy = yearoptions.value[0];
        else yearoptions.value = [new Date().getFullYear().toString()]

		const resgbn = await api.post('/api/ha00/ha00_00p_str', { gubun: 'e0', gbncd: '070' })
		statementoptions.value = (resgbn.data || []).map((r: any) => normalizekeys(r))
        if (statementoptions.value.length > 0) searchform.gubun = statementoptions.value[0].codecd;

		const restype = await api.post('/api/ha00/ha00_00p_str', { gubun: 'e0', gbncd: '080' })
		calctypeoptions.value = (restype.data || []).map((r: any) => normalizekeys(r))

		const resformula = await api.post('/api/ha00/ha00_00p_str', { gubun: 'e0', gbncd: '090' })
		formulaoptions.value = (resformula.data || []).map((r: any) => normalizekeys(r))

        nextTick(() => fetchleftgrid());
	} catch (e) { console.error('초기 데이터 로드 실패') }
}

const fetchleftgrid = async () => {
	try {
		const res = await api.post('/api/haba/haba_022u_str', { actkind: 's3', cmpycd: authstore.cmpycd, yyyy: searchform.yyyy, gubun: searchform.gubun })
		leftgrid?.setData((res.data || []).map((r: any) => {
            const n = normalizekeys(r);
            return {
                ...n,
                gubun_nm: n.gubun_nm || (n.actgbn === '1' ? '집계' : '상세')
            }
		}));
        rightgrid?.clearData();
        initialize();
	} catch (e) { valerterror('목록 조회 실패') }
}

const fetchrightgrid = async (row: any) => {
	try {
		const res = await api.post('/api/haba/haba_022u_str', { actkind: 'sr', cmpycd: authstore.cmpycd, yyyy: searchform.yyyy, gubun: row.gubun, upacct: row.upacct })
		rightgrid?.setData((res.data || []).map((r: any) => {
            const n = normalizekeys(r);
            return {
                ...n,
                caltype_nm: n.caltypenm,
                calgagam_nm: n.calgagamnm
            }
        }));
	} catch (e) { valerterror('수식 내역 조회 실패') }
}

const save = async () => {
	if (masterdata.updyn !== 'Y') return valerterror('수정할 수 없는 항목입니다.')
	if (masterdata.rstyn !== 'Y') return valerterror('충당금계정(결과) 항목만 등록 가능합니다.')
	if (!masterdata.upacct) return valerterror('좌측 집계항목을 선택하십시오.')
	if (!masterdata.acctcd) return valerterror('계정과목을 선택하십시오.')
	if (masterdata.caltype === '000') return valerterror('연산대상을 선택하십시오.')
	if (masterdata.calgagam === '000') return valerterror('연산수식을 선택하십시오.')

	try {
		masterdata.yyyy = searchform.yyyy; masterdata.gubun = searchform.gubun
		const res = await api.post('/api/haba/haba_022u_save', { ...masterdata, cmpycd: authstore.cmpycd, userid: authstore.userid })
        const resdata = normalizekeys(res.data?.[0]);
		if (resdata.ret_yn === 'N') { valerterror(resdata.ret_msg) }
        else { valert('저장되었습니다.'); fetchrightgrid({ gubun: masterdata.gubun, upacct: masterdata.upacct }); initialize(); }
	} catch (e) { valerterror('저장 실패') }
}

const initialize = () => {
    const curupacct = masterdata.upacct; const curupacctt = masterdata.upacct_t;
    const currstyn = masterdata.rstyn; const curupdyn = masterdata.updyn; const curgubun = masterdata.gubun;
    resetform(masterdata);
    Object.assign(masterdata, {
        actkind: 'i1', cmpycd: authstore.cmpycd, useyn: 'Y',
        upacct: curupacct, upacct_t: curupacctt, rstyn: currstyn, updyn: curupdyn, gubun: curgubun,
        caltype: '000', calgagam: '000'
    });
}

function openhelp(type: string) {
    if (type === 'acct') {
        commonopenhelp('ACCT', (d) => {
            const n = normalizekeys(d);
            masterdata.acctcd = n.acctcd; masterdata.acctcd_t = n.acctnm;
        });
    }
}

const checkbuttonformatter = (cell: any) => {
    const ischecked = String(cell.getValue() || '').trim().toLowerCase() === 'Y';
    const icon = ischecked ? 'bi-check-square-fill text-primary' : 'bi-square text-muted opacity-50';
    return `<div class="d-flex align-items-center justify-content-center h-100 w-100" style="font-size: 1.15rem;">
                <i class="bi ${icon}"></i>
            </div>`;
}

onMounted(async () => {
	if (leftgridref.value) {
		leftgrid = new Tabulator(leftgridref.value, {
			layout: 'fitColumns', height: '100%',
			columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
			columns: [
				{ title: "코드", field: "upacct", width: 130 },
				{
					title: "집계계정명", field: "acctnm", minWidth: 200, widthGrow: 1,
					formatter: (cell) => {
						const d = normalizekeys(cell.getData()); const code = String(d.upacct);
                        let indent = code.endsWith("00000") ? 0 : code.endsWith("0000") ? 15 : code.endsWith("00") ? 30 : 45;
						const color = d.rstyn === 'Y' ? '#0d6efd' : '#212529';
						return `<div style="padding-left: ${indent}px; color: ${color}; font-weight: ${d.rstyn === 'Y' ? '800' : 'normal'}">${indent > 0 ? '· ' : ''}${cell.getValue()}</div>`
					}
				},
				{ title: "구분", field: "gubun_nm", width: 120 }
			]
		});
		leftgrid.on('rowClick', (e, row) => {
            const d = normalizekeys(row.getData());
            masterdata.upacct = d.upacct; masterdata.upacct_t = d.acctnm; masterdata.gubun = d.gubun; masterdata.updyn = d.updyn; masterdata.rstyn = d.rstyn;
            fetchrightgrid(d); initialize();
        });
	}
	if (rightgridref.value) {
		rightgrid = new Tabulator(rightgridref.value, {
			layout: 'fitColumns', height: '100%', selectable: 1,
			columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },
			columns: [
				{ title: "계정코드", field: "acctcd", width: 130 },
				{ title: "계정과목명", field: "acctnm", minWidth: 200, widthGrow: 1, cssClass: "fw-bold", hozAlign: "left" },
                { title: "연산대상", field: "caltype_nm", width: 130 },
                { title: "연산수식", field: "calgagam_nm", width: 130 },
                { title: "사용", field: "useyn", width: 80, formatter: (c) => c.getValue() === 'Y' ? 'O' : 'X' }
			]
		});
        rightgrid.on("rowClick", (e, row) => {
            const d = normalizekeys(row.getData()); Object.assign(masterdata, d);
            masterdata.acctcd_t = d.acctnm; masterdata.actkind = 'u1';
        });
	}
    await loadinitdata()
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
:deep(.tabulator-cell) { display: flex !important; align-items: center !important; padding: 0 8px !important; min-height: 40px !important; }
:deep(.tabulator-cell.tabulator-align-center) { justify-content: center !important; text-align: center !important; }
:deep(.tabulator-cell.tabulator-align-right) { justify-content: flex-end !important; text-align: right !important; }
:deep(.tabulator-cell.tabulator-align-left) { justify-content: flex-start !important; text-align: left !important; }
:deep(.tabulator-col-title) { text-align: center !important; width: 100%; }
</style>
