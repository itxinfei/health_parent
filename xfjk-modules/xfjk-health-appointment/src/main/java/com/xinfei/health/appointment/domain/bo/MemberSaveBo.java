package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 会员基础信息保存入参（07 §3.1 add/edit；动态信息走 MemberProfileBo）
 *
 * <p>idCard/phoneNumber 的必填性不在 Bean 校验上判：列表值已脱敏无法回填，
 * 编辑时留空表示「不改原值」（07 §5.1、§5.8），是否必填由服务按新增/编辑分别判定。
 *
 * @author xinfei
 */
@Data
public class MemberSaveBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 编辑时必填；新增为空
     */
    private Integer id;

    @NotBlank(message = "会员姓名不能为空")
    @Size(max = 32, message = "会员姓名长度不能超过32个字符")
    private String name;

    /**
     * 性别（sys_user_sex：0 男 / 1 女 / 2 未知）
     */
    private String sex;

    /**
     * 身份证号：新增必填且合法（6005，服务判定）；建后不可改（07 §5.1）
     */
    @Size(max = 18, message = "身份证号长度不能超过18个字符")
    private String idCard;

    /**
     * 新增必填（服务判定）；编辑留空 = 不改原手机号（列表值是脱敏串，无法回填）
     */
    @Size(max = 11, message = "手机号长度必须为11位")
    private String phoneNumber;

    private LocalDate birthday;

    private String email;

    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;

}
