package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ordering.entity.FlashSale;
import com.campus.ordering.entity.FlashSaleOrder;
import com.campus.ordering.entity.Dish;
import com.campus.ordering.entity.Shop;
import com.campus.ordering.mapper.FlashSaleMapper;
import com.campus.ordering.mapper.FlashSaleOrderMapper;
import com.campus.ordering.mapper.DishMapper;
import com.campus.ordering.mapper.ShopMapper;
import com.campus.ordering.service.FlashSaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FlashSaleServiceImpl extends ServiceImpl<FlashSaleMapper, FlashSale> implements FlashSaleService {

    @Autowired
    private FlashSaleOrderMapper flashSaleOrderMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Override
    public List<FlashSale> getCurrentFlashSales() {
        LocalDateTime now = LocalDateTime.now();
        List<FlashSale> list = this.list(new LambdaQueryWrapper<FlashSale>()
                .le(FlashSale::getStartTime, now)
                .ge(FlashSale::getEndTime, now)
                .eq(FlashSale::getStatus, 1)
                .orderByAsc(FlashSale::getEndTime));
        enrichFlashSales(list);
        return list;
    }

    @Override
    public List<FlashSale> getUpcomingFlashSales() {
        LocalDateTime now = LocalDateTime.now();
        List<FlashSale> list = this.list(new LambdaQueryWrapper<FlashSale>()
                .gt(FlashSale::getStartTime, now)
                .eq(FlashSale::getStatus, 1)
                .orderByAsc(FlashSale::getStartTime)
                .last("LIMIT 10"));
        enrichFlashSales(list);
        return list;
    }

    @Override
    @Transactional
    public Map<String, Object> grab(Long flashSaleId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        FlashSale flashSale = this.getById(flashSaleId);
        if (flashSale == null) {
            result.put("success", false);
            result.put("message", "秒杀活动不存在");
            return result;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(flashSale.getStartTime())) {
            result.put("success", false);
            result.put("message", "秒杀活动尚未开始");
            return result;
        }

        if (now.isAfter(flashSale.getEndTime())) {
            result.put("success", false);
            result.put("message", "秒杀活动已结束");
            return result;
        }

        // 检查是否已抢购
        Long existingCount = flashSaleOrderMapper.selectCount(new LambdaQueryWrapper<FlashSaleOrder>()
                .eq(FlashSaleOrder::getFlashSaleId, flashSaleId)
                .eq(FlashSaleOrder::getUserId, userId));
        if (existingCount > 0) {
            result.put("success", false);
            result.put("message", "您已经抢购过了");
            return result;
        }

        // 检查库存（使用乐观锁防止超卖）
        if (flashSale.getSold() >= flashSale.getStock()) {
            result.put("success", false);
            result.put("message", "已售罄");
            return result;
        }

        // 更新库存
        flashSale.setSold(flashSale.getSold() + 1);
        this.updateById(flashSale);

        // 创建秒杀订单
        FlashSaleOrder order = new FlashSaleOrder();
        order.setFlashSaleId(flashSaleId);
        order.setUserId(userId);
        order.setStatus(0); // 待支付
        flashSaleOrderMapper.insert(order);

        result.put("success", true);
        result.put("message", "抢购成功");
        result.put("order", order);
        return result;
    }

    @Override
    public List<FlashSale> findByShopId(Long shopId) {
        List<FlashSale> list = this.list(new LambdaQueryWrapper<FlashSale>()
                .eq(FlashSale::getShopId, shopId)
                .orderByDesc(FlashSale::getCreatedAt));
        enrichFlashSales(list);
        return list;
    }

    private void enrichFlashSales(List<FlashSale> list) {
        for (FlashSale fs : list) {
            Dish dish = dishMapper.selectById(fs.getDishId());
            if (dish != null) {
                fs.setDishName(dish.getName());
                fs.setDishImage(dish.getImage());
            }
            Shop shop = shopMapper.selectById(fs.getShopId());
            if (shop != null) {
                fs.setShopName(shop.getName());
            }
        }
    }
}
