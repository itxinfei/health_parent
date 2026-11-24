package com.xinfei.health.assessment.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 提交作答（C 端/后台代填共用，09 §5.1）
 *
 * @author xinfei
 */
@Data
public class AssessmentSubmitBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 问卷 id */
    private Long questionnaireId;

    /** 会员 id：C 端取自会话，B 端代填必填（6001） */
    private Long memberId;

    /** 开始作答时间（可空，duration 据此计算） */
    private LocalDateTime startTime;

    /** 作答明细 */
    private java.util.List<AnswerItemBo> answers;

}
