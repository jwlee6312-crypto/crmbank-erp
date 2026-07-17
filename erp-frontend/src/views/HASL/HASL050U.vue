<!--
	=============================================================
	프로그램명	: 자동분개전표입력 (hasl050u)
	작성일자	: 2025.02.24
	설명        : 자동분개 유형 또는 기존 전표를 불러와서 편집/등록 (hasl010u 기능 완벽 이식)
	=============================================================
-->

<template>
  <appalert :show="showalert" :error="showerror" :message="alertmessage" />
  <appmodal v-model:visible="modalvisible" :modal-props="modalprops" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-lightning-charge me-2 text-primary" style="font-size: 18px;"></i>
        전표관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">자동분개전표입력 (hasl050u)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-2">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- 상세 마스터 폼 -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
              <col style="width: 100px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required bg-light text-center">발행부서</th>
                <td>
                  <div class="input-group input-group-sm">
                    <input v-model="masterform.deptnm" class="form-control" readonly />
                    <button class="btn btn-outline-secondary px-2" @click="handle_open_help('DEPT')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
                <th class="bg-light text-center">전표번호</th>
                <td>
                  <div class="d-flex gap-1">
                    <input v-model="masterform.slipymd" type="date" class="form-control" />
                    <input v-model="masterform.slipno" class="form-control text-center bg-light fw-bold" readonly style="width: 60px;" />
                  </div>
                </td>
                <th class="bg-light text-center">발행인</th>
                <td><input v-model="masterform.empnm" class="form-control bg-light" readonly /></td>
              </tr>
              <tr>
                <th class="required bg-light text-center border-top">거래내역</th>
                <td colspan="3" class="border-top">
                    <input v-model="masterform.business" class="form-control fw-bold text-primary" placeholder="전표 거래내역 입력" @input="syncremarktorows" />
                </td>
                <th class="bg-light text-center border-top">회계일자</th>
                <td class="border-top"><input v-model="masterform.acctymd" class="form-control bg-light" readonly /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 분개 그리드 & 관리항목 사이드 패널 -->
      <div class="flex-grow-1 d-flex gap-2 overflow-hidden">

        <!-- (중앙) 분개 그리드 -->
        <div class="card border shadow-sm flex-grow-1 d-flex flex-column overflow-hidden grid-container-right">
          <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
            <span class="fw-bold small text-dark"><i class="bi bi-list-columns me-2 text-primary"></i>분개 상세</span>
            <button class="btn btn-sm btn-primary py-0 px-2 fw-bold" @click="add_row" style="font-size: 11px;">+ 행추가</button>
          </div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="maingridref" class="tabulator-instance flex-grow-1"></div>
          </div>
          <div class="card-footer p-0 border-top bg-dark text-white">
            <div class="row g-0 text-end fw-bold small py-1 px-3">
              <div class="col-4 text-center border-end border-secondary">분개 합계</div>
              <div class="col-2 text-info border-end border-secondary pe-2">{{ format_money(total_debit) }}</div>
              <div class="col-2 text-warning border-end border-secondary pe-2">{{ format_money(total_credit) }}</div>
              <div class="col-4" :class="balance === 0 ? 'text-success' : 'text-danger'">차액: {{ format_money(balance) }}</div>
            </div>
          </div>
        </div>

        <!-- (우측) 상세 관리항목 설정 사이드 바 -->
        <div class="card border shadow-sm flex-shrink-0 d-flex flex-column bg-white overflow-hidden side-panel-wrapper" style="width: 440px;" v-if="selectedrow">
          <div class="card-header py-2 px-3 bg-secondary text-white small fw-bold d-flex justify-content-between align-items-center">
             <span><i class="bi bi-gear-fill me-2"></i>상세 관리항목 설정</span>
             <span class="badge" :class="String(selectedrow.dbcr || '').trim().toUpperCase() === 'D' ? 'bg-primary' : 'bg-danger'">{{ String(selectedrow.dbcr || '').trim().toUpperCase() === 'D' ? '차변' : '대변' }}</span>
          </div>
          <div class="card-body p-0 overflow-auto custom-scrollbar flex-grow-1">

             <div class="p-2 bg-light border-bottom d-flex align-items-center gap-2 small sticky-top">
                <span class="badge bg-white text-dark border px-2 py-1">{{ selectedrow.acctcd }}</span>
                <b class="text-primary">{{ selectedrow.acctnm || '계정 미선택' }}</b>
             </div>

             <table class="erp-table-dense side-detail-table w-100 border-0">
                <colgroup><col style="width: 100px;" /><col /></colgroup>
                <tbody>
                    <!-- [1] 접대비 상세 (typeacct: 100) -->
                    <template v-if="String(selectedrow.typeacct || '').trim() === '100'">
                        <tr>
                            <th class="bg-light border-bottom text-end pe-3 required">유&nbsp;&nbsp;&nbsp;&nbsp;형</th>
                            <td class="border-bottom">
                                <select v-model="selectedrow.docno3" class="form-select form-select-sm border-0">
                                    <option v-for="opt in enttypeoptions" :key="opt.code" :value="opt.code">{{ opt.name }}</option>
                                </select>
                            </td>
                        </tr>
                        <tr>
                            <th class="bg-light border-bottom text-end pe-3 required">접대일자</th>
                            <td class="border-bottom">
                                <input v-model="selectedrow.docno6" type="date" class="form-control form-control-sm border-0" />
                            </td>
                        </tr>
                        <tr>
                            <th class="bg-light border-bottom text-end pe-3">사업번호</th>
                            <td class="border-bottom">
                                <input v-model="selectedrow.docno2" class="form-control form-control-sm border-0" placeholder="숫자만 입력" />
                            </td>
                        </tr>
                        <tr>
                            <th class="bg-light border-bottom text-end pe-3 text-primary">상&nbsp;&nbsp;&nbsp;&nbsp;호</th>
                            <td class="border-bottom">
                                <div class="input-group input-group-sm">
                                    <input v-model="selectedrow.docno4" class="form-control border-0" placeholder="상호명 직접입력 또는 검색" />
                                    <button class="btn btn-outline-secondary border-0 border-start px-2" @click="handle_open_help('vat_cust')"><i class="bi bi-search"></i></button>
                                </div>
                            </td>
                        </tr>
                        <tr v-if="String(selectedrow.docno3 || '').trim() === '01'">
                            <th class="bg-light border-bottom text-end pe-3 text-primary fw-bold">카&nbsp;&nbsp;&nbsp;&nbsp;드</th>
                            <td class="border-bottom">
                                <div class="input-group input-group-sm">
                                    <input v-model="selectedrow.docno5" class="form-control border-0 text-primary" placeholder="카드번호 도움창 선택" />
                                    <button class="btn btn-outline-secondary border-0 border-start px-2" @click="handle_open_help('CARD')"><i class="bi bi-credit-card"></i></button>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <th class="bg-light border-bottom text-end pe-3 fw-bold text-primary">순&nbsp;접&nbsp;대&nbsp;비</th>
                            <td class="border-bottom">
                                <input v-model="selectedrow.docno8" type="number" class="form-control form-control-sm border-0 text-end fw-bold" placeholder="0" />
                            </td>
                        </tr>
                        <tr>
                            <th class="bg-light border-bottom text-end pe-3">봉&nbsp;사&nbsp;료</th>
                            <td class="border-bottom">
                                <input v-model="selectedrow.docno9" type="number" class="form-control form-control-sm border-0 text-end" placeholder="0" />
                            </td>
                        </tr>
                    </template>

                    <!-- [2] 부가세 상세 (typeacct: 090) -->
                    <template v-else-if="String(selectedrow.typeacct || '').trim() === '090'">
                        <template v-if="String(selectedrow.typedc || '').trim().toUpperCase() === String(selectedrow.dbcr || '').trim().toUpperCase()">
                            <tr>
                                <th class="bg-light border-bottom text-end pe-3 required">사&nbsp;업&nbsp;장</th>
                                <td class="border-bottom">
                                    <select v-model="selectedrow.docno1" class="form-select form-select-sm border-0">
                                        <option value="">-- 선택 --</option>
                                        <option v-for="opt in bizplaceoptions" :key="opt.code" :value="opt.code">{{ opt.name }}</option>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <th class="bg-light border-bottom text-end pe-3 required">유&nbsp;&nbsp;&nbsp;&nbsp;형</th>
                                <td class="border-bottom">
                                    <select v-model="selectedrow.docno3" class="form-select form-select-sm border-0">
                                        <option value="">유형 선택</option>
                                        <option v-for="opt in dynamicvatoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <th class="bg-light border-bottom text-end pe-3 required">거&nbsp;래&nbsp;처</th>
                                <td class="border-bottom">
                                    <div class="input-group input-group-sm">
                                        <input v-model="selectedrow.docno2nm" class="form-control border-0" readonly placeholder="거래처 도움창 선택" />
                                        <button class="btn btn-outline-secondary border-0 border-start px-2" @click="handle_open_help('vat_cust')"><i class="bi bi-search"></i></button>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <th class="bg-light border-bottom text-end pe-3 required">발&nbsp;행&nbsp;일</th>
                                <td class="border-bottom">
                                    <input v-model="selectedrow.docno6" type="date" class="form-control form-control-sm border-0" />
                                </td>
                            </tr>
                            <tr>
                                <th class="bg-light border-bottom text-end pe-3 fw-bold text-primary">공&nbsp;급&nbsp;가</th>
                                <td class="border-bottom">
                                    <input v-model="selectedrow.docno8" type="number" class="form-control form-control-sm border-0 text-end fw-bold" @input="calc_vat" />
                                </td>
                            </tr>
                            <tr>
                                <th class="bg-light border-bottom text-end pe-3 fw-bold text-danger">부&nbsp;가&nbsp;세</th>
                                <td class="border-bottom">
                                    <input v-model="selectedrow.docno9" type="number" class="form-control form-control-sm border-0 text-end fw-bold" />
                                </td>
                            </tr>
                            <tr v-if="String(selectedrow.dbcr || '').trim().toUpperCase() === 'D'">
                                <th class="bg-light border-bottom text-end pe-3 text-primary fw-bold">카&nbsp;&nbsp;&nbsp;&nbsp;드</th>
                                <td class="border-bottom">
                                    <div class="input-group input-group-sm">
                                        <input v-model="selectedrow.docno4" class="form-control border-0 text-primary" placeholder="카드번호 선택" />
                                        <button class="btn btn-outline-secondary border-0 border-start px-2" @click="handle_open_help('CARD')"><i class="bi bi-credit-card"></i></button>
                                    </div>
                                </td>
                            </tr>
                        </template>
                    </template>

                    <!-- [3] 어음 상세 (050, 060, 070) -->
                    <template v-else-if="['050', '060', '070'].includes(String(selectedrow.typeacct || '').trim())">
                        <tr>
                            <th class="bg-light border-bottom text-end pe-3 required">어음번호</th>
                            <td class="border-bottom">
                                <div class="input-group input-group-sm">
                                    <input v-model="selectedrow.mgtno" class="form-control border-0" @keydown.enter="handle_open_help('MGT')" placeholder="번호 입력" />
                                    <button class="btn btn-outline-secondary border-0 border-start px-2" @click="handle_open_help('MGT')"><i class="bi bi-search"></i></button>
                                </div>
                            </td>
                        </tr>
                        <tr v-if="(String(selectedrow.typeacct || '').trim() === '050' && String(selectedrow.dbcr || '').trim().toUpperCase() === 'D') || (String(selectedrow.typeacct || '').trim() === '060' && String(selectedrow.dbcr || '').trim().toUpperCase() === 'C')">
                            <th class="bg-light border-bottom text-end pe-3">지급/수취처</th>
                            <td class="border-bottom">
                                <div class="input-group input-group-sm">
                                    <input v-model="selectedrow.docno9nm" class="form-control border-0" readonly placeholder="도움창 선택" />
                                    <button class="btn btn-outline-secondary border-0 border-start px-2" @click="handle_open_help('vat_cust')"><i class="bi bi-search"></i></button>
                                </div>
                            </td>
                        </tr>
                        <tr v-if="(String(selectedrow.typeacct || '').trim() === '050' && String(selectedrow.dbcr || '').trim().toUpperCase() === 'D') || (String(selectedrow.typeacct || '').trim() === '060' && String(selectedrow.dbcr || '').trim().toUpperCase() === 'C')">
                            <th class="bg-light border-bottom text-end pe-3">만기일자</th>
                            <td class="border-bottom">
                                <input v-model="selectedrow.docno7" type="date" class="form-control form-control-sm border-0" />
                            </td>
                        </tr>
                    </template>

                    <!-- [4] 일반 관리항목 / 거래처 -->
                    <template v-else>
                        <tr v-if="String(selectedrow.typemgt || '').trim() > '000'">
                            <th class="bg-light border-bottom text-end pe-3 text-primary">
                                {{
                                    String(selectedrow.typemgt || '').trim() === '010' ? '구좌번호' :
                                    String(selectedrow.typemgt || '').trim() === '020' ? '차입금' :
                                    String(selectedrow.typemgt || '').trim() === '030' ? '유가증권' :
                                    String(selectedrow.typemgt || '').trim() === '040' ? '법인카드' : (selectedrow.titmgt || selectedrow.typemgtnm || '관리항목')
                                }}
                            </th>
                            <td class="border-bottom">
                                <div class="input-group input-group-sm">
                                    <input v-model="selectedrow.mgtno" class="form-control border-0" @keydown.enter="handle_open_help('MGT')" :placeholder="(String(selectedrow.typemgt || '').trim() >= '010' && String(selectedrow.typemgt || '').trim() <= '040' ? '코드' : '번호') + ' 입력'" />
                                    <button class="btn btn-outline-secondary border-0 border-start px-2" @click="handle_open_help('MGT')"><i class="bi bi-search"></i></button>
                                </div>
                                <div class="small text-primary mt-1 px-1 fw-bold" v-if="selectedrow.mgtnm">{{ selectedrow.mgtnm }}</div>
                            </td>
                        </tr>
                        <tr v-if="String(selectedrow.typesub || '').trim() > '000' && (String(selectedrow.typemgt || '').trim() <= '000' || String(selectedrow.typemgt || '').trim() === '090')">
                            <th class="bg-light border-bottom text-end pe-3 text-primary">{{ selectedrow.titsub || selectedrow.typesubnm || '거래처' }}</th>
                            <td class="border-bottom">
                                <div class="input-group input-group-sm">
                                    <input v-model="selectedrow.subnm" class="form-control border-0" readonly placeholder="도움창 선택" />
                                    <button class="btn btn-outline-secondary border-0 border-start px-2" @click="handle_open_help('SUB')"><i class="bi bi-search"></i></button>
                                </div>
                            </td>
                        </tr>
                    </template>

                    <!-- [5] 프로젝트/예산 공통 (acctcd > 40000) -->
                    <template v-if="String(selectedrow.acctcd || '').trim() > '40000'">
                        <tr>
                            <th class="bg-light border-bottom text-end pe-3">프로젝트</th>
                            <td class="border-bottom">
                                <div class="input-group input-group-sm">
                                    <input v-model="selectedrow.prjnm" class="form-control border-0" readonly />
                                    <button class="btn btn-outline-secondary border-0 border-start px-2" @click="handle_open_help('PRJ')"><i class="bi bi-search"></i></button>
                                </div>
                            </td>
                        </tr>
                        <tr v-if="String(selectedrow.typebugt || '').trim() > '000' && String(selectedrow.dbcr || '').trim().toUpperCase() === 'D'">
                            <th class="bg-light border-bottom text-end pe-3">예산과목</th>
                            <td class="border-bottom">
                                <div class="input-group input-group-sm">
                                    <input v-model="selectedrow.bugtnm" class="form-control border-0" readonly />
                                    <button class="btn btn-outline-secondary border-0 border-start px-2" @click="handle_open_help('BUGT')"><i class="bi bi-search"></i></button>
                                </div>
                            </td>
                        </tr>
                    </template>
                </tbody>
             </table>
          </div>
          <div class="card-footer bg-light p-2 border-top">
             <button class="btn btn-xs btn-primary w-100 py-1 fw-bold shadow-sm" @click="update_auto_remark(true)">
                <i class="bi bi-magic me-1"></i> 적요 자동 조합 (표준 포맷 적용)
             </button>
          </div>
        </div>

        <div class="card border shadow-sm flex-shrink-0 d-flex align-items-center justify-content-center bg-white" style="width: 440px;" v-else>
           <div class="text-center opacity-50">
              <i class="bi bi-cursor-fill mb-2" style="font-size: 2rem;"></i>
              <div class="small">분개 행을 선택하면<br/>상세 입력창이 나타납니다.</div>
           </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref as _ref, reactive as _reactive, onMounted as _on_mounted, computed as _computed, nextTick as _next_tick, watch as _watch } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts as use_alerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore as use_auth_store } from '@/stores/authStore'
import { useFormReset as use_form_reset } from '@/composables/useFormReset'
import { useCommonHelp as use_common_help } from '@/composables/useCommonHelp'
import { getDate as use_date } from '@/composables/useDate'
import { useRoute as use_route } from 'vue-router'
import appalert from '@/components/AppAlert.vue'
import appmodal from '@/components/Modal.vue'

const authstore = use_auth_store()
const route = use_route()
const { today } = use_date()
const { showAlert: showalert, showError: showerror, alertMessage: alertmessage, vAlert: valert, vAlertError: valerterror } = use_alerts()
const { resetForm: resetform } = use_form_reset()
const { modalVisible: modalvisible, modalProps: modalprops, openHelp: openhelp } = use_common_help()

// [0] 💡 데이터 키 소문자 정규화 및 거래처명 통합 매핑 헬퍼 (SQL 조회 결과 완벽 대응)
const normalizeKeys = (obj: any) => {
    if (!obj) return obj;
    const normalized = Object.fromEntries(
        Object.entries(obj).map(([k, v]) => [k.toLowerCase(), v && typeof v === 'string' ? v.trim() : v])
    );

    // 💡 사용자 지시: 조회 시 SUBCD = trim(Rs("CUSTCD")), SUBNM = trim(Rs("custnm")) 로 변환하여 로드
    const custCd = normalized.custcd || '';
    const custNm = normalized.custnm || '';

    if (custCd) normalized.subcd = custCd;
    if (custNm) normalized.subnm = custNm;

    // 💡 기타 명칭 필드 동기화
    if (!normalized.docno2nm) normalized.docno2nm = normalized.docnm2 || custNm;
    if (!normalized.docno4) normalized.docno4 = normalized.docnm4 || custNm;
    if (!normalized.docno9nm) normalized.docno9nm = normalized.docnm9 || custNm;

    // 접대비(100) 계정인 경우 상호(docno4) 필드에도 자동 세팅
    if (String(normalized.typeacct || '').trim() === '100' && !normalized.docno4) {
        normalized.docno4 = custNm;
    }

    // 💡 날짜 필드 포맷팅 (YYYYMMDD -> YYYY-MM-DD)
    ['docno6', 'docno7', 'payymd'].forEach(key => {
        if (normalized[key] && normalized[key].length === 8 && !normalized[key].includes('-')) {
            const v = normalized[key];
            normalized[key] = `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}`;
        }
    });

    return normalized;
};

const masterform = _reactive<any>({
  cmpycd: authstore.cmpycd, deptcd: authstore.deptcd, deptnm: authstore.deptnm,
  slipymd: today, slipno: '', empnm: authstore.usernm, business: '',
  acctymd: '00000000', slipgu: '010'
})

const maingridref = _ref<HTMLElement | null>(null)
let main_grid: Tabulator | null = null
const selectedrow = _ref<any>(null)
const total_debit = _ref(0); const total_credit = _ref(0)
const balance = _computed(() => total_debit.value - total_credit.value)
const format_money = (val: any) => Number(val || 0).toLocaleString()

const purchasevatoptions = _ref<any[]>([]); const salesvatoptions = _ref<any[]>([])
const bizplaceoptions = _ref<any[]>([]);
const enttypeoptions = [{code:'01', name:'카드'}, {code:'02', name:'현금'}, {code:'03', name:'세금계산서'}]

const dynamicvatoptions = _computed(() => {
  if (!selectedrow.value) return []
  return (selectedrow.value.dbcr||'').toLowerCase() === 'D' ? purchasevatoptions.value : salesvatoptions.value
})

_watch(selectedrow, (newval) => {
    if (newval && activeRowComponent.value) {
        activeRowComponent.value.update(newval);
        update_auto_remark();
    }
}, { deep: true });

const update_auto_remark = (force = false) => {
    const row = selectedrow.value; if (!row) return;
    let prefix = "";
    if (row.subnm) prefix = `[${row.subnm}] `;
    else if (row.docno4) prefix = `[${row.docno4}] `;
    else if (row.mgtnm) prefix = `[${row.mgtnm}] `;
    else if (row.acctnm) prefix = `[${row.acctnm}] `;

    if (force || !row.remark || row.remark.startsWith('[') || row.remark === masterform.business) {
        row.remark = prefix + (masterform.business || "");
    }
}

const syncremarktorows = () => {
    main_grid?.getRows().forEach(row => {
        const d = row.getData();
        if (!d.remark || d.remark.startsWith('[') || d.remark === "") {
            let prefix = d.subnm ? `[${d.subnm}] ` : (d.mgtnm ? `[${d.mgtnm}] ` : (d.acctnm ? `[${d.acctnm}] ` : ""));
            row.update({ remark: prefix + masterform.business });
        }
    });
}

const init_grid = () => {
  main_grid = new Tabulator(maingridref.value!, {
    layout: "fitColumns", height: "100%", selectable: 1,
    columnDefaults: {
        headerHozAlign: 'center', headerSort: false, vertAlign: "middle",
        // 💡 그리드 셀 클릭 시 우측 관리항목 로드 보장
        cellClick: (e, cell) => {
            cell.getRow().select();
            selectedrow.value = normalizeKeys(cell.getRow().getData());
            activeRowComponent.value = cell.getRow();
        }
    },
    columns: [
      { title: "No", field: "srowno", width: 40, hozAlign: "center" },
      { title: "상태", field: "_status", width: 50, hozAlign: "center", formatter: (c) => {
          const v = c.getValue();
          if (v === '입력') return '<span class="badge bg-primary">신규</span>';
          if (v === '수정') return '<span class="badge bg-warning text-dark">수정</span>';
          if (v === '삭제') return '<span class="badge bg-danger">삭제</span>';
          return '';
      }},
      { title: "차/대", field: "dbcr", width: 70, hozAlign: "center",
        formatter: (c: any) => {
            const v = (c.getValue()||'').toLowerCase();
            return v === 'D' ? '<b class="text-primary">차변</b>' : (v === 'C' ? '<b class="text-danger">대변</b>' : v);
        }
      },
      { title: "계정과목", field: "acctnm", width: 160, cellClick: (e: any, cell: any) => handle_open_help('ACCT', cell.getRow()), cssClass: "cursor-pointer text-primary fw-bold" },
      { title: "적요", field: "remark", minWidth: 200, editor: "input", cssClass: "bg-light-yellow" },
      { title: "금액", field: "amount", width: 110, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: 0 } },
      { title: "관리내역", field: "mgt_summary", width: 130, formatter: (c) => {
          const d = c.getData(); let s = [];
          if (d.subnm) s.push(d.subnm); else if (d.docno4) s.push(d.docno4);
          if (d.mgtnm) s.push(d.mgtnm);
          return `<span class="small text-muted text-truncate d-block">${s.join(', ')}</span>`;
      }},
      { title: "삭제", width: 40, hozAlign: "center", formatter: () => '<i class="bi bi-trash text-danger"></i>', cellClick: (e: any, cell: any) => handle_row_action(cell.getRow()) }
    ]
  });
  main_grid.on("rowSelected", (row: any) => {
      selectedrow.value = normalizeKeys(row.getData());
      activeRowComponent.value = row;
  });
  main_grid.on("cellEdited", () => update_totals());
}

const update_totals = () => {
  const data = main_grid?.getData() || []
  total_debit.value = data.reduce((s, r: any) => s + ((r.dbcr||'').toLowerCase() === 'D' ? Number(r.amount || 0) : 0), 0)
  total_credit.value = data.reduce((s, r: any) => s + ((r.dbcr||'').toLowerCase() === 'C' ? Number(r.amount || 0) : 0), 0)
}

const fetch_template = async (jurncd: string, gbn: string) => {
  try {
    masterform.slipno = ''; // 🚀 HASL050U는 항상 신규 전표 생성이므로 번호 초기화
    masterform.slipymd = today;

    if (gbn === '100') {
      masterform.business = ''; masterform.slipgu = '010';
      masterform.deptcd = authstore.deptcd; masterform.deptnm = authstore.deptnm; masterform.empnm = authstore.usernm;
    } else {
      const res_m = await api.post('/api/hasl/hasl_050u_master', {
        actkind: 'S', cmpycd: authstore.cmpycd, jurncd: jurncd, search_gbn: gbn,
        deptcd: authstore.deptcd, empnm: authstore.usernm, updemp: authstore.userid
      });
      if (res_m.data && res_m.data.length > 0) {
        const m = normalizeKeys(res_m.data[0]);
        masterform.business = m.business || ''; masterform.slipgu = '010';
      }
    }

    const res_d = await api.post('/api/hasl/hasl_050u_str', {
      actkind: 'S', cmpycd: authstore.cmpycd, jurncd: jurncd, search_gbn: gbn,
      deptcd: authstore.deptcd, updemp: authstore.userid
    });

    const details = (res_d.data || []).map((row: any) => {
      const d = normalizeKeys(row);
      return {
        ...d,
        amount: Number(d.dbamt || 0) > 0 ? d.dbamt : d.cramt,
        dbcr: (d.dbcr || (Number(d.dbamt || 0) > 0 ? 'D' : 'C')).toLowerCase(),
        typedc: (d.typedc || d.drcr || '').trim(),
        _state: 'NEW', _status: '입력' // 🚀 과거 이력이든 템플릿이든 항상 '신규 입력' 상태
      };
    });

    if (gbn === '100' && details.length > 0) masterform.business = details[0].jurnnm || '';

    main_grid?.setData(details);
    update_totals();
    if (details.length > 0) {
        setTimeout(() => {
            const firstRow = main_grid?.getRows()[0];
            if (firstRow) firstRow.select();
        }, 150);
    }
  } catch (e) {
    console.error("❌ fetch_template error:", e);
    valerterror('데이터 로드 중 오류 발생');
  }
}

const save = async () => {
  if (balance.value !== 0) return valerterror('차/대변 금액이 일치하지 않습니다.');
  if (!masterform.business) return valerterror('거래내역을 입력하십시오.');

  const allData = main_grid?.getData() || [];
  if (allData.length === 0) return valerterror('저장할 내역이 없습니다.');

  try {
    const payload = {
      actkind: 'A', // 🚀 HASL050U는 항상 신규 입력('A')
      master: { ...masterform, slipno: '', slipymd: masterform.slipymd.replace(/-/g, ''), acctymd: masterform.acctymd.replace(/-/g, '') },
      details: allData.map(d => {
        const item = { ...d };
        item.custcd = item.subcd;

        ['docno6', 'docno7', 'payymd'].forEach(key => {
            if (item[key]) item[key] = item[key].replace(/-/g, '');
        });

        return {
            ...item,
            dbamt: item.dbcr.toLowerCase() === 'd' ? (item.amount || 0) : 0,
            cramt: item.dbcr.toLowerCase() === 'c' ? (item.amount || 0) : 0,
            upkind: 'A' // 🚀 상세도 항상 입력('A')
        };
      })
    };
    const res = await api.post('/api/hasl/hasl_010u_save', payload);
    valert('성공적으로 전표가 생성되었습니다.');
    if (res.data?.slipno) masterform.slipno = res.data.slipno;
  } catch (e) { valerterror('전표 생성 실패'); }
}

const initialize = () => {
    resetform(masterform);
    Object.assign(masterform, { cmpycd: authstore.cmpycd, deptcd: authstore.deptcd, deptnm: authstore.deptnm, slipymd: today, acctymd: '00000000', empnm: authstore.usernm, slipgu: '010' });
    main_grid?.clearData(); selectedrow.value = null; total_debit.value = 0; total_credit.value = 0;
}

const add_row = () => {
  const data = main_grid?.getData() || [];
  const lastRow = data.length > 0 ? data[data.length - 1] : null;
  const dbcrVal = lastRow ? (String(lastRow.dbcr || '').toLowerCase() === 'D' ? 'C' : 'D') : 'D';

  main_grid?.addRow(normalizeKeys({ dbcr: dbcrVal, remark: masterform.business, amount: 0, srowno: (data.length + 1), _status: '입력', _state: 'NEW' }), false)
    .then(row => {
        nextTick(() => { row.select(); main_grid?.scrollToRow(row, "bottom", false); });
    });
}

const handle_row_action = (row: any) => {
    // 🚀 HASL050U는 항상 신규 전표 생성이므로 화면에서 바로 삭제
    row.delete();
}

function handle_open_help(type: string, target?: any) {
  if (type === 'DEPT') openhelp('DEPT', (d) => { masterform.deptcd = d.deptcd; masterform.deptnm = d.deptnm });
  else if (type === 'ACCT') {
    openhelp('ACCT', (d: any) => {
        const res = normalizeKeys(d);
        const typeacct = String(res.typeacct || '').trim();
        target.update({
            acctcd: res.acctcd, acctnm: res.acctnm, typeacct: typeacct,
            typemgt: String(res.typemgt || '').trim(),
            typesub: String(res.typesub || '').trim(),
            typedc: String(res.typedc || '').trim(),
            frgnyn: res.frgnyn,
            titmgt: (res.typemgtnm || '').trim(),
            titsub: (res.typesubnm || '').trim(),
            typebugt: res.bugtcd,
            docno3: typeacct === '100' ? '01' : '',
            docno6: (typeacct === '100' || typeacct === '090') ? today : ''
        });
        selectedrow.value = normalizeKeys(target.getData());
    });
  } else if (type === 'MGT' || type === 'CARD') {
      const mgtgbn = type === 'CARD' ? '040' : selectedrow.value.typemgt;
      openhelp('MGT', (d) => {
          const res = normalizeKeys(d);
          if (type === 'CARD' && String(selectedrow.value.typeacct || '').trim() === '090') {
              selectedrow.value.docno4 = res.mgtno;
          } else {
              selectedrow.value.mgtno = res.mgtno;
              selectedrow.value.mgtnm = res.mgtnm;
          }
          update_auto_remark();
      }, { acctcd: selectedrow.value.acctcd, mgtgbn: mgtgbn });
  } else if (type === 'SUB' || type === 'vat_cust') {
      openhelp('CUST', (d) => {
        const res = normalizeKeys(d);
        if (type === 'SUB') {
            selectedrow.value.subcd = res.custcd;
            selectedrow.value.subnm = res.custnm;
        } else {
            selectedrow.value.docno2 = res.busino || res.custcd;
            selectedrow.value.docno2nm = res.custnm;
            selectedrow.value.docno4 = res.custnm;
            selectedrow.value.docno9nm = res.custnm;
        }
        // 동기화
        selectedrow.value.custcd = res.custcd;
        selectedrow.value.custnm = res.custnm;
      });
  } else if (type === 'PRJ') {
      openhelp('PRJ', (d) => { selectedrow.value.prjcd = d.prjcd; selectedrow.value.prjnm = d.prjnm });
  } else if (type === 'BUGT') {
      openhelp('BUGT', (d) => { selectedrow.value.bugtcd = d.bugtcd; selectedrow.value.bugtnm = d.bugtnm }, { acctcd: selectedrow.value.acctcd });
  }
}

const calc_vat = () => { if (String(selectedrow.value?.typeacct || '').trim() === '090') selectedrow.value.docno9 = Math.floor(Number(selectedrow.value.docno8 || 0) * 0.1); }

_on_mounted(() => {
  api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '120' }).then(r => purchasevatoptions.value = (r.data || []).map((i: any) => ({ codecd: i.codecd, codenm: i.codenm })));
  api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '130' }).then(r => salesvatoptions.value = (r.data || []).map((i: any) => ({ codecd: i.codecd, codenm: i.codenm })));
  api.post('/api/ha00/HA00_00P_STR', { gubun: 'SA', cmpycd: authstore.cmpycd }).then(r => bizplaceoptions.value = (r.data || []).map((i: any) => ({ code: i.taxunit || i.code || i.whcd || '', name: i.unitnm || i.codenm || i.whnm || '' })));
  init_grid();
  const { jurncd, gbn } = route.query;
  if (jurncd && gbn) fetch_template(String(jurncd), String(gbn));
});
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
.custom-scrollbar { overflow-y: auto; scrollbar-width: thin; }
.side-detail-table { border-collapse: collapse; width: 100%; }
.side-detail-table th { background-color: #f8f9fa; padding: 2px 10px !important; font-size: 12px; font-weight: 600; color: #555; border: 1px solid #eee; text-align: right; height: 25px; white-space: nowrap; }
.side-detail-table td { padding: 1px 6px !important; border: 1px solid #eee; vertical-align: middle; }
.bg-light-yellow { background-color: #fffdf0 !important; }
.cursor-pointer { cursor: pointer; }
.required::after { content: " *"; color: #dc3545; }
:deep(.tabulator-row-selected) { background-color: #e7f1ff !important; }
</style>
