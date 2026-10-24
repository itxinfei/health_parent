<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb8">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="处理状态">
              <el-select v-model="queryParams.handled" placeholder="全部" clearable style="width: 120px">
                <el-option label="未处理" :value="false" />
                <el-option label="已处理" :value="true" />
              </el-select>
            </el-form-item>
            <el-form-item label="送达确认">
              <el-select v-model="queryParams.confirmed" placeholder="全部" clearable style="width: 120px">
                <el-option label="未确认" :value="false" />
                <el-option label="已确认" :value="true" />
              </el-select>
            </el-form-item>
            <el-form-item label="规则码">
              <el-select v-model="queryParams.ruleCode" placeholder="全部" clearable style="width: 140px">
                <el-option v-for="code in RULE_CODES" :key="code" :label="code" :value="code" />
              </el-select>
            </el-form-item>
            <el-form-item label="档案号">
              <el-input v-model="queryParams.archiveNo" placeholder="精确匹配" clearable style="width: 160px" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="接收医生">
              <el-input v-model="queryParams.receiver" placeholder="模糊" clearable style="width: 120px" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="通知方式">
              <el-select v-model="queryParams.notifyWay" placeholder="全部" clearable style="width: 140px">
                <el-option v-for="dict in health_notify_way" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="科室">
              <el-select v-model="queryParams.deptId" placeholder="全部" clearable filterable style="width: 150px">
                <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
              </el-select>
            </el-form-item>
            <el-form-item label="报告时间">
              <el-date-picker
                v-model="timeRange"
                type="datetimerange"
                value-format="YYYY-MM-DD HH:mm:ss"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                style="width: 360px"
              />
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
          <el-col :span="24">
            <span class="hint">质控清单与专人查访入口（07 §3.2.1）；handled / confirmed 为派生筛选，无确认视为未送达（18 D5）</span>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="criticalList">
        <el-table-column label="报告时间" prop="reportTime" width="165" />
        <el-table-column label="会员" width="110">
          <template #default="scope">{{ scope.row.memberName || scope.row.memberId || '-' }}</template>
        </el-table-column>
        <el-table-column label="档案号" prop="archiveNo" width="175" show-overflow-tooltip />
        <el-table-column label="项目" prop="itemName" width="130" show-overflow-tooltip />
        <el-table-column label="危急值" prop="criticalValue" width="110" />
        <el-table-column label="规则码" prop="ruleCode" width="130" />
        <el-table-column label="科室" width="120">
          <template #default="scope">{{ deptName(scope.row.deptId) }}</template>
        </el-table-column>
        <el-table-column label="接收医生" prop="receiver" width="100" />
        <el-table-column label="通知方式" width="130">
          <template #default="scope">
            <dict-tag :options="health_notify_way" :value="scope.row.notifyWay" />
          </template>
        </el-table-column>
        <el-table-column label="送达确认" width="165">
          <template #default="scope">
            <el-tag v-if="scope.row.confirmed" type="success" size="small">{{ scope.row.receiverConfirmTime }}</el-tag>
            <el-tag v-else type="danger" size="small">未确认</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处置结果" prop="handleResult" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.handled" type="success" size="small">已处理</el-tag>
            <el-tag v-else type="warning" size="small">未处理</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center">
          <template #default="scope">
            <el-button v-hasPermi="['health:critical:edit']" link type="primary" icon="Edit" @click="openHandle(scope.row)">处置</el-button>
            <el-button v-hasPermi="['health:memberreport:query']" link type="primary" icon="View" @click="viewReport(scope.row)">报告</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-dialog v-model="handleDialog.visible" title="危急值处置" width="520px" append-to-body>
      <el-form label-width="110px">
        <el-form-item label="触发项">{{ handleDialog.row?.itemName }}（{{ handleDialog.row?.criticalValue }}）</el-form-item>
        <el-form-item label="规则码">{{ handleDialog.row?.ruleCode }}</el-form-item>
        <el-form-item label="接收医生">
          <el-input v-model="handleForm.receiver" />
        </el-form-item>
        <el-form-item label="通知方式">
          <el-select v-model="handleForm.notifyWay" clearable style="width: 100%">
            <el-option v-for="dict in health_notify_way" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="接收确认时间">
          <el-date-picker v-model="handleForm.receiverConfirmTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="处置结果">
          <el-input v-model="handleForm.handleResult" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="跟踪人">
          <el-input v-model="handleForm.trackBy" />
        </el-form-item>
        <el-form-item label="跟踪时间">
          <el-date-picker v-model="handleForm.trackTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="跟踪备注">
          <el-input v-model="handleForm.trackRemark" type="textarea" :rows="2" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialog.visible = false">取 消</el-button>
        <el-button :loading="buttonLoading" type="primary" @click="submitHandle">保 存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="reportDrawer.visible" title="关联报告" size="60%">
      <template v-if="reportDetail">
        <el-descriptions :column="3" border class="mb8">
          <el-descriptions-item label="报告号">{{ reportDetail.report?.reportNo }}</el-descriptions-item>
          <el-descriptions-item label="会员">{{ reportDetail.report?.memberName }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ reportDetail.report?.status }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="reportDetail.items || []" border size="small">
          <el-table-column label="项目" prop="itemName" width="140" />
          <el-table-column label="结果" prop="result" width="100" />
          <el-table-column label="单位" prop="unit" width="90" />
          <el-table-column label="参考范围" prop="refRange" width="150" />
          <el-table-column label="判定" width="90" align="center">
            <template #default="scope">
              <dict-tag :options="health_abnormal_flag" :value="scope.row.abnormalFlag" />
            </template>
          </el-table-column>
          <el-table-column label="危急" width="70" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.criticalFlag === '1'" type="danger" size="small">危急</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-drawer>
  </div>
</template>

<script setup name="HealthCritical" lang="ts">
import { getReport } from '@/api/health/report';
import { listCritical, handleCritical } from '@/api/health/critical';
import { CriticalHandleForm, CriticalQuery } from '@/api/health/critical/types';
import { CriticalLogVO, ReportDetailVO } from '@/api/health/report/types';
import { listDept } from '@/api/system/dept';
import { DeptVO } from '@/api/system/dept/types';

const RULE_CODES = ['D4-WBC-LOW', 'D4-WBC-HIGH', 'D4-HB-LOW', 'D4-HB-HIGH', 'D4-PLT-LOW', 'D4-PLT-HIGH'];

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { health_notify_way, health_abnormal_flag } = toRefs<any>(proxy?.useDict('health_notify_way', 'health_abnormal_flag'));

const criticalList = ref<CriticalLogVO[]>([]);
const deptOptions = ref<DeptVO[]>([]);
const loading = ref(true);
const buttonLoading = ref(false);
const showSearch = ref(true);
const total = ref(0);
const timeRange = ref<string[]>([]);
const queryFormRef = ref<ElFormInstance>();

const queryParams = reactive<CriticalQuery>({
  pageNum: 1,
  pageSize: 10,
  handled: undefined,
  confirmed: undefined,
  ruleCode: undefined,
  archiveNo: undefined,
  receiver: undefined,
  notifyWay: undefined,
  deptId: undefined,
  beginTime: undefined,
  endTime: undefined
});

const handleDialog = reactive<DialogOption & { row?: CriticalLogVO }>({ visible: false, title: '危急值处置' });
const handleForm = reactive<CriticalHandleForm>({});
const reportDrawer = reactive({ visible: false });
const reportDetail = ref<ReportDetailVO>();

const deptName = (deptId?: number) => deptOptions.value.find((d) => d.deptId === deptId)?.deptName || (deptId ?? '-');

const getList = async () => {
  loading.value = true;
  try {
    queryParams.beginTime = timeRange.value?.[0];
    queryParams.endTime = timeRange.value?.[1];
    const res = await listCritical(queryParams);
    criticalList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

const resetQuery = () => {
  timeRange.value = [];
  queryFormRef.value?.resetFields();
  queryParams.handled = undefined;
  queryParams.confirmed = undefined;
  queryParams.ruleCode = undefined;
  handleQuery();
};

const openHandle = (row: CriticalLogVO) => {
  handleDialog.row = row;
  Object.assign(handleForm, {
    receiver: row.receiver,
    receiverConfirmTime: row.receiverConfirmTime,
    notifyWay: row.notifyWay,
    handleResult: row.handleResult,
    trackBy: row.trackBy,
    trackTime: row.trackTime,
    trackRemark: row.trackRemark
  });
  handleDialog.visible = true;
};

const submitHandle = async () => {
  buttonLoading.value = true;
  try {
    await handleCritical(handleDialog.row!.id!, handleForm);
    proxy?.$modal.msgSuccess('处置已保存');
    handleDialog.visible = false;
    await getList();
  } finally {
    buttonLoading.value = false;
  }
};

const viewReport = async (row: CriticalLogVO) => {
  if (!row.reportId) return;
  const res = await getReport(row.reportId);
  reportDetail.value = res.data;
  reportDrawer.visible = true;
};

onMounted(async () => {
  const res = await listDept();
  deptOptions.value = res.data || [];
  await getList();
});
</script>

<style scoped>
.hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
