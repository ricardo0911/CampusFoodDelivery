package com.campus.ordering.controller.customer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.campus.ordering.entity.Address;
import com.campus.ordering.service.AddressService;
import com.campus.ordering.util.JwtUtil;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/customer/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    @GetMapping("/list")
    public Result<?> list(HttpServletRequest request) {
        Long userId = getUserId(request);
        List<Address> list = addressService.list(
            new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
        );
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result<?> add(@RequestBody Address address, HttpServletRequest request) {
        Long userId = getUserId(request);
        address.setUserId(userId);

        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressService.update(
                new LambdaUpdateWrapper<Address>()
                    .eq(Address::getUserId, userId)
                    .set(Address::getIsDefault, 0)
            );
        }

        addressService.save(address);
        return Result.success("添加成功", address);
    }

    @PutMapping("/update/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Address address, HttpServletRequest request) {
        Long userId = getUserId(request);

        Address existing = addressService.getById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error(404, "地址不存在");
        }

        address.setId(id);
        address.setUserId(userId);

        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressService.update(
                new LambdaUpdateWrapper<Address>()
                    .eq(Address::getUserId, userId)
                    .set(Address::getIsDefault, 0)
            );
        }

        addressService.updateById(address);
        return Result.success("更新成功");
    }

    @PutMapping("/setDefault/{id}")
    public Result<?> setDefault(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);

        Address existing = addressService.getById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error(404, "地址不存在");
        }

        // 取消其他默认地址
        addressService.update(
            new LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0)
        );

        // 设置新的默认地址
        addressService.update(
            new LambdaUpdateWrapper<Address>()
                .eq(Address::getId, id)
                .set(Address::getIsDefault, 1)
        );

        return Result.success("设置成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);

        Address existing = addressService.getById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error(404, "地址不存在");
        }

        addressService.removeById(id);
        return Result.success("删除成功");
    }
}
