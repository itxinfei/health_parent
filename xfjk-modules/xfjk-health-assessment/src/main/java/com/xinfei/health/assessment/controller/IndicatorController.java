package com.xinfei.health.assessment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.xinfei.common.core.domain.R;
import com.xinfei.common.idempotent.annotation.RepeatSubmit;
import com.xinfei.common.log.annotation.Log;
import com.xinfei.common.log.enums.BusinessType;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.common.web.core.BaseController;
import com.xinfei.health.assessment.domain.Indicator;
import com.xinfei.health.assessment.service.IIndicatorService;
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
 * 评估指标库（09 §2.11）
 *
 * @author xinfei
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/health/indicators")
@Tag(name = "管理端-评估指标库")
public class IndicatorController extends BaseController {

    private final IIndicatorService indicatorService;

    @Operation(summary = "分页查询指标")
    @SaCheckPermission("health:indicator:list")
    @GetMapping
    public TableDataInfo<Indicator> list(Indicator bo, PageQuery pageQuery) {
        return indicatorService.queryPageList(bo, pageQuery);
    }

    @Operation(summary = "查询全部指标")
    @SaCheckPermission("health:indicator:query")
    @GetMapping("/all")
    public R<List<Indicator>> all(Indicator bo) {
        return R.ok(indicatorService.queryList(bo));
    }

    @Operation(summary = "查询指标详情")
    @SaCheckPermission("health:indicator:query")
    @GetMapping("/{id}")
    public R<Indicator> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(indicatorService.queryById(id));
    }

    @Operation(summary = "新增指标")
    @SaCheckPermission("health:indicator:add")
    @Log(title = "评估指标库", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Long> add(@Validated @RequestBody Indicator entity) {
        return R.ok(indicatorService.insert(entity));
    }

    @Operation(summary = "编辑指标")
    @SaCheckPermission("health:indicator:edit")
    @Log(title = "评估指标库", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}")
    public R<Void> edit(@NotNull(message = "主键不能为空") @PathVariable Long id,
                        @Validated @RequestBody Indicator entity) {
        entity.setId(id);
        return toAjax(indicatorService.update(entity));
    }

    @Operation(summary = "删除指标")
    @SaCheckPermission("health:indicator:remove")
    @Log(title = "评估指标库", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return toAjax(indicatorService.deleteById(id));
    }

}
