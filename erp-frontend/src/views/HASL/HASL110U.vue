<!--
	=============================================================
	프로그램명	: 경리전표입력 (HASL110U)
	작성일자	: 2025.02.24
	설명        : 재무 부서용 회계 전표 입력 (상세항목 표준 레이아웃 및 간격 극한 최적화 적용)
	=============================================================
-->

<template>
  <AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
  <Modal v-model:visible="modalVisible" :modalProps="modalProps" />

  <div class="erp-container d-flex flex-column h-100 bg-white">
    <!-- 🚀 1. 상단 액션 바 -->
    <div class="erp-header d-flex justify-content-between align-items-center flex-shrink-0 border-bottom">
      <div class="fw-bold ps-1 text-dark d-flex align-items-center" style="font-size: 14px;">
        <i class="bi bi-journal-check me-2 text-primary" style="font-size: 18px;"></i>
        전표관리 <i class="bi bi-chevron-right mx-1 small opacity-50"></i>
        <span class="text-primary fw-bolder">경리전표입력 (HASL110U)</span>
      </div>
      <div class="btn-group-erp d-flex gap-1 pe-2">
        <button class="btn-erp btn-init" @click="initialize">초기화</button>
        <button class="btn-erp btn-search" @click="searchSlips">조회</button>
        <button class="btn-erp btn-save" @click="save">저장</button>
        <button v-if="masterForm.slipno" class="btn-erp btn-delete" @click="deleteData">삭제</button>
        <button v-if="masterForm.slipno" class="btn-erp btn-print" @click="print">인쇄</button>
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
                <th class="text-center bg-light">발행기간</th>
                <td class="d-flex align-items-center border-0 gap-1" style="height: 32px;">
                  <DateForm v-model:fromdt="searchParams.fromdt" v-model:todt="searchParams.todt" />
                </td>
                <th class="text-center bg-light">거래내역</th>
                <td>
                  <input v-model="searchParams.business" class="form-control form-control-sm" placeholder="검색어 입력" @keyup.enter="searchSlips" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- [하단] 메인 레이아웃 (전표목록 | 전표상세) -->
      <div class="d-flex gap-2 flex-grow-1 overflow-hidden" style="min-height: 0;">

        <!-- ⬅️ 좌측: 전표 목록 -->
        <div class="card border shadow-sm d-flex flex-column overflow-hidden grid-container-left" style="width: 350px; min-width: 350px;">
          <div class="card-header bg-white py-1 px-3 border-bottom fw-bold small text-dark">전표 목록</div>
          <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
            <div ref="slipListGridRef" class="tabulator-instance flex-grow-1"></div>
          </div>
        </div>

        <!-- ➡️ 우측: 전표 상세 -->
        <div class="flex-grow-1 d-flex flex-column gap-2 overflow-hidden">

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
                        <input v-model="masterForm.deptnm" class="form-control" readonly />
                        <button class="btn btn-outline-secondary px-2" @click="handleOpenHelp('DEPT')"><i class="bi bi-search"></i></button>
                      </div>
                    </td>
                    <th class="bg-light text-center">전표번호</th>
                    <td>
                      <div class="d-flex gap-1">
                        <input v-model="masterForm.slipymd" type="date" class="form-control" />
                        <input v-model="masterForm.slipno" class="form-control text-center bg-light fw-bold" readonly style="width: 60px;" />
                      </div>
                    </td>
                    <th class="required bg-light text-center">회계일자</th>
                    <td><input v-model="masterForm.acctymd" type="date" class="form-control" /></td>
                  </tr>
                  <tr>
                    <th class="required bg-light text-center border-top">거래내역</th>
                    <td colspan="3" class="border-top">
                        <input v-model="masterForm.business" class="form-control fw-bold text-primary" placeholder="전표 거래내역 입력" @input="syncRemarkToRows" />
                    </td>
                    <th class="bg-light text-center border-top">상태</th>
                    <td class="text-center border-top">
                      <span :class="masterForm.slipgu === '020' ? 'badge bg-success' : 'badge bg-primary'">
                        {{ masterForm.slipgu === '020' ? '미확정' : '확정' }}
                      </span>
                    </td>
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
                <button class="btn btn-sm btn-primary py-0 px-2 fw-bold" @click="addRow" style="font-size: 11px;">+ 행추가</button>
              </div>
              <div class="card-body p-0 flex-grow-1 bg-white overflow-hidden d-flex flex-column">
                <div ref="mainGridRef" class="tabulator-instance flex-grow-1"></div>
              </div>
              <div class="card-footer p-0 border-top bg-dark text-white">
                <div class="row g-0 text-end fw-bold small py-1 px-3">
                  <div class="col-4 text-center border-end border-secondary">분개 합계</div>
                  <div class="col-2 text-info border-end border-secondary pe-2">{{ formatMoney(totalDebit) }}</div>
                  <div class="col-2 text-warning border-end border-secondary pe-2">{{ formatMoney(totalCredit) }}</div>
                  <div class="col-4" :class="balance === 0 ? 'text-success' : 'text-danger'">차액: {{ formatMoney(balance) }}</div>
                </div>
              </div>
            </div>

            <!-- (우측) 상세 관리항목 설정 사이드 바 -->
            <div class="card border shadow-sm flex-shrink-0 d-flex flex-column bg-white overflow-hidden side-panel-wrapper" style="width: 440px;" v-if="selectedRow">
              <div class="card-header py-2 px-3 bg-secondary text-white small fw-bold d-flex justify-content-between align-items-center">
                 <span><i class="bi bi-gear-fill me-2"></i>상세 관리항목 설정</span>
                 <span class="badge" :class="String(selectedRow.dbcr || '').trim().toUpperCase() === 'D' ? 'bg-primary' : 'bg-danger'">{{ String(selectedRow.dbcr || '').trim().toUpperCase() === 'D' ? '차변' : '대변' }}</span>
              </div>
              <div class="card-body p-0 overflow-auto custom-scrollbar flex-grow-1">

                 <!-- 💡 선택 계정 정보 헤더 -->
                 <div class="p-2 bg-light border-bottom d-flex align-items-center gap-2 small sticky-top">
                    <span class="badge bg-white text-dark border px-2 py-1">{{ selectedRow.acctcd }}</span>
                    <b class="text-primary">{{ selectedRow.acctnm || '계정 미선택' }}</b>
                 </div>

                 <table class="erp-table-dense side-detail-table w-100 border-0">
                    <colgroup>
                        <col style="width: 100px;" /><col />
                    </colgroup>
                    <tbody>
                        <!-- [1] 접대비 상세 (typeacct: 100) -->
                        <template v-if="String(selectedRow.typeacct || '').trim() === '100'">
                            <tr>
                                <th class="bg-light border-bottom text-end pe-3 required">증빙유형</th>
                                <td class="border-bottom">
                                    <select v-model="selectedRow.docno3" class="form-select form-select-sm border-0">
                                        <option v-for="opt in entTypeOptions" :key="opt.code" :value="opt.code">{{ opt.name }}</option>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <th class="bg-light border-bottom text-end pe-3 required">접대일자</th>
                                <td class="border-bottom">
                                    <input v-model="selectedRow.docno6" type="date" class="form-control form-control-sm border-0" />
                                </td>
                            </tr>
                            <tr>
                                <th class="bg-light border-bottom text-end pe-3">사업번호</th>
                                <td class="border-bottom">
                                    <input v-model="selectedRow.docno2" class="form-control form-control-sm border-0" placeholder="숫자만 입력" />
                                </td>
                            </tr>
                            <tr>
                                <th class="bg-light border-bottom text-end pe-3 text-primary">상호/접대지</th>
                                <td class="border-bottom">
                                    <div class="input-group input-group-sm">
                                        <input v-model="selectedRow.docno4" class="form-control border-0" placeholder="상호명 입력" />
                                        <button class="btn btn-outline-secondary border-0 border-start px-2" @click="handleOpenHelp('vat_cust')"><i class="bi bi-search"></i></button>
                                    </div>
                                </td>
                            </tr>
                            <tr v-if="String(selectedRow.docno3 || '').trim() === '01'">
                                <th class="bg-light border-bottom text-end pe-3 text-primary fw-bold">카드번호</th>
                                <td class="border-bottom">
                                    <div class="input-group input-group-sm">
                                        <input v-model="selectedRow.docno5" class="form-control border-0 text-primary" placeholder="카드 도움창 선택" />
                                        <button class="btn btn-outline-secondary border-0 border-start px-2" @click="handleOpenHelp('CARD')"><i class="bi bi-credit-card"></i></button>
                                    </div>
                                </td>
                            </tr>
                            <tr>
                                <th class="bg-light border-bottom text-end pe-3 fw-bold text-primary">순접대비</th>
                                <td class="border-bottom">
                                    <input v-model="selectedRow.docno8" type="number" class="form-control form-control-sm border-0 text-end fw-bold" placeholder="0" />
                                </td>
                            </tr>
                            <tr>
                                <th class="bg-light border-bottom text-end pe-3">봉사료/기타</th>
                                <td class="border-bottom">
                                    <input v-model="selectedRow.docno9" type="number" class="form-control form-control-sm border-0 text-end" placeholder="0" />
                                </td>
                            </tr>
                        </template>

                        <!-- [2] 부가세 상세 (typeacct: 090) -->
                        <template v-else-if="String(selectedRow.typeacct || '').trim() === '090'">
                            <template v-if="String(selectedRow.typedc || '').trim().toUpperCase() === String(selectedRow.dbcr || '').trim().toUpperCase()">
                                <tr>
                                    <th class="bg-light border-bottom text-end pe-3 required">사&nbsp;업&nbsp;장</th>
                                    <td class="border-bottom">
                                        <select v-model="selectedRow.docno1" class="form-select form-select-sm border-0">
                                            <option value="">-- 선택 --</option>
                                            <option v-for="opt in bizPlaceOptions" :key="opt.code" :value="opt.code">{{ opt.name }}</option>
                                        </select>
                                    </td>
                                </tr>
                                <tr>
                                    <th class="bg-light border-bottom text-end pe-3 required">세무유형</th>
                                    <td class="border-bottom">
                                        <select v-model="selectedRow.docno3" class="form-select form-select-sm border-0">
                                            <option value="">유형 선택</option>
                                            <option v-for="opt in dynamicVatOptions" :key="opt.codecd" :value="opt.codecd">{{ opt.codenm }}</option>
                                        </select>
                                    </td>
                                </tr>
                                <tr>
                                    <th class="bg-light border-bottom text-end pe-3 required">거&nbsp;래&nbsp;처</th>
                                    <td class="border-bottom">
                                        <div class="input-group input-group-sm">
                                            <input v-model="selectedRow.docno2nm" class="form-control border-0" readonly placeholder="거래처 도움창 선택" />
                                            <button class="btn btn-outline-secondary border-0 border-start px-2" @click="handleOpenHelp('vat_cust')"><i class="bi bi-search"></i></button>
                                        </div>
                                    </td>
                                </tr>
                                <tr>
                                    <th class="bg-light border-bottom text-end pe-3 required">발&nbsp;행&nbsp;일</th>
                                    <td class="border-bottom">
                                        <input v-model="selectedRow.docno6" type="date" class="form-control form-control-sm border-0" />
                                    </td>
                                </tr>
                                <tr>
                                    <th class="bg-light border-bottom text-end pe-3 fw-bold text-primary">공&nbsp;급&nbsp;가</th>
                                    <td class="border-bottom">
                                        <input v-model="selectedRow.docno8" type="number" class="form-control form-control-sm border-0 text-end fw-bold" @input="calcVat" />
                                    </td>
                                </tr>
                                <tr>
                                    <th class="bg-light border-bottom text-end pe-3 fw-bold text-danger">부&nbsp;가&nbsp;세</th>
                                    <td class="border-bottom">
                                        <input v-model="selectedRow.docno9" type="number" class="form-control form-control-sm border-0 text-end fw-bold" />
                                    </td>
                                </tr>
                                <tr v-if="String(selectedRow.dbcr || '').trim().toUpperCase() === 'D'">
                                    <th class="bg-light border-bottom text-end pe-3 text-primary fw-bold">카&nbsp;&nbsp;&nbsp;&nbsp;드</th>
                                    <td class="border-bottom">
                                        <div class="input-group input-group-sm">
                                            <input v-model="selectedRow.docno4" class="form-control border-0 text-primary" placeholder="카드번호 선택" />
                                            <button class="btn btn-outline-secondary border-0 border-start px-2" @click="handleOpenHelp('CARD')"><i class="bi bi-credit-card"></i></button>
                                        </div>
                                    </td>
                                </tr>
                            </template>
                        </template>

                        <!-- [3] 어음 상세 (050, 060, 070) -->
                        <template v-else-if="['050', '060', '070'].includes(String(selectedRow.typeacct || '').trim())">
                            <tr>
                                <th class="bg-light border-bottom text-end pe-3 required">어음번호</th>
                                <td class="border-bottom">
                                    <div class="input-group input-group-sm">
                                        <input v-model="selectedRow.mgtno" class="form-control border-0" @keydown.enter="handleOpenHelp('MGT')" placeholder="번호 입력" />
                                        <button class="btn btn-outline-secondary border-0 border-start px-2" @click="handleOpenHelp('MGT')"><i class="bi bi-search"></i></button>
                                    </div>
                                </td>
                            </tr>
                            <tr v-if="(String(selectedRow.typeacct || '').trim() === '050' && String(selectedRow.dbcr || '').trim().toUpperCase() === 'D') || (String(selectedRow.typeacct || '').trim() === '060' && String(selectedRow.dbcr || '').trim().toUpperCase() === 'C')">
                                <th class="bg-light border-bottom text-end pe-3">지급/수취처</th>
                                <td class="border-bottom">
                                    <div class="input-group input-group-sm">
                                        <input v-model="selectedRow.docno9nm" class="form-control border-0" readonly placeholder="도움창 선택" />
                                        <button class="btn btn-outline-secondary border-0 border-start px-2" @click="handleOpenHelp('vat_cust')"><i class="bi bi-search"></i></button>
                                    </div>
                                </td>
                            </tr>
                            <tr v-if="(String(selectedRow.typeacct || '').trim() === '050' && String(selectedRow.dbcr || '').trim().toUpperCase() === 'D') || (String(selectedRow.typeacct || '').trim() === '060' && String(selectedRow.dbcr || '').trim().toUpperCase() === 'C')">
                                <th class="bg-light border-bottom text-end pe-3">만기일자</th>
                                <td class="border-bottom">
                                    <input v-model="selectedRow.docno7" type="date" class="form-control form-control-sm border-0" />
                                </td>
                            </tr>
                        </template>

                        <!-- [4] 일반 관리항목 / 거래처 (010~040 포함) -->
                        <template v-else>
                            <tr v-if="String(selectedRow.typemgt || '').trim() > '000'">
                                <th class="bg-light border-bottom text-end pe-3 text-primary">
                                    {{
                                        String(selectedRow.typemgt || '').trim() === '010' ? '구좌번호' :
                                        String(selectedRow.typemgt || '').trim() === '020' ? '차입금' :
                                        String(selectedRow.typemgt || '').trim() === '030' ? '유가증권' :
                                        String(selectedRow.typemgt || '').trim() === '040' ? '법인카드' : (selectedRow.titmgt || selectedRow.typemgtnm || '관리항목')
                                    }}
                                </th>
                                <td class="border-bottom">
                                    <div class="input-group input-group-sm">
                                        <input v-model="selectedRow.mgtno" class="form-control border-0" @keydown.enter="handleOpenHelp('MGT')" :placeholder="(String(selectedRow.typemgt || '').trim() >= '010' && String(selectedRow.typemgt || '').trim() <= '040' ? '코드' : '번호') + ' 입력'" />
                                        <button class="btn btn-outline-secondary border-0 border-start px-2" @click="handleOpenHelp('MGT')"><i class="bi bi-search"></i></button>
                                    </div>
                                    <div class="small text-primary mt-1 px-1 fw-bold" v-if="selectedRow.mgtnm">{{ selectedRow.mgtnm }}</div>
                                </td>
                            </tr>
                            <tr v-if="String(selectedRow.typesub || '').trim() > '000' && (String(selectedRow.typemgt || '').trim() <= '000' || String(selectedRow.typemgt || '').trim() === '090')">
                                <th class="bg-light border-bottom text-end pe-3 text-primary">{{ selectedRow.titsub || selectedRow.typesubnm || '거래처' }}</th>
                                <td class="border-bottom">
                                    <div class="input-group input-group-sm">
                                        <input v-model="selectedRow.subnm" class="form-control border-0" readonly placeholder="도움창 선택" />
                                        <button class="btn btn-outline-secondary border-0 border-start px-2" @click="handleOpenHelp('SUB')"><i class="bi bi-search"></i></button>
                                    </div>
                                </td>
                            </tr>
                        </template>

                        <!-- [5] 프로젝트/예산 공통 (acctcd > 40000) -->
                        <template v-if="String(selectedRow.acctcd || '').trim() > '40000'">
                            <tr>
                                <th class="bg-light border-bottom text-end pe-3">프로젝트</th>
                                <td class="border-bottom">
                                    <div class="input-group input-group-sm">
                                        <input v-model="selectedRow.prjnm" class="form-control border-0" readonly />
                                        <button class="btn btn-outline-secondary border-0 border-start px-2" @click="handleOpenHelp('PRJ')"><i class="bi bi-search"></i></button>
                                    </div>
                                </td>
                            </tr>
                            <tr v-if="String(selectedRow.typebugt || '').trim() > '000' && String(selectedRow.dbcr || '').trim().toUpperCase() === 'D'">
                                <th class="bg-light border-bottom text-end pe-3">예산과목</th>
                                <td class="border-bottom">
                                    <div class="input-group input-group-sm">
                                        <input v-model="selectedRow.bugtnm" class="form-control border-0" readonly />
                                        <button class="btn btn-outline-secondary border-0 border-start px-2" @click="handleOpenHelp('BUGT')"><i class="bi bi-search"></i></button>
                                    </div>
                                </td>
                            </tr>
                        </template>
                    </tbody>
                 </table>
              </div>

              <!-- 💡 하단 액션 영역 (적요 도우미) -->
              <div class="card-footer bg-light p-2 border-top">
                 <div class="d-flex flex-column gap-2">
                    <button class="btn btn-xs btn-primary w-100 py-1 fw-bold shadow-sm" @click="updateAutoRemark(true)">
                        <i class="bi bi-magic me-1"></i> 적요 자동 조합 (표준 포맷 적용)
                    </button>
                    <div class="text-center bg-white border rounded p-1" style="font-size: 10px; color: #666; line-height: 1.4;">
                        직접 수정한 적요를 취소하고 <b>[거래처/관리명] + 전표내역</b><br/>
                        형식으로 다시 조립하고 싶을 때 위 버튼을 클릭하세요.
                    </div>
                 </div>
              </div>
            </div>

            <!-- 미선택 안내 -->
            <div class="card border shadow-sm flex-shrink-0 d-flex align-items-center justify-content-center bg-white" style="width: 440px;" v-else>
               <div class="text-center opacity-50">
                  <i class="bi bi-cursor-fill mb-2" style="font-size: 2rem;"></i>
                  <div class="small">분개 행을 선택하면<br/>상세 입력창이 나타납니다.</div>
               </div>
            </div>

          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick, watch } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'
import { useAuthStore } from '@/stores/authStore'
import { useFormReset } from '@/composables/useFormReset'
import { useCommonHelp } from '@/composables/useCommonHelp'
import { getDate } from '@/composables/useDate'
import { useRoute } from 'vue-router'
import AppAlert from '@/components/AppAlert.vue'
import Modal from '@/components/Modal.vue'
import DateForm from '@/components/DateForm.vue'

const authStore = useAuthStore()
const route = useRoute()
const { firstDay, today } = getDate()
const { showAlert, showError, alertMessage, vAlert, vAlertError } = useAlerts()
const { resetForm } = useFormReset()
const { modalVisible, modalProps, openHelp } = useCommonHelp()

// [0] 💡 데이터 키 소문자 정규화 헬퍼 (모든 키를 소문자로 변환, 문자열은 Trim)
const normalizeKeys = (obj: any) => {
    if (!obj) return obj;
    const normalized = Object.fromEntries(
        Object.entries(obj).map(([k, v]) => [k.toLowerCase(), v && typeof v === 'string' ? v.trim() : v])
    );

    // 💡 사용자 지시: 조회 시 SUBCD = trim(Rs("CUSTCD")), SUBNM = trim(Rs("CUSTNM")) 로 변환하여 로드
    const custCd = normalized.custcd || normalized.subcd || normalized.docno2 || normalized.docno9 || '';
    const custNm = normalized.custnm || normalized.subnm || normalized.docnm2 || normalized.docnm4 || normalized.docnm9 || '';

    if (custCd) normalized.subcd = custCd;
    if (custNm) normalized.subnm = custNm;

    // 💡 기타 명칭 필드 동기화
    if (!normalized.docno2nm) normalized.docno2nm = normalized.docnm2 || custNm;
    if (!normalized.docno4) normalized.docno4 = normalized.docnm4 || custNm;
    if (!normalized.docno9nm) normalized.docno9nm = normalized.docnm9 || custNm;

    // 💡 날짜 필드 포맷팅 (YYYYMMDD -> YYYY-MM-DD)
    ['docno6', 'docno7', 'payymd'].forEach(key => {
        if (normalized[key] && normalized[key].length === 8 && !normalized[key].includes('-')) {
            const v = normalized[key];
            normalized[key] = `${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}`;
        }
    });

    return normalized;
};

// [1] 데이터 모델링
const searchParams = reactive({ fromdt: firstDay, todt: today, business: '' })
const masterForm = reactive<any>({
  cmpycd: authStore.cmpycd, deptcd: authStore.deptcd, deptnm: authStore.deptnm,
  slipymd: today, slipno: '', empnm: authStore.usernm, business: '',
  acctymd: today, slipgu: '020'
})

const slipListGridRef = ref<HTMLElement | null>(null)
const mainGridRef = ref<HTMLElement | null>(null)
let grid1: Tabulator | null = null
let grid2: Tabulator | null = null
const selectedRow = ref<any>(null)
const activeRowComponent = ref<any>(null)

// 옵션 데이터
const purchaseVatOptions = ref<any[]>([]); const salesVatOptions = ref<any[]>([])
const bizPlaceOptions = ref<any[]>([]);
const entTypeOptions = [{code:'01', name:'카드'}, {code:'02', name:'현금'}, {code:'03', name:'세금계산서'}]

const dynamicVatOptions = computed(() => {
  if (!selectedRow.value) return []
  const dbcr = String(selectedRow.value.dbcr || '').trim().toUpperCase();
  return dbcr === 'D' ? purchaseVatOptions.value : salesVatOptions.value
})

const totalDebit = ref(0); const totalCredit = ref(0)
const balance = computed(() => totalDebit.value - totalCredit.value)
const formatMoney = (val: any) => Number(val || 0).toLocaleString()

// 💡 [실시간 연동] 상세 패널 수정 시 그리드 행 데이터와 즉시 동기화
watch(selectedRow, (newVal) => {
    if (newVal && activeRowComponent.value) {
        activeRowComponent.value.update(newVal);
        updateAutoRemark();
    }
}, { deep: true });

// 💡 라우트 변경 감지 (다른 화면에서 링크 클릭 시)
watch(() => route.query, (newQuery) => {
    if (newQuery.slipymd && newQuery.slipno) {
        handleIncomingSlip(String(newQuery.slipymd), String(newQuery.slipno));
    }
}, { deep: true });

function updateAutoRemark(force = false) {
    const row = selectedRow.value;
    if (!row) return;

    let prefix = "";
    if (row.subnm) prefix = `[${row.subnm}] `;
    else if (row.docno4) prefix = `[${row.docno4}] `;
    else if (row.mgtnm) prefix = `[${row.mgtnm}] `;
    else if (row.acctnm) prefix = `[${row.acctnm}] `;

    const currentRemark = (row.remark || "").trim();
    if (force || !currentRemark || currentRemark.startsWith('[') || currentRemark === masterForm.business) {
        row.remark = prefix + (masterForm.business || "");
    }
}

function syncRemarkToRows() {
    grid2?.getRows().forEach(row => {
        const d = row.getData();
        if (!d.remark || d.remark.startsWith('[') || d.remark === "") {
            let prefix = d.subnm ? `[${d.subnm}] ` : (d.mgtnm ? `[${d.mgtnm}] ` : (d.acctnm ? `[${d.acctnm}] ` : ""));
            row.update({ remark: prefix + masterForm.business });
        }
    });
}

// [2] 그리드 초기화
const initGrids = () => {
  grid1 = new Tabulator(slipListGridRef.value!, {
    layout: "fitColumns", height: "100%", placeholder: "데이터 없음",
    columns: [
      { title: "No", formatter: "rownum", width: 40, hozAlign: "center", headerSort: false },
      { title: "발행일", field: "slipymd", width: 95, hozAlign: "center", formatter: (c) => {
          const v = c.getValue(); return v && v.length === 8 ? `${v.substring(0,4)}-${v.substring(4,6)}-${v.substring(6,8)}` : v;
      }},
      { title: "번호", field: "slipno", width: 50, hozAlign: "center", cssClass: "fw-bold text-primary", visible: false },
      { title: "거래내역", field: "business", hozAlign: "left", headerSort: false }
    ],
  });
  grid1.on("rowClick", (e, row) => fetchDetail(row.getData()));

  grid2 = new Tabulator(mainGridRef.value!, {
    layout: "fitColumns", height: "100%", selectable: 1,
    columnDefaults: {
        headerHozAlign: 'center', headerSort: false, vertAlign: "middle",
        // 💡 중앙 그리드 어느 셀이든 클릭 시 우측 관리항목 로드 보장
        cellClick: (e, cell) => {
            cell.getRow().select();
            selectedRow.value = normalizeKeys(cell.getRow().getData());
            activeRowComponent.value = cell.getRow();
        }
    },
    columns: [
      { title: "No", field: "srowno", width: 40, hozAlign: "center" },
      { title: "차/대", field: "dbcr", width: 70, hozAlign: "center", editor: "list", editorParams: { values: { "D": "차변", "C": "대변" } },
        formatter: (c) => {
            const v = String(c.getValue() || '').trim().toUpperCase();
            return v === 'D' ? '<b class="text-primary">차변</b>' : (v === 'C' ? '<b class="text-danger">대변</b>' : v);
        }
      },
      { title: "계정과목", field: "acctnm", width: 160, cellClick: (e, cell) => handleOpenHelp('ACCT', cell.getRow()), cssClass: "cursor-pointer text-primary fw-bold" },
      { title: "적요(자동조합)", field: "remark", minWidth: 200, editor: "input", cssClass: "bg-light-yellow" },
      { title: "금액", field: "amount", width: 110, hozAlign: "right", editor: "number", formatter: "money", formatterParams: { precision: 0 } },
      { title: "관리내역", field: "mgt_summary", width: 130, formatter: (c) => {
          const d = c.getData(); let s = [];
          if (d.subnm) s.push(d.subnm); else if (d.docno4) s.push(d.docno4);
          if (d.mgtnm) s.push(d.mgtnm);
          return `<span class="small text-muted text-truncate d-block">${s.join(', ')}</span>`;
      }},
      { title: "삭제", width: 40, hozAlign: "center", formatter: () => '<i class="bi bi-trash text-danger"></i>', cellClick: (e, cell) => cell.getRow().delete() }
    ]
  });

  grid2.on("cellEdited", (cell) => {
    selectedRow.value = normalizeKeys(cell.getRow().getData());
    activeRowComponent.value = cell.getRow();
    updateTotals();
  });

  grid2.on("rowSelected", (row) => {
    selectedRow.value = normalizeKeys(row.getData());
    activeRowComponent.value = row;
  });
}

const updateTotals = () => {
  const data = grid2?.getData() || []
  totalDebit.value = data.reduce((s, r: any) => s + (String(r.dbcr || '').trim().toUpperCase() === 'D' ? Number(r.amount || 0) : 0), 0)
  totalCredit.value = data.reduce((s, r: any) => s + (String(r.dbcr || '').trim().toUpperCase() === 'C' ? Number(r.amount || 0) : 0), 0)
}

// [3] 비즈니스 로직
async function searchSlips() {
  try {
    const res = await api.post('/api/hasl/HASL_110U_STR', {
      actkind: 'F', cmpycd: authStore.cmpycd,
      fromdt: searchParams.fromdt.replace(/-/g, ''),
      todt: searchParams.todt.replace(/-/g, ''),
      keyword: searchParams.business
    });
    const list = (res.data || []).map((item: any) => normalizeKeys(item));
    grid1?.setData(list);
    return list;
  } catch (e) { vAlertError('조회 실패'); }
}

async function handleIncomingSlip(slipymd: string, slipno: string) {
    const formattedYmd = slipymd.length === 8 ? `${slipymd.substring(0,4)}-${slipymd.substring(4,6)}-${slipymd.substring(6,8)}` : slipymd;
    searchParams.fromdt = formattedYmd;
    searchParams.todt = formattedYmd;
    searchParams.business = '';

    const data = await searchSlips();

    if (data && data.length > 0) {
        const target = data.find((d: any) => String(d.slipno) === String(slipno));
        if (target) {
            fetchDetail(target);
            nextTick(() => {
                const rows = grid1?.getRows();
                const row = rows?.find(r => String(r.getData().slipno) === String(slipno));
                row?.select();
            });
        }
    }
}

async function fetchDetail(row: any) {
  const master = normalizeKeys(row);
  Object.assign(masterForm, master);

  if (masterForm.slipymd && masterForm.slipymd.length === 8) {
    const d = masterForm.slipymd; masterForm.slipymd = `${d.substring(0,4)}-${d.substring(4,6)}-${d.substring(6,8)}`;
  }
  if (masterForm.acctymd && masterForm.acctymd.length === 8) {
    const d = masterForm.acctymd; masterForm.acctymd = `${d.substring(0,4)}-${d.substring(4,6)}-${d.substring(6,8)}`;
  }
  try {
    const resD = await api.post('/api/hasl/HASL_111U_STR', {
      actkind: 'S', cmpycd: authStore.cmpycd, slipymd: String(masterForm.slipymd).replace(/-/g,''), slipno: masterForm.slipno
    });

    const details = (resD.data || []).map((rowItem: any) => {
        const d = normalizeKeys(rowItem);
        return {
            ...d,
            dbcr: (d.dbcr || (Number(d.dbamt || 0) > 0 ? 'D' : 'C')).toUpperCase(),
            amount: Number(d.dbamt || 0) > 0 ? d.dbamt : d.cramt,
            typedc: (d.typedc || d.drcr || '').toUpperCase()
        };
    });

    grid2?.setData(details); updateTotals();

    if (details.length > 0) {
        setTimeout(() => {
            const firstRow = grid2?.getRows()[0];
            if (firstRow) firstRow.select();
        }, 150);
    }
  } catch (e) { vAlertError('상세 조회 실패'); }
}

function handleOpenHelp(type: string, target?: any) {
  if (type === 'DEPT') openHelp('DEPT', (d) => { masterForm.deptcd = d.deptcd; masterForm.deptnm = d.deptnm });
  else if (type === 'ACCT') {
    openHelp('ACCT', (d: any) => {
        const res = normalizeKeys(d);
        const typeacct = String(res.typeacct || '').trim();
        target.update({
            acctcd: res.acctcd,
            acctnm: res.acctnm,
            typeacct: typeacct,
            typemgt: res.typemgt,
            typesub: res.typesub,
            typedc: res.typedc,
            frgnyn: res.frgnyn,
            titmgt: res.typemgtnm,
            titsub: res.typesubnm,
            typebugt: res.bugtcd,
            docno3: typeacct === '100' ? '01' : '',
            docno6: (typeacct === '100' || typeacct === '090') ? today : ''
        });
        selectedRow.value = normalizeKeys(target.getData());
        activeRowComponent.value = target;
    });
  } else if (type === 'MGT' || type === 'CARD') {
      const mgtgbn = type === 'CARD' ? '040' : selectedRow.value.typemgt;
      openHelp('MGT', (d) => {
          const res = normalizeKeys(d);
          if (type === 'CARD' && String(selectedRow.value.typeacct || '').trim() === '090') {
              selectedRow.value.docno4 = res.mgtno;
          } else {
              selectedRow.value.mgtno = res.mgtno;
              selectedRow.value.mgtnm = res.mgtnm;
          }
          updateAutoRemark();
      }, { acctcd: selectedRow.value.acctcd, mgtgbn: mgtgbn });
  } else if (type === 'SUB' || type === 'vat_cust') {
      openHelp('CUST', (d) => {
        const res = normalizeKeys(d);
        // 💡 사용자 지시: CUSTCD/CUSTNM -> SUBCD/SUBNM 로 변환하여 로드
        selectedRow.value.subcd = res.custcd;
        selectedRow.value.subnm = res.custnm;

        // 💡 코드값 동기화 (저장 시 custcd로 매핑됨)
        selectedRow.value.custcd = res.custcd;
        selectedRow.value.custnm = res.custnm;

        // 부가세 거래처 필드도 동기화
        selectedRow.value.docno2 = res.busino || res.custcd;
        selectedRow.value.docno2nm = res.custnm;
        selectedRow.value.docno9nm = res.custnm;
      });
  } else if (type === 'PRJ') {
      openHelp('PRJ', (d) => { selectedRow.value.prjcd = d.prjcd; selectedRow.value.prjnm = d.prjnm });
  } else if (type === 'BUGT') {
      openHelp('BUGT', (d) => { selectedRow.value.bugtcd = d.bugtcd; selectedRow.value.bugtnm = d.bugtnm }, { acctcd: selectedRow.value.acctcd });
  }
}

const calcVat = () => { if (String(selectedRow.value?.typeacct || '').trim() === '090') selectedRow.value.docno9 = Math.floor(Number(selectedRow.value.docno8 || 0) * 0.1); }

async function save() {
  if (balance.value !== 0) return vAlertError('차/대변 금액이 일치하지 않습니다.');
  if (!masterForm.business) return vAlertError('거래내역을 입력하십시오.');
  try {
    const payload = {
      actkind: masterForm.slipno ? 'U' : 'A',
      MASTER: { ...masterForm, slipymd: masterForm.slipymd.replace(/-/g, ''), acctymd: masterForm.acctymd.replace(/-/g, '') },
      DETAILS: grid2?.getData().map(d => {
        const item = { ...d };
        // 💡 사용자 지시: 저장 시 UI의 subcd를 백엔드의 custcd로 역매핑
        item.custcd = item.subcd;

        // 💡 날짜 필드 원복 (YYYY-MM-DD -> YYYYMMDD)
        ['docno6', 'docno7', 'payymd'].forEach(key => {
            if (item[key]) item[key] = item[key].replace(/-/g, '');
        });

        return {
            ...item,
            dbamt: String(item.dbcr || '').trim().toUpperCase() === 'D' ? (item.amount || 0) : 0,
            cramt: String(item.dbcr || '').trim().toUpperCase() === 'C' ? (item.amount || 0) : 0,
            upkind: item.upkind || (masterForm.slipno ? 'U' : 'A')
        };
      })
    };
    await api.post('/api/hasl/HASL_110U_SAVE', payload); vAlert('저장되었습니다.'); searchSlips();
  } catch (e) { vAlertError('저장 실패'); }
}

const initialize = () => {
    resetForm(masterForm);
    Object.assign(masterForm, { cmpycd: authStore.cmpycd, deptcd: authStore.deptcd, deptnm: authStore.deptnm, slipymd: today, acctymd: today, empnm: authStore.usernm, slipgu: '020' });
    grid2?.clearData(); selectedRow.value = null; totalDebit.value = 0; totalCredit.value = 0;
}

const addRow = () => {
    const data = grid2?.getData() || [];
    const lastRow = data.length > 0 ? data[data.length - 1] : null;
    const newDbcr = lastRow ? (String(lastRow.dbcr || '').trim().toUpperCase() === 'D' ? 'C' : 'D') : 'D';

    grid2?.addRow(normalizeKeys({
        dbcr: newDbcr, remark: masterForm.business, amount: 0, upkind: 'A', srowno: data.length + 1
    }), false).then(row => {
        nextTick(() => { row.select(); grid2?.scrollToRow(row, "bottom", false); });
    });
}

const print = () => window.open(`/api/hasl/HASL_SLIP_PRINT?slipymd=${masterForm.slipymd.replace(/-/g, '')}&slipno=${masterForm.slipno}&deptcd=${masterForm.deptcd}`)

const deleteData = async () => {
    if (!confirm('전표를 삭제하시겠습니까?')) return;
    try {
        await api.post('/api/hasl/HASL_110U_SAVE', { actkind: 'D', MASTER: { ...masterForm, slipymd: masterForm.slipymd.replace(/-/g, '') } });
        vAlert('삭제되었습니다.'); initialize(); searchSlips();
    } catch (e) { vAlertError('삭제 실패'); }
}

onMounted(async () => {
  api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '120' }).then(r => {
    purchaseVatOptions.value = (r.data || []).map((i: any) => ({ codecd: i.codecd, codenm: i.codenm }));
  });
  api.post('/api/ha00/HA00_00P_STR', { gubun: 'E0', gbncd: '130' }).then(r => {
    salesVatOptions.value = (r.data || []).map((i: any) => ({ codecd: i.codecd, codenm: i.codenm }));
  });
  api.post('/api/ha00/HA00_00P_STR', { gubun: 'SA', cmpycd: authStore.cmpycd }).then(r => {
    bizPlaceOptions.value = (r.data || []).map((i: any) => ({
      code: i.taxunit || i.code || i.whcd || '',
      name: i.unitnm || i.codenm || i.whnm || ''
    }));
  });
  nextTick(() => {
    initGrids();
    if (route.query.slipymd && route.query.slipno) {
        handleIncomingSlip(String(route.query.slipymd), String(route.query.slipno));
    } else {
        searchSlips();
    }
  });
})
</script>

<style scoped>
.tabulator-instance { width: 100% !important; background-color: #fff; }
.cursor-pointer { cursor: pointer; }
.bg-light-yellow { background-color: #fffdf0 !important; }
.custom-scrollbar { overflow-y: auto; scrollbar-width: thin; }
:deep(.tabulator-row-selected) { background-color: #e7f1ff !important; }

/* 💡 사이드바 테이블 스타일 최적화 (표준화 및 간격 극한 축소) */
.side-detail-table {
    border-collapse: collapse;
}
.side-detail-table th {
    padding: 2px 10px !important;
    font-size: 12px;
    font-weight: 600;
    color: #555;
    background-color: #f8f9fa;
    border-right: 1px solid #eee;
    border-bottom: 1px solid #eee !important;
    white-space: nowrap;
    vertical-align: middle;
    height: 25px; /* 높이 축소 */
}
.side-detail-table td {
    padding: 1px 6px !important;
    border-bottom: 1px solid #eee !important;
    vertical-align: middle;
}
.side-detail-table .form-control-sm,
.side-detail-table .form-select-sm {
    height: 22px; /* 컨트롤 높이 축소 */
    padding: 0 6px;
    font-size: 12px;
    border: 1px solid #ced4da;
    border-radius: 2px;
}
.side-detail-table .form-control-sm:focus {
    border-color: #86b7fe;
    box-shadow: none;
}
.bg-warning-subtle { background-color: #fffcf0 !important; }
.bg-info-subtle { background-color: #f0faff !important; }

/* 필수 항목 마크 */
.side-detail-table th.required::after {
    content: " *";
    color: #dc3545;
}
</style>
