<!--
	=============================================================
	프로그램명	: 계정과목관리 (HABA010U)
	작성일자	: 2025.03.14
	설명        : 계정과목 등록 및 속성 설정 (원본 ASP 구성을 충실히 재현 및 HSOD100U 디자인 표준 준수)
	=============================================================
-->

<template>
  <AppAlert :show="showalert" :error="showerror" :message="alertmessage" />
  <Modal v-model:visible="modalvisible" :modalProps="modalprops" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-diagram-3-fill me-2 text-primary" style="font-size: 18px;"></i>
        기본정보 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">계정과목관리 (HABA010U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-3">
        <button class="btn-erp btn-init" @click="initialize">신규</button>
        <button class="btn-erp btn-search" @click="search">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
      </div>
    </div>

    <!-- 💡 2. 메인 컨텐츠 영역 -->
    <div class="flex-grow-1 overflow-hidden p-2 d-flex flex-column gap-2 bg-light main-content-wrapper">

      <!-- [상단] 조회 필터 영역 (Original ASP Search Form 준수) -->
      <div class="card border shadow-sm flex-shrink-0 overflow-hidden">
        <div class="card-body p-2 bg-white">
          <table class="erp-table-dense w-100">
            <tbody>
              <tr>
                <th class="bg-light text-center" style="width: 100px;">재무제표</th>
                <td style="width: 200px;">
                  <select v-model="searchform.gubun" class="form-select form-select-sm" @change="search">
                    <option v-for="opt in statementoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="bg-light text-center border-start" style="width: 100px;">계 정 명</th>
                <td style="width: 250px;">
                    <input v-model="searchform.acctnm" type="text" class="form-control form-control-sm" placeholder="계정명 검색" @keydown.enter="search" />
                </td>
                <td class="px-3">
                    <button class="btn btn-sm btn-secondary px-3" @click="search"><i class="bi bi-search me-1"></i>조회</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [중간] 상세 정보 입력 영역 (Original ASP frmBody 구성 준수) -->
      <div class="card border shadow-sm overflow-hidden flex-shrink-0">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center justify-content-between">
          <div class="fw-bold small text-dark"><i class="bi bi-pencil-square me-2 text-secondary"></i>계정과목 상세 정보</div>
          <div v-if="masterform.actkind === 'U1'" class="badge bg-primary-subtle text-primary border border-primary-subtle px-2">수정 중</div>
          <div v-else class="badge bg-success-subtle text-success border border-success-subtle px-2">신규 등록</div>
        </div>
        <div class="card-body p-0 bg-white">
          <table class="erp-table-dense w-100">
            <colgroup>
              <col style="width: 100px;" /><col style="width: 25%;" />
              <col style="width: 100px;" /><col style="width: 25%;" />
              <col style="width: 100px;" /><col />
            </colgroup>
            <tbody>
              <tr>
                <th class="required bg-light text-center">계정과목</th>
                <td class="bg-light-yellow"><input v-model="masterform.acctcd" type="text" class="form-control form-control-sm text-center fw-bold" maxlength="7" :readonly="masterform.actkind === 'U1'" /></td>
                <th class="required bg-light text-center border-start">계 정 명</th>
                <td class="bg-light-yellow"><input v-model="masterform.acctnm" type="text" class="form-control form-control-sm" maxlength="30" /></td>
                <th class="bg-light text-center border-start">상위계정</th>
                <td class="bg-light-yellow">
                  <div class="input-group input-group-sm">
                    <input v-model="masterform.upacct" type="text" class="form-control text-center bg-light-fix fw-bold" style="max-width: 80px;" readonly />
                    <input v-model="masterform.upacct_t" type="text" class="form-control" placeholder="선택" @keydown.enter="openhelp('upacct')" />
                    <button class="btn btn-outline-secondary px-2" @click="openhelp('upacct')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
              <tr>
                <th class="required bg-light text-center border-top">차대구분</th>
                <td class="bg-light-yellow border-top">
                  <select v-model="masterform.typedc" class="form-select form-select-sm">
                    <option value="D">차변</option>
                    <option value="C">대변</option>
                  </select>
                </td>
                <th class="required bg-light text-center border-start border-top">전표발행</th>
                <td class="bg-light-yellow border-top">
                  <select v-model="masterform.slipyn" class="form-select form-select-sm">
                    <option value="Y">발행</option>
                    <option value="N">미발행</option>
                  </select>
                </td>
                <th class="bg-light text-center border-start border-top">보 조 부</th>
                <td class="bg-light-yellow border-top">
                  <select v-model="masterform.typesub" class="form-select form-select-sm">
                    <option value="000">없음</option>
                    <option v-for="opt in subsidiaryoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th class="bg-light text-center border-top">관리번호</th>
                <td class="bg-light-yellow border-top">
                  <select v-model="masterform.typemgt" class="form-select form-select-sm">
                    <option value="000">없음</option>
                    <option v-for="opt in managementoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="bg-light text-center border-start border-top">상계유형</th>
                <td class="bg-light-yellow border-top">
                  <select v-model="masterform.typecrs" class="form-select form-select-sm">
                    <option value="000">없음</option>
                    <option v-for="opt in offsetoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="bg-light text-center border-start border-top">기타관리</th>
                <td class="bg-light-yellow border-top">
                  <select v-model="masterform.typeect" class="form-select form-select-sm">
                    <option value="000">없음</option>
                    <option v-for="opt in otheroptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
              </tr>
              <tr>
                <th class="bg-light text-center border-top">계정유형</th>
                <td class="bg-light-yellow border-top">
                  <select v-model="masterform.typeacct" class="form-select form-select-sm">
                    <option value="000">없음</option>
                    <option v-for="opt in accttypeoptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                  </select>
                </td>
                <th class="bg-light text-center border-start border-top">증빙유무</th>
                <td class="bg-light-yellow border-top">
                  <select v-model="masterform.profyn" class="form-select form-select-sm">
                    <option value="N">무</option>
                    <option value="Y">유</option>
                  </select>
                </td>
                <th class="bg-light text-center border-start border-top">예산코드</th>
                <td class="bg-light-yellow border-top">
                  <div class="input-group input-group-sm">
                    <input v-model="masterform.bugtcd" type="text" class="form-control text-center bg-light-fix fw-bold" style="max-width: 80px;" readonly />
                    <input v-model="masterform.bugtcd_t" type="text" class="form-control" placeholder="선택" @keydown.enter="openhelp('bugt')" />
                    <button class="btn btn-outline-secondary px-2" @click="openhelp('bugt')"><i class="bi bi-search"></i></button>
                  </div>
                </td>
              </tr>
              <tr>
                <th class="bg-light text-center border-top">외화관리</th>
                <td class="bg-light-yellow border-top">
                  <div class="form-check form-check-inline m-0 ms-2 d-flex align-items-center h-100">
                    <input v-model="masterform.frgnyn" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="frgnSwitch">
                    <label class="form-check-label ms-2 small fw-bold" for="frgnSwitch">관리</label>
                  </div>
                </td>
                <th class="bg-light text-center border-start border-top">검 색 어</th>
                <td class="bg-light-yellow border-top"><input v-model="masterform.keyword" type="text" class="form-control form-control-sm" maxlength="50" /></td>
                <th class="bg-light text-center border-start border-top">사용여부</th>
                <td class="bg-light-yellow border-top">
                  <div class="form-check form-check-inline m-0 ms-2 d-flex align-items-center h-100">
                    <input v-model="masterform.useyn" class="form-check-input mt-0" type="checkbox" true-value="Y" false-value="N" id="useSwitch010">
                    <label class="form-check-label ms-2 small fw-bold" for="useSwitch010">사용</label>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 그리드 영역 -->
      <div class="card border shadow-sm flex-grow-1 overflow-hidden d-flex flex-column">
        <div class="card-header bg-white py-1 px-3 border-bottom d-flex align-items-center">
          <i class="bi bi-table me-2 text-primary"></i>
          <span class="fw-bold small text-dark">계정과목 목록</span>
        </div>
        <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
          <div ref="maingridelement" class="tabulator-instance flex-grow-1"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'

const authstore = useAuthStore()
const { showAlert: showalert, showError: showerror, alertMessage: alertmessage, vAlert: valert, vAlertError: valerterror } = useAlerts()
const { resetForm: resetform } = useFormReset()
const { modalVisible: modalvisible, modalProps: modalprops } = useCommonHelp()

const searchform = reactive({ gubun: '010', acctnm: '' })
const masterform = reactive<any>({
  actkind: 'I1', gubun: '010', acctcd: '', acctnm: '', upacct: '', upacct_t: '',
  typedc: 'D', slipyn: 'Y', typesub: '000', typemgt: '000', typecrs: '000', typeect: '000', typeacct: '000',
  profyn: 'N', bugtcd: '', bugtcd_t: '', frgnyn: 'N', keyword: '', useyn: 'Y'
})

const statementoptions = ref<any[]>([])
const subsidiaryoptions = ref<any[]>([])
const managementoptions = ref<any[]>([])
const offsetoptions = ref<any[]>([])
const otheroptions = ref<any[]>([])
const accttypeoptions = ref<any[]>([])

const maingridelement = ref<HTMLDivElement | null>(null)
let maingrid: Tabulator | null = null

const normalizekeys = (obj: any) => {
  const n: any = {}; if (!obj) return n;
  Object.keys(obj).forEach(k => n[k.toLowerCase()] = typeof obj[k] === 'string' ? obj[k].trim() : obj[k]);
  return n;
}

const search = async () => {
  try {
    const res = await api.post('/api/haba/HABA_010U_STR',
    {
        actkind: 'S2',
        cmpycd: authstore.cmpycd,
        gubun: searchform.gubun,
        acctnm: searchform.acctnm,
        acctcd: '', upacct: '', typedc: '', slipyn: '', typeacct: '', typesub: '', typemgt: '', typecrs: '', bugtcd: '', profyn: '', frgnyn: '', typeect: '', useyn: '', keyword: '', userid: authstore.userid
    });
    console.log('Search Result = ', res.data);
    maingrid?.setData((res.data || []).map((r: any) => normalizekeys(r)));
  } catch (e) { valerterror('조회 실패') }
}

const save = async () => {
  if (!masterform.acctcd || !masterform.acctnm) return valerterror('필수 항목을 입력하세요.');
  try {
    masterform.gubun = searchform.gubun;
    const res = await api.post('/api/haba/HABA_010U_STR', {
        ...masterform,
        cmpycd: authstore.cmpycd,
        userid: authstore.userid
    });

    const resdata = normalizekeys(res.data?.[0]);
    if (resdata.res === 'OK' || resdata.ret_yn === 'Y') {
      valert('성공적으로 저장되었습니다.');
      search();
      initialize();
    } else {
      valerterror(resdata.msg || resdata.ret_msg || '저장 처리 중 오류가 발생했습니다.');
    }
  } catch (e) { valerterror('저장 실패') }
}

const initialize = () => {
  resetform(masterform);
  Object.assign(masterform, {
    actkind: 'I1', gubun: searchform.gubun, typedc: 'D', slipyn: 'Y',
    typesub: '000', typemgt: '000', typecrs: '000', typeect: '000', typeacct: '000',
    profyn: 'N', frgnyn: 'N', useyn: 'Y'
  });
}

function openhelp(type: string) {
  if (type === 'upacct') {
    // 🚀 상위계정 도움창 직접 처리
    Object.assign(modalprops, {
      title: '상위계정 선택',
      path: '/api/ha00/HA00_00P_STR',
      data: { gubun: 'A0', gbncd: searchform.gubun, cmpycd: authstore.cmpycd, code: masterform.upacct_t },
      columns: [
        { title: '코드', field: 'acctcd', width: 100, hozAlign: 'center' },
        { title: '계정명', field: 'acctnm', width: 250 }
      ],
      onConfirm: (d: any) => {
        const n = normalizekeys(d);
        masterform.upacct = n.acctcd || n.codecd;
        masterform.upacct_t = n.acctnm || n.codenm;
      },
      type: 'table'
    });
    modalvisible.value = true;
  } else if (type === 'bugt') {
    // 🚀 예산코드 도움창 직접 처리
    Object.assign(modalprops, {
      title: '예산코드 선택',
      path: '/api/ha00/HA00_00P_STR',
      data: { gubun: 'BU', gbncd: '', cmpycd: authstore.cmpycd, code: masterform.bugtcd_t },
      columns: [
        { title: '코드', field: 'codecd', width: 100, hozAlign: 'center' },
        { title: '예산명', field: 'codenm', width: 250 }
      ],
      onConfirm: (d: any) => {
        const n = normalizekeys(d);
        masterform.bugtcd = n.codecd || n.acctcd;
        masterform.bugtcd_t = n.codenm || n.acctnm;
      },
      type: 'table'
    });
    modalvisible.value = true;
  }
}

onMounted(async () => {
  // 초기 옵션 로드
  const loadOptions = async () => {
    try {
      const p070 = api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '070', cmpycd: authstore.cmpycd })
      const p010 = api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '010', cmpycd: authstore.cmpycd })
      const p020 = api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '020', cmpycd: authstore.cmpycd })
      const p030 = api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '030', cmpycd: authstore.cmpycd })
      const p040 = api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '040', cmpycd: authstore.cmpycd })
      const p050 = api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '050', cmpycd: authstore.cmpycd })

      const [r070, r010, r020, r030, r040, r050] = await Promise.all([p070, p010, p020, p030, p040, p050]);

      statementoptions.value = (r070.data || []).map((r: any) => normalizekeys(r)).filter((n: any) => n.codecd <= '030');
      subsidiaryoptions.value = (r010.data || []).map((r: any) => normalizekeys(r));
      managementoptions.value = (r020.data || []).map((r: any) => normalizekeys(r));
      offsetoptions.value = (r030.data || []).map((r: any) => normalizekeys(r));
      accttypeoptions.value = (r040.data || []).map((r: any) => normalizekeys(r));
      otheroptions.value = (r050.data || []).map((r: any) => normalizekeys(r));
    } catch (e) { console.error('Option Load Failed') }
  };

  await loadOptions();

  if (maingridelement.value) {
    maingrid = new Tabulator(maingridelement.value, {
      layout: "fitColumns", height: "100%", selectable: 1,
      columnDefaults: { headerSort: false, headerHozAlign: "center", hozAlign: "center", vertAlign: "middle" },

      columns: [
        { title: "No", formatter: "rownum", width: 40 },
        { title: "계정과목", field: "acctcd", width: 90, cssClass: "fw-bold text-primary border-end" },
        { title: "계정명", field: "acctnm", minWidth: 200, hozAlign: "left" },
        { title: "차대", field: "typedc", width: 60, formatter: (c) => String(c.getValue() || '').toUpperCase() === 'D' ? '차변' : '대변' },
        { title: "전표", field: "slipyn", width: 60, formatter: (c) => String(c.getValue() || '').toUpperCase() === 'Y' ? '발행' : '미발행' },
        { title: "보조부", field: "typesubnm", width: 100 },
        { title: "관리번호", field: "typemgtnm", width: 100 },
        { title: "상계", field: "typecrsnm", width: 100 },
        { title: "기타", field: "typeectnm", width: 100 },
        { title: "유형", field: "typeacctnm", width: 80 },
        { title: "증빙", field: "profyn", width: 60, formatter: (c) => String(c.getValue() || '').toUpperCase() === 'Y' ? '유' : '무' },
        { title: "예산", field: "bugtnm", width: 80 },
        { title: "사용", field: "useyn", width: 60, formatter: (c) => String(c.getValue() || '').toUpperCase() === 'Y' ? '<span class="text-success fw-bold">O</span>' : '<span class="text-danger fw-bold">X</span>' }
      ]
    });

    maingrid.on("rowClick", (e, row) => {
      const data = row.getData();
      Object.assign(masterform, data);

      // 도움창 필드 명칭 동기화
      if (data.bugtnm) masterform.bugtcd_t = data.bugtnm;
      if (data.upacctnm) masterform.upacct_t = data.upacctnm;
      if (data.upacct_t) masterform.upacct_t = data.upacct_t;

      masterform.actkind = 'U1';
    });
  }

  nextTick(() => search());
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
:deep(.tabulator-row-selected) { background-color: #e7f1ff !important; }
.bg-light-yellow { background-color: #f9f6e7 !important; }
.bg-light-fix { background-color: #f8f9fa !important; }
</style>
