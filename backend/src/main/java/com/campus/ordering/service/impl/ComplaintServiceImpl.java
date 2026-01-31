package com.campus.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.ordering.entity.*;
import com.campus.ordering.mapper.*;
import com.campus.ordering.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    @Autowired
    private ComplaintMapper complaintMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ShopMapper shopMapper;

    @Override
    public Page<Complaint> getList(int page, int size, Integer status) {
        Page<Complaint> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<Complaint>()
                .orderByDesc(Complaint::getCreatedAt);

        if (status != null) {
            wrapper.eq(Complaint::getStatus, status);
        }

        Page<Complaint> result = complaintMapper.selectPage(pageParam, wrapper);

        for (Complaint complaint : result.getRecords()) {
            enrichComplaint(complaint);
        }

        return result;
    }

    @Override
    public Complaint getDetail(Long id) {
        Complaint complaint = complaintMapper.selectById(id);
        if (complaint != null) {
            enrichComplaint(complaint);
        }
        return complaint;
    }

    @Override
    @Transactional
    public void handle(Long id, Long handlerId, String result) {
        Complaint complaint = complaintMapper.selectById(id);
        if (complaint == null) return;

        complaint.setStatus(2); // 已解决
        complaint.setHandlerId(handlerId);
        complaint.setResult(result);
        complaintMapper.updateById(complaint);
    }

    @Override
    @Transactional
    public void refund(Long id, Long handlerId) {
        Complaint complaint = complaintMapper.selectById(id);
        if (complaint == null) return;

        // 更新订单状态为退款中
        Order order = orderMapper.selectById(complaint.getOrderId());
        if (order != null) {
            order.setStatus(6); // 退款中
            orderMapper.updateById(order);
        }

        complaint.setStatus(2);
        complaint.setHandlerId(handlerId);
        complaint.setResult("已同意退款");
        complaintMapper.updateById(complaint);
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> result = new HashMap<>();

        result.put("total", complaintMapper.selectCount(null));
        result.put("pending", complaintMapper.selectCount(new LambdaQueryWrapper<Complaint>()
                .eq(Complaint::getStatus, 0)));
        result.put("processing", complaintMapper.selectCount(new LambdaQueryWrapper<Complaint>()
                .eq(Complaint::getStatus, 1)));
        result.put("resolved", complaintMapper.selectCount(new LambdaQueryWrapper<Complaint>()
                .eq(Complaint::getStatus, 2)));

        return result;
    }

    private void enrichComplaint(Complaint complaint) {
        Order order = orderMapper.selectById(complaint.getOrderId());
        if (order != null) {
            complaint.setOrderNo(order.getOrderNo());
            complaint.setShopName(order.getShopName());
        }

        if (complaint.getUserId() != null) {
            User user = userMapper.selectById(complaint.getUserId());
            if (user != null) {
                complaint.setUserName(user.getNickname());
            }
        }
    }
}
