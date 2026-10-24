package com.xinfei.health.appointment.domain.vo;

import com.xinfei.common.excel.annotation.ExcelDictFormat;
import com.xinfei.common.excel.convert.ExcelDictConvert;
import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelIgnoreUnannotated;
import org.apache.fesod.sheet.annotation.ExcelProperty;
import org.apache.fesod.sheet.annotation.format.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 会员导出视图（07 §3.1 export：身份证/手机号脱敏，07 §9；
 * 脱敏级别本期约定=部分脱敏，health_export_mask）
 *
 * @author xinfei
 */
@Data
@ExcelIgnoreUnannotated
public class MemberExportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "会员ID")
    private Integer id;

    @ExcelProperty(value = "档案号")
    private String fileNumber;

    @ExcelProperty(value = "姓名")
    private String name;

    @ExcelProperty(value = "性别", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_user_sex")
    private String sex;

    @ExcelProperty(value = "身份证号")
    private String idCard;

    @ExcelProperty(value = "手机号")
    private String phoneNumber;

    @DateTimeFormat("yyyy-MM-dd")
    @ExcelProperty(value = "出生日期")
    private LocalDate birthday;

    @DateTimeFormat("yyyy-MM-dd")
    @ExcelProperty(value = "注册时间")
    private LocalDate regTime;

    @ExcelProperty(value = "会员状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "sys_normal_disable")
    private String memberStatus;

    @ExcelProperty(value = "完整度")
    private String completeness;

}
