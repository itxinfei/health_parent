package com.xinfei.health.assessment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.xinfei.common.core.domain.R;
import com.xinfei.common.idempotent.annotation.RepeatSubmit;
import com.xinfei.common.log.annotation.Log;
import com.xinfei.common.log.enums.BusinessType;
import com.xinfei.common.web.core.BaseController;
import com.xinfei.health.assessment.domain.ScaleConfig;
import com.xinfei.health.assessment.domain.ScaleFactor;
import com.xinfei.health.assessment.domain.ScaleRule;
import com.xinfei.health.assessment.service.IScaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 量表因子/判定规则/阈值配置（09 §2.4~§2.6）
 *
 * @author xinfei
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/health")
@Tag(name = "管理端-量表配置")
public class ScaleController extends BaseController {

    private final IScaleService scaleService;

    // ---------- 因子（09 §2.4） ----------

    /**
     * 问卷下的因子列表
     */
    @Operation(summary = "查询因子列表")
    @SaCheckPermission("health:scale:list")
    @GetMapping("/scalefactors")
    public R<List<ScaleFactor>> listFactors(@NotNull(message = "问卷主键不能为空") @RequestParam Long questionnaireId) {
        return R.ok(scaleService.listFactors(questionnaireId));
    }

    /**
     * 新增因子（问卷已发布 7007）
     */
    @Operation(summary = "新增因子")
    @SaCheckPermission("health:scale:add")
    @Log(title = "量表配置", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping("/scalefactors")
    public R<Long> addFactor(@Validated @RequestBody ScaleFactor entity) {
        return R.ok(scaleService.insertFactor(entity));
    }

    /**
     * 编辑因子（已发布 7007）
     */
    @Operation(summary = "编辑因子")
    @SaCheckPermission("health:scale:edit")
    @Log(title = "量表配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/scalefactors/{id}")
    public R<Void> editFactor(@NotNull(message = "主键不能为空") @PathVariable Long id,
                              @Validated @RequestBody ScaleFactor entity) {
        entity.setId(id);
        return toAjax(scaleService.updateFactor(entity));
    }

    /**
     * 删除因子（已发布 7007）
     */
    @Operation(summary = "删除因子")
    @SaCheckPermission("health:scale:remove")
    @Log(title = "量表配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/scalefactors/{id}")
    public R<Void> removeFactor(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return toAjax(scaleService.deleteFactor(id));
    }

    // ---------- 判定规则（09 §2.5） ----------

    /**
     * 问卷下的规则列表
     */
    @Operation(summary = "查询判定规则列表")
    @SaCheckPermission("health:scale:list")
    @GetMapping("/scalerules")
    public R<List<ScaleRule>> listRules(@NotNull(message = "问卷主键不能为空") @RequestParam Long questionnaireId) {
        return R.ok(scaleService.listRules(questionnaireId));
    }

    /**
     * 新增规则（区间 min<max、level_code 属值域，违规 7004；已发布 7007）
     */
    @Operation(summary = "新增判定规则")
    @SaCheckPermission("health:scale:add")
    @Log(title = "量表配置", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping("/scalerules")
    public R<Long> addRule(@Validated @RequestBody ScaleRule entity) {
        return R.ok(scaleService.insertRule(entity));
    }

    /**
     * 编辑规则
     */
    @Operation(summary = "编辑判定规则")
    @SaCheckPermission("health:scale:edit")
    @Log(title = "量表配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/scalerules/{id}")
    public R<Void> editRule(@NotNull(message = "主键不能为空") @PathVariable Long id,
                            @Validated @RequestBody ScaleRule entity) {
        entity.setId(id);
        return toAjax(scaleService.updateRule(entity));
    }

    /**
     * 删除规则
     */
    @Operation(summary = "删除判定规则")
    @SaCheckPermission("health:scale:remove")
    @Log(title = "量表配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/scalerules/{id}")
    public R<Void> removeRule(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return toAjax(scaleService.deleteRule(id));
    }

    // ---------- 阈值配置（09 §2.6，A13） ----------

    /**
     * 问卷下的阈值配置列表
     */
    @Operation(summary = "查询阈值配置")
    @SaCheckPermission("health:scale:edit")
    @GetMapping("/questionnaires/{id}/scale-config")
    public R<List<ScaleConfig>> listConfigs(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(scaleService.listConfigs(id));
    }

    /**
     * 全量保存配置（upsert；已发布 7007）
     */
    @Operation(summary = "保存阈值配置")
    @SaCheckPermission("health:scale:edit")
    @Log(title = "量表配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/questionnaires/{id}/scale-config")
    public R<Void> saveConfigs(@NotNull(message = "主键不能为空") @PathVariable Long id,
                               @Validated @RequestBody List<ScaleConfig> configs) {
        return toAjax(scaleService.saveConfigs(id, configs));
    }

}
