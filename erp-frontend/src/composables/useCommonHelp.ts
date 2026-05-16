import { reactive, ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import type { ModalProps } from '@/types/modal'

export function useCommonHelp() {
  const authStore = useAuthStore()
  const modalVisible = ref(false)
  const modalProps = reactive<ModalProps>({
    title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table'
  })

  /**
   * 💡 공통 도움창 호출 함수
   * @param type 팝업 유형 (DEPT, CUST, ITEM 등)
   * @param callback 선택 완료 후 실행될 콜백 함수
   * @param extraData 추가 파라미터 (예: { ASTKIND: '2' } 등)
   */
  const openHelp = (type: string, callback: (data: any) => void, extraData: any = {}) => {
    if (type === 'DEPT') {
      // 💡 부서: 부서코드, 부서명
      Object.assign(modalProps, {
        title: '부서 선택',
        path: '/api/ha00/HA00_00P_STR',
        defaultField: 'DEPTNM',
        large: false,
        data: { GUBUN: 'D0', CMPYCD: authStore.CMPYCD, GBNCD: '', CODE: '', REMARK: '' },
        columns: [
          { title: '부서코드', field: 'DEPTCD', width: 100, hozAlign: 'center', headerSort: false },
          { title: '부서명', field: 'DEPTNM', minWidth: 200, widthGrow: 1, headerSort: false }
        ],
        onConfirm: callback
      })
    } else if (type === 'CUST') {
      // 💡 거래처: 거래처, 거래처명, 비고
      Object.assign(modalProps, {
        title: '거래처 선택',
        path: '/api/ha00/HA00_00P_STR',
        defaultField: 'CUSTNM',
        large: true,
        data: { GUBUN: 'C4', CMPYCD: authStore.CMPYCD, GBNCD: '', CODE: '', REMARK: '' },
        columns: [
          { title: '거래처', field: 'CUSTCD', width: 100, hozAlign: 'center', headerSort: false },
          { title: '거래처명', field: 'CUSTNM', minWidth: 250, widthGrow: 1, headerSort: false },
          { title: '비고(REMARK)', field: 'REMARK', width: 200, headerSort: false }
        ],
        onConfirm: callback
      })
    } else if (type === 'ITEM') {
      // 💡 품목: 품목코드, 품목명, 규격, 단위, 재고자산명(ASTKINDNM), 재고수량(QTY)
      // ASTKIND '2'는 영업 공통 코드로 상품(12), 원부자재(10), 제품(20), 반제품(21)을 포함
      Object.assign(modalProps, {
        title: '품목 선택',
        path: '/api/hs00/HS00_000S_STR',
        defaultField: 'ITEMNM',
        large: true,
        data: {
          GUBUN: 'I1',
          CMPYCD: authStore.CMPYCD,
          GBNCD: extraData.ASTKIND || extraData.GBNCD || '2', // 💡 기본값 '2' (영업공통)
          CODE: '',
          CODENM: '',
          ETCVAL: ''
        },
        columns: [
          { title: '품목코드', field: 'ITEMCD', width: 120, hozAlign: 'center', headerSort: false },
          { title: '품목명', field: 'ITEMNM', minWidth: 200, widthGrow: 1, headerSort: true },
          { title: '규격', field: 'ITSIZE', width: 150, headerSort: false },
          { title: '단위', field: 'UNITNM', width: 80, hozAlign: 'center', headerSort: false },
          { title: '재고자산명', field: 'ASTKINDNM', width: 120, hozAlign: 'center', headerSort: false },
          {
            title: '재고수량',
            field: 'QTY',
            width: 100,
            hozAlign: 'right',
            headerSort: false,
            formatter: (cell: any) => new Intl.NumberFormat().format(Number(cell.getValue()) || 0)
          }
        ],
        onConfirm: callback
      })
    }
    modalVisible.value = true
  }

  return { modalVisible, modalProps, openHelp }
}
