package com.xinfei.health.appointment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
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
import com.xinfei.common.core.domain.R;
import com.xinfei.common.excel.utils.ExcelUtil;
import com.xinfei.common.idempotent.annotation.RepeatSubmit;
import com.xinfei.common.log.annotation.Log;
import com.xinfei.common.log.enums.BusinessType;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.common.web.core.BaseController;
import com.xinfei.health.appointment.domain.HealthMemberProfile;
import com.xinfei.health.appointment.domain.HealthMemberTag;
import com.xinfei.health.appointment.domain.bo.MemberHistoryBo;
import com.xinfei.health.appointment.domain.bo.MemberMissingBo;
import com.xinfei.health.appointment.domain.bo.MemberProfileBo;
import com.xinfei.health.appointment.domain.bo.MemberQueryBo;
import com.xinfei.health.appointment.domain.bo.MemberSaveBo;
import com.xinfei.health.appointment.domain.bo.MemberStatusBo;
import com.xinfei.health.appointment.domain.vo.MemberCompletenessVo;
import com.xinfei.health.appointment.domain.vo.MemberDetailVo;
import com.xinfei.health.appointment.domain.vo.MemberExportVo;
import com.xinfei.health.appointment.domain.vo.MemberVo;
import com.xinfei.health.appointment.service.IMemberService;

import java.util.List;

/**
 * 会员与档案（07 §3.1，B6 会员/档案侧）
 *
 * <p>不提供 DELETE /health/members/{id}（07 §3.1/§5.8：t_member 冻结无 del_flag，
 * 且被 t_order 物理外键引用；不可用一律走停用）。
 *
 * @author xinfei
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/health/members")
@Tag(name = "会员与档案管理")
public class MemberController extends BaseController {

    private final IMemberService memberService;

    @Operation(summary = "分页查询会员（默认只出正常状态，07 §5.8）")
    @SaCheckPermission("health:member:list")
    @GetMapping
    public TableDataInfo<MemberVo> list(MemberQueryBo bo, PageQuery pageQuery) {
        return memberService.queryPageList(bo, pageQuery);
    }

    @Operation(summary = "会员详情（基础 + 动态信息 + 病史 + 完整度 + 标签）")
    @SaCheckPermission("health:member:list")
    @GetMapping("/{id}")
    public R<MemberDetailVo> detail(@NotNull(message = "会员id不能为空") @PathVariable Integer id) {
        return R.ok(memberService.queryDetail(id));
    }

    @Operation(summary = "新增会员（同事务产出 t_member + 档案号 + profile 三行）")
    @SaCheckPermission("health:member:add")
    @Log(title = "会员管理", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<MemberVo> add(@Validated @RequestBody MemberSaveBo bo) {
        return R.ok(memberService.insertMember(bo));
    }

    @Operation(summary = "编辑会员基础信息（idCard 建后不可改）")
    @SaCheckPermission("health:member:edit")
    @Log(title = "会员管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}")
    public R<Void> edit(@PathVariable Integer id, @Validated @RequestBody MemberSaveBo bo) {
        bo.setId(id);
        return toAjax(memberService.updateMember(bo));
    }

    @Operation(summary = "停用/恢复会员（member_status 0↔1，07 §5.8）")
    @SaCheckPermission("health:member:remove")
    @Log(title = "会员管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Integer id, @Validated @RequestBody MemberStatusBo bo) {
        return toAjax(memberService.updateStatus(id, bo));
    }

    @Operation(summary = "取档案号与建档来源（一号制真值，07 §2.2）")
    @SaCheckPermission("health:member:query")
    @GetMapping("/{id}/archive-no")
    public R<MemberVo> archiveNo(@PathVariable Integer id) {
        return R.ok(memberService.queryArchiveNo(id));
    }

    @Operation(summary = "动态信息查询（无行返回 null，前端按首次录入处理）")
    @SaCheckPermission("health:member:query")
    @GetMapping("/{id}/profile")
    public R<HealthMemberProfile> profile(@PathVariable Integer id) {
        return R.ok(memberService.queryProfile(id));
    }

    @Operation(summary = "动态信息保存（1:1 upsert；bmi 服务端计算）")
    @SaCheckPermission("health:member:edit")
    @Log(title = "会员动态信息", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}/profile")
    public R<Void> saveProfile(@PathVariable Integer id, @RequestBody MemberProfileBo bo) {
        return toAjax(memberService.saveProfile(id, bo));
    }

    @Operation(summary = "完整度与缺项清单（实时计算，07 §5.2）")
    @SaCheckPermission("health:member:query")
    @GetMapping("/{id}/completeness")
    public R<MemberCompletenessVo> completeness(@PathVariable Integer id) {
        return R.ok(memberService.queryCompleteness(id));
    }

    @Operation(summary = "标记缺项（已确认缺失，与留空区分，07 §2.4）")
    @SaCheckPermission("health:member:edit")
    @Log(title = "档案缺项标记", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping("/{id}/missing")
    public R<Void> markMissing(@PathVariable Integer id, @Validated @RequestBody MemberMissingBo bo) {
        return toAjax(memberService.markMissing(id, bo));
    }

    @Operation(summary = "取消缺项标记")
    @SaCheckPermission("health:member:edit")
    @Log(title = "档案缺项标记", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}/missing/{fieldKey}")
    public R<Void> unmarkMissing(@PathVariable Integer id,
                                 @NotBlank(message = "缺项字段名不能为空") @PathVariable String fieldKey) {
        return toAjax(memberService.unmarkMissing(id, fieldKey));
    }

    @Operation(summary = "病史列表（可按类型过滤）")
    @SaCheckPermission("health:memberhistory:list")
    @GetMapping("/{id}/histories")
    public R<List<MemberHistoryBo>> histories(@PathVariable Integer id, String type) {
        return R.ok(memberService.queryHistories(id, type));
    }

    @Operation(summary = "病史整组替换（入参列表即最终集合，07 §2.5）")
    @SaCheckPermission("health:memberhistory:edit")
    @Log(title = "会员病史", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}/histories")
    public R<Void> saveHistories(@PathVariable Integer id, @Validated @RequestBody List<MemberHistoryBo> list) {
        return toAjax(memberService.saveHistories(id, list));
    }

    @Operation(summary = "启用中的标签全集（设置弹窗选项源）")
    @SaCheckPermission("health:member:list")
    @GetMapping("/tags/options")
    public R<List<HealthMemberTag>> tagOptions() {
        return R.ok(memberService.queryEnabledTags());
    }

    @Operation(summary = "设置会员标签（整组替换）")
    @SaCheckPermission("health:membertag:edit")
    @Log(title = "会员标签", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}/tags")
    public R<Void> saveTags(@PathVariable Integer id, @RequestBody List<Long> tagIds) {
        return toAjax(memberService.saveTags(id, tagIds));
    }

    @Operation(summary = "导出会员（脱敏，07 §9；POST form 走框架 download，03 导出约定）")
    @SaCheckPermission("health:member:export")
    @Log(title = "会员管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(@Validated MemberQueryBo bo, HttpServletResponse response) {
        List<MemberExportVo> list = memberService.queryExportList(bo);
        ExcelUtil.exportExcel(list, "会员数据", MemberExportVo.class, response);
    }

}
