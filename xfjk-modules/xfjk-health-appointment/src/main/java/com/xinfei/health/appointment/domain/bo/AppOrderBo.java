package com.xinfei.health.appointment.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * C 端预约下单入参（13 §4.2：POST /app/orders）
 *
 * <p>member/ ext 结构与 13 §4.2 对齐：member 为体检人，ext 为禁忌四维 +
 * 延后补检二次确认。禁忌拦截返回 5009；提示档放行；延后补检档须 confirmedDelay=true。
 *
 * @author xinfei
 */
@Data
public class AppOrderBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 套餐 id
     */
    @NotNull(message = "套餐不能为空")
    private Integer setmealId;

    /**
     * 预约日期
     */
    @NotNull(message = "预约日期不能为空")
    private LocalDate orderDate;

    /**
     * 短信验证码（必带，13 R5）
     */
    @NotBlank(message = "短信验证码不能为空")
    private String smsCode;

    /**
     * 短信验证码收件手机号
     */
    @NotBlank(message = "手机号不能为空")
    private String phone;

    /**
     * 延后补检档用户二次确认（13 §5.3：确认后放行并记 postpone 候选）
     */
    private Boolean confirmedDelay;

    /**
     * 体检人信息（13 §4.2：member{name,idCard,sex,phone,email,remark}）
     */
    @Valid
    @NotNull(message = "体检人信息不能为空")
    private AppOrderMember member;

    /**
     * 禁忌四维（13 §4.2：ext{married,hasSexHistory,pregnancy,inMenstruation,metalImplant}）
     */
    private AppOrderExt ext;

    /**
     * 体检人（姓名 + 身份证为会员识别键，08 §5.3 步骤 4）
     */
    @Data
    public static class AppOrderMember implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @NotBlank(message = "姓名不能为空")
        private String name;

        @NotBlank(message = "身份证号不能为空")
        private String idCard;

        @NotBlank(message = "性别不能为空")
        private String sex;

        private String phone;

        private String email;

        private String remark;

    }

    /**
     * 禁忌四维画像
     */
    @Data
    public static class AppOrderExt implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String married;

        private String hasSexHistory;

        private String pregnancy;

        private Boolean inMenstruation;

        private Boolean metalImplant;

    }

}
