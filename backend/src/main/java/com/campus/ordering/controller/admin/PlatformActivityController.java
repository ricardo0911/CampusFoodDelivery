package com.campus.ordering.controller.admin;

import com.campus.ordering.entity.PlatformActivity;
import com.campus.ordering.service.PlatformActivityService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/activity")
public class PlatformActivityController {

    @Autowired
    private PlatformActivityService activityService;

    /**
     * 获取活动列表
     */
    @GetMapping("/list")
    public Result<?> list() {
        return Result.success(activityService.list());
    }

    /**
     * 获取进行中的活动
     */
    @GetMapping("/active")
    public Result<?> active() {
        return Result.success(activityService.getActiveActivities());
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.success(activityService.getById(id));
    }

    /**
     * 创建活动
     */
    @PostMapping("/create")
    public Result<?> create(@RequestBody PlatformActivity activity) {
        activity.setStatus(0); // 草稿
        activityService.save(activity);
        return Result.success("创建成功", activity);
    }

    /**
     * 更新活动
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody PlatformActivity activity) {
        activity.setId(id);
        activityService.updateById(activity);
        return Result.success("更新成功");
    }

    /**
     * 启用活动
     */
    @PostMapping("/{id}/enable")
    public Result<?> enable(@PathVariable Long id) {
        PlatformActivity activity = activityService.getById(id);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        activity.setStatus(1);
        activityService.updateById(activity);
        return Result.success("启用成功");
    }

    /**
     * 停用活动
     */
    @PostMapping("/{id}/disable")
    public Result<?> disable(@PathVariable Long id) {
        PlatformActivity activity = activityService.getById(id);
        if (activity == null) {
            return Result.error("活动不存在");
        }
        activity.setStatus(2);
        activityService.updateById(activity);
        return Result.success("停用成功");
    }

    /**
     * 删除活动
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        activityService.removeById(id);
        return Result.success("删除成功");
    }
}
