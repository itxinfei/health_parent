package com.xinfei.health.assessment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.xinfei.common.core.domain.R;
import com.xinfei.common.idempotent.annotation.RepeatSubmit;
import com.xinfei.common.log.annotation.Log;
import com.xinfei.common.log.enums.BusinessType;
import com.xinfei.common.web.core.BaseController;
import com.xinfei.health.assessment.domain.vo.QuestionVo;
import com.xinfei.health.assessment.service.IQuestionService;
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
 * 题目管理（09 §2.2、§2.3；含选项一次提交）
 *
 * @author xinfei
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/health/questions")
@Tag(name = "管理端-题目管理")
public class QuestionController extends BaseController {

    private final IQuestionService questionService;

    /**
     * 查询问卷下全部题目（含选项，按 seq 排序）
     */
    @Operation(summary = "查询问卷下全部题目")
    @SaCheckPermission("health:question:query")
    @GetMapping
    public R<List<QuestionVo>> list(@NotNull(message = "问卷主键不能为空") @RequestParam Long questionnaireId) {
        return R.ok(questionService.queryByQuestionnaire(questionnaireId));
    }

    /**
     * 题目详情（含选项）
     */
    @Operation(summary = "查询题目详情")
    @SaCheckPermission("health:question:query")
    @GetMapping("/{id}")
    public R<QuestionVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(questionService.queryById(id));
    }

    /**
     * 新增题目（含选项一次提交；选项分值越界 7011；问卷已发布 7007）
     */
    @Operation(summary = "新增题目")
    @SaCheckPermission("health:question:add")
    @Log(title = "题目管理", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Long> add(@Validated @RequestBody QuestionVo vo) {
        return R.ok(questionService.insert(vo));
    }

    /**
     * 编辑题目（选项差异更新；已发布 7007）
     */
    @Operation(summary = "编辑题目")
    @SaCheckPermission("health:question:edit")
    @Log(title = "题目管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}")
    public R<Void> edit(@NotNull(message = "主键不能为空") @PathVariable Long id,
                        @Validated @RequestBody QuestionVo vo) {
        vo.setId(id);
        return toAjax(questionService.update(vo));
    }

    /**
     * 删除题目（级联删选项；已发布 7007）
     */
    @Operation(summary = "删除题目")
    @SaCheckPermission("health:question:remove")
    @Log(title = "题目管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return toAjax(questionService.deleteById(id));
    }

}
