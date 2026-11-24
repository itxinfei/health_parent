package com.xinfei.health.assessment.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.xinfei.common.core.domain.R;
import com.xinfei.common.idempotent.annotation.RepeatSubmit;
import com.xinfei.common.log.annotation.Log;
import com.xinfei.common.log.enums.BusinessType;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.common.web.core.BaseController;
import com.xinfei.health.appointment.service.impl.MemberLoginServiceImpl;
import com.xinfei.health.assessment.domain.bo.AssessmentSubmitBo;
import com.xinfei.health.assessment.domain.vo.AssessmentRecordVo;
import com.xinfei.health.assessment.domain.vo.QuestionnaireVo;
import com.xinfei.health.assessment.service.IAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * C 端健康评估（09 §6.2；按 08 §4.6：C 端入口只暴露在 /app/**，不在 /health/** 出现）
 *
 * <p>问卷列表/题目为公开接口；提交与本人记录需登录（会员鉴权链路见 13 §4.1）。
 *
 * @author xinfei
 */
@Validated
@SaIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("/app/assessments")
@Tag(name = "C端-健康评估")
public class AppAssessmentController extends BaseController {

    private final IAssessmentService assessmentService;

    /**
     * 已发布且 is_public=1 的问卷列表（09 §6.2）
     */
    @Operation(summary = "问卷列表")
    @GetMapping("/questionnaires")
    public R<List<QuestionnaireVo>> questionnaires() {
        return R.ok(assessmentService.appQuestionnaireList());
    }

    /**
     * 问卷题目与选项（不返回 scale_rule、norm_*、factor_code 等算分内部字段）
     */
    @Operation(summary = "问卷题目与选项")
    @GetMapping("/questionnaires/{id}")
    public R<QuestionnaireVo> questionnaireDetail(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(assessmentService.appQuestionnaireDetail(id));
    }

    /**
     * 提交作答 → §5.1 全流程（会员取自会话）
     */
    @Operation(summary = "提交作答并算分")
    @SaCheckLogin
    @Log(title = "C端健康评估", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<AssessmentRecordVo> submit(@Valid @RequestBody AssessmentSubmitBo bo) {
        return R.ok(assessmentService.submit(bo, MemberLoginServiceImpl.currentMemberId().longValue(), "app"));
    }

    /**
     * 本人记录分页
     */
    @Operation(summary = "本人评估记录")
    @SaCheckLogin
    @GetMapping
    public TableDataInfo<AssessmentRecordVo> myRecords(PageQuery pageQuery) {
        return assessmentService.appRecords(MemberLoginServiceImpl.currentMemberId().longValue(), pageQuery);
    }

    /**
     * 本人结果详情；非本人 7008
     */
    @Operation(summary = "本人评估结果详情")
    @SaCheckLogin
    @GetMapping("/{id}")
    public R<AssessmentRecordVo> myDetail(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(assessmentService.appDetail(MemberLoginServiceImpl.currentMemberId().longValue(), id));
    }

}
