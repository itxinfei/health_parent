<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb8">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="queryParams.name" placeholder="姓名模糊" clearable style="width: 130px" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="手机号" prop="phoneNumber">
              <el-input v-model="queryParams.phoneNumber" placeholder="手机号模糊" clearable style="width: 140px" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="档案号" prop="fileNumber">
              <el-input v-model="queryParams.fileNumber" placeholder="精确（一号制）" clearable style="width: 170px" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="性别" prop="sex">
              <el-select v-model="queryParams.sex" placeholder="全部" clearable style="width: 100px">
                <el-option v-for="dict in sys_user_sex" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="memberStatus">
              <el-select v-model="queryParams.memberStatus" placeholder="正常" clearable style="width: 110px">
                <el-option v-for="dict in sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="标签" prop="tagId">
              <el-select v-model="queryParams.tagId" placeholder="全部" clearable style="width: 130px">
                <el-option v-for="tag in allTags" :key="tag.id" :label="tag.name" :value="tag.id" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
              <el-button icon="Refresh" @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </transition>

    <el-card shadow="hover">
      <template #header>
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button v-hasPermi="['health:member:add']" type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button v-hasPermi="['health:member:export']" type="warning" plain icon="Download" @click="handleExport">导出</el-button>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="memberList">
        <el-table-column label="ID" prop="id" width="60" align="center" />
        <el-table-column label="档案号" prop="fileNumber" width="180" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.fileNumber || '未建档' }}</template>
        </el-table-column>
        <el-table-column label="姓名" prop="name" width="90" show-overflow-tooltip />
        <el-table-column label="性别" width="60" align="center">
          <template #default="scope">
            <dict-tag :options="sys_user_sex" :value="scope.row.sex" />
          </template>
        </el-table-column>
        <el-table-column label="身份证号" prop="idCard" width="160" show-overflow-tooltip />
        <el-table-column label="手机号" prop="phoneNumber" width="120" />
        <el-table-column label="状态" width="70" align="center">
          <template #default="scope">
            <dict-tag :options="sys_normal_disable" :value="scope.row.memberStatus || '0'" />
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="120">
          <template #default="scope">
            <el-tag v-for="tag in scope.row.tags || []" :key="tag.id" size="small" class="mr4">{{ tag.name }}</el-tag>
            <span v-if="!(scope.row.tags || []).length">-</span>
          </template>
        </el-table-column>
        <el-table-column label="完整度" width="90" align="center" prop="completeness" />
        <el-table-column label="注册时间" prop="regTime" width="100" align="center" />
        <el-table-column label="操作" min-width="250" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button v-hasPermi="['health:member:list']" link type="primary" @click="handleDetail(scope.row)">详情</el-button>
            <el-button v-hasPermi="['health:member:edit']" link type="primary" @click="handleUpdate(scope.row)">编辑</el-button>
            <el-button
              v-if="scope.row.memberStatus !== '1'"
              v-hasPermi="['health:member:edit']"
              link
              type="primary"
              @click="openProfileDialog(scope.row)"
            >
              动态信息
            </el-button>
            <el-button
              v-hasPermi="['health:member:remove']"
              link
              :type="scope.row.memberStatus === '1' ? 'success' : 'danger'"
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.memberStatus === '1' ? '恢复' : '停用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 新增/编辑基础信息（idCard 建后不可改，07 §5.1） -->
    <el-dialog v-model="memberDialog.visible" :title="memberDialog.title" width="520px" append-to-body>
      <el-form ref="memberFormRef" :model="memberForm" :rules="memberRules" label-width="90px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="memberForm.name" maxlength="32" placeholder="必填" />
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-select v-model="memberForm.sex" style="width: 100%">
            <el-option v-for="dict in sys_user_sex" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="memberForm.idCard" maxlength="18" :disabled="isEdit" :placeholder="isEdit ? '建档后不可修改' : '18 位，校验位合法性后端把关（6005）'" />
        </el-form-item>
        <el-form-item label="手机号" prop="phoneNumber">
          <el-input v-model="memberForm.phoneNumber" maxlength="11" :placeholder="isEdit ? '11 位 1 开头；留空=不改原号' : '11 位 1 开头'" />
        </el-form-item>
        <el-form-item label="出生日期" prop="birthday">
          <el-date-picker v-model="memberForm.birthday" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="memberForm.email" maxlength="64" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="memberForm.remark" type="textarea" :rows="2" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="memberDialog.visible = false">取 消</el-button>
        <el-button :loading="buttonLoading" type="primary" @click="submitMember">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 动态信息（07 §2.3 分组；bmi/腰臀比服务端计算） -->
    <el-dialog v-model="profileDialog.visible" title="动态信息（1:1 upsert）" width="720px" append-to-body>
      <el-form :model="profileForm" label-width="100px">
        <el-divider content-position="left">体格（BMI 与腰臀比由服务端计算）</el-divider>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="身高 cm">
              <el-input-number v-model="profileForm.height" :min="0" :precision="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="体重 kg">
              <el-input-number v-model="profileForm.weight" :min="0" :precision="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="BMI">
              <el-input :model-value="profileForm.bmi ?? '-'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="腰围 cm">
              <el-input-number v-model="profileForm.waist" :min="0" :precision="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="臀围 cm">
              <el-input-number v-model="profileForm.hip" :min="0" :precision="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="腰臀比">
              <el-input :model-value="profileForm.whtr ?? '-'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="收缩压">
              <el-input-number v-model="profileForm.bloodPressureHigh" :min="0" :precision="0" controls-position="right" style="width: 100%" />
              <div class="field-tip">尾数须为偶（6007）</div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="舒张压">
              <el-input-number v-model="profileForm.bloodPressureLow" :min="0" :precision="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="心率">
              <el-input-number v-model="profileForm.heartRate" :min="0" :precision="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">基本（禁忌校验首屏三项：婚姻/性生活史/妊娠）</el-divider>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="血型">
              <el-select v-model="profileForm.bloodType" clearable style="width: 100%">
                <el-option v-for="dict in health_blood_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="婚姻">
              <el-select v-model="profileForm.maritalStatus" clearable style="width: 100%">
                <el-option v-for="dict in health_marital_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="性生活史">
              <el-select v-model="profileForm.sexHistory" clearable style="width: 100%">
                <el-option v-for="dict in health_sex_history" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="妊娠状态">
              <el-select v-model="profileForm.pregnancyStatus" clearable style="width: 100%">
                <el-option v-for="dict in health_pregnancy_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="经期">
              <el-select v-model="profileForm.menstruationFlag" clearable style="width: 100%">
                <el-option v-for="dict in health_flag_yn" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="末次经期">
              <el-date-picker v-model="profileForm.menstruationDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="职业"><el-input v-model="profileForm.occupation" maxlength="64" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="本人电话"><el-input v-model="profileForm.phoneSelf" maxlength="16" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="紧急联系人"><el-input v-model="profileForm.contactName" maxlength="32" /></el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">生活方式</el-divider>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="吸烟">
              <el-select v-model="profileForm.smoking" clearable style="width: 100%">
                <el-option label="不吸" value="0" />
                <el-option label="吸" value="1" />
                <el-option label="已戒" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="吸烟 支/日"><el-input-number v-model="profileForm.smokingCount" :min="0" controls-position="right" style="width: 100%" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="饮酒">
              <el-select v-model="profileForm.drinking" clearable style="width: 100%">
                <el-option label="不饮" value="0" />
                <el-option label="饮" value="1" />
                <el-option label="已戒" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="运动 次/周"><el-input-number v-model="profileForm.exerciseFreq" :min="0" controls-position="right" style="width: 100%" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="睡眠 h"><el-input-number v-model="profileForm.sleepHours" :min="0" :precision="1" controls-position="right" style="width: 100%" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="饮食习惯">
              <el-select v-model="profileForm.dietHabit" clearable style="width: 100%">
                <el-option v-for="dict in health_diet_type" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">既往史标志（明细走病史页签）</el-divider>
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="手术史">
              <el-select v-model="profileForm.surgeonHistoryFlag" clearable style="width: 100%">
                <el-option v-for="dict in health_flag_yn" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="外伤史">
              <el-select v-model="profileForm.traumaFlag" clearable style="width: 100%">
                <el-option v-for="dict in health_flag_yn" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="输血史">
              <el-select v-model="profileForm.transfusionFlag" clearable style="width: 100%">
                <el-option v-for="dict in health_flag_yn" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="药物过敏">
              <el-select v-model="profileForm.drugAllergyFlag" clearable style="width: 100%">
                <el-option v-for="dict in health_flag_yn" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="采集时间">
              <el-date-picker v-model="profileForm.dataTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="备注"><el-input v-model="profileForm.remark" maxlength="255" /></el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="profileDialog.visible = false">取 消</el-button>
        <el-button :loading="buttonLoading" type="primary" @click="submitProfile">保 存</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉：基本 + 完整度 + 缺项 + 病史 + 标签 -->
    <el-drawer v-model="detailDrawer.visible" title="会员详情" size="560px">
      <template v-if="detail">
        <el-descriptions :column="1" border class="mb8">
          <el-descriptions-item label="档案号">{{ detail.member?.fileNumber || '未建档' }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ detail.member?.name }}</el-descriptions-item>
          <el-descriptions-item label="性别">
            <dict-tag :options="sys_user_sex" :value="detail.member?.sex" />
          </el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ detail.member?.idCard || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detail.member?.phoneNumber || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <dict-tag :options="sys_normal_disable" :value="detail.member?.memberStatus || '0'" />
          </el-descriptions-item>
          <el-descriptions-item label="完整度">
            {{ detail.completeness?.text || '-' }}
            <span class="field-tip">（必填清单键：{{ detail.completeness?.requiredKey }}）</span>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">缺项与空白</el-divider>
        <div class="mb8">
          <el-tag v-for="f in detail.completeness?.unmarkedFields || []" :key="f" type="danger" size="small" class="mr4 clickable" @click="openMarkMissing(f)">
            {{ f }} 未标
          </el-tag>
          <el-tag v-for="m in detail.completeness?.missingFields || []" :key="m.fieldKey" type="info" size="small" class="mr4 clickable" @click="openUnmarkMissing(m.fieldKey)">
            {{ m.fieldKey }} 已标缺失
          </el-tag>
          <span v-if="!(detail.completeness?.unmarkedFields || []).length && !(detail.completeness?.missingFields || []).length">无缺项</span>
        </div>

        <el-divider content-position="left">标签（整组替换）</el-divider>
        <div class="mb8">
          <el-select v-model="detailTagIds" multiple placeholder="选择启用中的标签" style="width: 100%" @change="submitTags">
            <el-option v-for="tag in allTags" :key="tag.id" :label="tag.name" :value="tag.id" />
          </el-select>
        </div>

        <el-divider content-position="left">病史（整组替换）</el-divider>
        <el-table :data="detail.histories || []" size="small" border class="mb8">
          <el-table-column label="类型" prop="type" width="90" />
          <el-table-column label="描述" prop="content" show-overflow-tooltip />
        </el-table>
        <el-button v-hasPermi="['health:memberhistory:edit']" link type="primary" icon="Edit" @click="historyEditorVisible = true">编辑病史</el-button>

        <el-divider content-position="left">动态信息摘要</el-divider>
        <el-descriptions v-if="detail.profile" :column="2" border size="small">
          <el-descriptions-item label="身高/体重">{{ detail.profile.height ?? '-' }} / {{ detail.profile.weight ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="BMI">{{ detail.profile.bmi ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="血压">{{ detail.profile.bloodPressureHigh ?? '-' }}/{{ detail.profile.bloodPressureLow ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="婚姻/性生活/妊娠">
            {{ detail.profile.maritalStatus ?? '-' }} / {{ detail.profile.sexHistory ?? '-' }} / {{ detail.profile.pregnancyStatus ?? '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="采集时间" :span="2">{{ detail.profile.dataTime ?? '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-empty v-else description="尚未录入动态信息" :image-size="60" />
      </template>
    </el-drawer>

    <!-- 病史编辑（整组替换：列表即最终集合） -->
    <el-dialog v-model="historyEditorVisible" title="病史编辑（保存即整组替换）" width="640px" append-to-body>
      <div v-for="(row, idx) in historyDraft" :key="idx" class="history-row mb8">
        <el-select v-model="row.type" placeholder="类型" style="width: 110px">
          <el-option label="疾病史" value="disease" />
          <el-option label="家族史" value="family" />
          <el-option label="过敏史" value="allergy" />
          <el-option label="手术史" value="surgery" />
          <el-option label="用药史" value="medication" />
        </el-select>
        <el-input v-model="row.content" placeholder="描述（必填，≤500 字）" style="flex: 1" maxlength="500" />
        <el-button link type="danger" icon="Delete" @click="historyDraft.splice(idx, 1)" />
      </div>
      <el-button link type="primary" icon="Plus" @click="historyDraft.push({ type: 'disease', content: '' })">添加一行</el-button>
      <template #footer>
        <el-button @click="historyEditorVisible = false">取 消</el-button>
        <el-button :loading="buttonLoading" type="primary" @click="submitHistories">保 存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Member" lang="ts">
import {
  listMember,
  getMember,
  addMember,
  updateMember,
  updateMemberStatus,
  getProfile,
  saveProfile,
  saveHistories,
  markMissing,
  unmarkMissing,
  tagOptions,
  saveMemberTags
} from '@/api/health/member';
import { MemberVO, MemberQuery, MemberForm, MemberDetailVO, MemberProfileVO, MemberHistoryVO, MemberTagVO } from '@/api/health/member/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { sys_user_sex, sys_normal_disable, health_blood_type, health_marital_status, health_sex_history, health_pregnancy_status, health_flag_yn, health_diet_type } =
  toRefs<any>(proxy?.useDict('sys_user_sex', 'sys_normal_disable', 'health_blood_type', 'health_marital_status', 'health_sex_history', 'health_pregnancy_status', 'health_flag_yn', 'health_diet_type'));

const memberList = ref<MemberVO[]>([]);
const allTags = ref<MemberTagVO[]>([]);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const buttonLoading = ref(false);
const isEdit = ref(false);

const queryFormRef = ref<ElFormInstance>();
const memberFormRef = ref<ElFormInstance>();

const data = reactive<{ queryParams: MemberQuery }>({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: undefined,
    phoneNumber: undefined,
    fileNumber: undefined,
    sex: undefined,
    memberStatus: undefined,
    tagId: undefined
  }
});
const { queryParams } = toRefs(data);

const memberDialog = reactive<DialogOption>({ visible: false, title: '' });
const memberForm = ref<MemberForm>({ name: '', sex: '2', idCard: '', phoneNumber: '', birthday: undefined, email: '', remark: '' });
const memberRules = computed(() => ({
  name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
  idCard: isEdit.value
    ? []
    : [
        { required: true, message: '身份证号不能为空', trigger: 'blur' },
        { pattern: /^\d{17}[\dXx]$/, message: '身份证须为 18 位', trigger: 'blur' }
      ],
  phoneNumber: [
    { required: !isEdit.value, message: '手机号不能为空', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号须为 11 位 1 开头', trigger: 'blur' }
  ]
}));

const profileDialog = reactive({ visible: false, memberId: 0 as number });
const profileForm = ref<MemberProfileVO>({});

const detailDrawer = reactive({ visible: false, memberId: 0 as number, value: undefined as MemberDetailVO | undefined });
const detail = computed(() => detailDrawer.value);
const detailTagIds = ref<number[]>([]);

const historyEditorVisible = ref(false);
const historyDraft = ref<MemberHistoryVO[]>([]);

const getList = async () => {
  loading.value = true;
  try {
    const res = await listMember(queryParams.value);
    memberList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

const getTags = async () => {
  const res = await tagOptions();
  allTags.value = res.data;
};

const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};
const resetQuery = () => {
  queryFormRef.value?.resetFields();
  handleQuery();
};

const handleAdd = () => {
  isEdit.value = false;
  memberForm.value = { name: '', sex: '2', idCard: '', phoneNumber: '', birthday: undefined, email: '', remark: '' };
  memberDialog.visible = true;
  memberDialog.title = '新增会员（同事务产出 t_member + 档案号 + profile）';
};

const handleUpdate = (row: MemberVO) => {
  isEdit.value = true;
  // 列表值已脱敏（07 §9）：idCard 不可改故留空禁用；手机号留空待用户输入完整新值
  memberForm.value = {
    id: row.id,
    name: row.name,
    sex: row.sex,
    idCard: '',
    phoneNumber: '',
    birthday: row.birthday,
    email: row.email,
    remark: row.remark
  };
  memberDialog.visible = true;
  memberDialog.title = '编辑会员基础信息';
};

const submitMember = async () => {
  await memberFormRef.value?.validate();
  buttonLoading.value = true;
  try {
    if (isEdit.value && memberForm.value.id) {
      await updateMember(memberForm.value.id, memberForm.value);
      proxy?.$modal.msgSuccess('修改成功');
    } else {
      const res = await addMember(memberForm.value);
      proxy?.$modal.msgSuccess('建档成功，档案号：' + res.data.fileNumber);
    }
    memberDialog.visible = false;
    await getList();
  } finally {
    buttonLoading.value = false;
  }
};

const handleToggleStatus = async (row: MemberVO) => {
  const target = row.memberStatus === '1' ? '0' : '1';
  const text = target === '1' ? '停用（历史订单与报告全部保留可查，07 §5.8）' : '恢复为正常';
  await proxy?.$modal.confirm('确认' + text + '会员「' + row.name + '」？');
  await updateMemberStatus(row.id, target);
  proxy?.$modal.msgSuccess('操作成功');
  await getList();
};

const openProfileDialog = async (row: MemberVO) => {
  const res = await getProfile(row.id);
  profileForm.value = res.data || {};
  profileDialog.memberId = row.id;
  profileDialog.visible = true;
};

const submitProfile = async () => {
  buttonLoading.value = true;
  try {
    await saveProfile(profileDialog.memberId, profileForm.value);
    proxy?.$modal.msgSuccess('动态信息已保存（BMI 由服务端重算）');
    profileDialog.visible = false;
    await getList();
  } catch {
    // 业务拒绝（如 6007 血压尾数）已由请求拦截器提示，停留弹窗供修正
  } finally {
    buttonLoading.value = false;
  }
};

const handleDetail = async (row: MemberVO) => {
  const res = await getMember(row.id);
  detailDrawer.value = res.data;
  detailDrawer.memberId = row.id;
  detailTagIds.value = (res.data.member?.tags || []).map((t) => t.id);
  historyDraft.value = (res.data.histories || []).map((h) => ({ ...h }));
  detailDrawer.visible = true;
};

const submitTags = async () => {
  await saveMemberTags(detailDrawer.memberId, detailTagIds.value);
  proxy?.$modal.msgSuccess('标签已保存');
  await getList();
};

const submitHistories = async () => {
  buttonLoading.value = true;
  try {
    await saveHistories(detailDrawer.memberId, historyDraft.value);
    proxy?.$modal.msgSuccess('病史已整组保存');
    historyEditorVisible.value = false;
    const res = await getMember(detailDrawer.memberId);
    detailDrawer.value = res.data;
  } finally {
    buttonLoading.value = false;
  }
};

const openMarkMissing = async (fieldKey: string) => {
  const { value } = await proxy?.$modal.prompt('标记「' + fieldKey + '」为已确认缺失（留空=未采集，本操作=已确认缺失，07 §2.4）');
  await markMissing(detailDrawer.memberId, fieldKey, value);
  proxy?.$modal.msgSuccess('已标记缺项');
  const res = await getMember(detailDrawer.memberId);
  detailDrawer.value = res.data;
};

const openUnmarkMissing = async (fieldKey: string) => {
  await proxy?.$modal.confirm('取消「' + fieldKey + '」的缺项标记？');
  await unmarkMissing(detailDrawer.memberId, fieldKey);
  const res = await getMember(detailDrawer.memberId);
  detailDrawer.value = res.data;
};

/** 导出（03 §导出约定：download 走 POST 表单绑定，筛选与列表一致、不带分页参数） */
const handleExport = () => {
  const params = {
    name: queryParams.value.name,
    phoneNumber: queryParams.value.phoneNumber,
    idCard: queryParams.value.idCard,
    fileNumber: queryParams.value.fileNumber,
    sex: queryParams.value.sex,
    memberStatus: queryParams.value.memberStatus,
    tagId: queryParams.value.tagId
  };
  proxy?.download('health/members/export', params, `会员数据_${new Date().getTime()}.xlsx`);
};

onMounted(() => {
  getList();
  getTags();
});
</script>

<style scoped>
.field-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.history-row {
  display: flex;
  gap: 8px;
  align-items: center;
}
.clickable {
  cursor: pointer;
}
.mr4 {
  margin-right: 4px;
}
</style>
