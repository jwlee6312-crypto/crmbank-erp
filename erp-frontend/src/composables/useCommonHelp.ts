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
   * @param type 팝업 유형 (DEPT, CUST, ACCT, MGT, PRJ, USER, SLIP, SUB, BUGT, ITEM, ADDR)
   * @param callback 선택 완료 후 콜백
   * @param extraData 추가 파라미터 (search, acctcd, custcd, mgtgbn, prjcd 등)
   */
  const openHelp = (type: string, callback: (data: any) => void, extraData: any = {}) => {
    const commonPath = '/api/ha00/HA00_00P_STR'

    if (type === 'DEPT') {
      // 💡 부서 선택 팝업 (D0)
      Object.assign(modalProps, {
        title: '부서 선택',
        path: commonPath,
        defaultField: 'deptnm',
        large: false,
        data: { gubun: 'D0', cmpycd: authStore.cmpycd, gbncd: extraData.gbncd || '', code: extraData.search || '', remark: '' },
        columns: [
          { title: '부서코드', field: 'deptcd', width: 120, hozAlign: 'center', headerSort: false },
          { title: '부서명', field: 'deptnm', minWidth: 200, widthGrow: 1, hozAlign: 'left', headerSort: false }
        ],
        onConfirm: callback
      })
    } else if (type === 'CUST') {
      // 💡 거래처 선택 팝업 (C4)
      Object.assign(modalProps, {
        title: '거래처 선택',
        path: commonPath,
        defaultField: 'custnm',
        large: true,
        data: { gubun: 'C4', cmpycd: authStore.cmpycd, gbncd: extraData.gbncd || '', code: extraData.search || '', remark: '' },
        columns: [
          { title: '거래처코드', field: 'custcd', width: 120, hozAlign: 'center', headerSort: false },
          { title: '거래처명', field: 'custnm', minWidth: 250, widthGrow: 1, hozAlign: 'left', headerSort: false }
        ],
        onConfirm: callback
      })
    } else if (type === 'ACCT') {
      // 💡 계정과목 선택 팝업 (A0)
      Object.assign(modalProps, {
        title: '계정과목 선택',
        path: commonPath,
        defaultField: 'ACCTNM',
        large: true,
        data: { gubun: 'A0', cmpycd: authStore.cmpycd, gbncd: '', code: extraData.search || '', remark: '' },
        columns: [
          { title: '계정코드', field: 'ACCTCD', width: 120, hozAlign: 'center', headerSort: false },
          { title: '계정명', field: 'ACCTNM', minWidth: 200, widthGrow: 1, hozAlign: 'left', headerSort: true },
          { title: '비고', field: 'CACCTNM', minWidth: 150, hozAlign: 'left', headerSort: false }
        ],
        onConfirm: callback
      })
    } else if (type === 'MGT') {
      // 💡 관리번호 선택 팝업 (M0)
      Object.assign(modalProps, {
        title: '관리번호 선택',
        path: commonPath,
        defaultField: 'MGTNM',
        large: true,
        data: { gubun: 'M0', cmpycd: authStore.cmpycd, gbncd: extraData.mgtgbn || '', code: extraData.search || '', remark: extraData.acctcd || '' },
        columns: [
          { title: '코드', field: 'MGTNO', width: 120, hozAlign: 'center', headerSort: false },
          { title: '코드명', field: 'MGTNM', minWidth: 200, widthGrow: 1, hozAlign: 'left', headerSort: true },
          { title: '비고', field: 'BIGO', minWidth: 150, hozAlign: 'left', headerSort: false }
        ],
        onConfirm: callback
      })
    } else if (type === 'PRJ') {
      // 💡 프로젝트 선택 팝업 (P0)
      Object.assign(modalProps, {
        title: '프로젝트 선택',
        path: commonPath,
        defaultField: 'PRJNM',
        large: true,
        data: { gubun: 'P0', cmpycd: authStore.cmpycd, gbncd: extraData.prjcd || '', code: extraData.search || '', remark: '' },
        columns: [
          { title: '프로젝트코드', field: 'PRJCD', width: 120, hozAlign: 'center', headerSort: false },
          { title: '프로젝트명', field: 'PRJNM', minWidth: 200, widthGrow: 1, hozAlign: 'left', headerSort: true },
          { title: '비고', field: 'BIGO', minWidth: 150, hozAlign: 'left', headerSort: false }
        ],
        onConfirm: callback
      })
    } else if (type === 'USER') {
      // 💡 사용자 선택 팝업 (U0)
      Object.assign(modalProps, {
        title: '사용자 선택',
        path: commonPath,
        defaultField: 'usernm',
        large: false,
        data: { gubun: 'U0', cmpycd: authStore.cmpycd, gbncd: '', code: extraData.search || '', remark: '' },
        columns: [
          { title: '사번', field: 'userid', width: 120, hozAlign: 'center', headerSort: false },
          { title: '성명', field: 'usernm', minWidth: 150, widthGrow: 1, hozAlign: 'left', headerSort: true }
        ],
        onConfirm: callback
      })
    } else if (type === 'SLIP') {
      // 💡 전표번호 선택 팝업 (P1)
      Object.assign(modalProps, {
        title: '전표번호 선택',
        path: commonPath,
        defaultField: 'descnm',
        large: true,
        data: { gubun: 'P1', cmpycd: authStore.cmpycd, gbncd: extraData.acctcd || '', code: extraData.search || '', remark: extraData.custcd || '' },
        columns: [
          {
            title: '전표번호', field: 'slipno', width: 150, hozAlign: 'center',
            formatter: (cell: any) => {
              const d = cell.getData();
              return d.slipno || (d.col0 && d.col1 ? `${d.col0}${d.col1}${d.col2 || ''}` : '');
            }
          },
          { title: '거래처', field: 'custnm', width: 180, hozAlign: 'left' },
          { title: '적요', field: 'descnm', minWidth: 200, widthGrow: 1, hozAlign: 'left' },
          { title: '미지불액', field: 'janamt', width: 120, hozAlign: 'right', formatter: 'money', formatterParams: { precision: 0 } }
        ],
        onConfirm: callback
      })
    } else if (type === 'SUB') {
      // 💡 보조과목 선택 팝업 (S0)
      Object.assign(modalProps, {
        title: '보조과목 선택',
        path: commonPath,
        defaultField: 'subnm',
        large: true,
        data: { gubun: 'S0', cmpycd: authStore.cmpycd, gbncd: '', code: extraData.search || '', remark: '' },
        columns: [
          { title: '코드', field: 'subcd', width: 120, hozAlign: 'center', headerSort: false },
          { title: '보조과목명', field: 'subnm', minWidth: 200, widthGrow: 1, hozAlign: 'left', headerSort: true },
          { title: '비고', field: 'bigo', minWidth: 150, hozAlign: 'left', headerSort: false }
        ],
        onConfirm: callback
      })
    } else if (type === 'BUGT') {
      // 💡 계정예산코드 선택 팝업 (B0)
      Object.assign(modalProps, {
        title: '예산코드 선택',
        path: commonPath,
        defaultField: 'bugtnm',
        large: true,
        data: { gubun: 'B0', cmpycd: authStore.cmpycd, gbncd: extraData.acctcd || '', code: extraData.search || '', remark: '' },
        columns: [
          { title: '예산코드', field: 'bugtcd', width: 120, hozAlign: 'center', headerSort: false },
          { title: '예산명', field: 'bugtnm', minWidth: 200, widthGrow: 1, hozAlign: 'left', headerSort: true },
          { title: '비고', field: 'codenm', minWidth: 150, hozAlign: 'left', headerSort: false }
        ],
        onConfirm: callback
      })
    } else if (type === 'ITEM') {
      Object.assign(modalProps, {
        title: '품목 선택',
        path: '/api/hs00/HS00_000S_STR',
        defaultField: 'itemnm',
        large: true,
        data: { gubun: 'I1', cmpycd: authStore.cmpycd, gbncd: extraData.gbncd || '2', code: '', codenm: '', etcval: '' },
        columns: [
          { title: '품목코드', field: 'itemcd', width: 120, hozAlign: 'center', headerSort: false },
          { title: '품목명', field: 'itemnm', minWidth: 200, widthGrow: 1, headerSort: true },
          { title: '규격', field: 'itsize', width: 150, headerSort: false },
          { title: '단위', field: 'unitnm', width: 80, hozAlign: 'center', headerSort: false },
          { title: '재고자산', field: 'astkindnm', width: 120, hozAlign: 'center', headerSort: false },
          { title: '재고수량', field: 'qty', width: 100, hozAlign: 'right', formatter: (cell: any) => new Intl.NumberFormat().format(Number(cell.getValue()) || 0) }
        ],
        onConfirm: callback
      })
    } else if (type === 'ADDR') {
      if (!extraData.custcd) { alert('거래처를 먼저 선택하십시오.'); return }
      Object.assign(modalProps, {
        title: '배송지 선택',
        path: '/api/hs00/HS00_000S_STR',
        defaultField: 'address',
        large: true,
        data: { gubun: 'T0', cmpycd: authStore.cmpycd, gbncd: '', code: extraData.custcd, codenm: '', etcval: '' },
        columns: [
          { title: '배송지코드', field: 'trancd', width: 100, hozAlign: 'center', headerSort: false },
          { title: '우편번호', field: 'postno', width: 100, hozAlign: 'center', headerSort: false },
          { title: '주소', field: 'address', minWidth: 250, widthGrow: 1, hozAlign: 'left', headerSort: false }
        ],
        onConfirm: callback
      })
    }
    modalVisible.value = true
  }

  return { modalVisible, modalProps, openHelp }
}
