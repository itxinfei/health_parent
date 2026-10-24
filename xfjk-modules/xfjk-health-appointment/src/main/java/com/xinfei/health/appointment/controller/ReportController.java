package com.xinfei.health.appointment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.xinfei.common.core.domain.R;
import com.xinfei.common.idempotent.annotation.RepeatSubmit;
import com.xinfei.common.log.annotation.Log;
import com.xinfei.common.log.enums.BusinessType;
import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.common.web.core.BaseController;
import com.xinfei.health.appointment.domain.HealthConclusionDict;
import com.xinfei.health.appointment.domain.bo.ArchiveBo;
import com.xinfei.health.appointment.domain.bo.CriticalRegisterBo;
import com.xinfei.health.appointment.domain.bo.DeptSummaryBo;
import com.xinfei.health.appointment.domain.bo.ReportConclusionBo;
import com.xinfei.health.appointment.domain.bo.ReportItemBo;
import com.xinfei.health.appointment.domain.bo.ReportQueryBo;
import com.xinfei.health.appointment.domain.bo.ReportRevokeBo;
import com.xinfei.health.appointment.domain.bo.ReportSaveBo;
import com.xinfei.health.appointment.domain.bo.ReportSummaryBo;
import com.xinfei.health.appointment.domain.bo.SendBackBo;
import com.xinfei.health.appointment.domain.vo.ReportDetailVo;
import com.xinfei.health.appointment.domain.vo.ReportItemResultVo;
import com.xinfei.health.appointment.domain.vo.ReportParseVo;
import com.xinfei.health.appointment.domain.vo.ReportVo;
import com.xinfei.health.appointment.service.IHealthCriticalService;
import com.xinfei.health.appointment.service.IHealthReportService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 体检报告（07 §3.2，B7 报告侧）
 *
 * @author xinfei
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/health/reports")
@Tag(name = "体检报告管理")
public class ReportController extends BaseController {

    private final IHealthReportService reportService;

    private final IHealthCriticalService criticalService;

    @Operation(summary = "分页查询报告（档案号/批次/状态/日期区间）")
    @SaCheckPermission("health:memberreport:list")
    @GetMapping
    public TableDataInfo<ReportVo> list(ReportQueryBo bo, PageQuery pageQuery) {
        return reportService.queryPageList(bo, pageQuery);
    }

    @Operation(summary = "报告详情（五段 + 明细 + 小结 + 结论 + 危急值 + 归档）")
    @SaCheckPermission("health:memberreport:query")
    @GetMapping("/{id}")
    public R<ReportDetailVo> detail(@NotNull(message = "报告id不能为空") @PathVariable Long id) {
        return R.ok(reportService.queryDetail(id));
    }

    @Operation(summary = "手工建档报告")
    @SaCheckPermission("health:memberreport:add")
    @Log(title = "体检报告", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public R<ReportVo> add(@Validated @RequestBody ReportSaveBo bo) {
        return R.ok(reportService.insertReport(bo));
    }

    @Operation(summary = "上传附件（可按需同时解析结果文件，07 §3.2/§4）")
    @SaCheckPermission("health:memberreport:add")
    @Log(title = "体检报告附件", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping("/upload")
    public R<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                         @RequestParam(value = "resultFile", required = false) MultipartFile resultFile,
                                         @RequestParam(value = "reportId", required = false) Long reportId) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (reportId != null) {
            data.put("ossId", reportService.uploadFile(file, reportId));
        } else if (resultFile == null) {
            return R.fail("reportId 与 resultFile 至少传一个");
        }
        if (resultFile != null) {
            if (reportId == null) {
                return R.fail("解析结果文件须指定 reportId");
            }
            data.put("parse", reportService.parseResult(reportId, resultFile));
        }
        return R.ok(data);
    }

    @Operation(summary = "解析结果文件为明细（逐行容错，07 §4）")
    @SaCheckPermission("health:memberreport:add")
    @Log(title = "报告结果解析", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping("/{id}/parse")
    public R<ReportParseVo> parse(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return R.ok(reportService.parseResult(id, file));
    }

    @Operation(summary = "录入/修改单项结果（走 07 §5.3 判定）")
    @SaCheckPermission("health:memberreport:edit")
    @Log(title = "报告明细录入", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/{id}/items")
    public R<ReportItemResultVo> saveItem(@PathVariable Long id, @Validated @RequestBody ReportItemBo bo) {
        return R.ok(reportService.saveItem(id, bo));
    }

    @Operation(summary = "科室小结提交 / 修改（18 C9）")
    @SaCheckPermission("health:memberreport:edit")
    @Log(title = "科室小结", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}/dept-summary")
    public R<Void> saveDeptSummary(@PathVariable Long id, @Validated @RequestBody DeptSummaryBo bo) {
        reportService.saveDeptSummary(id, bo);
        return R.ok();
    }

    @Operation(summary = "总检提交（须 08 §3.2 允许该迁移）")
    @SaCheckPermission("health:report:summary")
    @Log(title = "总检提交", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}/summary")
    public R<Void> summary(@PathVariable Long id, @Validated @RequestBody ReportSummaryBo bo) {
        reportService.submitSummary(id, bo);
        return R.ok();
    }

    @Operation(summary = "总检撤销（须填原因，18 C9）")
    @SaCheckPermission("health:report:revoke")
    @Log(title = "总检撤销", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}/revoke")
    public R<Void> revoke(@PathVariable Long id, @Validated @RequestBody ReportRevokeBo bo) {
        reportService.revoke(id, bo);
        return R.ok();
    }

    @Operation(summary = "异常打回某科室（18 C9）")
    @SaCheckPermission("health:report:revoke")
    @Log(title = "报告打回", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}/send-back")
    public R<Void> sendBack(@PathVariable Long id, @Validated @RequestBody SendBackBo bo) {
        reportService.sendBack(id, bo);
        return R.ok();
    }

    @Operation(summary = "终审签发（18 C8）")
    @SaCheckPermission("health:report:finalcheck")
    @Log(title = "终审签发", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}/final-check")
    public R<Void> finalCheck(@PathVariable Long id) {
        reportService.finalCheck(id);
        return R.ok();
    }

    @Operation(summary = "打印（同步 08 流程状态）")
    @SaCheckPermission("health:memberreport:edit")
    @Log(title = "报告打印", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/{id}/print")
    public R<Void> print(@PathVariable Long id) {
        reportService.printOrIssue(id, false);
        return R.ok();
    }

    @Operation(summary = "发放（须已终审签发）")
    @SaCheckPermission("health:memberreport:edit")
    @Log(title = "报告发放", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/{id}/issue")
    public R<Void> issue(@PathVariable Long id) {
        reportService.printOrIssue(id, true);
        return R.ok();
    }

    @Operation(summary = "保存结论条目（末次语义，07 §5.4）")
    @SaCheckPermission("health:report:summary")
    @Log(title = "总检结论", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{id}/conclusions")
    public R<Void> saveConclusions(@PathVariable Long id, @RequestBody List<ReportConclusionBo> list) {
        reportService.saveConclusions(id, list);
        return R.ok();
    }

    @Operation(summary = "结论词候选（仅 published，keyword 模糊）")
    @SaCheckPermission("health:report:summary")
    @GetMapping("/conclusion-options")
    public R<List<HealthConclusionDict>> conclusionOptions(String keyword) {
        return R.ok(reportService.queryConclusionOptions(keyword));
    }

    @Operation(summary = "危急值登记")
    @SaCheckPermission("health:critical:add")
    @Log(title = "危急值登记", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping("/{id}/critical")
    public R<Void> registerCritical(@PathVariable Long id, @Validated @RequestBody CriticalRegisterBo bo) {
        criticalService.register(id, bo);
        return R.ok();
    }

    @Operation(summary = "归档附属资料")
    @SaCheckPermission("health:memberreport:edit")
    @Log(title = "报告归档", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping("/{id}/archives")
    public R<Void> addArchive(@PathVariable Long id, @Validated @RequestBody ArchiveBo bo) {
        reportService.addArchive(id, bo);
        return R.ok();
    }

    @Operation(summary = "下载附件（鉴权）")
    @SaCheckPermission("health:memberreport:query")
    @GetMapping("/{id}/file")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        reportService.downloadFile(id, response);
    }

    @Operation(summary = "上次报告（按档案号，18 D11）")
    @SaCheckPermission("health:memberreport:query")
    @GetMapping("/prev")
    public R<ReportVo> prev(@RequestParam("archiveNo") String archiveNo) {
        return R.ok(reportService.queryPrev(archiveNo));
    }

}
