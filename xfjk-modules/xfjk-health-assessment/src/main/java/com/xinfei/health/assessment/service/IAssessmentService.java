package com.xinfei.health.assessment.service;

import com.xinfei.common.mybatis.core.page.PageQuery;
import com.xinfei.common.mybatis.core.page.TableDataInfo;
import com.xinfei.health.assessment.domain.bo.AssessmentQueryBo;
import com.xinfei.health.assessment.domain.bo.AssessmentSubmitBo;
import com.xinfei.health.assessment.domain.vo.AssessmentRecordVo;
import com.xinfei.health.assessment.domain.vo.QuestionnaireVo;

import java.util.List;
import java.util.Map;

/**
 * 评估记录 服务层（09 §5）
 *
 * @author xinfei
 */
public interface IAssessmentService {

    /**
     * 提交作答并算分（09 §5.1 九步，单事务；source 取 app/admin/batch）
     */
    AssessmentRecordVo submit(AssessmentSubmitBo bo, Long memberId, String source);

    /**
     * B 端记录分页（会员/问卷/等级/时间/来源）
     */
    TableDataInfo<AssessmentRecordVo> queryPageList(AssessmentQueryBo bo, PageQuery pageQuery);

    /**
     * 记录详情（含因子分、建议、人群）
     */
    AssessmentRecordVo queryById(Long id);

    /**
     * 评估统计：评估人数、阳性率、各等级分布（09 §6.1；阳性取 positive_flag、严重度取 severity 五值）
     */
    Map<String, Object> statistics(AssessmentQueryBo bo);

    /**
     * 逻辑删除（del_flag）
     */
    Boolean deleteById(Long id);

    /**
     * C 端：已发布且 is_public=1 的问卷列表（09 §6.2）
     */
    List<QuestionnaireVo> appQuestionnaireList();

    /**
     * C 端：问卷题目与选项（不返回 scale_rule、norm_*、factor_code 等算分内部字段）
     */
    QuestionnaireVo appQuestionnaireDetail(Long id);

    /**
     * C 端：本人记录分页
     */
    TableDataInfo<AssessmentRecordVo> appRecords(Long memberId, PageQuery pageQuery);

    /**
     * C 端：本人结果详情；非本人 7008
     */
    AssessmentRecordVo appDetail(Long memberId, Long id);

}
