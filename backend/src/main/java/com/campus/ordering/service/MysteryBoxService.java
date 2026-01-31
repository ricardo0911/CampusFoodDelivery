package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.ordering.entity.Dish;
import com.campus.ordering.entity.MysteryBox;

import java.util.List;

public interface MysteryBoxService extends IService<MysteryBox> {
    List<MysteryBox> getAvailableBoxes();
    List<Dish> openBox(Long boxId);
    List<Dish> previewBox(Long boxId);
}
