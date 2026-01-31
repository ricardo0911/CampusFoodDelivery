package com.campus.ordering.controller.customer;

import com.campus.ordering.service.MysteryBoxService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/mystery-box")
public class MysteryBoxController {

    @Autowired
    private MysteryBoxService mysteryBoxService;

    /**
     * 获取盲盒列表
     */
    @GetMapping("/list")
    public Result<?> list() {
        return Result.success(mysteryBoxService.getAvailableBoxes());
    }

    /**
     * 开盲盒
     */
    @PostMapping("/open/{id}")
    public Result<?> open(@PathVariable Long id) {
        return Result.success(mysteryBoxService.openBox(id));
    }

    /**
     * 预览可能的菜品
     */
    @GetMapping("/preview/{id}")
    public Result<?> preview(@PathVariable Long id) {
        return Result.success(mysteryBoxService.previewBox(id));
    }
}
