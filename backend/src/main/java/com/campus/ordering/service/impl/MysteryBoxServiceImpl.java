package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.ordering.entity.Dish;
import com.campus.ordering.entity.MysteryBox;
import com.campus.ordering.entity.MysteryBoxDish;
import com.campus.ordering.entity.Shop;
import com.campus.ordering.mapper.DishMapper;
import com.campus.ordering.mapper.MysteryBoxDishMapper;
import com.campus.ordering.mapper.MysteryBoxMapper;
import com.campus.ordering.mapper.ShopMapper;
import com.campus.ordering.service.MysteryBoxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class MysteryBoxServiceImpl extends ServiceImpl<MysteryBoxMapper, MysteryBox> implements MysteryBoxService {

    @Autowired
    private MysteryBoxDishMapper mysteryBoxDishMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Override
    public List<MysteryBox> getAvailableBoxes() {
        List<MysteryBox> boxes = this.list(new LambdaQueryWrapper<MysteryBox>()
                .eq(MysteryBox::getStatus, 1)
                .orderByAsc(MysteryBox::getType));

        for (MysteryBox box : boxes) {
            if (box.getShopId() != null) {
                Shop shop = shopMapper.selectById(box.getShopId());
                if (shop != null) {
                    box.setShopName(shop.getName());
                }
            }
        }
        return boxes;
    }

    @Override
    public List<Dish> openBox(Long boxId) {
        MysteryBox box = this.getById(boxId);
        if (box == null || box.getStatus() != 1) {
            return new ArrayList<>();
        }

        // 获取盲盒菜品池
        List<MysteryBoxDish> pool = mysteryBoxDishMapper.selectList(
                new LambdaQueryWrapper<MysteryBoxDish>()
                        .eq(MysteryBoxDish::getBoxId, boxId));

        if (pool.isEmpty()) {
            return new ArrayList<>();
        }

        // 根据权重随机选择菜品
        int dishCount = box.getDishCount() != null ? box.getDishCount() : 3;
        List<Dish> selectedDishes = new ArrayList<>();
        Random random = new Random();

        // 计算总权重
        int totalWeight = pool.stream().mapToInt(MysteryBoxDish::getWeight).sum();

        for (int i = 0; i < dishCount && !pool.isEmpty(); i++) {
            int randomWeight = random.nextInt(totalWeight) + 1;
            int currentWeight = 0;

            for (int j = 0; j < pool.size(); j++) {
                currentWeight += pool.get(j).getWeight();
                if (currentWeight >= randomWeight) {
                    MysteryBoxDish selected = pool.get(j);
                    Dish dish = dishMapper.selectById(selected.getDishId());
                    if (dish != null) {
                        selectedDishes.add(dish);
                    }
                    // 移除已选择的，避免重复
                    totalWeight -= selected.getWeight();
                    pool.remove(j);
                    break;
                }
            }
        }

        return selectedDishes;
    }

    @Override
    public List<Dish> previewBox(Long boxId) {
        List<MysteryBoxDish> pool = mysteryBoxDishMapper.selectList(
                new LambdaQueryWrapper<MysteryBoxDish>()
                        .eq(MysteryBoxDish::getBoxId, boxId));

        List<Dish> dishes = new ArrayList<>();
        for (MysteryBoxDish mbd : pool) {
            Dish dish = dishMapper.selectById(mbd.getDishId());
            if (dish != null) {
                dishes.add(dish);
            }
        }
        return dishes;
    }
}
