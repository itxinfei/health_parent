<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb8">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="档案号" prop="archiveNo">
              <el-input v-model="queryParams.archiveNo" placeholder="精确匹配" clearable style="width: 180px" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
                <el-option v-for="dict in health_report_status" :key="dict.value" :label="dict.label" :value="dict.value" />
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
          <el-col :span="24">
            <span class="hint">总检工作台：逐科室提交小结 → 选择结论词 → 总检提交（07 §5.4 / 18 C8）</span>
          </el-col>
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="reportList">
        <el-table-column label="报告号" prop="reportNo" width="150" show-overflow-tooltip />
        <el-table-column label="会员" width="100" align="center">
          <template #default="scope">{{ scope.row.memberName || scope.row.memberId }}</template>
        </el-table-column>
        <el-table-column label="档案号" prop="archiveNo" width="180" show-overflow-tooltip />
        <el-table-column label="体检日期" prop="checkDate" width="110" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <dict-tag :options="health_report_status" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="异常/危急" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.abnormalCount" type="warning" size="small" class="mr4">异常 {{ scope.row.abnormalCount }}</el-tag>
            <el-tag v-if="scope.row.criticalCount" type="danger" size="small">危急 {{ scope.row.criticalCount }}</el-tag>
            <span v-if="!scope.row.abnormalCount && !scope.row.criticalCount">-</span>
          </template>
        </el-table-column>
        <el-table-column label="主检审核" width="150" align="center">
          <template #default="scope">{{ scope.row.summaryBy || '-' }}<br />{{ scope.row.summaryTime || '' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" align="center">
          <template #default="scope">
            <el-button v-hasPermi="['health:report:summary']" link type="primary" icon="Edit" @click="openWorkbench(scope.row)">
              {{ scope.row.status === '待总检' || scope.row.revokeFlag === '1' ? '开始总检' : '查看' }}
            </el-button>
            <el-button v-hasPermi="['health:report:finalcheck']" v-if="scope.row.status === '已总检' && !scope.row.finalCheckTime" link type="success" @click="handleFinalCheck(scope.row)">终审</el-button>
            <el-button v-hasPermi="['health:memberreport:edit']" v-if="scope.row.status === '已总检'" link type="primary" @click="handlePrint(scope.row)">打印</el-button>
            <el-button v-hasPermi="['health:memberreport:edit']" v-if="scope.row.status === '已打印'" link type="primary" @click="handleIssue(scope.row)">发放</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <el-drawer v-model="workbench.visible" title="总检工作台" size="78%">
      <template v-if="detail">
        <el-descriptions :column="2" border label-width="96px" class="mb8">
          <el-descriptions-item label="报告号">{{ detail.report?.reportNo }}</el-descriptions-item>
          <el-descriptions-item label="会员">{{ detail.report?.memberName }}（{{ detail.report?.memberId }}）</el-descriptions-item>
          <el-descriptions-item label="体检日期">{{ detail.report?.checkDate }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <dict-tag :options="health_report_status" :value="detail.report?.status" />
            <el-tag v-if="detail.report?.revokeFlag === '1'" type="warning" size="small" class="ml4">已撤销</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-alert v-if="blockingItems.length" type="error" :closable="false" class="mb8"
          :title="`存在 ${blockingItems.length} 项未检/待查，不满足总检提交条件：${blockingItems.map((i) => i.itemName).join('、')}`" />

        <el-divider content-position="left">检查明细（按科室）</el-divider>
        <el-table :data="detail.items || []" border size="small" class="mb8">
          <el-table-column label="科室" width="130">
            <template #default="scope">{{ deptName(scope.row.deptId) }}</template>
          </el-table-column>
          <el-table-column label="项目" prop="itemName" min-width="140" show-overflow-tooltip />
          <el-table-column label="结果" width="120">
            <template #default="scope">{{ scope.row.result }} {{ scope.row.unit }}</template>
          </el-table-column>
          <el-table-column label="参考范围" prop="refRange" width="150" show-overflow-tooltip />
          <el-table-column label="判定" width="90" align="center">
            <template #default="scope">
              <dict-tag :options="health_abnormal_flag" :value="scope.row.abnormalFlag" />
            </template>
          </el-table-column>
          <el-table-column label="项目状态" width="90" align="center">
            <template #default="scope">
              <dict-tag :options="health_item_status" :value="scope.row.itemStatus" />
            </template>
          </el-table-column>
          <el-table-column label="标记" width="130" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.criticalFlag === '1'" type="danger" size="small">危急</el-tag>
              <el-tag v-if="scope.row.majorPositive === '1'" type="danger" size="small" effect="plain">重大阳性</el-tag>
              <el-tag v-if="scope.row.edgeFlag === '1'" type="warning" size="small">临界</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <el-divider content-position="left">科室小结</el-divider>
        <div v-for="d in deptRows" :key="d.deptId" class="dept-block">
          <div class="dept-head">
            <b>{{ deptName(d.deptId) }}</b>
            <el-tag v-if="d.sendBackFlag === '1'" type="warning" size="small" class="ml4">打回：{{ d.sendBackReason }}</el-tag>
            <el-tag v-else-if="d.submitFlag === '1'" type="success" size="small" class="ml4">已提交</el-tag>
          </div>
          <el-input v-model="deptText[d.deptId]" type="textarea" :rows="2" maxlength="500" show-word-limit
            :placeholder="`${deptName(d.deptId)}小结`" />
          <div class="dept-actions">
            <el-button size="small" @click="saveSummaryText(d, false)">暂存</el-button>
            <el-button v-hasPermi="['health:memberreport:edit']" size="small" type="primary" @click="saveSummaryText(d, true)">提交小结</el-button>
          </div>
        </div>
        <el-empty v-if="!deptRows.length" description="暂无科室明细" :image-size="60" />

        <el-divider content-position="left">总检结论</el-divider>
        <el-form label-width="96px">
          <el-form-item label="结论词">
            <el-select v-model="selectedDictIds" multiple filterable clearable style="width: 100%"
              placeholder="仅可选用已发布结论词（自由文本仅限「其他结论」）">
              <el-option v-for="c in conclusionDicts" :key="c.id" :label="`${c.code} ${c.term}`" :value="c.id!" />
            </el-select>
          </el-form-item>
          <el-form-item label="结论类型">
            <el-select v-model="summaryForm.conclusionType" clearable style="width: 220px">
              <el-option v-for="dict in health_conclusion_type" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="异常汇总">
            <el-input v-model="summaryForm.abnormalSummary" type="textarea" :rows="2" placeholder="留空则按异常项自动拼接" />
          </el-form-item>
          <el-form-item label="科室小结">
            <el-input v-model="summaryForm.deptSummaryText" type="textarea" :rows="2" placeholder="留空则按已提交科室小结自动拼接" />
          </el-form-item>
          <el-form-item label="依据">
            <el-input v-model="summaryForm.basis" type="textarea" :rows="2" maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item label="健康建议">
            <el-input v-model="summaryForm.advice" type="textarea" :rows="2" maxlength="500" show-word-limit />
          </el-form-item>
          <el-form-item label="总检医生">
            <el-input v-model="summaryForm.doctor" style="width: 220px" />
          </el-form-item>
        </el-form>

        <el-alert v-if="detail.revokeReason" type="warning" :closable="false" class="mb8"
          :title="`撤销/打回原因：${detail.revokeReason}`" />
      </template>

      <template #footer>
        <el-button v-hasPermi="['health:report:summary']" :loading="buttonLoading" @click="saveConclusionsOnly">仅保存结论</el-button>
        <el-button v-hasPermi="['health:report:revoke']" type="warning" @click="openRevoke">撤销总检</el-button>
        <el-button v-hasPermi="['health:report:revoke']" @click="openSendBack">打回科室</el-button>
        <el-button v-hasPermi="['health:report:summary']" type="success" :loading="buttonLoading" @click="submitSummary">提交总检</el-button>
      </template>
    </el-drawer>

    <el-dialog v-model="sendBackDialog.visible" title="异常打回科室" width="440px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="科室" required>
          <el-select v-model="sendBackForm.deptId" filterable style="width: 100%">
            <el-option v-for="d in deptRows" :key="d.deptId" :label="deptName(d.deptId)" :value="d.deptId" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因" required>
          <el-input v-model="sendBackForm.reason" type="textarea" :rows="3" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sendBackDialog.visible = false">取 消</el-button>
        <el-button :loading="buttonLoading" type="warning" @click="submitSendBack">打 回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="HealthSummary" lang="ts">
import {
  conclusionOptions,
  finalCheckReport,
  getReport,
  issueReport,
  listReport,
  printReport,
  revokeReport,
  saveDeptSummary,
  saveReportConclusions,
  sendBackReport,
  submitReportSummary
} from '@/api/health/report';
import { ConclusionDictVO, ConclusionForm, DeptSummaryVO, ReportDetailVO, ReportVO } from '@/api/health/report/types';
import { listDept } from '@/api/system/dept';
import { DeptVO } from '@/api/system/dept/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { health_report_status, health_abnormal_flag, health_item_status, health_conclusion_type } = toRefs<any>(
  proxy?.useDict('health_report_status', 'health_abnormal_flag', 'health_item_status', 'health_conclusion_type')
);

const READY_STATUS = ['已检', '弃检', '延期'];

const reportList = ref<ReportVO[]>([]);
const deptOptions = ref<DeptVO[]>([]);
const conclusionDicts = ref<ConclusionDictVO[]>([]);
const loading = ref(true);
const buttonLoading = ref(false);
const showSearch = ref(true);
const total = ref(0);
const queryFormRef = ref<ElFormInstance>();

const queryParams = reactive<any>({ pageNum: 1, pageSize: 10, archiveNo: undefined, status: '待总检' });

const workbench = reactive<DialogOption & { row?: ReportVO }>({ visible: false, title: '总检工作台' });
const detail = ref<ReportDetailVO>();
const deptText = reactive<Record<number, string>>({});
const selectedDictIds = ref<number[]>([]);
const summaryForm = reactive<any>({ conclusionType: undefined, abnormalSummary: undefined, deptSummaryText: undefined, basis: undefined, advice: undefined, doctor: undefined });
const prevConclusions = ref<ConclusionForm[]>([]);

const sendBackDialog = reactive<DialogOption>({ visible: false, title: '异常打回科室' });
const sendBackForm = reactive<any>({ deptId: undefined, reason: '' });

const deptName = (deptId?: number) => deptOptions.value.find((d) => d.deptId === deptId)?.deptName || (deptId ?? '-');

const blockingItems = computed(() => (detail.value?.items || []).filter((i) => !READY_STATUS.includes(i.itemStatus || '')));

const deptRows = computed<DeptSummaryVO[]>(() => {
  const map = new Map<number, DeptSummaryVO>();
  (detail.value?.deptSummaries || []).forEach((s) => s.deptId && map.set(s.deptId, s));
  (detail.value?.items || []).forEach((i) => {
    if (i.deptId && !map.has(i.deptId)) {
      map.set(i.deptId, { deptId: i.deptId, submitFlag: '0' });
    }
  });
  return Array.from(map.values()).sort((a, b) => (a.deptId || 0) - (b.deptId || 0));
});

const getList = async () => {
  loading.value = true;
  try {
    const res = await listReport(queryParams);
    reportList.value = res.rows;
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
  queryFormRef.value?.resetFields();
  queryParams.status = '待总检';
  handleQuery();
};

const openWorkbench = async (row: ReportVO) => {
  workbench.row = row;
  await loadDetail(row.id!);
  workbench.visible = true;
};

const loadDetail = async (reportId: number) => {
  const res = await getReport(reportId);
  detail.value = res.data;
  Object.keys(deptText).forEach((k) => delete deptText[Number(k)]);
  (res.data.deptSummaries || []).forEach((s) => {
    if (s.deptId) deptText[s.deptId] = s.summaryText || '';
  });
  (res.data.items || []).forEach((i) => {
    if (i.deptId && deptText[i.deptId] === undefined) deptText[i.deptId] = '';
  });
  const finals = (res.data.conclusions || []).filter((c) => c.finalFlag === '1');
  selectedDictIds.value = finals.map((c) => c.conclusionDictId!).filter(Boolean);
  prevConclusions.value = finals.map((c) => ({
    conclusionDictId: c.conclusionDictId,
    conclusionType: c.conclusionType,
    grade: c.grade,
    mergeGroupId: c.mergeGroupId,
    sortNo: c.sortNo
  }));
  Object.assign(summaryForm, {
    conclusionType: res.data.report?.conclusionType,
    abnormalSummary: res.data.abnormalSummary,
    deptSummaryText: res.data.deptSummaryText,
    basis: res.data.basis,
    advice: res.data.advice,
    doctor: res.data.report?.doctor
  });
  if (!conclusionDicts.value.length) {
    const dictRes = await conclusionOptions();
    conclusionDicts.value = dictRes.data || [];
  }
};

const buildConclusions = (): ConclusionForm[] => {
  return selectedDictIds.value.map((id, idx) => {
    const prev = prevConclusions.value.find((p) => p.conclusionDictId === id);
    return { conclusionDictId: id, conclusionType: prev?.conclusionType, grade: prev?.grade, mergeGroupId: prev?.mergeGroupId, sortNo: idx + 1 };
  });
};

const saveSummaryText = async (d: DeptSummaryVO, submit: boolean) => {
  if (submit && !(deptText[d.deptId!] || '').trim()) {
    proxy?.$modal.msgWarning('提交科室小结不能为空（6010）');
    return;
  }
  await saveDeptSummary(detail.value!.report!.id!, {
    deptId: d.deptId,
    summaryText: deptText[d.deptId!],
    submit
  });
  proxy?.$modal.msgSuccess(submit ? '科室小结已提交' : '已暂存');
  await loadDetail(detail.value!.report!.id!);
};

const saveConclusionsOnly = async () => {
  const list = buildConclusions();
  if (!list.length) {
    proxy?.$modal.msgWarning('请至少选择一个结论词');
    return;
  }
  buttonLoading.value = true;
  try {
    await saveReportConclusions(detail.value!.report!.id!, list);
    proxy?.$modal.msgSuccess('结论条目已保存（末次语义）');
    await loadDetail(detail.value!.report!.id!);
  } finally {
    buttonLoading.value = false;
  }
};

const submitSummary = async () => {
  if (blockingItems.value.length) {
    proxy?.$modal.msgWarning(`存在未检/待查项目：${blockingItems.value.map((i) => i.itemName).join('、')}`);
    return;
  }
  if (!selectedDictIds.value.length) {
    proxy?.$modal.msgWarning('请至少选择一个结论词');
    return;
  }
  await proxy?.$modal.confirm('确认提交总检？提交后报告状态变为「已总检」。');
  buttonLoading.value = true;
  try {
    await submitReportSummary(detail.value!.report!.id!, { ...summaryForm, conclusions: buildConclusions() });
    proxy?.$modal.msgSuccess('总检提交成功');
    workbench.visible = false;
    await getList();
  } finally {
    buttonLoading.value = false;
  }
};

const openRevoke = async () => {
  let reason = '';
  try {
    const res = await proxy?.$modal.prompt('撤销总检须填写原因（18 C9）：');
    reason = res?.value || '';
  } catch {
    return;
  }
  if (!reason) return;
  await revokeReport(detail.value!.report!.id!, reason);
  proxy?.$modal.msgSuccess('已撤销，报告回待总检');
  await loadDetail(detail.value!.report!.id!);
  await getList();
};

const openSendBack = () => {
  sendBackForm.deptId = undefined;
  sendBackForm.reason = '';
  sendBackDialog.visible = true;
};

const submitSendBack = async () => {
  if (!sendBackForm.deptId || !sendBackForm.reason) {
    proxy?.$modal.msgWarning('科室与原因均必填');
    return;
  }
  buttonLoading.value = true;
  try {
    await sendBackReport(detail.value!.report!.id!, sendBackForm);
    proxy?.$modal.msgSuccess('已打回科室');
    sendBackDialog.visible = false;
    await loadDetail(detail.value!.report!.id!);
  } finally {
    buttonLoading.value = false;
  }
};

const handleFinalCheck = async (row: ReportVO) => {
  await proxy?.$modal.confirm('确认终审签发？签发后不可打回。');
  await finalCheckReport(row.id!);
  proxy?.$modal.msgSuccess('终审签发完成');
  await getList();
};

const handlePrint = async (row: ReportVO) => {
  await printReport(row.id!);
  proxy?.$modal.msgSuccess('已标记打印');
  await getList();
};

const handleIssue = async (row: ReportVO) => {
  await issueReport(row.id!);
  proxy?.$modal.msgSuccess('已发放');
  await getList();
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
.mr4 {
  margin-right: 4px;
}
.ml4 {
  margin-left: 4px;
}
.dept-block {
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 10px;
}
.dept-head {
  margin-bottom: 6px;
}
.dept-actions {
  margin-top: 6px;
  text-align: right;
}
:deep(.el-descriptions__label) {
  white-space: nowrap;
}
</style>
