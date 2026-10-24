import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { MemberVO, MemberQuery, MemberForm, MemberDetailVO, MemberProfileVO, MemberHistoryVO, MemberCompletenessVO, MemberTagVO } from '@/api/health/member/types';

/** 分页查询会员（GET /health/members，07 §3.1；默认只出正常状态） */
export const listMember = (query?: MemberQuery): AxiosPromise<MemberVO[]> => {
  return request({
    url: '/health/members',
    method: 'get',
    params: query
  });
};

/** 会员详情（基础 + profile + 病史 + 完整度 + 标签） */
export const getMember = (id: string | number): AxiosPromise<MemberDetailVO> => {
  return request({
    url: '/health/members/' + id,
    method: 'get'
  });
};

/** 新增会员（同事务产出 t_member + 档案号 + profile 三行） */
export const addMember = (data: MemberForm) => {
  return request({
    url: '/health/members',
    method: 'post',
    data
  });
};

/** 编辑基础信息（idCard 建后不可改，07 §5.1） */
export const updateMember = (id: string | number, data: MemberForm) => {
  return request({
    url: '/health/members/' + id,
    method: 'put',
    data
  });
};

/** 停用/恢复（member_status 0↔1，07 §5.8） */
export const updateMemberStatus = (id: string | number, memberStatus: string) => {
  return request({
    url: '/health/members/' + id + '/status',
    method: 'put',
    data: { memberStatus }
  });
};

/** 取档案号真值（一号制表，07 §2.2） */
export const getArchiveNo = (id: string | number): AxiosPromise<MemberVO> => {
  return request({
    url: '/health/members/' + id + '/archive-no',
    method: 'get'
  });
};

/** 动态信息查询（无行返回 null，按首次录入处理） */
export const getProfile = (id: string | number): AxiosPromise<MemberProfileVO> => {
  return request({
    url: '/health/members/' + id + '/profile',
    method: 'get'
  });
};

/** 动态信息保存（1:1 upsert；bmi/腰臀比服务端计算） */
export const saveProfile = (id: string | number, data: MemberProfileVO) => {
  return request({
    url: '/health/members/' + id + '/profile',
    method: 'put',
    data
  });
};

/** 完整度与缺项清单（实时计算，07 §5.2） */
export const getCompleteness = (id: string | number): AxiosPromise<MemberCompletenessVO> => {
  return request({
    url: '/health/members/' + id + '/completeness',
    method: 'get'
  });
};

/** 标记缺项（已确认缺失，与留空区分） */
export const markMissing = (id: string | number, fieldKey: string, reason?: string) => {
  return request({
    url: '/health/members/' + id + '/missing',
    method: 'post',
    data: { fieldKey, reason }
  });
};

/** 取消缺项标记 */
export const unmarkMissing = (id: string | number, fieldKey: string) => {
  return request({
    url: '/health/members/' + id + '/missing/' + fieldKey,
    method: 'delete'
  });
};

/** 病史列表（可按类型过滤） */
export const listHistories = (id: string | number, type?: string): AxiosPromise<MemberHistoryVO[]> => {
  return request({
    url: '/health/members/' + id + '/histories',
    method: 'get',
    params: { type }
  });
};

/** 病史整组替换（入参列表即最终集合，07 §2.5） */
export const saveHistories = (id: string | number, data: MemberHistoryVO[]) => {
  return request({
    url: '/health/members/' + id + '/histories',
    method: 'put',
    data
  });
};

/** 启用中的标签全集 */
export const tagOptions = (): AxiosPromise<MemberTagVO[]> => {
  return request({
    url: '/health/members/tags/options',
    method: 'get'
  });
};

/** 设置会员标签（整组替换） */
export const saveMemberTags = (id: string | number, tagIds: number[]) => {
  return request({
    url: '/health/members/' + id + '/tags',
    method: 'put',
    data: tagIds
  });
};
