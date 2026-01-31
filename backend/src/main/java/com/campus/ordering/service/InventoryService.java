package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.Inventory;
import com.campus.ordering.entity.InventoryLog;

import java.math.BigDecimal;
import java.util.List;

public interface InventoryService extends IService<Inventory> {
    List<Inventory> findByShopId(Long shopId);
    List<Inventory> getWarningList(Long shopId);
    void addStock(Long inventoryId, BigDecimal quantity, String remark);
    void reduceStock(Long inventoryId, BigDecimal quantity, String remark);
    void adjustStock(Long inventoryId, BigDecimal quantity, String remark);
    List<InventoryLog> getLogs(Long inventoryId);
}
