package com.xinfei.health.assessment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.xinfei.common.core.domain.R;
import com.xinfei.common.idempotent.annotation.RepeatSubmit;
import com.xinfei.common.log.annotation.Log;
import com.xinfei.common.log.enums.BusinessType;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.common.web.core.BaseController;
import com.xinfei.health.assessment.domain.Crowd;
import com.xinfei.health.assessment.domain.CrowdRule;
import com.xinfei.health.assessment.service.ICrowdService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 人群分类与规则（09 §2.12）
 *
 * @author xinfei
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/health/crowds")
@Tag(name = "管理端-人群分类")
public class CrowdController extends BaseController {

    private final ICrowdService crowdService;

    @Operation(summary = "分页查询人群")
    @SaCheckPermission("health:crowd:list")
    @GetMapping
    public TableDataInfo<Crowd> list(Crowd bo, PageQuery pageQuery) {
        return crowdService.queryPageList(bo, pageQuery);
    }

    @Operation(summary = "查询全部人群")
    @SaCheckPermission("health:crowd:query")
    @GetMapping("/all")
    public R<List<Crowd>> all(Crowd bo) {
        return R.ok(crowdService.queryList(bo));
    }

    @Operation(summary = "查询人群详情")
    @SaCheckPermission("health:crowd:query")
    @GetMapping("/{id}")
    public R<Crowd> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(crowdService.queryById(id));
    }

    @Operation(summary = "新增人群")
    @SaCheckPermission("health:crowd:add")
    @Log(title = "人群分类", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Long> add(@Validated @RequestBody Crowd entity) {
        return R.ok(crowdService.insert(entity));
    }

    @Operation(summary = "编辑人群")
    @SaCheckPermission("health:crowd:edit")
    @Log(title = "人群分类", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}")
    public R<Void> edit(@NotNull(message = "主键不能为空") @PathVariable Long id,
                        @Validated @RequestBody Crowd entity) {
        entity.setId(id);
        return toAjax(crowdService.update(entity));
    }

    @Operation(summary = "删除人群（级联删规则与成员关系）")
    @SaCheckPermission("health:crowd:remove")
    @Log(title = "人群分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return toAjax(crowdService.deleteById(id));
    }

    // ---------- 人群规则（09 §2.12） ----------

    @Operation(summary = "查询人群规则列表")
    @SaCheckPermission("health:crowd:query")
    @GetMapping("/{id}/rules")
    public R<List<CrowdRule>> listRules(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(crowdService.listRules(id));
    }

    @Operation(summary = "新增人群规则")
    @SaCheckPermission("health:crowd:edit")
    @Log(title = "人群分类", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping("/{id}/rules")
    public R<Long> addRule(@NotNull(message = "主键不能为空") @PathVariable Long id,
                           @Validated @RequestBody CrowdRule entity) {
        entity.setCrowdId(id);
        return R.ok(crowdService.insertRule(entity));
    }

    @Operation(summary = "编辑人群规则")
    @SaCheckPermission("health:crowd:edit")
    @Log(title = "人群分类", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}/rules/{ruleId}")
    public R<Void> editRule(@NotNull(message = "主键不能为空") @PathVariable Long id,
                            @NotNull(message = "规则主键不能为空") @PathVariable Long ruleId,
                            @Validated @RequestBody CrowdRule entity) {
        entity.setId(ruleId);
        entity.setCrowdId(id);
        return toAjax(crowdService.updateRule(entity));
    }

    @Operation(summary = "删除人群规则")
    @SaCheckPermission("health:crowd:edit")
    @Log(title = "人群分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}/rules/{ruleId}")
    public R<Void> removeRule(@NotNull(message = "主键不能为空") @PathVariable Long id,
                              @NotNull(message = "规则主键不能为空") @PathVariable Long ruleId) {
        return toAjax(crowdService.deleteRule(ruleId));
    }

}
