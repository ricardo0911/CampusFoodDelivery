package com.campus.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.ordering.entity.GroupOrder;
import com.campus.ordering.entity.GroupOrderMember;
import com.campus.ordering.mapper.GroupOrderMapper;
import com.campus.ordering.mapper.GroupOrderMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GroupOrderService {

    private final GroupOrderMapper groupOrderMapper;
    private final GroupOrderMemberMapper memberMapper;

    /**
     * 创建拼单
     */
    @Transactional
    public GroupOrder createGroupOrder(Long userId, String userName, Long shopId, String shopName,
            String title, Integer maxMembers, BigDecimal targetAmount,
            Integer discountType, BigDecimal discountValue) {
        GroupOrder group = new GroupOrder();
        group.setGroupCode(generateGroupCode());
        group.setCreatorId(userId);
        group.setCreatorName(userName);
        group.setShopId(shopId);
        group.setShopName(shopName);
        group.setTitle(title != null ? title : userName + "的拼单");
        group.setMaxMembers(maxMembers != null ? maxMembers : 4);
        group.setCurrentMembers(1);
        group.setTargetAmount(targetAmount != null ? targetAmount : new BigDecimal("50"));
        group.setCurrentAmount(BigDecimal.ZERO);
        group.setDiscountType(discountType != null ? discountType : 3); // 默认免配送费
        group.setDiscountValue(discountValue != null ? discountValue : new BigDecimal("5"));
        group.setStatus(0); // 拼单中
        group.setExpireTime(LocalDateTime.now().plusHours(2)); // 2小时后过期
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        group.setDeleted(0);

        groupOrderMapper.insert(group);

        // 创建者自动加入
        GroupOrderMember creator = new GroupOrderMember();
        creator.setGroupOrderId(group.getId());
        creator.setUserId(userId);
        creator.setNickname(userName);
        creator.setSubtotal(BigDecimal.ZERO);
        creator.setPaid(0);
        creator.setCreatedAt(LocalDateTime.now());
        creator.setUpdatedAt(LocalDateTime.now());
        memberMapper.insert(creator);

        return group;
    }

    /**
     * 加入拼单
     */
    @Transactional
    public Map<String, Object> joinGroupOrder(String groupCode, Long userId, String nickname, String avatar) {
        Map<String, Object> result = new HashMap<>();

        GroupOrder group = getByCode(groupCode);
        if (group == null) {
            result.put("success", false);
            result.put("message", "拼单不存在");
            return result;
        }

        if (group.getStatus() != 0) {
            result.put("success", false);
            result.put("message", "拼单已结束");
            return result;
        }

        if (LocalDateTime.now().isAfter(group.getExpireTime())) {
            result.put("success", false);
            result.put("message", "拼单已过期");
            return result;
        }

        if (group.getCurrentMembers() >= group.getMaxMembers()) {
            result.put("success", false);
            result.put("message", "拼单人数已满");
            return result;
        }

        // 检查是否已加入
        LambdaQueryWrapper<GroupOrderMember> existQuery = new LambdaQueryWrapper<>();
        existQuery.eq(GroupOrderMember::getGroupOrderId, group.getId())
                .eq(GroupOrderMember::getUserId, userId);
        if (memberMapper.selectCount(existQuery) > 0) {
            result.put("success", false);
            result.put("message", "您已加入该拼单");
            return result;
        }

        // 加入拼单
        GroupOrderMember member = new GroupOrderMember();
        member.setGroupOrderId(group.getId());
        member.setUserId(userId);
        member.setNickname(nickname);
        member.setAvatar(avatar);
        member.setSubtotal(BigDecimal.ZERO);
        member.setPaid(0);
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        memberMapper.insert(member);

        // 更新人数
        group.setCurrentMembers(group.getCurrentMembers() + 1);
        group.setUpdatedAt(LocalDateTime.now());
        groupOrderMapper.updateById(group);

        result.put("success", true);
        result.put("message", "加入成功");
        result.put("groupOrder", group);
        return result;
    }

    /**
     * 获取拼单详情
     */
    public Map<String, Object> getGroupOrderDetail(String groupCode) {
        Map<String, Object> result = new HashMap<>();

        GroupOrder group = getByCode(groupCode);
        if (group == null) {
            return null;
        }

        // 获取成员列表
        LambdaQueryWrapper<GroupOrderMember> query = new LambdaQueryWrapper<>();
        query.eq(GroupOrderMember::getGroupOrderId, group.getId())
                .orderByAsc(GroupOrderMember::getCreatedAt);
        List<GroupOrderMember> members = memberMapper.selectList(query);

        result.put("groupOrder", group);
        result.put("members", members);
        result.put("progress", calculateProgress(group));

        return result;
    }

    /**
     * 更新成员购物车
     */
    @Transactional
    public void updateMemberCart(Long groupOrderId, Long userId, String cartItems, BigDecimal subtotal) {
        LambdaQueryWrapper<GroupOrderMember> query = new LambdaQueryWrapper<>();
        query.eq(GroupOrderMember::getGroupOrderId, groupOrderId)
                .eq(GroupOrderMember::getUserId, userId);
        GroupOrderMember member = memberMapper.selectOne(query);

        if (member != null) {
            member.setCartItems(cartItems);
            member.setSubtotal(subtotal);
            member.setUpdatedAt(LocalDateTime.now());
            memberMapper.updateById(member);

            // 更新拼单总金额
            updateGroupTotal(groupOrderId);
        }
    }

    /**
     * 获取用户参与的拼单列表
     */
    public List<GroupOrder> getUserGroupOrders(Long userId) {
        LambdaQueryWrapper<GroupOrderMember> memberQuery = new LambdaQueryWrapper<>();
        memberQuery.eq(GroupOrderMember::getUserId, userId);
        List<GroupOrderMember> memberships = memberMapper.selectList(memberQuery);

        if (memberships.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> groupIds = new ArrayList<>();
        for (GroupOrderMember m : memberships) {
            groupIds.add(m.getGroupOrderId());
        }

        LambdaQueryWrapper<GroupOrder> groupQuery = new LambdaQueryWrapper<>();
        groupQuery.in(GroupOrder::getId, groupIds)
                .eq(GroupOrder::getDeleted, 0)
                .orderByDesc(GroupOrder::getCreatedAt);

        return groupOrderMapper.selectList(groupQuery);
    }

    // 辅助方法
    private GroupOrder getByCode(String groupCode) {
        LambdaQueryWrapper<GroupOrder> query = new LambdaQueryWrapper<>();
        query.eq(GroupOrder::getGroupCode, groupCode)
                .eq(GroupOrder::getDeleted, 0);
        return groupOrderMapper.selectOne(query);
    }

    private String generateGroupCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void updateGroupTotal(Long groupOrderId) {
        LambdaQueryWrapper<GroupOrderMember> query = new LambdaQueryWrapper<>();
        query.eq(GroupOrderMember::getGroupOrderId, groupOrderId);
        List<GroupOrderMember> members = memberMapper.selectList(query);

        BigDecimal total = BigDecimal.ZERO;
        for (GroupOrderMember m : members) {
            if (m.getSubtotal() != null) {
                total = total.add(m.getSubtotal());
            }
        }

        GroupOrder group = groupOrderMapper.selectById(groupOrderId);
        if (group != null) {
            group.setCurrentAmount(total);
            // 检查是否达到目标
            if (total.compareTo(group.getTargetAmount()) >= 0 && group.getStatus() == 0) {
                group.setStatus(1); // 已成团
            }
            group.setUpdatedAt(LocalDateTime.now());
            groupOrderMapper.updateById(group);
        }
    }

    private Map<String, Object> calculateProgress(GroupOrder group) {
        Map<String, Object> progress = new HashMap<>();
        progress.put("currentMembers", group.getCurrentMembers());
        progress.put("maxMembers", group.getMaxMembers());
        progress.put("memberProgress", (group.getCurrentMembers() * 100) / group.getMaxMembers());
        progress.put("currentAmount", group.getCurrentAmount());
        progress.put("targetAmount", group.getTargetAmount());

        BigDecimal amountProgress = BigDecimal.ZERO;
        if (group.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            amountProgress = group.getCurrentAmount()
                    .multiply(new BigDecimal("100"))
                    .divide(group.getTargetAmount(), 0, RoundingMode.DOWN);
        }
        progress.put("amountProgress", amountProgress.intValue());

        return progress;
    }
}
