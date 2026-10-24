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
 * 订单导出视图（08 §4：手机号脱敏，不含身份证，08 §8 脱敏红线）
 *
 * @author xinfei
 */
@Data
@ExcelIgnoreUnannotated
public class OrderExportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "订单号")
    private Integer id;

    @DateTimeFormat("yyyy-MM-dd")
    @ExcelProperty(value = "预约日期")
    private LocalDate orderDate;

    @ExcelProperty(value = "会员姓名")
    private String memberName;

    @ExcelProperty(value = "档案号")
    private String fileNumber;

    @ExcelProperty(value = "手机号")
    private String phoneNumber;

    @ExcelProperty(value = "订单类型", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "health_order_type")
    private String orderType;

    @ExcelProperty(value = "原状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "health_order_status")
    private String orderStatus;

    @ExcelProperty(value = "流程状态", converter = ExcelDictConvert.class)
    @ExcelDictFormat(dictType = "health_flow_status")
    private String flowStatus;

    @ExcelProperty(value = "体检号")
    private String examNo;

}
