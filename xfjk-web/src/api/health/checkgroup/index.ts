import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { CheckgroupVO, CheckgroupForm, CheckgroupQuery } from '@/api/health/checkgroup/types';
import { CheckitemVO } from '@/api/health/checkitem/types';

export const listCheckgroup = (query?: CheckgroupQuery): AxiosPromise<CheckgroupVO[]> => {
  return request({
    url: '/health/checkgroups',
    method: 'get',
    params: query
  });
};

/** 全量列表（套餐页勾选候选数据源） */
export const listAllCheckgroup = (query?: CheckgroupQuery): AxiosPromise<CheckgroupVO[]> => {
  return request({
    url: '/health/checkgroups/all',
    method: 'get',
    params: query
  });
};

export const getCheckgroup = (id: string | number): AxiosPromise<CheckgroupVO> => {
  return request({
    url: '/health/checkgroups/' + id,
    method: 'get'
  });
};

/** 组内检查项（编辑回显勾选） */
export const getCheckgroupItems = (id: string | number): AxiosPromise<CheckitemVO[]> => {
  return request({
    url: '/health/checkgroups/' + id + '/checkitems',
    method: 'get'
  });
};

/** 新增：body 含 checkItemIds 关联集合 */
export const addCheckgroup = (data: CheckgroupForm) => {
  return request({
    url: '/health/checkgroups',
    method: 'post',
    data: data
  });
};

/** 修改：关联差异更新（修正原「只删不增」缺陷，08 §5.1） */
export const updateCheckgroup = (id: string | number, data: CheckgroupForm) => {
  return request({
    url: '/health/checkgroups/' + id,
    method: 'put',
    data: data
  });
};

/** 删除：被套餐引用返回 3002 */
export const delCheckgroup = (id: string | number | Array<string | number>) => {
  return request({
    url: '/health/checkgroups/' + id,
    method: 'delete'
  });
};
