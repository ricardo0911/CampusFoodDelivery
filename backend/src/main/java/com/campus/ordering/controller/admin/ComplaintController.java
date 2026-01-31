package com.campus.ordering.controller.admin;

import com.campus.ordering.service.ComplaintService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/complaint")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    /**
     * 投诉列表
     */
    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return Result.success(complaintService.getList(page, size, status));
    }

    /**
     * 投诉详情
     */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.success(complaintService.getDetail(id));
    }

    /**
     * 处理投诉
     */
    @PostMapping("/{id}/handle")
    public Result<?> handle(
            @PathVariable Long id,
            @RequestBody Map<String, String> params,
            HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        String result = params.get("result");
        complaintService.handle(id, adminId, result);
        return Result.success("处理成功");
    }

    /**
     * 退款处理
     */
    @PostMapping("/{id}/refund")
    public Result<?> refund(@PathVariable Long id, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        complaintService.refund(id, adminId);
        return Result.success("退款处理成功");
    }

    /**
     * 投诉统计
     */
    @GetMapping("/stats")
    public Result<?> stats() {
        return Result.success(complaintService.getStats());
    }
}
