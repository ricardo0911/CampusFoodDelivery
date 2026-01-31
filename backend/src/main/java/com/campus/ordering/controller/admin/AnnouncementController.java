package com.campus.ordering.controller.admin;

import com.campus.ordering.entity.Announcement;
import com.campus.ordering.service.AnnouncementService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 获取公告列表
     */
    @GetMapping("/list")
    public Result<?> list() {
        return Result.success(announcementService.list());
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.success(announcementService.getById(id));
    }

    /**
     * 创建公告
     */
    @PostMapping("/create")
    public Result<?> create(@RequestBody Announcement announcement, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        announcement.setCreatedBy(adminId);
        announcement.setStatus(0); // 草稿
        announcementService.save(announcement);
        return Result.success("创建成功", announcement);
    }

    /**
     * 更新公告
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcement.setId(id);
        announcementService.updateById(announcement);
        return Result.success("更新成功");
    }

    /**
     * 发布公告
     */
    @PostMapping("/{id}/publish")
    public Result<?> publish(@PathVariable Long id) {
        Announcement announcement = announcementService.getById(id);
        if (announcement == null) {
            return Result.error("公告不存在");
        }
        announcement.setStatus(1);
        announcementService.updateById(announcement);
        return Result.success("发布成功");
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        announcementService.removeById(id);
        return Result.success("删除成功");
    }
}
