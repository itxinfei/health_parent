package com.xinfei.health.assessment.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 题库导入回执（逐行标错，形态同 20 §6 行级回执，09 §6.1）
 *
 * @author xinfei
 */
@Data
public class QuestionImportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int total;

    private int success;

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
