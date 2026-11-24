package com.xinfei.health.assessment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.xinfei.common.core.domain.R;
import com.xinfei.common.idempotent.annotation.RepeatSubmit;
import com.xinfei.common.log.annotation.Log;
import com.xinfei.common.log.enums.BusinessType;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.common.web.core.BaseController;
import com.xinfei.health.assessment.domain.Suggestion;
import com.xinfei.health.assessment.service.ISuggestionService;
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
 * 评估建议库（09 §2.10）
 *
 * @author xinfei
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/health/suggestions")
@Tag(name = "管理端-评估建议库")
public class SuggestionController extends BaseController {

    private final ISuggestionService suggestionService;

    @Operation(summary = "分页查询建议")
    @SaCheckPermission("health:suggestion:list")
    @GetMapping
    public TableDataInfo<Suggestion> list(Suggestion bo, PageQuery pageQuery) {
        return suggestionService.queryPageList(bo, pageQuery);
    }

    @Operation(summary = "查询全部建议")
    @SaCheckPermission("health:suggestion:query")
    @GetMapping("/all")
    public R<List<Suggestion>> all(Suggestion bo) {
        return R.ok(suggestionService.queryList(bo));
    }

    @Operation(summary = "查询建议详情")
    @SaCheckPermission("health:suggestion:query")
    @GetMapping("/{id}")
    public R<Suggestion> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(suggestionService.queryById(id));
    }

    @Operation(summary = "新增建议")
    @SaCheckPermission("health:suggestion:add")
    @Log(title = "评估建议库", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Long> add(@Validated @RequestBody Suggestion entity) {
        return R.ok(suggestionService.insert(entity));
    }

    @Operation(summary = "编辑建议")
    @SaCheckPermission("health:suggestion:edit")
    @Log(title = "评估建议库", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}")
    public R<Void> edit(@NotNull(message = "主键不能为空") @PathVariable Long id,
                        @Validated @RequestBody Suggestion entity) {
        entity.setId(id);
        return toAjax(suggestionService.update(entity));
    }

    @Operation(summary = "删除建议")
    @SaCheckPermission("health:suggestion:remove")
    @Log(title = "评估建议库", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return toAjax(suggestionService.deleteById(id));
    }

}
