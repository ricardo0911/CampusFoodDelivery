package com.campus.ordering.controller.admin;

import com.campus.ordering.service.FinanceService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/finance")
public class FinanceController {

    @Autowired
    private FinanceService financeService;

    /**
     * 财务总览
     */
    @GetMapping("/overview")
    public Result<?> overview() {
        return Result.success(financeService.getOverview());
    }

    /**
     * 佣金统计
     */
    @GetMapping("/commission")
    public Result<?> commission(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1).toString();
        }
        if (endDate == null) {
            endDate = LocalDate.now().toString();
        }
        return Result.success(financeService.getCommission(startDate, endDate));
    }

    /**
     * 商家结算
     */
    @GetMapping("/settlement")
    public Result<?> settlement(
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) String month) {
        if (month == null) {
            month = LocalDate.now().toString().substring(0, 7);
        }
        return Result.success(financeService.getSettlement(shopId, month));
    }

    /**
     * 导出报表
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(defaultValue = "daily") String type,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        byte[] data = financeService.exportReport(type, startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment",
                "finance_report_" + startDate + "_" + endDate + ".csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }
}
