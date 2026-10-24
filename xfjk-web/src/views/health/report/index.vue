<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb8">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="档案号" prop="archiveNo">
              <el-input v-model="queryParams.archiveNo" placeholder="精确匹配" clearable style="width: 170px" @keyup.enter="handleQuery" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 130px">
                <el-option v-for="dict in health_report_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="来源" prop="source">
              <el-select v-model="queryParams.source" placeholder="全部" clearable style="width: 120px">
                <el-option v-for="dict in health_report_source" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="体检日期">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                value-format="YYYY-MM-DD"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 240px"
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
          <el-col :span="1.5">
            <el-button v-hasPermi="['health:memberreport:add']" type="primary" plain icon="Plus" @click="openCreate">新建报告</el-button>
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
        <el-table-column label="来源" width="80" align="center">
          <template #default="scope">
            <dict-tag :options="health_report_source" :value="scope.row.source" />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <dict-tag :options="health_report_status" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="异常/危急" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.abnormalCount" type="warning" size="small" class="mr4">异常 {{ scope.row.abnormalCount }}</el-tag>
            <el-tag v-if="scope.row.criticalCount" type="danger" size="small">危急 {{ scope.row.criticalCount }}</el-tag>
            <span v-if="!scope.row.abnormalCount && !scope.row.criticalCount">-</span>
          </template>
        </el-table-column>
        <el-table-column label="终审" width="150" align="center">
          <template #default="scope">
            <span v-if="scope.row.finalCheckTime">{{ scope.row.finalCheckBy }}<br />{{ scope.row.finalCheckTime }}</span>
            <el-tag v-else type="info" size="small">未签发</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="200" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button v-hasPermi="['health:memberreport:query']" link type="primary" icon="View" @click="openDetail(scope.row)">详情</el-button>
            <el-button v-hasPermi="['health:memberreport:edit']" link type="primary" icon="Edit" @click="openItemDialog(scope.row)">录入</el-button>
            <el-button v-hasPermi="['health:memberreport:add']" link type="primary" icon="Upload" @click="openUpload(scope.row)">上传</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 新建报告 -->
    <el-dialog v-model="createDialog.visible" title="手工建档报告" width="520px" append-to-body>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="90px">
        <el-form-item label="会员ID" prop="memberId">
          <el-input v-model.number="createForm.memberId" placeholder="会员 id" />
        </el-form-item>
        <el-form-item label="体检日期" prop="checkDate">
          <el-date-picker v-model="createForm.checkDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="报告来源" prop="source">
          <el-select v-model="createForm.source" style="width: 100%">
            <el-option v-for="dict in health_report_source" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="体检号">
          <el-input v-model="createForm.examNo" placeholder="可空" />
        </el-form-item>
        <el-form-item label="总检医生">
          <el-input v-model="createForm.doctor" placeholder="可空" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialog.visible = false">取 消</el-button>
        <el-button :loading="buttonLoading" type="primary" @click="submitCreate">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 附件上传 / 解析 -->
    <el-dialog v-model="uploadDialog.visible" title="上传附件与结果文件" width="520px" append-to-body>
      <el-alert type="warning" :closable="false" class="mb8" title="附件与解析文件均走 OSS；MinIO 凭据未配置时上传会失败（17 待确认 7）" />
      <el-form label-width="90px">
        <el-form-item label="报告号">{{ uploadDialog.row?.reportNo }}</el-form-item>
        <el-form-item label="附件（必选）">
          <input type="file" @change="onPickAttachment" />
        </el-form-item>
        <el-form-item label="结果文件">
          <input type="file" accept=".xlsx,.xls,.csv" @change="onPickResultFile" />
          <div class="tip">可选：勾选后将同时按 07 §4 解析明细</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialog.visible = false">取 消</el-button>
        <el-button :loading="buttonLoading" type="primary" @click="submitUpload">上 传</el-button>
      </template>
    </el-dialog>

    <!-- 单项结果录入 -->
    <el-dialog v-model="itemDialog.visible" title="录入单项结果" width="560px" append-to-body>
      <el-form ref="itemFormRef" :model="itemForm" :rules="itemRules" label-width="90px">
        <el-form-item label="项目名称" prop="itemName">
          <el-input v-model="itemForm.itemName" placeholder="按名称匹配参考区间" />
        </el-form-item>
        <el-form-item label="检查项ID">
          <el-input v-model.number="itemForm.checkitemId" placeholder="可空（未匹配项）" />
        </el-form-item>
        <el-form-item label="结果类型" prop="resultType">
          <el-select v-model="itemForm.resultType" style="width: 100%">
            <el-option v-for="dict in health_result_type" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="结果" prop="result">
          <el-input v-model="itemForm.result" placeholder="数值 / 阳性阴性 / 文本" />
        </el-form-item>
        <el-form-item v-if="itemForm.resultType === '1'" label="定性">
          <el-select v-model="itemForm.qualitative" clearable style="width: 100%">
            <el-option v-for="dict in health_qualitative" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="itemForm.unit" />
        </el-form-item>
        <el-form-item label="承检科室" prop="deptId">
          <el-select v-model="itemForm.deptId" filterable style="width: 100%">
            <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目状态" prop="itemStatus">
          <el-select v-model="itemForm.itemStatus" style="width: 100%">
            <el-option v-for="dict in health_item_status" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="itemForm.itemStatus === '弃检'" label="弃检原因" prop="discardReason">
          <el-input v-model="itemForm.discardReason" placeholder="弃检必填（6009）" />
        </el-form-item>
        <el-form-item label="临界值">
          <el-select v-model="itemForm.edgeFlag" clearable style="width: 100%">
            <el-option v-for="dict in health_edge_flag" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="重大阳性">
          <el-select v-model="itemForm.majorPositive" clearable style="width: 100%">
            <el-option v-for="dict in health_major_positive" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="['2', '3'].includes(itemForm.resultType)" label="异常标志">
          <el-select v-model="itemForm.abnormalFlag" clearable style="width: 100%">
            <el-option v-for="dict in health_abnormal_flag" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="检查医生">
          <el-input v-model="itemForm.doctor" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialog.visible = false">取 消</el-button>
        <el-button :loading="buttonLoading" type="primary" @click="submitItem">保 存</el-button>
      </template>
    </el-dialog>

    <!-- 危急值登记 -->
    <el-dialog v-model="criticalDialog.visible" title="危急值登记" width="480px" append-to-body>
      <el-form label-width="100px">
        <el-form-item label="触发项">{{ criticalDialog.item?.itemName }}（{{ criticalDialog.item?.result }} {{ criticalDialog.item?.unit }}）</el-form-item>
        <el-form-item label="接收医生" required>
          <el-input v-model="criticalForm.receiver" />
        </el-form-item>
        <el-form-item label="通知方式">
          <el-select v-model="criticalForm.notifyWay" clearable style="width: 100%">
            <el-option v-for="dict in health_notify_way" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="报告时间">
          <el-date-picker v-model="criticalForm.reportTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="接收确认时间">
          <el-date-picker v-model="criticalForm.receiverConfirmTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="处置结果">
          <el-input v-model="criticalForm.handleResult" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="criticalDialog.visible = false">取 消</el-button>
        <el-button :loading="buttonLoading" type="danger" @click="submitCritical">登 记</el-button>
      </template>
    </el-dialog>

    <!-- 报告详情 -->
    <el-drawer v-model="detailDrawer.visible" title="体检报告详情" size="72%">
      <template v-if="detail">
        <el-alert v-if="detail.prevReport" type="info" :closable="false" class="mb8"
          :title="`上次报告：${detail.prevReport.reportNo}（${detail.prevReport.checkDate}）`" />
        <el-descriptions :column="2" border label-width="96px" class="mb8">
          <el-descriptions-item label="报告号">{{ detail.report?.reportNo }}</el-descriptions-item>
          <el-descriptions-item label="会员">{{ detail.report?.memberName }}（{{ detail.report?.memberId }}）</el-descriptions-item>
          <el-descriptions-item label="档案号">{{ detail.report?.archiveNo }}</el-descriptions-item>
          <el-descriptions-item label="体检日期">{{ detail.report?.checkDate }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <dict-tag :options="health_report_status" :value="detail.report?.status" />
            <el-tag v-if="detail.report?.revokeFlag === '1'" type="warning" size="small" class="ml4">已撤销</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="来源">{{ detail.report?.source }}</el-descriptions-item>
          <el-descriptions-item label="总检医生">{{ detail.report?.doctor || '-' }}</el-descriptions-item>
          <el-descriptions-item label="体检号">{{ detail.report?.examNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="科室自查">{{ detail.firstCheckBy || '-' }} {{ detail.firstCheckTime || '' }}</el-descriptions-item>
          <el-descriptions-item label="主检审核">{{ detail.summaryBy || '-' }} {{ detail.summaryTime || '' }}</el-descriptions-item>
          <el-descriptions-item label="终审签发">{{ detail.report?.finalCheckBy || '-' }} {{ detail.report?.finalCheckTime || '' }}</el-descriptions-item>
          <el-descriptions-item label="撤销原因">{{ detail.revokeReason || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">五段正文</el-divider>
        <el-descriptions :column="1" border class="mb8">
          <el-descriptions-item label="科室小结">{{ detail.deptSummaryText || '-' }}</el-descriptions-item>
          <el-descriptions-item label="异常汇总">{{ detail.abnormalSummary || '-' }}</el-descriptions-item>
          <el-descriptions-item label="总检结论">{{ detail.conclusion || '-' }}</el-descriptions-item>
          <el-descriptions-item label="依据">{{ detail.basis || '-' }}</el-descriptions-item>
          <el-descriptions-item label="健康建议">{{ detail.advice || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">检查明细</el-divider>
        <el-table :data="detail.items || []" border size="small" class="mb8">
          <el-table-column label="项目" prop="itemName" min-width="140" show-overflow-tooltip />
          <el-table-column label="结果" width="110">
            <template #default="scope">{{ scope.row.result }} {{ scope.row.unit }}</template>
          </el-table-column>
          <el-table-column label="参考范围" prop="refRange" width="150" show-overflow-tooltip>
            <template #default="scope">
              <span :class="{ 'ref-missing': scope.row.refConfigured === '0' }">{{ scope.row.refRange || '未配置' }}</span>
            </template>
          </el-table-column>
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
          <el-table-column label="标记" width="110" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.criticalFlag === '1'" type="danger" size="small">危急</el-tag>
              <el-tag v-if="scope.row.majorPositive === '1'" type="danger" size="small" effect="plain">重大阳性</el-tag>
              <el-tag v-if="scope.row.edgeFlag === '1'" type="warning" size="small">临界</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="上次" prop="prevResult" width="100" show-overflow-tooltip />
          <el-table-column label="操作" width="150" align="center">
            <template #default="scope">
              <el-button v-hasPermi="['health:memberreport:edit']" link type="primary" @click="openItemDialog(detail.report!, scope.row)">修改</el-button>
              <el-button
                v-hasPermi="['health:critical:add']"
                v-if="scope.row.criticalFlag === '1' && !isCriticalRegistered(scope.row.id)"
                link
                type="danger"
                @click="openCritical(detail.report!, scope.row)"
              >登记危急值</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-divider content-position="left">科室小结</el-divider>
        <el-table :data="detail.deptSummaries || []" border size="small" class="mb8">
          <el-table-column label="科室" width="150">
            <template #default="scope">{{ deptName(scope.row.deptId) }}</template>
          </el-table-column>
          <el-table-column label="小结" prop="summaryText" min-width="240" show-overflow-tooltip />
          <el-table-column label="提交" width="70" align="center">
            <template #default="scope">{{ scope.row.submitFlag === '1' ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column label="打回" width="150">
            <template #default="scope">
              <el-tag v-if="scope.row.sendBackFlag === '1'" type="warning" size="small">{{ scope.row.sendBackReason }}</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>

        <el-divider content-position="left">总检结论（末次）</el-divider>
        <el-tag v-for="c in finalConclusions" :key="c.id" class="mr4 mb4">{{ conclusionTerm(c.conclusionDictId) }}<span v-if="c.grade">（{{ c.grade }}）</span></el-tag>
        <span v-if="!finalConclusions.length" class="text-muted">暂无结论条目</span>

        <el-divider content-position="left">危急值</el-divider>
        <el-table :data="detail.criticals || []" border size="small" class="mb8">
          <el-table-column label="项目" prop="itemName" width="140" />
          <el-table-column label="危急值" prop="criticalValue" width="110" />
          <el-table-column label="规则" prop="ruleCode" width="130" />
          <el-table-column label="接收医生" prop="receiver" width="100" />
          <el-table-column label="报告时间" prop="reportTime" width="160" />
          <el-table-column label="确认时间" prop="receiverConfirmTime" width="160" />
          <el-table-column label="处置结果" prop="handleResult" min-width="160" show-overflow-tooltip />
        </el-table>

        <el-divider content-position="left">归档资料</el-divider>
        <el-table :data="detail.archives || []" border size="small">
          <el-table-column label="类型" prop="type" width="120" />
          <el-table-column label="OSS ID" prop="ossId" width="180" />
          <el-table-column label="页数" prop="pageCount" width="80" />
          <el-table-column label="备注" prop="remark" min-width="160" />
          <el-table-column label="上传时间" prop="createTime" width="160" />
        </el-table>
      </template>

      <template #footer>
        <el-button v-hasPermi="['health:memberreport:edit']" @click="openArchive">归档</el-button>
        <el-button v-hasPermi="['health:report:revoke']" @click="openSendBack">打回科室</el-button>
        <el-button v-hasPermi="['health:report:revoke']" type="warning" @click="handleRevoke">撤销总检</el-button>
        <el-button v-hasPermi="['health:report:finalcheck']" type="success" @click="handleFinalCheck">终审签发</el-button>
        <el-button v-hasPermi="['health:memberreport:edit']" type="primary" @click="handlePrint">打印</el-button>
        <el-button v-hasPermi="['health:memberreport:edit']" type="primary" plain @click="handleIssue">发放</el-button>
      </template>
    </el-drawer>

    <!-- 打回 -->
    <el-dialog v-model="sendBackDialog.visible" title="异常打回科室" width="440px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="科室" required>
          <el-select v-model="sendBackForm.deptId" filterable style="width: 100%">
            <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
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

    <!-- 归档 -->
    <el-dialog v-model="archiveDialog.visible" title="归档附属资料" width="440px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="类型" required>
          <el-select v-model="archiveForm.type" style="width: 100%">
            <el-option v-for="dict in health_archive_type" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="OSS ID">
          <el-input v-model="archiveForm.ossId" placeholder="先上传取得 ossId，可空" />
        </el-form-item>
        <el-form-item label="页数">
          <el-input-number v-model="archiveForm.pageCount" :min="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="archiveForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="archiveDialog.visible = false">取 消</el-button>
        <el-button :loading="buttonLoading" type="primary" @click="submitArchive">保 存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="HealthReport" lang="ts">
import {
  addReport,
  addReportArchive,
  conclusionOptions,
  finalCheckReport,
  getReport,
  issueReport,
  listReport,
  printReport,
  registerCritical,
  revokeReport,
  saveReportItem,
  sendBackReport,
  uploadReportFile
} from '@/api/health/report';
import { ConclusionDictVO, ReportDetailVO, ReportItemForm, ReportItemVO, ReportVO } from '@/api/health/report/types';
import { listDept } from '@/api/system/dept';
import { DeptVO } from '@/api/system/dept/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const {
  health_report_status,
  health_report_source,
  health_result_type,
  health_item_status,
  health_qualitative,
  health_abnormal_flag,
  health_edge_flag,
  health_major_positive,
  health_archive_type,
  health_notify_way
} = toRefs<any>(
  proxy?.useDict(
    'health_report_status',
    'health_report_source',
    'health_result_type',
    'health_item_status',
    'health_qualitative',
    'health_abnormal_flag',
    'health_edge_flag',
    'health_major_positive',
    'health_archive_type',
    'health_notify_way'
  )
);

const reportList = ref<ReportVO[]>([]);
const deptOptions = ref<DeptVO[]>([]);
const conclusionDicts = ref<ConclusionDictVO[]>([]);
const loading = ref(true);
const buttonLoading = ref(false);
const showSearch = ref(true);
const total = ref(0);
const dateRange = ref<string[]>([]);
const queryFormRef = ref<ElFormInstance>();

const queryParams = reactive<any>({
  pageNum: 1,
  pageSize: 10,
  archiveNo: undefined,
  status: undefined,
  source: undefined,
  beginDate: undefined,
  endDate: undefined
});

const createDialog = reactive<DialogOption>({ visible: false, title: '手工建档报告' });
const createFormRef = ref<ElFormInstance>();
const createForm = reactive<any>({ memberId: undefined, checkDate: undefined, source: '手工录入', examNo: undefined, doctor: undefined, remark: undefined });
const createRules = {
  memberId: [{ required: true, message: '会员ID不能为空', trigger: 'blur' }],
  checkDate: [{ required: true, message: '体检日期不能为空', trigger: 'change' }],
  source: [{ required: true, message: '报告来源不能为空', trigger: 'change' }]
};

const uploadDialog = reactive<DialogOption & { row?: ReportVO }>({ visible: false, title: '上传附件与结果文件' });
const uploadFileObj = ref<File>();
const resultFileObj = ref<File>();

const itemDialog = reactive<DialogOption & { row?: ReportVO; item?: ReportItemVO }>({ visible: false, title: '录入单项结果' });
const itemFormRef = ref<ElFormInstance>();
const itemForm = reactive<ReportItemForm>(emptyItemForm());
const itemRules = {
  itemName: [{ required: true, message: '项目名称不能为空', trigger: 'blur' }],
  resultType: [{ required: true, message: '结果类型不能为空', trigger: 'change' }],
  result: [{ required: true, message: '结果不能为空', trigger: 'blur' }],
  deptId: [{ required: true, message: '承检科室不能为空', trigger: 'change' }],
  itemStatus: [{ required: true, message: '项目状态不能为空', trigger: 'change' }]
};

const criticalDialog = reactive<DialogOption & { row?: ReportVO; item?: ReportItemVO }>({ visible: false, title: '危急值登记' });
const criticalForm = reactive<any>({ receiver: '', notifyWay: '电话+系统推送', reportTime: undefined, receiverConfirmTime: undefined, handleResult: undefined });

const detailDrawer = reactive({ visible: false, data: undefined as ReportDetailVO | undefined });
const detail = computed(() => detailDrawer.data);
const finalConclusions = computed(() => (detail.value?.conclusions || []).filter((c) => c.finalFlag === '1'));

const sendBackDialog = reactive<DialogOption>({ visible: false, title: '异常打回科室' });
const sendBackForm = reactive<any>({ deptId: undefined, reason: '' });

const archiveDialog = reactive<DialogOption>({ visible: false, title: '归档附属资料' });
const archiveForm = reactive<any>({ type: undefined, ossId: undefined, pageCount: undefined, remark: undefined });

function emptyItemForm(): ReportItemForm {
  return {
    checkitemId: undefined,
    itemName: '',
    resultType: '0',
    result: '',
    unit: '',
    deptId: undefined,
    itemStatus: '已检',
    discardReason: undefined,
    qualitative: undefined,
    abnormalFlag: undefined,
    majorPositive: '0',
    edgeFlag: '0',
    doctor: undefined
  };
}

const deptName = (deptId?: number) => deptOptions.value.find((d) => d.deptId === deptId)?.deptName || (deptId ?? '-');
const conclusionTerm = (dictId?: number) => conclusionDicts.value.find((c) => c.id === dictId)?.term || `#${dictId}`;
const isCriticalRegistered = (itemId?: number) => (detail.value?.criticals || []).some((c) => c.reportItemId === itemId);

const getList = async () => {
  loading.value = true;
  try {
    queryParams.beginDate = dateRange.value?.[0];
    queryParams.endDate = dateRange.value?.[1];
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
  dateRange.value = [];
  queryFormRef.value?.resetFields();
  handleQuery();
};

const openCreate = () => {
  Object.assign(createForm, { memberId: undefined, checkDate: undefined, source: '手工录入', examNo: undefined, doctor: undefined, remark: undefined });
  createDialog.visible = true;
};

const submitCreate = () => {
  createFormRef.value?.validate(async (valid) => {
    if (!valid) return;
    buttonLoading.value = true;
    try {
      const res = await addReport(createForm);
      proxy?.$modal.msgSuccess(`已建档：${res.data.reportNo}`);
      createDialog.visible = false;
      await getList();
    } finally {
      buttonLoading.value = false;
    }
  });
};

const openUpload = (row: ReportVO) => {
  uploadDialog.row = row;
  uploadFileObj.value = undefined;
  resultFileObj.value = undefined;
  uploadDialog.visible = true;
};

const onPickAttachment = (e: Event) => {
  uploadFileObj.value = (e.target as HTMLInputElement).files?.[0];
};
const onPickResultFile = (e: Event) => {
  resultFileObj.value = (e.target as HTMLInputElement).files?.[0];
};

const submitUpload = async () => {
  if (!uploadFileObj.value) {
    proxy?.$modal.msgWarning('请选择附件文件');
    return;
  }
  buttonLoading.value = true;
  try {
    const res = await uploadReportFile(uploadDialog.row!.id!, uploadFileObj.value, resultFileObj.value);
    proxy?.$modal.msgSuccess('上传成功');
    const parse = res.data?.parse;
    if (parse) {
      await proxy?.$modal.alert(`解析完成：共 ${parse.total} 行，成功 ${parse.success} 行，未匹配 ${parse.unmatched} 行${parse.errors?.length ? '，异常 ' + parse.errors.length + ' 行' : ''}`);
    }
    uploadDialog.visible = false;
    await refresh();
  } finally {
    buttonLoading.value = false;
  }
};

const openItemDialog = async (row: ReportVO, item?: ReportItemVO) => {
  itemDialog.row = row;
  itemDialog.item = item;
  Object.assign(itemForm, emptyItemForm());
  if (item) {
    Object.assign(itemForm, {
      checkitemId: item.checkitemId,
      itemName: item.itemName,
      resultType: item.resultType,
      result: item.result,
      unit: item.unit,
      deptId: item.deptId,
      itemStatus: item.itemStatus,
      discardReason: item.discardReason,
      qualitative: item.qualitative,
      abnormalFlag: item.abnormalFlag,
      majorPositive: item.majorPositive,
      edgeFlag: item.edgeFlag,
      mergeGroupId: item.mergeGroupId,
      doctor: item.doctor,
      checkDevice: item.checkDevice,
      sortNo: item.sortNo,
      remark: item.remark
    });
  }
  itemDialog.visible = true;
};

const submitItem = () => {
  itemFormRef.value?.validate(async (valid) => {
    if (!valid) return;
    if (itemForm.itemStatus === '弃检' && !itemForm.discardReason) {
      proxy?.$modal.msgWarning('弃检必须填写原因');
      return;
    }
    buttonLoading.value = true;
    try {
      const res = await saveReportItem(itemDialog.row!.id!, itemForm);
      const tip = res.data?.tip;
      proxy?.$modal.msgSuccess(tip ? `已保存：${tip}` : '已保存');
      itemDialog.visible = false;
      await refresh();
    } finally {
      buttonLoading.value = false;
    }
  });
};

const openCritical = (row: ReportVO, item: ReportItemVO) => {
  criticalDialog.row = row;
  criticalDialog.item = item;
  Object.assign(criticalForm, {
    receiver: '',
    notifyWay: '电话+系统推送',
    reportTime: proxy?.parseTime(new Date()),
    receiverConfirmTime: undefined,
    handleResult: undefined
  });
  criticalDialog.visible = true;
};

const submitCritical = async () => {
  if (!criticalForm.receiver) {
    proxy?.$modal.msgWarning('接收医生必填');
    return;
  }
  buttonLoading.value = true;
  try {
    await registerCritical(criticalDialog.row!.id!, { reportItemId: criticalDialog.item!.id, ...criticalForm });
    proxy?.$modal.msgSuccess('危急值已登记');
    criticalDialog.visible = false;
    await refresh();
  } finally {
    buttonLoading.value = false;
  }
};

const openDetail = async (row: ReportVO) => {
  const res = await getReport(row.id!);
  detailDrawer.data = res.data;
  detailDrawer.visible = true;
  await loadConclusionDicts();
};

const refresh = async () => {
  await getList();
  if (detailDrawer.visible && detail.value?.report?.id) {
    const res = await getReport(detail.value.report.id);
    detailDrawer.data = res.data;
  }
};

const loadConclusionDicts = async () => {
  if (conclusionDicts.value.length) return;
  const res = await conclusionOptions();
  conclusionDicts.value = res.data || [];
};

const handleRevoke = async () => {
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
  await refresh();
};

const handleFinalCheck = async () => {
  await proxy?.$modal.confirm('确认终审签发？签发后不可打回。');
  await finalCheckReport(detail.value!.report!.id!);
  proxy?.$modal.msgSuccess('终审签发完成');
  await refresh();
};

const handlePrint = async () => {
  await printReport(detail.value!.report!.id!);
  proxy?.$modal.msgSuccess('已标记打印');
  await refresh();
};

const handleIssue = async () => {
  await issueReport(detail.value!.report!.id!);
  proxy?.$modal.msgSuccess('已发放');
  await refresh();
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
    await refresh();
  } finally {
    buttonLoading.value = false;
  }
};

const openArchive = () => {
  Object.assign(archiveForm, { type: undefined, ossId: undefined, pageCount: undefined, remark: undefined });
  archiveDialog.visible = true;
};

const submitArchive = async () => {
  if (!archiveForm.type) {
    proxy?.$modal.msgWarning('归档类型必填');
    return;
  }
  buttonLoading.value = true;
  try {
    await addReportArchive(detail.value!.report!.id!, {
      type: archiveForm.type,
      ossId: archiveForm.ossId ? Number(archiveForm.ossId) : undefined,
      pageCount: archiveForm.pageCount,
      remark: archiveForm.remark
    });
    proxy?.$modal.msgSuccess('归档完成');
    archiveDialog.visible = false;
    await refresh();
  } finally {
    buttonLoading.value = false;
  }
};

onMounted(async () => {
  const res = await listDept();
  deptOptions.value = res.data || [];
  await getList();
});
</script>

<style scoped>
.mr4 {
  margin-right: 4px;
}
.ml4 {
  margin-left: 4px;
}
.mb4 {
  margin-bottom: 4px;
}
.ref-missing {
  color: var(--el-color-warning);
}
.tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.text-muted {
  color: var(--el-text-color-secondary);
}
:deep(.el-descriptions__label) {
  white-space: nowrap;
}
</style>
