package com.campus.ordering.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.ordering.entity.Complaint;

import java.util.Map;

public interface ComplaintService {
    Page<Complaint> getList(int page, int size, Integer status);
    Complaint getDetail(Long id);
    void handle(Long id, Long handlerId, String result);
    void refund(Long id, Long handlerId);
    Map<String, Object> getStats();
}
