<template>
	<div class="d-flex gap-1 w-100 align-items-center">
		<!-- 📬 우편번호 및 검색 버튼 그룹 -->
		<div class="d-flex gap-1" style="min-width: 185px;">
			<input
				v-model="postno_val"
				type="text"
				class="form-control form-control-sm text-center bg-light"
				placeholder="우편번호"
				maxlength="6"
				readonly
			/>
			<!-- 1️⃣ 신규 주소 검색 (돋보기) -->
			<button
				type="button"
				class="btn btn-sm btn-dark"
				title="다음 주소 검색"
				@click="openPostcode"
			>
				<i class="bi bi-search"></i>
			</button>
			<!-- 2️⃣ 기존 배송지 팝업 (리스트) -->
			<button
				type="button"
				class="btn btn-sm btn-outline-secondary"
				title="기존 배송지 선택"
				@click="emit('open-address')"
			>
				<i class="bi bi-list-stars"></i>
			</button>
		</div>

		<!-- 🏠 기본주소 -->
		<input
			v-model="address_val"
			type="text"
			class="form-control form-control-sm flex-grow-1 bg-light"
			placeholder="기본주소"
			readonly
		/>

		<!-- ✏️ 상세주소 -->
		<input
			v-model="d_address_val"
			type="text"
			class="form-control form-control-sm"
			style="flex: 0 0 35%;"
			placeholder="상세주소 입력"
		/>
	</div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
	postno: String,
	address: String,
	d_address: String,
})

const emit = defineEmits(['update:postno', 'update:address', 'update:d_address', 'open-address'])

const postno_val = computed({ get: () => props.postno, set: (val) => emit('update:postno', val) })
const address_val = computed({ get: () => props.address, set: (val) => emit('update:address', val) })
const d_address_val = computed({ get: () => props.d_address, set: (val) => emit('update:d_address', val) })

const openPostcode = () => {
	if (!(window as any).daum) { alert('주소 서비스를 로드할 수 없습니다.'); return }
	new (window as any).daum.Postcode({
		oncomplete: (data: any) => {
			postno_val.value = data.zonecode
			address_val.value = data.address
			d_address_val.value = ''
		},
	}).open()
}
</script>
