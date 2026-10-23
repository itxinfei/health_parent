package com.xinfei.health.appointment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xinfei.common.core.domain.R;
import com.xinfei.common.idempotent.annotation.RepeatSubmit;
import com.xinfei.common.log.annotation.Log;
import com.xinfei.common.log.enums.BusinessType;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.common.web.core.BaseController;
import com.xinfei.health.appointment.domain.bo.OrderCancelBo;
import com.xinfei.health.appointment.domain.bo.OrderFlowBo;
import com.xinfei.health.appointment.domain.bo.OrderPostponeBo;
import com.xinfei.health.appointment.domain.bo.OrderQueryBo;
import com.xinfei.health.appointment.domain.bo.OrderRescheduleBo;
import com.xinfei.health.appointment.domain.vo.OrderVo;
import com.xinfei.health.appointment.service.IOrderService;

import java.util.Map;

/**
 * 预约订单与流转（08 §4.5）
 *
 * @author xinfei
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/health/orders")
@Tag(name = "管理端-预约订单")
public class OrderController extends BaseController {

    private final IOrderService orderService;

    /**
     * 分页查询订单
     */
    @Operation(summary = "分页查询订单")
    @SaCheckPermission("health:order:list")
    @GetMapping
    public TableDataInfo<OrderVo> list(OrderQueryBo bo, PageQuery pageQuery) {
        return orderService.queryPageList(bo, pageQuery);
    }

    /**
     * 订单详情（含 ext 与流水）
     */
    @Operation(summary = "订单详情")
    @SaCheckPermission("health:order:query")
    @GetMapping("/{id}")
    public R<OrderVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Integer id) {
        return R.ok(orderService.queryById(id));
    }

    /**
     * 到诊登记：生成体检号、flow_status/orderStatus 同步为已到诊
     */
    @Operation(summary = "到诊登记")
    @SaCheckPermission("health:order:edit")
    @Log(title = "预约订单", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}/register")
    public R<Map<String, String>> register(@NotNull(message = "主键不能为空") @PathVariable Integer id,
                                           @RequestBody(required = false) Map<String, String> body) {
        String examNo = orderService.register(id, body == null ? null : body.get("remark"));
        return R.ok(Map.of("orderId", String.valueOf(id), "examNo", examNo,
            "flowStatus", "已到诊", "orderStatus", "已到诊"));
    }

    /**
     * 取消订单（须 reason；回退当日名额）
     */
    @Operation(summary = "取消订单")
    @SaCheckPermission("health:order:cancel")
    @Log(title = "预约订单", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}/cancel")
    public R<Void> cancel(@NotNull(message = "主键不能为空") @PathVariable Integer id,
                          @Validated @RequestBody OrderCancelBo bo) {
        return toAjax(orderService.cancel(id, bo));
    }

    /**
     * 改期（目标日 +1 → 原日 -1 原子双更新）
     */
    @Operation(summary = "订单改期")
    @SaCheckPermission("health:order:edit")
    @Log(title = "预约订单", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}/reschedule")
    public R<Void> reschedule(@NotNull(message = "主键不能为空") @PathVariable Integer id,
                              @Validated @RequestBody OrderRescheduleBo bo) {
        return toAjax(orderService.reschedule(id, bo));
    }

    /**
     * 延期
     */
    @Operation(summary = "订单延期")
    @SaCheckPermission("health:order:edit")
    @Log(title = "预约订单", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}/postpone")
    public R<Void> postpone(@NotNull(message = "主键不能为空") @PathVariable Integer id,
                            @Validated @RequestBody OrderPostponeBo bo) {
        return toAjax(orderService.postpone(id, bo));
    }

    /**
     * 延期恢复
     */
    @Operation(summary = "延期恢复")
    @SaCheckPermission("health:order:edit")
    @Log(title = "预约订单", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}/postpone-restore")
    public R<Void> postponeRestore(@NotNull(message = "主键不能为空") @PathVariable Integer id) {
        return toAjax(orderService.postponeRestore(id));
    }

    /**
     * 状态迁移（仅允许 §3.2 合法边）
     */
    @Operation(summary = "订单状态迁移")
    @SaCheckPermission("health:order:edit")
    @Log(title = "预约订单", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}/flow")
    public R<Void> transitFlow(@NotNull(message = "主键不能为空") @PathVariable Integer id,
                               @Validated @RequestBody OrderFlowBo bo) {
        return toAjax(orderService.transitFlow(id, bo.getToStatus(), bo.getRemark()));
    }

}
