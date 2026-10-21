package com.pangzhao.controller;

import com.pangzhao.constant.MessageConstant;
import com.pangzhao.entity.Result;
import com.pangzhao.service.MemberService;
import com.pangzhao.service.OrderService;
import com.pangzhao.service.ReportService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 *
 */
@RestController
@RequestMapping("/report")
public class ReportController {

    @Reference
    private MemberService memberService;
    @Reference
    private OrderService orderService;
    @Reference
    private ReportService reportService;


    @RequestMapping("/getMemberReport.do")
    public Map getMemberReport(){
        return memberService.findByDate();
    }

    @RequestMapping("/getSetmealReport.do")
    public Map getSetmealReport(){
        return orderService.findBySetmeal();
    }


    @RequestMapping("/getBusinessReportData.do")
    public Map getBusinessReportData() throws Exception {
        Map map = reportService.getBusinessReportData();
        return map;
    }

    @RequestMapping("/exportBusinessReport.do")
    public Result exportBusinessReport(HttpServletRequest request, HttpServletResponse response) throws Exception {


        try {
            Map map = reportService.getBusinessReportData();
            String reportDate = (String) map.get("reportDate");
            Integer totalMember = (Integer) map.get("totalMember");
            Integer thisWeekNewMember = (Integer) map.get("thisWeekNewMember");
            Integer todayNewMember = (Integer) map.get("todayNewMember");
            Integer thisMonthNewMember = (Integer) map.get("thisMonthNewMember");
            Integer todayOrderNumber = (Integer) map.get("todayOrderNumber");
            Integer thisWeekOrderNumber = (Integer) map.get("thisWeekOrderNumber");
            Integer thisMonthOrderNumber = (Integer) map.get("thisMonthOrderNumber");
            Integer todayVisitsNumber = (Integer) map.get("todayVisitsNumber");
            Integer thisWeekVisitsNumber = (Integer) map.get("thisWeekVisitsNumber");
            Integer thisMonthVisitsNumber = (Integer) map.get("thisMonthVisitsNumber");
            List<Map> hotSetmeal = (List<Map>) map.get("hotSetmeal");

            String path = request.getSession().getServletContext().getRealPath("template");
            File file = new File(path + "/" + "report_template.xlsx");

            XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));

            XSSFSheet sheet = workbook.getSheetAt(0);

            XSSFRow row = sheet.getRow(2);
            row.getCell(5).setCellValue(reportDate);

            row = sheet.getRow(4);
            row.getCell(5).setCellValue(todayNewMember);
            row.getCell(7).setCellValue(totalMember);

            row = sheet.getRow(5);
            row.getCell(5).setCellValue(thisWeekNewMember);
            row.getCell(7).setCellValue(thisMonthNewMember);

            row = sheet.getRow(7);
            row.getCell(5).setCellValue(todayOrderNumber);
            row.getCell(7).setCellValue(todayVisitsNumber);


            row = sheet.getRow(8);
            row.getCell(5).setCellValue(thisWeekOrderNumber);
            row.getCell(7).setCellValue(thisWeekVisitsNumber);

            row = sheet.getRow(9);
            row.getCell(5).setCellValue(thisMonthOrderNumber);
            row.getCell(7).setCellValue(thisMonthVisitsNumber);

            for (int i = 0; i < hotSetmeal.size(); i++) {
                row = sheet.getRow(i + 12);
                Map map1 = hotSetmeal.get(i);
                row.getCell(4).setCellValue((String) map1.get("name"));
                row.getCell(5).setCellValue((Long) map1.get("total"));
                row.getCell(6).setCellValue(String.valueOf((BigDecimal)map1.get("proportion")));
                row.getCell(7).setCellValue((String) map1.get("attention"));
            }


            //通过输出流进行文件下载
            ServletOutputStream os = response.getOutputStream();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("content-Disposition", "attachment;filename=report.xlsx");
            workbook.write(os);

            os.flush();
            os.close();
            workbook.close();

            return new Result(true, MessageConstant.GET_BUSINESS_REPORT_SUCCESS);
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false, MessageConstant.GET_BUSINESS_REPORT_FAIL);
        }
    }
}
