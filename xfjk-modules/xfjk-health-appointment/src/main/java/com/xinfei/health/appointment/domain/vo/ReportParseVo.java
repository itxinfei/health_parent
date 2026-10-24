package com.xinfei.health.appointment.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 结果文件解析响应（07 §4；逐行容错，异常行返回明细 9001）
 *
 * @author xinfei
 */
@Data
public class ReportParseVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int total;

    private int success;

    /** 匹配不到 t_checkitem 仍落库的行数（ref_configured=0） */
    private int unmatched;

    private List<RowError> errors = new ArrayList<>();

    @Data
    public static class RowError implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private int row;

        private String message;

        public RowError() {
        }

        public RowError(int row, String message) {
            this.row = row;
            this.message = message;
        }
    }

}
