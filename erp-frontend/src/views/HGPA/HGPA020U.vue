<!--
	=============================================================
	프로그램명	  : 수신그룹 관리 (소문자 표준 적용)
    프로그램 ID	: HGPA020U
	작성일자	    : 25.03.06
	작성자	      : AI Assistant
	=============================================================
-->

<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	
    <!-- 수신그룹 상세 정책 설정 모달 -->
    <teleport to="body">
        <div v-if="show_queue_modal" class="modal-overlay" @click.self="show_queue_modal = false">
            <div class="modal-content card shadow-lg border-0" style="width: 550px;">
                <div class="card-header bg-primary text-white fw-bold py-2 d-flex justify-content-between">
                    <span><i class="bi bi-patch-question-fill me-2"></i>수신그룹 정책 설정 (Queue Policy)</span>
                    <button type="button" class="btn-close btn-close-white" @click="show_queue_modal = false"></button>
                </div>
                <div class="card-body p-3 text-start">
                    <table class="table table-sm table-bordered mb-2 small form-table">
                        <colgroup><col style="width: 30%" /><col style="width: 70%" /></colgroup>
                        <tbody>
                            <tr>
                                <th class="bg-light fw-bold text-end pe-2">그룹명 (ID)</th>
                                <td><input v-model="temp_queue.name" class="form-control form-control-sm fw-bold" placeholder="예: 8000" /></td>
                            </tr>
                            <tr>
                                <th class="bg-light fw-bold text-end pe-2">전략 (Strategy)</th>
                                <td>
                                    <select v-model="temp_queue.strategy" class="form-select form-select-sm border-primary fw-bold text-primary">
                                        <option value="ringall">전체벨 (ringall)</option>
                                        <option value="linear">순차벨 (linear)</option>
                                        <option value="leastrecent">최근휴무순 (leastrecent)</option>
                                        <option value="fewestcalls">최소통화순 (fewestcalls)</option>
                                        <option value="random">무작위 (random)</option>
                                        <option value="roundrobin">순환벨 (roundrobin)</option>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2" class="p-3 border-0" style="background-color: #fff9db;">
                                    <div class="fw-bold mb-1" style="color: #856404; font-size: 0.8rem;"><i class="bi bi-lightbulb-fill me-1"></i> 전략 동작 가이드:</div>
                                    <div class="fw-bold" style="color: #533f03; line-height: 1.5; font-size: 0.85rem;">{{ strategy_guides[temp_queue.strategy] }}</div>
                                </td>
                            </tr>
                            <tr>
                                <th class="bg-light fw-bold text-end pe-2">타임아웃 (초)</th>
                                <td>
                                    <div class="d-flex align-items-center gap-2">
                                        <input v-model.number="temp_queue.timeout" type="number" class="form-control form-control-sm w-25 fw-bold" />
                                        <span class="text-danger fw-bold" style="font-size: 0.7rem;">(벨 울림 제한 시간)</span>
                                    </div>
                                </td>
                            </tr>
                            <tr><th class="bg-light fw-bold text-end pe-2">컨텍스트 (Context)</th><td><input v-model="temp_queue.context" class="form-control form-control-sm" /></td></tr>
                            <tr><th class="bg-light fw-bold text-end pe-2">대기음 (MOH)</th><td><input v-model="temp_queue.musiconhold" class="form-control form-control-sm" /></td></tr>
                        </tbody>
                    </table>
                    <div class="d-flex justify-content-end gap-2 mt-3">
                        <button class="btn btn-sm btn-secondary px-3" @click="show_queue_modal = false">닫기</button>
                        <button class="btn btn-sm btn-primary px-4 fw-bold shadow-sm" @click="confirm_queue_add">정책 적용</button>
                    </div>
                </div>
            </div>
        </div>
    </teleport>

    <div class="hgpa020-wrapper bg-light text-start p-2 h-100">
        <!-- 1. 상단 메인 툴바 -->
        <div class="d-flex justify-content-between align-items-center mb-1 bg-white p-2 rounded shadow-sm border border-secondary-subtle">
            <div class="fw-bold text-dark small"><i class="bi bi-diagram-3-fill me-2 text-primary"></i>수신그룹 관리 (Queue Management)</div>
            <div class="btn-group shadow-sm">
                <button class="btn btn-sm btn-outline-secondary px-3" @click="initialize">초기화</button>
                <button class="btn btn-sm btn-dark px-3" @click="search_queues">조회</button>
                <button class="btn btn-sm btn-primary px-4 fw-bold" @click="save_all"><i class="bi bi-save me-1"></i>리얼타임 적용</button>
            </div>
        </div>

        <div class="row g-1 content-area flex-grow-1 overflow-hidden mt-1">
            <!-- 2. 좌측: 수신그룹 목록 -->
            <div class="col-md-2 h-100">
                <div class="card shadow-sm border-0 h-100 d-flex flex-column border-top border-3 border-dark overflow-hidden">
                    <div class="card-header bg-white py-1 px-2 fw-bold small d-flex justify-content-between align-items-center border-bottom">
                        <span>Queue</span>
                        <button class="btn btn-xs btn-outline-primary fw-bold py-0 shadow-sm" @click="open_queue_add_modal">추가</button>
                    </div>
                    <div class="card-body p-0 flex-grow-1 position-relative">
                        <div ref="queue_table_ref" class="tabulator-full-height" />
                    </div>
                </div>
            </div>

            <!-- 3. 중앙: 소속 상담원 -->
            <div class="col h-100">
                <div class="card shadow-sm border-0 h-100 d-flex flex-column border-top border-3 border-primary overflow-hidden">
                    <div class="card-header bg-white py-1 px-2 fw-bold small d-flex justify-content-between align-items-center border-bottom">
                        <span>Assigned</span>
                    </div>
                    <div class="card-body p-0 bg-white flex-grow-1">
                        <div ref="member_table_ref" class="tabulator-full-height" />
                    </div>
                </div>
            </div>

            <!-- 4. 이동 버튼 -->
            <div class="col-auto d-flex flex-column justify-content-center px-0 gap-2 mx-1">
                <button class="btn btn-primary shadow-sm mini-move-btn" @click="move_right_to_left"><i class="bi bi-chevron-left"></i></button>
                <button class="btn btn-outline-secondary shadow-sm mini-move-btn" @click="move_left_to_right"><i class="bi bi-chevron-right"></i></button>
            </div>

            <!-- 5. 우측: 전체 내선 명단 (Available) -->
            <div class="col h-100">
                <div class="card shadow-sm border-0 h-100 d-flex flex-column border-top border-3 border-secondary overflow-hidden">
                    <div class="card-header bg-white py-1 fw-bold small d-flex justify-content-between border-bottom">
                        <div class="d-flex align-items-center gap-2">
                            <span>Available (PJSIP)</span>
                            <input type="text" v-model="search_agent_name" @input="filter_agents" class="form-control form-control-sm py-0 ms-2" placeholder="내선/상담원 검색" style="width: 140px; height: 22px;">
                        </div>
                    </div>
                    <div class="card-body p-0 flex-grow-1">
                        <div ref="agent_table_ref" class="tabulator-full-height" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
import 'tabulator-tables/dist/css/tabulator_bootstrap5.min.css'
import { useAlerts } from '@/composables/useAlerts'
import { api } from '@/utils/axios'

const { showAlert, showError, vAlert, vAlertError, alertMessage } = useAlerts()

// 💡 상태 변수 대문자 통일
const selected_queue_name = ref<string | null>(null)
const search_agent_name = ref('')
const show_queue_modal = ref(false)

const strategy_guides: Record<string, string> = {
    ringall: "모든 상담원의 전화가 동시에 울립니다. 가장 먼저 받는 상담원에게 연결됩니다.",
    linear: "등록된 상담원 순서대로 벨이 울립니다. 항상 정해진 순번대로 전화를 배분합니다.",
    leastrecent: "가장 오랫동안 전화를 받지 않은(오래 쉰) 상담원에게 우선적으로 벨이 울립니다.",
    fewestcalls: "오늘 하루 통화 건수가 가장 적은 상담원에게 전화를 우선 배분합니다.",
    random: "상담원 순서와 상관없이 무작위로 벨이 울립니다.",
    roundrobin: "상담원들을 돌아가면서 균등하게 벨을 울립니다."
}

const temp_queue = reactive({ name: '', strategy: 'ringall', timeout: 15, context: 'from-internal', musiconhold: 'default' })

const queue_table_ref = ref<HTMLDivElement | null>(null)
const member_table_ref = ref<HTMLDivElement | null>(null)
const agent_table_ref = ref<HTMLDivElement | null>(null)

let queue_table_instance: Tabulator | null = null
let member_table_instance: Tabulator | null = null
let agent_table_instance: Tabulator | null = null

onMounted(() => {
	nextTick(() => {
        initTables();
        search_queues();
        search_all_endpoints();
    })
})
onUnmounted(() => {
    queue_table_instance?.destroy();
    member_table_instance?.destroy();
    agent_table_instance?.destroy();
})

function initTables() {
    // 💡 필드명 대문자 통일
    if (queue_table_instance) queue_table_instance.destroy();
	queue_table_instance = new Tabulator(queue_table_ref.value!, {
		placeholder: 'No Data', layout: 'fitColumns', selectable: 1, height: '100%',
		columns: [{ title: '그룹명 (Name)', field: 'name', hozAlign: 'left', headerSort: false, editor: 'input' }]
	})
	queue_table_instance.on("rowClick", (e, row) => {
        const data = row.getData();
		selected_queue_name.value = data.name;
		search_members(data.name);
	})

    if (member_table_instance) member_table_instance.destroy();
	member_table_instance = new Tabulator(member_table_ref.value!, {
		placeholder: '상담원 없음', layout: 'fitColumns', selectable: true, height: '100%',
		columns: [
			{ formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center", width: 40, headerSort: false },
			{ title: '상담원명', field: 'membername', hozAlign: 'center', editor: 'input' },
			{ title: '내선', field: 'interface_no', hozAlign: 'center', editor: 'input' },
			{ title: '순위', field: 'penalty', editor: 'number', hozAlign: 'center' },
            { title: '정리시간', field: 'wrapuptime', editor: 'number', hozAlign: 'center' },
			{ title: '상태', field: 'paused', editor: 'list', editorParams: { values: { "0": "정상", "1": "정지" } },
                hozAlign: 'center', formatter: (cell) => cell.getValue() == "1" ? "<span class='text-danger fw-bold'>정지</span>" : "<span class='text-success fw-bold'>정상</span>"
            }
		],
	})

    if (agent_table_instance) agent_table_instance.destroy();
	agent_table_instance = new Tabulator(agent_table_ref.value!, {
		placeholder: '내선 정보 없음', layout: 'fitColumns', selectable: true, height: '100%',
		columns: [
            { formatter: "rowSelection", titleFormatter: "rowSelection", hozAlign: "center", width: 40, headerSort: false },
			{ title: '내선번호', field: 'id', hozAlign: 'center' },
			{ title: '상담원명', field: 'callerid', hozAlign: 'center' }
		],
	})
}

const open_queue_add_modal = () => { Object.assign(temp_queue, { name: '', strategy: 'ringall', timeout: 15, context: 'from-internal', musiconhold: 'default' }); show_queue_modal.value = true; }
const confirm_queue_add = () => { if(!temp_queue.name) return vAlertError('그룹명을 입력하세요.'); queue_table_instance?.addRow({ ...temp_queue }, true); show_queue_modal.value = false; }

async function search_queues() {
    try { 
        const { data } = await api.get('/api/crm/asterisk/queue/search');
        console.log(data)
        queue_table_instance?.setData(data);
    } catch (e) {} 
}

async function search_all_endpoints() {
    try { 
        const { data } = await api.get('/api/crm/asterisk/pjsip/search');
        agent_table_instance?.setData(data);
    } catch (e) {} 
}

async function search_members(name: string) {
    try { 
        const { data } = await api.get('/api/crm/asterisk/queue/member/search', { params: { queue_name: name } });
        console.log("search_members:", data)
        member_table_instance?.setData(data);
    } catch (e) {} 
}

function move_right_to_left() {
    if (!selected_queue_name.value) return vAlertError('그룹을 먼저 선택하세요.');
    const agents = agent_table_instance?.getSelectedData();
    if (!agents || agents.length === 0) return vAlertError('내선을 선택하세요.');
    
    const current = member_table_instance?.getData() || [];
    agents.forEach(a => { 
        if (!current.find(m => m.interface_no === a.id)) {
            member_table_instance?.addRow({
                queue_name: selected_queue_name.value,
                membername: a.callerid || '상담원',
                interface_no: a.id,
                penalty: 0, paused: "0", wrapuptime: 0
            }); 
        }
    });
    agent_table_instance?.deselectRow();
}

function move_left_to_right() {
    const selectedRows = member_table_instance?.getSelectedRows();
    if (!selectedRows || selectedRows.length === 0) return vAlertError('상담원을 선택하세요.');
    selectedRows.forEach(row => row.delete());
}

async function save_all() {
	try {
        await api.post('/api/crm/asterisk/queue/save', queue_table_instance?.getData());
		if (selected_queue_name.value) {
            await api.post('/api/crm/asterisk/queue/member/save', {
                queue_name: selected_queue_name.value,
                members: member_table_instance?.getData()
            });
        }
		vAlert('리얼타임 적용 완료'); search_queues();
	} catch (e) { vAlertError('저장 실패') }
}

function initialize() { selected_queue_name.value = null; member_table_instance?.clearData(); queue_table_instance?.deselectRow(); vAlert('초기화 완료'); }
function filter_agents() {
    agent_table_instance?.setFilter([
        [ {field:"id", type:"like", value:search_agent_name.value}, {field:"callerid", type:"like", value:search_agent_name.value} ]
    ]);
}
</script>

<style scoped>
.hgpa020-wrapper { height: calc(100vh - 110px); display: flex; flex-direction: column; overflow: hidden; }
.content-area { flex-grow: 1; min-height: 0; }
.tabulator-full-height { height: 100%; border-top: 1px solid #dee2e6; font-size: 0.85rem; }
.mini-move-btn { width: 34px; height: 34px; display: flex; align-items: center; justify-content: center; }
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1050; display: flex; align-items: center; justify-content: center; }
.form-table th { background-color: #f8f9fa; font-size: 0.85rem; text-align: right; padding: 5px 10px; font-weight: bold; }
</style>
