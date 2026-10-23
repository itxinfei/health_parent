package com.xinfei.health.appointment.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.xinfei.common.core.domain.R;
import com.xinfei.common.idempotent.annotation.RepeatSubmit;
import com.xinfei.common.log.annotation.Log;
import com.xinfei.common.log.enums.BusinessType;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.common.web.core.BaseController;
import com.xinfei.health.appointment.domain.bo.AppOrderBo;
import com.xinfei.health.appointment.domain.bo.ForbidCheckBo;
import com.xinfei.health.appointment.domain.vo.AppOrderVo;
import com.xinfei.health.appointment.domain.vo.ForbidCheckVo;
import com.xinfei.health.appointment.service.IAppOrderService;
import com.xinfei.health.appointment.service.IForbidService;
import com.xinfei.health.appointment.service.impl.MemberLoginServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

/**
 * C 端预约下单（13 §4.2；公开接口，游客可下单，下单即自动注册会员）
 *
 * <p>按 08 §4.6：C 端入口只暴露在 /app/**，不在 /health/** 出现。
 * 需登录的订单查询/改期/取消见 13 §4.2 登录态部分，待会员鉴权链路落地后接入。
 *
 * @author xinfei
 */
@Validated
@SaIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/orders")
@Tag(name = "C端-预约下单")
public class AppOrderController extends BaseController {

    private final IAppOrderService appOrderService;
    private final IForbidService forbidService;

    /**
     * 预检日期（UI 步骤 2，省验证码短信；不替代下单事务内任何校验）
     */
    @Operation(summary = "预检预约日期名额")
    @PostMapping("/check-date")
    public R<Boolean> checkDate(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate orderDate) {
        return R.ok(appOrderService.checkDate(orderDate));
    }

    /**
     * 预检验证码（只比对不删除；真正消费在下单事务内）
     */
    @Operation(summary = "预检短信验证码")
    @PostMapping("/check-code")
    public R<Boolean> checkCode(@NotBlank(message = "手机号不能为空") String phone,
                                @NotBlank(message = "短信验证码不能为空") String smsCode) {
        return R.ok(appOrderService.checkCode(phone, smsCode));
    }

    /**
     * 下单（事务内按 08 §5.3 步骤 1~9 全量执行；禁忌校验待规则表落地后叠加）
     */
    @Operation(summary = "提交预约订单")
    @Log(title = "C端预约下单", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<AppOrderVo> submit(@Valid @RequestBody AppOrderBo bo) {
        return R.ok(appOrderService.submitOrder(bo));
    }

    /**
     * 禁忌试算预览（13 §4.2：进入下单页自动取一次；不是校验替代，下单事务内仍执行）
     */
    @Operation(summary = "禁忌试算预览")
    @PostMapping("/forbid-preview")
    public R<ForbidCheckVo> forbidPreview(@Valid @RequestBody ForbidCheckBo bo) {
        return R.ok(forbidService.trialCalc(bo));
    }

    // ---------- 以下为登录态接口（13 R3：仅本人） ----------

    /**
     * 我的预约列表（tab：待检查/已完成/已取消/全部）
     */
    @Operation(summary = "我的预约列表")
    @SaCheckLogin
    @GetMapping
    public TableDataInfo<AppOrderVo> myOrders(@RequestParam(required = false) String tab, PageQuery pageQuery) {
        return appOrderService.myOrders(MemberLoginServiceImpl.currentMemberId(), tab, pageQuery);
    }

    /**
     * 我的订单详情（仅本人）
     */
    @Operation(summary = "我的订单详情")
    @SaCheckLogin
    @GetMapping("/{id}")
    public R<AppOrderVo> myOrder(@NotNull(message = "主键不能为空") @PathVariable Integer id) {
        return R.ok(appOrderService.myOrder(MemberLoginServiceImpl.currentMemberId(), id));
    }

    /**
     * 我的订单改期（仅已预约；原子双更新）
     */
    @Operation(summary = "我的订单改期")
    @Log(title = "C端改期", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @SaCheckLogin
    @PutMapping("/{id}/reschedule")
    public R<Void> reschedule(@NotNull(message = "主键不能为空") @PathVariable Integer id,
                              @Valid @RequestBody AppRescheduleBody body) {
        appOrderService.rescheduleMine(MemberLoginServiceImpl.currentMemberId(), id, body.getOrderDate());
        return R.ok();
    }

    /**
     * 我的订单取消（reason 必填；名额回退）
     */
    @Operation(summary = "我的订单取消")
    @Log(title = "C端取消预约", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @SaCheckLogin
    @PutMapping("/{id}/cancel")
    public R<Void> cancel(@NotNull(message = "主键不能为空") @PathVariable Integer id,
                          @Valid @RequestBody AppCancelBody body) {
        appOrderService.cancelMine(MemberLoginServiceImpl.currentMemberId(), id, body.getReason());
        return R.ok();
    }

    /**
     * 改期入参（13 §4.2：{orderDate}）
     */
    @Data
    public static class AppRescheduleBody {

        @NotNull(message = "目标日期不能为空")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate orderDate;

    }

    /**
     * 取消入参（13 §4.2：reason 必填 ≤255）
     */
    @Data
    public static class AppCancelBody {

        @NotBlank(message = "取消原因不能为空")
        @jakarta.validation.constraints.Size(max = 255, message = "取消原因不能超过 255 字符")
        private String reason;

    }

}
