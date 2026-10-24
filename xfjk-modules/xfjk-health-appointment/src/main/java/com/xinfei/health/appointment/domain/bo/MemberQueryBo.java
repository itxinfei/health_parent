package com.xinfei.health.appointment.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 会员分页查询入参（07 §3.1：name/phone/idCard/fileNumber 模糊、性别、状态、标签）
 *
 * @author xinfei
 */
@Data
public class MemberQueryBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 姓名（模糊）
     */
    private String name;

    /**
     * 手机号（模糊）
     */
    private String phoneNumber;

    /**
     * 身份证号（模糊）
     */
    private String idCard;

    /**
     * 档案号（精确，一号制唯一性只认 t_health_member_archive_no）
     */
    private String fileNumber;

    /**
     * 性别（sys_user_sex：0 男 / 1 女 / 2 未知）
     */
    private String sex;

    /**
     * 会员状态（sys_normal_disable：0 正常 / 1 停用）；不传默认只看正常（07 §5.8）
     */
    private String memberStatus;

    /**
     * 标签 id（按 t_health_member_tag_rel 过滤）
     */
    private Long tagId;

}
