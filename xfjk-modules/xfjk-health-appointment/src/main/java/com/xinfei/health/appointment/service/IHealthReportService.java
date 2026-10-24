package com.xinfei.health.appointment.service;

import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.health.appointment.domain.HealthConclusionDict;
import com.xinfei.health.appointment.domain.HealthReportItem;
import com.xinfei.health.appointment.domain.bo.ArchiveBo;
import com.xinfei.health.appointment.domain.bo.DeptSummaryBo;
import com.xinfei.health.appointment.domain.bo.ReportItemBo;
import com.xinfei.health.appointment.domain.bo.ReportConclusionBo;
import com.xinfei.health.appointment.domain.bo.ReportQueryBo;
import com.xinfei.health.appointment.domain.bo.ReportRevokeBo;
import com.xinfei.health.appointment.domain.bo.ReportSaveBo;
import com.xinfei.health.appointment.domain.bo.ReportSummaryBo;
import com.xinfei.health.appointment.domain.bo.SendBackBo;
import com.xinfei.health.appointment.domain.vo.ReportCompareVo;
import com.xinfei.health.appointment.domain.vo.ReportDetailVo;
import com.xinfei.health.appointment.domain.vo.ReportItemResultVo;
import com.xinfei.health.appointment.domain.vo.ReportParseVo;
import com.xinfei.health.appointment.domain.vo.ReportVo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 体检报告服务（07 §3.2）
 *
 * @author xinfei
 */
public interface IHealthReportService {

    TableDataInfo<ReportVo> queryPageList(ReportQueryBo bo, PageQuery pageQuery);

    ReportDetailVo queryDetail(Long id);

    ReportVo insertReport(ReportSaveBo bo);

    /**
     * 到诊登记建报告草稿（08 §5.5）：复用 insertReport，会员无档案号时不建并返回 null，
     * 由调用方在订单流水注明（不抛 6001）
     */
    ReportVo createRegisterDraft(ReportSaveBo bo);

    /** 延期恢复：延期项目级回未检（08 §5.7、18 C6），返回重置条数 */
    int restoreDelayedItems(Long reportId);

    /**
     * 项目重置（08 §5.7、18 D11）：itemIds 为空重置全部明细，结果清空回未检；
     * 已总检且未撤销时抛 6013（18 C9）。返回受影响明细的重置前快照供留痕。
     */
    List<HealthReportItem> resetReportItems(Long reportId, List<Long> itemIds);

    /** 附件上传（走 OSS 抽象；MinIO 凭据未配置时运行时失败，见 17 待确认 7） */
    Long uploadFile(MultipartFile file, Long reportId);

    ReportParseVo parseResult(Long reportId, MultipartFile file);

    ReportItemResultVo saveItem(Long reportId, ReportItemBo bo);

    void saveDeptSummary(Long reportId, DeptSummaryBo bo);

    void submitSummary(Long reportId, ReportSummaryBo bo);

    /** 仅保存结论条目（末次语义，07 §5.4）；返回拼接文本 */
    String saveConclusions(Long reportId, List<ReportConclusionBo> list);

    void revoke(Long reportId, ReportRevokeBo bo);

    void sendBack(Long reportId, SendBackBo bo);

    void finalCheck(Long reportId);

    void printOrIssue(Long reportId, boolean issue);

    void addArchive(Long reportId, ArchiveBo bo);

    ReportVo queryPrev(String archiveNo);

    List<ReportCompareVo> queryCompare(Integer memberId, Long checkitemId);

    List<HealthConclusionDict> queryConclusionOptions(String keyword);

    void downloadFile(Long reportId, HttpServletResponse response);

}
