package com.xinfei.health.appointment.domain.bo;

import lombok.Data;
import org.apache.fesod.sheet.annotation.ExcelProperty;

import java.io.Serial;
import java.io.Serializable;

/**
 * 结果文件行（07 §4：列格式 检查项 / 结果 / 单位 / 参考范围）
 *
 * <p>表头映射本期固定为四列中文表头；`health_report_header` 字典尚未建档（12 未种），
 * 待 12 补种后再改为可配置映射。
 *
 * @author xinfei
 */
@Data
public class ReportResultRowBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "检查项", index = 0)
    private String itemName;

    @ExcelProperty(value = "结果", index = 1)
    private String result;

    @ExcelProperty(value = "单位", index = 2)
    private String unit;

    @ExcelProperty(value = "参考范围", index = 3)
    private String refRange;

}
