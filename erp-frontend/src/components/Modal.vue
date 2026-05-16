<template>
	<AppAlert :show="showAlert" :error="showError" :message="alertMessage" />
	<!-- 💡 확실한 배경과 z-index 고정 -->
	<div v-if="visible" class="modal fade show d-block" tabindex="-1" style="background: rgba(0, 0, 0, 0.7); z-index: 1060;">
		<div :class="props.modalProps.large ? 'modal-dialog modal-xl modal-dialog-centered' : 'modal-dialog modal-dialog-centered'" style="max-height: 95vh;">
			<div class="modal-content border-0 shadow-lg" style="border-radius: 6px; overflow: hidden;">
				<!-- 🚀 헤더 디자인 -->
				<div class="modal-header py-2 bg-primary text-white border-0">
					<h5 class="modal-title fw-bold" style="font-size: 15px;">
						<i class="bi bi-search me-2"></i>{{ props.modalProps.title || '데이터 조회' }}
					</h5>
					<button type="button" class="btn-close btn-close-white" @click="close"></button>
				</div>

				<div class="modal-body p-3 bg-white">
					<!-- 🔍 검색 영역 -->
					<div class="d-flex gap-2 align-items-center mb-3">
						<div class="input-group input-group-sm" style="width: 450px;">
							<select v-model="filterField" class="form-select bg-light fw-bold" style="max-width: 140px">
								<option v-for="opt in fieldOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
							</select>
							<input v-model="filterValue" type="text" class="form-control border-primary-subtle" placeholder="검색어 입력 후 엔터..." @keyup.enter="search" />
							<button class="btn btn-primary px-3 fw-bold" @click="search">검색 (F)</button>
						</div>
						<div class="small text-muted ms-auto"><i class="bi bi-info-circle me-1"></i> 항목을 선택(클릭)하여 적용하십시오.</div>
					</div>

					<!-- 📊 그리드 영역: 명시적 높이 500px 부여 -->
					<div class="popup-grid-wrapper border rounded bg-white shadow-sm" style="height: 500px; width: 100%; position: relative;">
						<div ref="popupRef" style="height: 100%; width: 100%;"></div>
					</div>
				</div>

				<div class="modal-footer py-2 bg-light border-top text-end">
					<button type="button" class="btn btn-sm btn-secondary px-4 fw-bold" @click="close">닫기</button>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, computed } from 'vue'
import { TabulatorFull as Tabulator } from 'tabulator-tables'
// 💡 표준 CSS 로드
import 'tabulator-tables/dist/css/tabulator.min.css'
import { api } from '@/utils/axios'
import AppAlert from '@/components/AppAlert.vue'
import { useAlerts } from '@/composables/useAlerts'
import type { ModalProps } from '@/types/modal'

const props = defineProps<{ visible: boolean, modalProps: ModalProps }>()
const emit = defineEmits(['update:visible'])
const { showAlert, showError, alertMessage } = useAlerts()

const popupRef = ref<HTMLElement | null>(null)
const popupGrid = ref<Tabulator | null>(null)
const filterField = ref<string>('')
const filterValue = ref<string>('')

watch(() => props.visible, async (isVisible) => {
	if (!isVisible) {
		if (popupGrid.value) { popupGrid.value.destroy(); popupGrid.value = null; }
		filterValue.value = '';
		return
	}

	await nextTick()

	// 💡 모달이 화면에 완전히 나타난 후(애니메이션 종료) 그리드 생성
	setTimeout(() => {
		if (!popupRef.value) return

		popupGrid.value = new Tabulator(popupRef.value, {
			layout: 'fitColumns',
			height: '100%',
			data: [],
			columns: props.modalProps.columns || [],
			columnDefaults: {
				headerHozAlign: 'center',
				headerSort: true,
				minWidth: 80
			},
			placeholder: "데이터를 조회 중입니다..."
		})

		// 초기 검색 필드 설정
		filterField.value = props.modalProps.defaultField || (props.modalProps.columns?.[0]?.field || '')

		// 💡 지연 없이 즉시 데이터 로드 실행
		search()

		popupGrid.value.on('rowClick', (_e, row) => {
			if (props.modalProps.type === 'table') {
				props.modalProps.onConfirm?.(row.getData())
				close()
			}
		})
	}, 300)
})

async function search() {
	if (!popupGrid.value) return
	try {
		const body = {
			...props.modalProps.data,
			GBNCD: props.modalProps.data.GBNCD || '',
			CODE: props.modalProps.data.CODE || '',
			CODENM: filterValue.value || '',
			ETCVAL: props.modalProps.data.ETCVAL || ''
		}

		const res = await api.post(props.modalProps.path, body)

		if (popupGrid.value) {
			await popupGrid.value.setData(res.data || [])
			// 💡 강제로 그리드를 다시 그려서 투명화 현상 방지
			popupGrid.value.redraw(true)
		}
	} catch (error) {
		console.error('팝업 조회 실패:', error)
	}
}

function close() { emit('update:visible', false) }

const fieldOptions = computed(() => {
	const cols = props.modalProps.columns || []
	return cols.filter((c: any) => c.field).map((c: any) => ({ label: c.title || c.field, value: c.field }))
})
</script>

<style>
/* 💡 표준 폰트 및 고밀도 디자인 강제 적용 (scoped 제거하여 전역 전파 보장) */
.popup-grid-wrapper .tabulator {
	border: none !important;
	font-size: 12.5px !important;
	background: #fff !important;
}
.popup-grid-wrapper .tabulator-header {
	background-color: #f1f5f9 !important;
	border-bottom: 2px solid #dee2e6 !important;
}
.popup-grid-wrapper .tabulator-col-title {
	line-height: 1.3 !important;
	text-align: center !important;
	font-weight: 800 !important;
	color: #334155 !important;
}
.popup-grid-wrapper .tabulator-cell {
	padding: 8px 4px !important;
	border-right: 1px solid #f1f5f9 !important;
}
.popup-grid-wrapper .tabulator-row.tabulator-selected {
	background-color: #e2e8f0 !important;
}
</style>
