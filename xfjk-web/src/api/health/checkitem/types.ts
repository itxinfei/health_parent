export interface CheckitemRefVO {
  id: number;
  checkitemId: number;
  sex: string;
  ageMin: number;
  ageMax: number;
  physioState: string;
  refText: string;
  refMin: number;
  refMax: number;
  priority: number;
}

export interface CheckitemExtVO {
  resultType: string;
  unit: string;
  deptId: number;
  specimen: string;
  isFasting: string;
  isRadiation: string;
  isRequired: string;
  dualDeptFlag: string;
  sortNo: number;
  defaultConclusionId: number;
  noticeText: string;
}

export interface CheckitemVO {
  id: number;
  code: string;
  name: string;
  sex: string;
  age: string;
  price: number;
  type: string;
  attention: string;
  remark: string;
  ext?: CheckitemExtVO;
  refs?: CheckitemRefVO[];
}

export interface CheckitemRefForm {
  id?: number;
  sex?: string;
  ageMin?: number;
  ageMax?: number;
  physioState?: string;
  refText?: string;
  refMin?: number;
  refMax?: number;
  priority?: number;
}

export interface CheckitemExtForm {
  resultType?: string;
  unit?: string;
  deptId?: number;
  specimen?: string;
  isFasting?: string;
  isRadiation?: string;
  isRequired?: string;
  dualDeptFlag?: string;
  sortNo?: number;
  defaultConclusionId?: number;
  noticeText?: string;
}

export interface CheckitemForm extends BaseEntity {
  id?: number;
  code?: string;
  name?: string;
  sex?: string;
  age?: string;
  price?: number;
  type?: string;
  attention?: string;
  remark?: string;
  // ext 必填（后端缺失返回 5007）；refs 新增至少一条（08 §11），页面初始化即给出
  ext: CheckitemExtForm;
  refs: CheckitemRefForm[];
}

export interface CheckitemQuery extends PageQuery {
  code?: string;
  name?: string;
  sex?: string;
  type?: string;
}
