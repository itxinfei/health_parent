package com.xinfei.health.appointment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.xinfei.common.core.domain.R;
import com.xinfei.common.idempotent.annotation.RepeatSubmit;
import com.xinfei.common.log.annotation.Log;
import com.xinfei.common.log.enums.BusinessType;
import com.xinfei.common.web.core.BaseController;
import com.xinfei.health.appointment.domain.bo.ForbidBo;
import com.xinfei.health.appointment.domain.bo.ForbidCheckBo;
import com.xinfei.health.appointment.domain.vo.ForbidCheckVo;
import com.xinfei.health.appointment.service.IForbidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 禁忌规则（08 §4.5：/health/forbids CRUD + /health/forbid-check 试算）
 *
 * @author xinfei
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/health")
@Tag(name = "管理端-禁忌规则")
public class ForbidController extends BaseController {

    private final IForbidService forbidService;

    /**
     * 规则列表（运营自检用，按优先级排序）
     */
    @Operation(summary = "禁忌规则列表")
    @SaCheckPermission("health:forbid:list")
    @GetMapping("/forbids")
    public R<List<com.xinfei.health.appointment.domain.HealthCheckitemForbid>> list() {
        return R.ok(forbidService.queryList());
    }

    /**
     * 新增规则（键白名单 / ruleCode 唯一，违反 5013）
     */
    @Operation(summary = "新增禁忌规则")
    @SaCheckPermission("health:forbid:add")
    @Log(title = "禁忌规则", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping("/forbids")
    public R<Long> add(@Valid @RequestBody ForbidBo bo) {
        return R.ok(forbidService.insertByBo(bo));
    }

    /**
     * 编辑规则
     */
    @Operation(summary = "编辑禁忌规则")
    @SaCheckPermission("health:forbid:edit")
    @Log(title = "禁忌规则", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/forbids")
    public R<Void> edit(@Valid @RequestBody ForbidBo bo) {
        return toAjax(forbidService.updateByBo(bo));
    }

    /**
     * 删除规则
     */
    @Operation(summary = "删除禁忌规则")
    @SaCheckPermission("health:forbid:remove")
    @Log(title = "禁忌规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/forbids/{id}")
    public R<Void> remove(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return toAjax(forbidService.deleteById(id));
    }

    /**
     * 禁忌试算（08 §4.5：/health/forbid-check，套餐 + 人员四维 → 命中规则与处置档）
     */
    @Operation(summary = "禁忌试算")
    @SaCheckPermission("health:order:query")
    @PostMapping("/forbid-check")
    public R<ForbidCheckVo> check(@Valid @RequestBody ForbidCheckBo bo) {
        return R.ok(forbidService.trialCalc(bo));
    }

}
