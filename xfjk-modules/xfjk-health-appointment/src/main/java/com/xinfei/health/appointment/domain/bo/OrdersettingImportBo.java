package com.xinfei.health.appointment.domain.bo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * 预约名额 Excel 导入行（08 §4.4：[日期, 可预约人数]）
 *
 * @author xinfei
 */
@Data
public class OrdersettingImportBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 预约日期，如 2026-11-01
     */
    @ExcelProperty(value = "日期", index = 0)
    private String orderDate;

    /**
     * 可预约人数
     */
    @ExcelProperty(value = "可预约人数", index = 1)
    private String number;

}
