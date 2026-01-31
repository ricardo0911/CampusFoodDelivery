package com.campus.ordering.controller.merchant;

import com.campus.ordering.entity.Inventory;
import com.campus.ordering.service.InventoryService;
import com.campus.ordering.service.ShopService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant/inventory")
public class MerchantInventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ShopService shopService;

    /**
     * 获取库存列表
     */
    @GetMapping("/list")
    public Result<?> list(HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Long shopId = shopService.findByMerchantId(merchantId).getId();
        return Result.success(inventoryService.findByShopId(shopId));
    }

    /**
     * 获取预警列表
     */
    @GetMapping("/warning")
    public Result<?> warning(HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Long shopId = shopService.findByMerchantId(merchantId).getId();
        return Result.success(inventoryService.getWarningList(shopId));
    }

    /**
     * 添加库存项
     */
    @PostMapping("/create")
    public Result<?> create(@RequestBody Inventory inventory, HttpServletRequest request) {
        Long merchantId = (Long) request.getAttribute("userId");
        Long shopId = shopService.findByMerchantId(merchantId).getId();

        inventory.setShopId(shopId);
        inventory.setStatus(1);
        inventoryService.save(inventory);
        return Result.success("添加成功", inventory);
    }

    /**
     * 更新库存项
     */
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Inventory inventory) {
        inventory.setId(id);
        inventoryService.updateById(inventory);
        return Result.success("更新成功");
    }

    /**
     * 入库
     */
    @PostMapping("/{id}/add")
    public Result<?> addStock(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        BigDecimal quantity = new BigDecimal(params.get("quantity").toString());
        String remark = (String) params.get("remark");
        inventoryService.addStock(id, quantity, remark);
        return Result.success("入库成功");
    }

    /**
     * 出库
     */
    @PostMapping("/{id}/reduce")
    public Result<?> reduceStock(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        BigDecimal quantity = new BigDecimal(params.get("quantity").toString());
        String remark = (String) params.get("remark");
        inventoryService.reduceStock(id, quantity, remark);
        return Result.success("出库成功");
    }

    /**
     * 盘点调整
     */
    @PostMapping("/{id}/adjust")
    public Result<?> adjustStock(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        BigDecimal quantity = new BigDecimal(params.get("quantity").toString());
        String remark = (String) params.get("remark");
        inventoryService.adjustStock(id, quantity, remark);
        return Result.success("调整成功");
    }

    /**
     * 获取变动记录
     */
    @GetMapping("/{id}/logs")
    public Result<?> logs(@PathVariable Long id) {
        return Result.success(inventoryService.getLogs(id));
    }

    /**
     * 删除库存项
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        inventoryService.removeById(id);
        return Result.success("删除成功");
    }
}
