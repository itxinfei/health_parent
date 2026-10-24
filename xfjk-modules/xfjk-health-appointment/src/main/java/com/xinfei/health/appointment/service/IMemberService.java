package com.xinfei.health.appointment.service;

import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
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
import com.xinfei.health.appointment.domain.HealthMemberProfile;
import com.xinfei.health.appointment.domain.HealthMemberTag;

import java.util.List;

/**
 * 会员与档案服务（07 §3.1，B6 会员/档案侧；报告侧 07 §3.2 留 B7）
 *
 * @author xinfei
 */
public interface IMemberService {

    /**
     * 分页查询（默认只出正常会员，07 §5.8）
     */
    TableDataInfo<MemberVo> queryPageList(MemberQueryBo bo, PageQuery pageQuery);

    /**
     * 详情（基础 + profile + 病史 + 完整度 + 标签，敏感字段脱敏）
     */
    MemberDetailVo queryDetail(Integer memberId);

    /**
     * 新增会员（同事务产出 t_member + archive_no + profile 三行，07 验收 §9）
     */
    MemberVo insertMember(MemberSaveBo bo);

    /**
     * 编辑基础信息（idCard 建后不可改，07 §5.1）
     */
    Boolean updateMember(MemberSaveBo bo);

    /**
     * 停用 / 恢复（member_status 0↔1，无 profile 行 upsert 先建行，07 §5.8）
     */
    Boolean updateStatus(Integer memberId, MemberStatusBo bo);

    /**
     * 取档案号与建档来源（07 §3.1 archive-no；历史会员未回写时 fileNumber 为空）
     */
    MemberVo queryArchiveNo(Integer memberId);

    /**
     * 动态信息（无行返回 null，前端按首次录入处理）
     */
    HealthMemberProfile queryProfile(Integer memberId);

    /**
     * 动态信息 upsert（bmi/waist-hip 服务端计算，血压尾数偶校验 6007）
     */
    Boolean saveProfile(Integer memberId, MemberProfileBo bo);

    /**
     * 完整度与缺项清单（07 §5.2，实时计算不落库）
     */
    MemberCompletenessVo queryCompleteness(Integer memberId);

    /**
     * 标记缺项（同 fieldKey 重复标记按更新处理）
     */
    Boolean markMissing(Integer memberId, MemberMissingBo bo);

    /**
     * 取消缺项标记
     */
    Boolean unmarkMissing(Integer memberId, String fieldKey);

    /**
     * 病史列表（可按 type 过滤）
     */
    List<MemberHistoryBo> queryHistories(Integer memberId, String type);

    /**
     * 整组替换病史（入参列表即最终集合，先删后插）
     */
    Boolean saveHistories(Integer memberId, List<MemberHistoryBo> list);

    /**
     * 启用中的标签全集（设置弹窗选项源）
     */
    List<HealthMemberTag> queryEnabledTags();

    /**
     * 设置会员标签（整组替换）
     */
    Boolean saveTags(Integer memberId, List<Long> tagIds);

    /**
     * 导出查询（07 §3.1 export，脱敏；行数上限 6104）
     */
    List<MemberExportVo> queryExportList(MemberQueryBo bo);

}
