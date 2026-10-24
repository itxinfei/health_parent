import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { ForbidVO, ForbidForm, ForbidCheckForm, ForbidCheckVO } from '@/api/health/forbid/types';

/** 规则列表（全量、按优先级排序，无分页） */
export const listForbid = (): AxiosPromise<ForbidVO[]> => {
  return request({
    url: '/health/forbids',
    method: 'get'
  });
};

/** 新增：ruleCode 唯一 / conditionJson 未知键返回 5013 */
export const addForbid = (data: ForbidForm) => {
  return request({
    url: '/health/forbids',
    method: 'post',
    data: data
  });
};

/** 修改 */
export const updateForbid = (data: ForbidForm) => {
  return request({
    url: '/health/forbids',
    method: 'put',
    data: data
  });
};

/** 删除 */
export const delForbid = (id: string | number) => {
  return request({
    url: '/health/forbids/' + id,
    method: 'delete'
  });
};

/** 套餐禁忌试算（08 §4.5） */
export const checkForbid = (data: ForbidCheckForm): AxiosPromise<ForbidCheckVO> => {
  return request({
    url: '/health/forbid-check',
    method: 'post',
    data: data
  });
};
