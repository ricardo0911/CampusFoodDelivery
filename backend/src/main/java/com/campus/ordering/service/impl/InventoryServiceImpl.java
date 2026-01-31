package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ordering.entity.Inventory;
import com.campus.ordering.entity.InventoryLog;
import com.campus.ordering.mapper.InventoryLogMapper;
import com.campus.ordering.mapper.InventoryMapper;
import com.campus.ordering.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    @Autowired
    private InventoryLogMapper inventoryLogMapper;

    @Override
    public List<Inventory> findByShopId(Long shopId) {
        List<Inventory> list = this.list(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getShopId, shopId)
                .eq(Inventory::getStatus, 1)
                .orderByAsc(Inventory::getName));

        for (Inventory inv : list) {
            inv.setIsWarning(inv.getQuantity().compareTo(inv.getWarningThreshold()) <= 0);
        }
        return list;
    }

    @Override
    public List<Inventory> getWarningList(Long shopId) {
        List<Inventory> all = findByShopId(shopId);
        all.removeIf(inv -> !inv.getIsWarning());
        return all;
    }

    @Override
    @Transactional
    public void addStock(Long inventoryId, BigDecimal quantity, String remark) {
        Inventory inventory = this.getById(inventoryId);
        if (inventory == null) return;

        inventory.setQuantity(inventory.getQuantity().add(quantity));
        this.updateById(inventory);

        // 记录日志
        InventoryLog log = new InventoryLog();
        log.setInventoryId(inventoryId);
        log.setChangeType(1); // 入库
        log.setChangeQuantity(quantity);
        log.setRemark(remark);
        inventoryLogMapper.insert(log);
    }

    @Override
    @Transactional
    public void reduceStock(Long inventoryId, BigDecimal quantity, String remark) {
        Inventory inventory = this.getById(inventoryId);
        if (inventory == null) return;

        inventory.setQuantity(inventory.getQuantity().subtract(quantity));
        this.updateById(inventory);

        // 记录日志
        InventoryLog log = new InventoryLog();
        log.setInventoryId(inventoryId);
        log.setChangeType(2); // 出库
        log.setChangeQuantity(quantity.negate());
        log.setRemark(remark);
        inventoryLogMapper.insert(log);
    }

    @Override
    @Transactional
    public void adjustStock(Long inventoryId, BigDecimal quantity, String remark) {
        Inventory inventory = this.getById(inventoryId);
        if (inventory == null) return;

        BigDecimal diff = quantity.subtract(inventory.getQuantity());
        inventory.setQuantity(quantity);
        this.updateById(inventory);

        // 记录日志
        InventoryLog log = new InventoryLog();
        log.setInventoryId(inventoryId);
        log.setChangeType(3); // 盘点
        log.setChangeQuantity(diff);
        log.setRemark(remark != null ? remark : "盘点调整");
        inventoryLogMapper.insert(log);
    }

    @Override
    public List<InventoryLog> getLogs(Long inventoryId) {
        return inventoryLogMapper.selectList(new LambdaQueryWrapper<InventoryLog>()
                .eq(InventoryLog::getInventoryId, inventoryId)
                .orderByDesc(InventoryLog::getCreatedAt)
                .last("LIMIT 50"));
    }
}
