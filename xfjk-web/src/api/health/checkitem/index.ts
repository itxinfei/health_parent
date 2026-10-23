import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { CheckitemVO, CheckitemForm, CheckitemQuery } from '@/api/health/checkitem/types';

/** 分页列表（GET /health/checkitems，query 绑定 code/name/sex/type） */
export const listCheckitem = (query?: CheckitemQuery): AxiosPromise<CheckitemVO[]> => {
  return request({
    url: '/health/checkitems',
    method: 'get',
    params: query
  });
};

/** 全量列表（穿梭框/勾选候选数据源） */
export const listAllCheckitem = (query?: CheckitemQuery): AxiosPromise<CheckitemVO[]> => {
  return request({
    url: '/health/checkitems/all',
    method: 'get',
    params: query
  });
};

/** 详情（含 ext 与 refs） */
export const getCheckitem = (id: string | number): AxiosPromise<CheckitemVO> => {
  return request({
    url: '/health/checkitems/' + id,
    method: 'get'
  });
};

/** 新增：ext 与 refs 并入同一 body；refs 至少一条（08 §11） */
export const addCheckitem = (data: CheckitemForm) => {
  return request({
    url: '/health/checkitems',
    method: 'post',
    data: data
  });
};

/** 修改：ext upsert、refs 传了即差异更新 */
export const updateCheckitem = (id: string | number, data: CheckitemForm) => {
  return request({
    url: '/health/checkitems/' + id,
    method: 'put',
    data: data
  });
};

/** 删除：被检查组引用返回 3001 */
export const delCheckitem = (id: string | number | Array<string | number>) => {
  return request({
    url: '/health/checkitems/' + id,
    method: 'delete'
  });
};
