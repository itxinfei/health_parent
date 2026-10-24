import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { CriticalHandleForm, CriticalQuery } from '@/api/health/critical/types';
import { CriticalLogVO } from '@/api/health/report/types';

/** 危急值分页（GET /health/criticals，07 §3.2.1） */
export const listCritical = (query?: CriticalQuery): AxiosPromise<CriticalLogVO[]> => {
  return request({ url: '/health/criticals', method: 'get', params: query });
};

/** 处置结果与接收确认 */
export const handleCritical = (id: number | string, data: CriticalHandleForm) => {
  return request({ url: `/health/criticals/${id}/handle`, method: 'put', data });
};
