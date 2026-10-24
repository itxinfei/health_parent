import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { OrderVO, OrderQuery } from '@/api/health/order/types';

/** 分页查询订单（GET /health/orders，08 §4.5） */
export const listOrder = (query?: OrderQuery): AxiosPromise<OrderVO[]> => {
  return request({
    url: '/health/orders',
    method: 'get',
    params: query
  });
};

/** 订单详情（含 ext 与流水 logs） */
export const getOrder = (id: string | number): AxiosPromise<OrderVO> => {
  return request({
    url: '/health/orders/' + id,
    method: 'get'
  });
};

/** 到诊登记：生成体检号，flow_status/orderStatus 同步为已到诊 */
export const registerOrder = (id: string | number, remark?: string): AxiosPromise<Record<string, string>> => {
  return request({
    url: '/health/orders/' + id + '/register',
    method: 'put',
    data: { remark }
  });
};

/** 取消订单（须 reason；回退当日名额） */
export const cancelOrder = (id: string | number, reason: string) => {
  return request({
    url: '/health/orders/' + id + '/cancel',
    method: 'put',
    data: { reason }
  });
};

/** 改期（目标日 +1 → 原日 -1 原子双更新） */
export const rescheduleOrder = (id: string | number, targetDate: string) => {
  return request({
    url: '/health/orders/' + id + '/reschedule',
    method: 'put',
    data: { targetDate }
  });
};

/** 延期（18 C6，须 until） */
export const postponeOrder = (id: string | number, until: string) => {
  return request({
    url: '/health/orders/' + id + '/postpone',
    method: 'put',
    data: { until }
  });
};

/** 延期恢复（订单侧恢复 flow_status=已到诊） */
export const restorePostpone = (id: string | number) => {
  return request({
    url: '/health/orders/' + id + '/postpone-restore',
    method: 'put'
  });
};

/** 取消恢复：已取消 → 已预约，重新占用当日名额（08 §3.2） */
export const restoreCancelOrder = (id: string | number) => {
  return request({
    url: '/health/orders/' + id + '/restore',
    method: 'put'
  });
};

/** 状态迁移（仅允许 08 §3.2 合法边，非法返回 5006） */
export const transitFlow = (id: string | number, toStatus: string, remark?: string) => {
  return request({
    url: '/health/orders/' + id + '/flow',
    method: 'put',
    data: { toStatus, remark }
  });
};
