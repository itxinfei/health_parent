/** 会员视图（07 §3.1：基础 + 状态 + 档案号 + 标签；身份证/手机号后端已脱敏） */
export interface MemberVO {
  id: number;
  fileNumber?: string;
  name: string;
  sex?: string;
  idCard?: string;
  phoneNumber?: string;
  birthday?: string;
  email?: string;
  remark?: string;
  regTime?: string;
  memberStatus?: string;
  completeness?: string;
  tags?: MemberTagVO[];
}

/** 会员标签（t_health_member_tag，07 §2.12） */
export interface MemberTagVO {
  id: number;
  name: string;
  type?: string;
  color?: string;
  enabled?: string;
}

/** 动态信息（t_health_member_profile，07 §2.3；bmi/whtr 服务端计算只读） */
export interface MemberProfileVO {
  id?: number;
  memberId?: number;
  height?: number;
  weight?: number;
  bmi?: number;
  waist?: number;
  hip?: number;
  whtr?: number;
  bloodPressureHigh?: number;
  bloodPressureLow?: number;
  heartRate?: number;
  visionLeft?: number;
  visionRight?: number;
  colorVision?: string;
  bloodType?: string;
  maritalStatus?: string;
  sexHistory?: string;
  pregnancyStatus?: string;
  menstruationFlag?: string;
  menstruationDate?: string;
  occupation?: string;
  eduLevel?: string;
  jobCategory?: string;
  address?: string;
  phoneSelf?: string;
  phoneContact?: string;
  contactName?: string;
  smoking?: string;
  smokingCount?: number;
  drinking?: string;
  drinkingCount?: number;
  exerciseFreq?: number;
  exerciseTime?: number;
  dietHabit?: string;
  sleepHours?: number;
  saltLevel?: string;
  psychAdjust?: string;
  compliance?: string;
  surgeonHistoryFlag?: string;
  traumaFlag?: string;
  transfusionFlag?: string;
  drugAllergyFlag?: string;
  menarcheAge?: number;
  menopauseAge?: number;
  parityCount?: number;
  memberStatus?: string;
  dataTime?: string;
  remark?: string;
}

/** 病史（t_health_member_history，07 §2.5；整组替换语义） */
export interface MemberHistoryVO {
  id?: number;
  type: string;
  content: string;
  relation?: string;
  allergen?: string;
  allergyType?: string;
  severity?: string;
  surgeryName?: string;
  surgeryDate?: string;
  hospital?: string;
  drugName?: string;
  drugDose?: string;
  drugFreq?: string;
  startDate?: string;
  endDate?: string;
  happenDate?: string;
  confirmFlag?: string;
  remark?: string;
}

/** 完整度（07 §5.2：已填/应填 + 空白未标项 + 已标缺项） */
export interface MemberCompletenessVO {
  requiredKey: string;
  total: number;
  filled: number;
  text: string;
  unmarkedFields: string[];
  missingFields: { fieldKey: string; reason?: string; markedBy?: string }[];
}

/** 会员详情（07 §3.1：基础 + profile + 病史 + 完整度） */
export interface MemberDetailVO {
  member: MemberVO;
  profile?: MemberProfileVO;
  histories?: MemberHistoryVO[];
  completeness?: MemberCompletenessVO;
}

/** 会员保存入参（07 §3.1 add/edit；编辑时 phoneNumber 留空=不改） */
export interface MemberForm {
  id?: number;
  name: string;
  sex?: string;
  idCard?: string;
  phoneNumber?: string;
  birthday?: string;
  email?: string;
  remark?: string;
}

/** 分页查询入参（07 §3.1，query 绑定） */
export interface MemberQuery extends PageQuery {
  name?: string;
  phoneNumber?: string;
  idCard?: string;
  fileNumber?: string;
  sex?: string;
  memberStatus?: string;
  tagId?: number;
}
