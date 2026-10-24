import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import {
  ConclusionDictVO,
  ConclusionForm,
  ReportCompareVO,
  ReportDetailVO,
  ReportForm,
  ReportItemForm,
  ReportItemResultVO,
  ReportParseVO,
  ReportQuery,
  ReportVO
} from '@/api/health/report/types';

/** 报告分页（GET /health/reports，07 §3.2） */
export const listReport = (query?: ReportQuery): AxiosPromise<ReportVO[]> => {
  return request({ url: '/health/reports', method: 'get', params: query });
};

/** 报告详情（五段 + 明细 + 小结 + 结论 + 危急值 + 归档） */
export const getReport = (id: number | string): AxiosPromise<ReportDetailVO> => {
  return request({ url: '/health/reports/' + id, method: 'get' });
};

/** 手工建档 */
export const addReport = (data: ReportForm): AxiosPromise<ReportVO> => {
  return request({ url: '/health/reports', method: 'post', data });
};

/** 上传附件（form-data；reportId + file，可选 resultFile 一并解析） */
export const uploadReportFile = (reportId: number | string, file: File, resultFile?: File): AxiosPromise<any> => {
  const form = new FormData();
  form.append('reportId', String(reportId));
  form.append('file', file);
  if (resultFile) {
    form.append('resultFile', resultFile);
  }
  return request({
    url: '/health/reports/upload',
    method: 'post',
    data: form,
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};

/** 解析结果文件（CSV / Excel） */
export const parseReportFile = (reportId: number | string, file: File): AxiosPromise<ReportParseVO> => {
  const form = new FormData();
  form.append('file', file);
  return request({
    url: `/health/reports/${reportId}/parse`,
    method: 'post',
    data: form,
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};

/** 录入/修改单项结果（服务端走 07 §5.3 判定） */
export const saveReportItem = (reportId: number | string, data: ReportItemForm): AxiosPromise<ReportItemResultVO> => {
  return request({ url: `/health/reports/${reportId}/items`, method: 'post', data });
};

/** 科室小结提交/修改 */
export const saveDeptSummary = (reportId: number | string, data: { deptId?: number; summaryText?: string; printLabel?: string; submit?: boolean }) => {
  return request({ url: `/health/reports/${reportId}/dept-summary`, method: 'put', data });
};

/** 总检提交 */
export const submitReportSummary = (reportId: number | string, data: any) => {
  return request({ url: `/health/reports/${reportId}/summary`, method: 'put', data });
};

/** 保存结论条目（末次语义） */
export const saveReportConclusions = (reportId: number | string, data: ConclusionForm[]) => {
  return request({ url: `/health/reports/${reportId}/conclusions`, method: 'put', data });
};

/** 结论词候选 */
export const conclusionOptions = (keyword?: string): AxiosPromise<ConclusionDictVO[]> => {
  return request({ url: '/health/reports/conclusion-options', method: 'get', params: { keyword } });
};

/** 总检撤销（须原因） */
export const revokeReport = (reportId: number | string, reason: string) => {
  return request({ url: `/health/reports/${reportId}/revoke`, method: 'put', data: { reason } });
};

/** 异常打回科室 */
export const sendBackReport = (reportId: number | string, data: { deptId?: number; reason: string }) => {
  return request({ url: `/health/reports/${reportId}/send-back`, method: 'put', data });
};

/** 终审签发 */
export const finalCheckReport = (reportId: number | string) => {
  return request({ url: `/health/reports/${reportId}/final-check`, method: 'put' });
};

/** 打印 / 发放（同步 08 流程状态） */
export const printReport = (reportId: number | string) => {
  return request({ url: `/health/reports/${reportId}/print`, method: 'post' });
};
export const issueReport = (reportId: number | string) => {
  return request({ url: `/health/reports/${reportId}/issue`, method: 'post' });
};

/** 归档附属资料 */
export const addReportArchive = (reportId: number | string, data: { type?: string; ossId?: number; pageCount?: number; remark?: string }) => {
  return request({ url: `/health/reports/${reportId}/archives`, method: 'post', data });
};

/** 危急值登记（07 §3.2 POST /health/reports/{id}/critical） */
export const registerCritical = (
  reportId: number | string,
  data: { reportItemId?: number; receiver?: string; notifyWay?: string; receiverConfirmTime?: string; handleResult?: string; reportTime?: string }
) => {
  return request({ url: `/health/reports/${reportId}/critical`, method: 'post', data });
};

/** 上次报告（18 D11） */
export const getPrevReport = (archiveNo: string): AxiosPromise<ReportVO> => {
  return request({ url: '/health/reports/prev', method: 'get', params: { archiveNo } });
};

/** 会员指标历史对比（18 D11） */
export const getMemberCompare = (memberId: number | string, checkitemId: number | string): AxiosPromise<ReportCompareVO[]> => {
  return request({ url: `/health/members/${memberId}/compare`, method: 'get', params: { checkitemId } });
};
