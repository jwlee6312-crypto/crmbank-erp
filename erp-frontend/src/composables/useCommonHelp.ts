import { reactive, ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import type { ModalProps } from '@/types/modal'

export function useCommonHelp() {
  const authStore = useAuthStore()
  const modalVisible = ref(false)
  const modalProps = reactive<ModalProps>({
    title: '', path: '', defaultField: '', columns: [], data: {}, onConfirm: () => {}, type: 'table'
  })

  const openHelp = (type: string, callback: (data: any) => void, extraData: any = {}) => {
    const commonPath = '/api/ha00/HA00_00P_STR'

    if (type === 'DEPT') {
      Object.assign(modalProps, {
        title: '부서 선택', path: commonPath, defaultField: 'deptnm', large: false,
        data: { gubun: 'D0', cmpycd: authStore.cmpycd, gbncd: extraData.gbncd || '', code: extraData.search || '', remark: '' },
        columns: [
          { title: '부서코드', field: 'deptcd', width: 120, hozAlign: 'center', headerSort: false },
          { title: '부서명', field: 'deptnm', minWidth: 200, widthGrow: 1, hozAlign: 'left', headerSort: false }
        ],
        onConfirm: callback
      })
    } else if (type === 'CUST') {
      const gubun = (extraData.gubun || 'C4').toUpperCase()
      Object.assign(modalProps, {
        title: '거래처 선택', path: commonPath, defaultField: 'custnm', large: true,
        data: { gubun: gubun, cmpycd: authStore.cmpycd, gbncd: extraData.gbncd || '', code: extraData.search || '', remark: '' },
        columns: [
          { title: '거래처코드', field: 'custcd', width: 120, hozAlign: 'center', headerSort: false },
          { title: '거래처명', field: 'custnm', minWidth: 250, widthGrow: 1, hozAlign: 'left', headerSort: false }
        ],
        onConfirm: callback
      })
    } else if (type === 'MGT') {
      Object.assign(modalProps, {
        title: extraData.title || '관리번호 선택', path: commonPath, defaultField: 'mgtnm', large: true,
        data: { gubun: 'M0', cmpycd: authStore.cmpycd, gbncd: extraData.mgtgbn || '', code: extraData.search || '', remark: extraData.acctcd || '' },
        columns: [
          { title: '번호', field: 'mgtno', width: 150, hozAlign: 'center', headerSort: false },
          { title: '명칭', field: 'mgtnm', minWidth: 150, widthGrow: 1, hozAlign: 'left' },
          { title: '은행/카드', field: 'bankcd', width: 100, hozAlign: 'center' },
          { title: '비고', field: 'remark', minWidth: 120 }
        ],
        onConfirm: callback
      })
    } else if (type === 'OFFSET') {
      Object.assign(modalProps, {
        title: '대체대상 전표 선택', path: commonPath, defaultField: 'REMARK', large: true,
        data: { gubun: 'P1', cmpycd: authStore.cmpycd, gbncd: extraData.dacctcd || '', code: extraData.custcd || '', remark: '' },
        columns: [
          { title: '전표일자', field: 'SLIPYMD', width: 100, hozAlign: 'center' },
          { title: '전표번호', field: 'SLIPNO', width: 120, hozAlign: 'center' },
          { title: '순번', field: 'SROWNO', width: 60, hozAlign: 'center' },
          { title: '적요', field: 'REMARK', minWidth: 200, widthGrow: 1 },
          { title: '거래처', field: 'CUSTNM', width: 150 },
          { title: '미결잔액', field: 'JANAMT', width: 110, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } }
        ],
        onConfirm: callback
      })
    } else if (type === 'ACCT') {
      const gubun = (extraData.gubun || 'A0').toUpperCase()
      const isMethod = gubun === 'E0'
      Object.assign(modalProps, {
        title: isMethod ? '지불수단 선택' : '계정과목 선택', path: commonPath, defaultField: isMethod ? 'codenm' : 'acctnm', large: true,
        data: { gubun: gubun, cmpycd: authStore.cmpycd, gbncd: extraData.gbncd || '', code: extraData.search || '', remark: '' },
        columns: [
          { title: '코드', field: 'acctcd', width: 100, hozAlign: 'center' },
          { title: '명칭', field: isMethod ? 'codenm' : 'acctnm', minWidth: 200, widthGrow: 1 },
          { title: '비고', field: isMethod ? 'remark' : 'cacctnm', width: 150 }
        ],
        onConfirm: callback
      })
    }
    modalVisible.value = true
  }

  return { modalVisible, modalProps, openHelp }
}
