<template>
  <div class="p-2">
    <transition :enter-active-class="proxy?.animate.searchAnimate.enter" :leave-active-class="proxy?.animate.searchAnimate.leave">
      <div v-show="showSearch" class="mb8">
        <el-card shadow="hover">
          <el-form ref="queryFormRef" :model="queryParams" :inline="true">
            <el-form-item label="预约日期" prop="dateRange">
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
            <el-form-item label="原状态" prop="orderStatus">
              <el-select v-model="queryParams.orderStatus" placeholder="全部" clearable style="width: 130px">
                <el-option v-for="dict in health_order_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="流程状态" prop="flowStatus">
              <el-select v-model="queryParams.flowStatus" placeholder="全部" clearable style="width: 140px">
                <el-option v-for="dict in health_flow_status" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="套餐" prop="setmealId">
              <el-select v-model="queryParams.setmealId" placeholder="全部" clearable filterable style="width: 160px">
                <el-option v-for="item in setmealOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="会员ID" prop="memberId">
              <el-input v-model="queryParams.memberId" placeholder="会员 id" clearable style="width: 110px" @keyup.enter="handleQuery" />
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
          <right-toolbar v-model:show-search="showSearch" @query-table="getList"></right-toolbar>
        </el-row>
      </template>

      <el-table v-loading="loading" border :data="orderList">
        <el-table-column label="订单号" prop="id" width="80" align="center" />
        <el-table-column label="会员ID" prop="memberId" width="80" align="center" />
        <el-table-column label="预约日期" prop="orderDate" width="110" align="center" />
        <el-table-column label="套餐" width="150" show-overflow-tooltip>
          <template #default="scope">{{ setmealName(scope.row.setmealId) }}</template>
        </el-table-column>
        <el-table-column label="类型" width="100" align="center">
          <template #default="scope">
            <dict-tag :options="health_order_type" :value="scope.row.orderType" />
          </template>
        </el-table-column>
        <el-table-column label="原状态" width="90" align="center">
          <template #default="scope">
            <dict-tag :options="health_order_status" :value="scope.row.orderStatus" />
          </template>
        </el-table-column>
        <el-table-column label="流程状态" width="110" align="center">
          <template #default="scope">
            <dict-tag :options="health_flow_status" :value="scope.row.ext?.flowStatus" />
          </template>
        </el-table-column>
        <el-table-column label="体检号" width="150" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.ext?.examNo || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="230" align="center" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-tooltip content="详情（含流水）" placement="top">
              <el-button v-hasPermi="['health:order:query']" link type="primary" icon="View" @click="handleDetail(scope.row)"></el-button>
            </el-tooltip>
            <el-tooltip content="到诊登记（生成体检号，08 §5.5）" placement="top">
              <el-button
                v-hasPermi="['health:order:edit']"
                v-if="scope.row.ext?.flowStatus === '已预约'"
                link
                type="primary"
                icon="CircleCheck"
                @click="handleRegister(scope.row)"
              >到诊</el-button>
            </el-tooltip>
            <el-tooltip content="改期（目标日+1 原日-1 原子双更新，08 §5.4）" placement="top">
              <el-button
                v-hasPermi="['health:order:edit']"
                v-if="scope.row.ext?.flowStatus === '已预约'"
                link
                type="primary"
                icon="Calendar"
                @click="openReschedule(scope.row)"
              >改期</el-button>
            </el-tooltip>
            <el-tooltip content="取消（须原因；回退当日名额，08 §5.6）" placement="top">
              <el-button
                v-hasPermi="['health:order:cancel']"
                v-if="canCancel(scope.row)"
                link
                type="primary"
                icon="CircleClose"
                @click="openCancel(scope.row)"
              >取消</el-button>
            </el-tooltip>
            <el-tooltip content="延期（须至日期，18 C6）" placement="top">
              <el-button
                v-hasPermi="['health:order:edit']"
                v-if="scope.row.ext?.flowStatus === '已到诊'"
                link
                type="primary"
                icon="Clock"
                @click="openPostpone(scope.row)"
              >延期</el-button>
            </el-tooltip>
            <el-tooltip content="延期恢复（回 已到诊）" placement="top">
              <el-button
                v-hasPermi="['health:order:edit']"
                v-if="scope.row.ext?.flowStatus === '延期'"
                link
                type="primary"
                icon="RefreshRight"
                @click="handleRestore(scope.row)"
              >恢复</el-button>
            </el-tooltip>
            <el-tooltip content="取消恢复（回 已预约，须当日名额有余量，08 §3.2）" placement="top">
              <el-button
                v-hasPermi="['health:order:edit']"
                v-if="scope.row.ext?.flowStatus === '已取消'"
                link
                type="primary"
                icon="RefreshLeft"
                @click="handleRestoreCancel(scope.row)"
              >恢复</el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 改期 -->
    <el-dialog v-model="rescheduleDialog.visible" title="订单改期" width="420px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="原日期">{{ rescheduleDialog.row?.orderDate }}</el-form-item>
        <el-form-item label="目标日期">
          <el-date-picker
            v-model="rescheduleTargetDate"
            type="date"
            value-format="YYYY-MM-DD"
            :disabled-date="(d: Date) => d.getTime() <= todayTime"
            placeholder="目标日须已设名额且有余量"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rescheduleDialog.visible = false">取 消</el-button>
        <el-button :loading="buttonLoading" type="primary" @click="submitReschedule">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 取消 -->
    <el-dialog v-model="cancelDialog.visible" title="取消订单" width="420px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="取消原因" required>
          <el-input v-model="cancelReason" type="textarea" :rows="3" maxlength="255" show-word-limit placeholder="必填（08 §5.6）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialog.visible = false">取 消</el-button>
        <el-button :loading="buttonLoading" type="danger" @click="submitCancel">确认取消订单</el-button>
      </template>
    </el-dialog>

    <!-- 延期 -->
    <el-dialog v-model="postponeDialog.visible" title="订单延期" width="420px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="延期至" required>
          <el-date-picker
            v-model="postponeUntil"
            type="date"
            value-format="YYYY-MM-DD"
            :disabled-date="(d: Date) => d.getTime() < todayTime"
            placeholder="不早于今天"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="postponeDialog.visible = false">取 消</el-button>
        <el-button :loading="buttonLoading" type="primary" @click="submitPostpone">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailDrawer.visible" title="订单详情" size="480px">
      <el-descriptions v-if="detail" :column="1" border class="mb8">
        <el-descriptions-item label="订单号">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="会员ID">{{ detail.memberId }}</el-descriptions-item>
        <el-descriptions-item label="预约日期">{{ detail.orderDate }}</el-descriptions-item>
        <el-descriptions-item label="套餐">{{ setmealName(detail.setmealId) }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detail.orderType }}</el-descriptions-item>
        <el-descriptions-item label="原状态">{{ detail.orderStatus }}</el-descriptions-item>
        <el-descriptions-item label="流程状态">{{ detail.ext?.flowStatus }}</el-descriptions-item>
        <el-descriptions-item label="体检号">{{ detail.ext?.examNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="取消原因">{{ detail.ext?.cancelReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="延期次数">{{ detail.ext?.postponeCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="延期至">{{ detail.ext?.postponeUntil || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">流转流水</el-divider>
      <el-timeline>
        <el-timeline-item v-for="log in detail?.logs || []" :key="log.id" :timestamp="log.createTime" placement="top">
          <b>{{ log.action }}</b>
          <span v-if="log.fromStatus || log.toStatus"> {{ log.fromStatus }} → {{ log.toStatus }}</span>
          <div class="log-meta">{{ log.operator }}（{{ log.operatorRole }}）{{ log.remark ? '：' + log.remark : '' }}</div>
        </el-timeline-item>
      </el-timeline>
    </el-drawer>
  </div>
</template>

<script setup name="Order" lang="ts">
import { listOrder, getOrder, registerOrder, cancelOrder, rescheduleOrder, postponeOrder, restorePostpone, restoreCancelOrder } from '@/api/health/order';
import { OrderVO, OrderQuery } from '@/api/health/order/types';
import { listAllSetmeal } from '@/api/health/setmeal';
import { SetmealVO } from '@/api/health/setmeal/types';

const { proxy } = getCurrentInstance() as ComponentInternalInstance;
const { health_order_status, health_order_type, health_flow_status } = toRefs<any>(
  proxy?.useDict('health_order_status', 'health_order_type', 'health_flow_status')
);

const orderList = ref<OrderVO[]>([]);
const setmealOptions = ref<SetmealVO[]>([]);
const loading = ref(true);
const showSearch = ref(true);
const total = ref(0);
const buttonLoading = ref(false);
const dateRange = ref<string[]>([]);
const todayTime = new Date(new Date().toDateString()).getTime();

const queryFormRef = ref<ElFormInstance>();

const data = reactive<{ queryParams: OrderQuery }>({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    orderDateBegin: undefined,
    orderDateEnd: undefined,
    orderStatus: undefined,
    flowStatus: undefined,
    memberId: undefined,
    setmealId: undefined
  }
});
const { queryParams } = toRefs(data);

const rescheduleDialog = reactive<DialogOption & { row?: OrderVO }>({ visible: false, title: '', row: undefined });
const rescheduleTargetDate = ref('');
const cancelDialog = reactive<DialogOption & { row?: OrderVO }>({ visible: false, title: '', row: undefined });
const cancelReason = ref('');
const postponeDialog = reactive<DialogOption & { row?: OrderVO }>({ visible: false, title: '', row: undefined });
const postponeUntil = ref('');
const detailDrawer = reactive({ visible: false, value: undefined as OrderVO | undefined });
const detail = computed(() => detailDrawer.value);

const setmealName = (id?: number) => setmealOptions.value.find((s) => s.id === id)?.name || (id ?? '-');

/** 已到诊及之后须先撤销登记才可取消（后端 5006 拦截），前端只放开 §3.2 允许边 */
const canCancel = (row: OrderVO) => ['已预约'].includes(row.ext?.flowStatus ?? '');

const getList = async () => {
  loading.value = true;
  try {
    queryParams.value.orderDateBegin = dateRange.value?.[0];
    queryParams.value.orderDateEnd = dateRange.value?.[1];
    const res = await listOrder(queryParams.value);
    orderList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.value.pageNum = 1;
  getList();
};

const resetQuery = () => {
  dateRange.value = [];
  queryFormRef.value?.resetFields();
  handleQuery();
};

const handleRegister = async (row: OrderVO) => {
  await proxy?.$modal.confirm(`确认对订单 ${row.id}（${row.orderDate}）做到诊登记？将生成体检号并建报告草稿`);
  const res = await registerOrder(row.id);
  proxy?.$modal.msgSuccess(`到诊登记成功，体检号：${res.data.examNo}`);
  await getList();
};

const openReschedule = (row: OrderVO) => {
  rescheduleDialog.row = row;
  rescheduleTargetDate.value = '';
  rescheduleDialog.visible = true;
};

const submitReschedule = async () => {
  if (!rescheduleTargetDate.value) {
    proxy?.$modal.msgWarning('请选择目标日期');
    return;
  }
  buttonLoading.value = true;
  try {
    await rescheduleOrder(rescheduleDialog.row!.id, rescheduleTargetDate.value);
    proxy?.$modal.msgSuccess('改期成功');
    rescheduleDialog.visible = false;
    await getList();
  } finally {
    buttonLoading.value = false;
  }
};

const openCancel = (row: OrderVO) => {
  cancelDialog.row = row;
  cancelReason.value = '';
  cancelDialog.visible = true;
};

const submitCancel = async () => {
  if (!cancelReason.value.trim()) {
    proxy?.$modal.msgWarning('取消原因必填');
    return;
  }
  buttonLoading.value = true;
  try {
    await cancelOrder(cancelDialog.row!.id, cancelReason.value.trim());
    proxy?.$modal.msgSuccess('订单已取消，名额已回退');
    cancelDialog.visible = false;
    await getList();
  } finally {
    buttonLoading.value = false;
  }
};

const openPostpone = (row: OrderVO) => {
  postponeDialog.row = row;
  postponeUntil.value = '';
  postponeDialog.visible = true;
};

const submitPostpone = async () => {
  if (!postponeUntil.value) {
    proxy?.$modal.msgWarning('延期至日期必填');
    return;
  }
  buttonLoading.value = true;
  try {
    await postponeOrder(postponeDialog.row!.id, postponeUntil.value);
    proxy?.$modal.msgSuccess('已延期');
    postponeDialog.visible = false;
    await getList();
  } finally {
    buttonLoading.value = false;
  }
};

const handleRestore = async (row: OrderVO) => {
  await proxy?.$modal.confirm(`确认恢复订单 ${row.id}？流程状态回「已到诊」`);
  await restorePostpone(row.id);
  proxy?.$modal.msgSuccess('已恢复');
  await getList();
};

const handleRestoreCancel = async (row: OrderVO) => {
  await proxy?.$modal.confirm(`确认恢复订单 ${row.id}？流程状态回「已预约」并重新占用当日名额`);
  await restoreCancelOrder(row.id);
  proxy?.$modal.msgSuccess('已恢复为已预约，名额已重新占用');
  await getList();
};

const handleDetail = async (row: OrderVO) => {
  const res = await getOrder(row.id);
  detailDrawer.value = res.data;
  detailDrawer.visible = true;
};

onMounted(async () => {
  const res = await listAllSetmeal();
  setmealOptions.value = res.data || [];
  await getList();
});
</script>

<style scoped>
.log-meta {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
