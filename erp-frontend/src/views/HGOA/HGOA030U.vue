<!--
	=============================================================
	프로그램명	  : 질문 및 답변 등록 (대문자 표준 적용)
    프로그램 ID	: HGOA030U
	작성일자	    : 25.03.06
	작성자	      : AI Assistant
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />

    <div class="hgo030-wrapper bg-light text-start p-2 h-100 d-flex flex-column">
        <!-- 조회 조건 -->
        <div class="card shadow-sm border-0 mb-1 flex-shrink-0">
            <div class="card-body p-1 px-3">
                <div class="row align-items-center g-2">
                    <div class="col-auto">
                        <span class="badge bg-primary px-2 py-1 small"><i class="bi bi-search me-1"></i>질문 검색</span>
                    </div>
                    <div class="col-4">
                        <input v-model="search_form.QUESTION" class="form-control form-control-sm" placeholder="질문 내용을 입력하세요..." @keyup.enter="search" />
                    </div>
                    <div class="col-auto">
                        <button class="btn btn-sm btn-dark px-3 fw-bold" @click="search" style="height: 26px; font-size: 0.8rem;">조회</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="row g-1 mb-1 flex-shrink-0">
            <!-- [좌측] 질문 기본 정보 -->
            <div class="col-lg-4 col-md-5">
                <div class="card shadow-sm border-0 h-100 border-top border-3 border-secondary">
                    <div class="card-header bg-white py-1 fw-bold small border-bottom-0">
                        <i class="bi bi-info-circle-fill text-secondary me-1"></i>질문 기본 정보
                    </div>
                    <div class="card-body p-2 pt-0">
                        <table class="table table-sm form-table mb-0">
                            <colgroup><col style="width: 30%" /><col style="width: 70%" /></colgroup>
                            <tbody>
                                <tr>
                                    <th class="required small">질문내용</th>
                                    <td>
                                        <textarea v-model="mst_form.QUESTION" class="form-control form-control-sm shadow-none" rows="3" style="font-size: 0.85rem;" placeholder="질문 문구 입력"></textarea>
                                    </td>
                                </tr>
                                <tr>
                                    <th class="small">답변유형</th>
                                    <td>
                                        <select v-model="mst_form.ANS_TP" class="form-select form-select-sm shadow-none" @change="handle_type_change">
                                            <option value="010">객관식 (선택형)</option>
                                            <option value="020">주관식 (서술형)</option>
                                            <option value="030">혼합형 (객관+주관)</option>
                                        </select>
                                    </td>
                                </tr>
                                <tr>
                                    <th class="small">정렬순서</th>
                                    <td>
                                        <input v-model="mst_form.DSPORD" type="text" maxlength="3" class="form-control form-control-sm w-50 shadow-none" />
                                    </td>
                                </tr>
                                <tr>
                                    <th class="small">사용여부</th>
                                    <td>
                                        <div class="form-check form-switch p-0 ps-5">
                                            <input class="form-check-input ms-0" type="checkbox" v-model="mst_form.USEYN" true-value="Y" false-value="N">
                                            <label class="form-check-label small fw-bold ms-2" :class="is_used ? 'text-primary' : 'text-danger'">
                                                {{ is_used ? '사용함' : '미사용' }}
                                            </label>
                                        </div>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- [우측] 답변 상세 구성 -->
            <div class="col-lg-8 col-md-7">
                <div class="card shadow-sm border-0 h-100 border-top border-3 border-primary">
                    <div class="card-header bg-white py-1 fw-bold small d-flex justify-content-between align-items-center border-bottom-0">
                        <span><i class="bi bi-check-square-fill text-primary me-1"></i>답변 상세 구성</span>
                        <div class="btn-group">
                            <button class="btn btn-xs btn-outline-primary px-2" @click="initialize">신규</button>
                            <button class="btn btn-xs btn-primary px-3 fw-bold" @click="save"><i class="bi bi-save me-1"></i>저장</button>
                            <button class="btn btn-xs btn-outline-danger px-2" @click="delete_item">삭제</button>
                        </div>
                    </div>
                    <div class="card-body p-2 pt-0">
                        <div class="answer-scroll-container border rounded overflow-auto bg-white" style="height: 185px;">
                            <table class="table table-sm table-hover mb-0 text-center align-middle dtl-grid-table">
                                <thead class="table-light sticky-top">
                                    <tr class="small text-muted">
                                        <th style="width: 40px;">No</th>
                                        <th>답변 보기 내용 (상담원용 예문)</th>
                                        <th style="width: 60px;">배점</th>
                                        <th style="width: 60px;">사용</th>
                                        <th style="width: 80px;">직접입력</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr v-for="(item, index) in dtl_list" :key="index">
                                        <td class="small fw-bold text-secondary">{{ index + 1 }}</td>
                                        <td class="p-0 px-1">
                                            <input v-model="item.ANS_CONT" class="form-control form-control-sm border-0 bg-transparent py-0 shadow-none"
                                                   :disabled="mst_form.ANS_TP === '020' && index > 0" placeholder="상담 시 노출될 답변 내용 입력" />
                                        </td>
                                        <td class="p-0">
                                            <input v-model.number="item.ANS_POINT" type="number" class="form-control form-control-sm border-0 bg-transparent text-center py-0 shadow-none"
                                                   :disabled="mst_form.ANS_TP === '020'" />
                                        </td>
                                        <td><input v-model="item.USEYN" type="checkbox" true-value="Y" false-value="N" class="form-check-input shadow-none" /></td>
                                        <td><input v-model="item.ESSAY_YN" type="checkbox" true-value="Y" false-value="N" class="form-check-input shadow-none" /></td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 하단 목록 -->
        <div class="card shadow-sm border-0 flex-grow-1 overflow-hidden d-flex flex-column">
            <div class="card-header bg-dark text-white py-1 px-3 fw-bold small d-flex justify-content-between align-items-center">
                <span><i class="bi bi-collection-fill me-2 text-info"></i>질문 및 답변 통합 데이터베이스</span>
            </div>
            <div class="card-body p-0 bg-white flex-grow-1 position-relative">
                <div ref="table_ref" class="tabulator-full-height" />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

const search_form = reactive({ QUESTION: '' })
const mst_form = reactive({ CMPYCD: '', SURV_NO: '', QUESTION: '', ANS_TP: '010', DSPORD: '001', USEYN: 'Y' })
const dtl_list = ref<any[]>([])

const is_used = computed(() => (mst_form.USEYN || "").toString().toUpperCase() === 'Y')

function create_empty_dtl(ansNo: string) { return { ANS_NO: ansNo, ANS_CONT: '', ANS_POINT: 0, USEYN: 'Y', ESSAY_YN: 'N' } }
function reset_dtl() { dtl_list.value = []; for (let i = 1; i <= 8; i++) dtl_list.value.push(create_empty_dtl(String(i).padStart(3, '0'))) }

const handle_type_change = () => { if (mst_form.ANS_TP === '020') { reset_dtl(); dtl_list.value[0].ANS_CONT = '자유 서술형 응답'; dtl_list.value[0].ESSAY_YN = 'Y'; } }

const table_ref = ref<HTMLDivElement | null>(null)
let table_instance: Tabulator | null = null

onMounted(() => { reset_dtl(); init_table(); search(); })

function init_table() {
	if (!table_ref.value) return
	table_instance = new Tabulator(table_ref.value, {
		placeholder: '등록된 질문이 없습니다.', layout: "fitColumns", height: "100%", pagination: "local", paginationSize: 20,
		columns: [
            { title: "No", formatter: "rownum", hozAlign: "center", width: 50 },
            { title: "질문번호", field: "SURV_NO", hozAlign: "center", width: 100 },
            { title: "질문 내용", field: "QUESTION", hozAlign: "left", formatter: (cell) => `<div class="fw-bold text-dark py-1">${cell.getValue() || ''}</div>` },
            { title: "유형", field: "ANS_TP", hozAlign: "center", width: 80, formatter: (cell) => cell.getValue() === '010' ? '객관' : cell.getValue() === '020' ? '주관' : '혼합' },
            { title: "정렬", field: "DSPORD", hozAlign: "center", width: 60 },
            { title: "사용", field: "USEYN", hozAlign: "center", width: 60, formatter: (cell) => (cell.getValue()||'').toString().toUpperCase()==='Y' ? "O" : "X" }
        ]
	})
    table_instance.on("rowClick", (e, row) => load_detail(row.getData()));
}

async function search() {
	try {
		const { data } = await api.get('/crm/outbound/surv/mst/search', { params: search_form })
		table_instance?.setData(data)
	} catch (e) { vAlertError('조회 실패'); }
}

async function load_detail(data: any) {
    Object.assign(mst_form, data)
    try {
        const { data: details } = await api.get('/crm/outbound/surv/dtl/search', { params: { SURV_NO: mst_form.SURV_NO } });
        reset_dtl();
        if (details && details.length > 0) {
            details.forEach((d: any, idx: number) => {
                if (idx < 8) dtl_list.value[idx] = d;
            });
        }
    } catch (e) { vAlertError('상세 조회 실패'); }
}

async function save() {
    if (!mst_form.QUESTION) return vAlertError('질문 내용을 입력하세요.');
    const valid_dtl = dtl_list.value.filter(item => (item.ANS_CONT || "").trim() !== '');

    const payload = {
        MST: mst_form,
        DTL: valid_dtl
    };

    try {
        await api.post('/crm/outbound/surv/save', payload);
        vAlert('저장되었습니다.'); initialize(); search();
    } catch (e) { vAlertError('저장 실패'); }
}

async function delete_item() {
    if (!mst_form.SURV_NO) return vAlertError('대상 선택 필요');
    if (!confirm('삭제하시겠습니까?')) return;
    try {
        await api.post('/crm/outbound/surv/delete', { SURV_NO: mst_form.SURV_NO });
        vAlert('삭제되었습니다.'); initialize(); search();
    } catch (e) { vAlertError('삭제 실패'); }
}

function initialize() {
    Object.assign(mst_form, { CMPYCD: '', SURV_NO: '', QUESTION: '', ANS_TP: '010', DSPORD: '001', USEYN: 'Y' });
    reset_dtl();
    table_instance?.deselectRow();
}
</script>

<style scoped>
.hgo030-wrapper { height: calc(100vh - 110px); }
.form-table th { background-color: #f8f9fa; font-size: 0.8rem; text-align: right; padding: 6px 12px; font-weight: bold; border: 1px solid #dee2e6; }
.form-table td { padding: 4px 8px; border: 1px solid #dee2e6; }
.tabulator-full-height { height: 100% !important; }
.btn-xs { padding: 2px 8px; font-size: 0.75rem; font-weight: bold; }
</style>
