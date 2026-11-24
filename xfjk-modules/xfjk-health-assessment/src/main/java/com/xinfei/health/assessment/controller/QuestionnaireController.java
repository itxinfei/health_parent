package com.xinfei.health.assessment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.xinfei.common.core.domain.R;
import com.xinfei.common.idempotent.annotation.RepeatSubmit;
import com.xinfei.common.log.annotation.Log;
import com.xinfei.common.log.enums.BusinessType;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.common.web.core.BaseController;
import com.xinfei.health.assessment.domain.Questionnaire;
import com.xinfei.health.assessment.domain.bo.QuestionnaireBo;
import com.xinfei.health.assessment.domain.vo.QuestionImportVo;
import com.xinfei.health.assessment.domain.vo.QuestionVo;
import com.xinfei.health.assessment.domain.vo.QuestionnaireVo;
import com.xinfei.health.assessment.engine.PublishValidator.PublishResult;
import com.xinfei.health.assessment.service.IQuestionService;
import com.xinfei.health.assessment.service.IQuestionnaireService;
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
 * 问卷/量表定义管理（09 §6.1、§5.2）
 *
 * @author xinfei
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/health/questionnaires")
@Tag(name = "管理端-问卷/量表定义")
public class QuestionnaireController extends BaseController {

    private final IQuestionnaireService questionnaireService;
    private final IQuestionService questionService;

    /**
     * 分页查询（name/code/category/scaleType/status）
     */
    @Operation(summary = "分页查询问卷")
    @SaCheckPermission("health:questionnaire:list")
    @GetMapping
    public TableDataInfo<QuestionnaireVo> list(QuestionnaireBo bo, PageQuery pageQuery) {
        return questionnaireService.queryPageList(bo, pageQuery);
    }

    /**
     * 详情（含题目数/因子数/规则数统计）
     */
    @Operation(summary = "查询问卷详情")
    @SaCheckPermission("health:questionnaire:query")
    @GetMapping("/{id}")
    public R<QuestionnaireVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(questionnaireService.queryById(id));
    }

    /**
     * 新增（草稿，version=1）
     */
    @Operation(summary = "新增问卷")
    @SaCheckPermission("health:questionnaire:add")
    @Log(title = "问卷管理", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<Long> add(@Validated @RequestBody Questionnaire entity) {
        return R.ok(questionnaireService.insert(entity));
    }

    /**
     * 编辑元信息（不含题目）
     */
    @Operation(summary = "编辑问卷元信息")
    @SaCheckPermission("health:questionnaire:edit")
    @Log(title = "问卷管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}")
    public R<Void> edit(@NotNull(message = "主键不能为空") @PathVariable Long id,
                        @Validated @RequestBody Questionnaire entity) {
        entity.setId(id);
        return toAjax(questionnaireService.update(entity));
    }

    /**
     * 删除（存在评估记录返回 7008）
     */
    @Operation(summary = "删除问卷")
    @SaCheckPermission("health:questionnaire:remove")
    @Log(title = "问卷管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return toAjax(questionnaireService.deleteById(id));
    }

    /**
     * 复制为新版本草稿（version+1，含题目/选项/因子/规则/配置）
     */
    @Operation(summary = "复制为新版本草稿")
    @SaCheckPermission("health:questionnaire:add")
    @Log(title = "问卷管理", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping("/{id}/copy-version")
    public R<Long> copyVersion(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(questionnaireService.copyVersion(id));
    }

    /**
     * 发布：六项校验通过才发布，否则 7004/7005/7006 + 逐项错误清单（09 §5.2）
     */
    @Operation(summary = "发布问卷")
    @SaCheckPermission("health:questionnaire:publish")
    @Log(title = "问卷管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}/publish")
    public R<PublishResult> publish(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        PublishResult result = questionnaireService.publish(id);
        if (!result.isSuccess()) {
            R<PublishResult> r = R.fail(result.getErrorCode(), "发布校验未通过");
            r.setData(result);
            return r;
        }
        return R.ok(result);
    }

    /**
     * 停用
     */
    @Operation(summary = "停用问卷")
    @SaCheckPermission("health:questionnaire:publish")
    @Log(title = "问卷管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}/disable")
    public R<Void> disable(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return toAjax(questionnaireService.disable(id));
    }

    /**
     * 题库导入（JSON 行数组，逐行标错，形态同 20 §6）
     */
    @Operation(summary = "题库导入")
    @SaCheckPermission("health:question:add")
    @Log(title = "题目管理", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping("/{id}/questions/import")
    public R<QuestionImportVo> importQuestions(@NotNull(message = "主键不能为空") @PathVariable Long id,
                                               @Validated @RequestBody List<QuestionVo> rows) {
        return R.ok(questionService.importQuestions(id, rows));
    }

}
