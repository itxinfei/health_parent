package com.xinfei.health.assessment.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 单题作答（09 §6.3）
 *
 * @author xinfei
 */
@Data
public class AnswerItemBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long questionId;

    /** 选项 id 列表：single 恰 1 个；multi ≥ 1；text 可为空 */
    private List<Long> optionIds;

    /** 填空/「其他」文本 */
    private String answerText;

}
