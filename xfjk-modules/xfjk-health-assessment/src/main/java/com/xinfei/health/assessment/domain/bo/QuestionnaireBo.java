package com.xinfei.health.assessment.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 问卷分页查询条件（09 §6.1）
 *
 * @author xinfei
 */
@Data
public class QuestionnaireBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;

    private String code;

    private String category;

    private String scaleType;

    private String status;

}
