package com.xinfei.health.assessment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.xinfei.common.core.domain.R;
import com.xinfei.common.idempotent.annotation.RepeatSubmit;
import com.xinfei.common.log.annotation.Log;
import com.xinfei.common.log.enums.BusinessType;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.common.web.core.BaseController;
import com.xinfei.health.assessment.domain.bo.AssessmentQueryBo;
import com.xinfei.health.assessment.domain.bo.AssessmentSubmitBo;
import com.xinfei.health.assessment.domain.vo.AssessmentRecordVo;
import com.xinfei.health.assessment.service.IAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 评估记录管理（09 §6.1）
 *
 * <p>结构化评估报告（{id}/report）留 B11，本批不开放（19 §3.2 已登记）。
 *
 * @author xinfei
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/health/assessments")
@Tag(name = "管理端-评估记录")
public class AssessmentController extends BaseController {

    private final IAssessmentService assessmentService;

    /**
     * 记录分页（会员/问卷/等级/时间/来源）
     */
    @Operation(summary = "分页查询评估记录")
    @SaCheckPermission("health:assessment:list")
    @GetMapping
    public TableDataInfo<AssessmentRecordVo> list(AssessmentQueryBo bo, PageQuery pageQuery) {
        return assessmentService.queryPageList(bo, pageQuery);
    }

    /**
     * 记录详情（含因子分、建议、人群）
     */
    @Operation(summary = "查询评估记录详情")
    @SaCheckPermission("health:assessment:query")
    @GetMapping("/{id}")
    public R<AssessmentRecordVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(assessmentService.queryById(id));
    }

    /**
     * 评估统计：评估人数、阳性率、各等级分布（09 §6.1）
     */
    @Operation(summary = "评估统计")
    @SaCheckPermission("health:assessment:list")
    @GetMapping("/statistics")
    public R<Map<String, Object>> statistics(AssessmentQueryBo bo) {
        return R.ok(assessmentService.statistics(bo));
    }

    /**
     * 后台代填提交（source=admin；会员 id 取入参，缺失 6001）
     */
    @Operation(summary = "后台代填提交")
    @SaCheckPermission("health:assessment:add")
    @Log(title = "评估记录", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<AssessmentRecordVo> add(@Validated @RequestBody AssessmentSubmitBo bo) {
        return R.ok(assessmentService.submit(bo, bo.getMemberId(), "admin"));
    }

    /**
     * 逻辑删除（del_flag）
     */
    @Operation(summary = "删除评估记录")
    @SaCheckPermission("health:assessment:remove")
    @Log(title = "评估记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public R<Void> remove(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return toAjax(assessmentService.deleteById(id));
    }

}
