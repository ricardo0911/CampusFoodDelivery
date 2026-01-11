package com.campus.ordering.controller.customer;

import com.campus.ordering.entity.Address;
import com.campus.ordering.entity.User;
import com.campus.ordering.service.AddressService;
import com.campus.ordering.service.UserService;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AddressService addressService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<?> info(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public Result<?> update(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        if (params.containsKey("nickname")) {
            user.setNickname(params.get("nickname"));
        }
        if (params.containsKey("avatar")) {
            user.setAvatar(params.get("avatar"));
        }
        if (params.containsKey("phone")) {
            user.setPhone(params.get("phone"));
        }

        userService.updateById(user);
        user.setPassword(null);
        return Result.success("更新成功", user);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<?> changePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!user.getPassword().equals(oldPassword)) {
            return Result.error("原密码错误");
        }

        user.setPassword(newPassword);
        userService.updateById(user);
        return Result.success("密码修改成功");
    }

    // ========== 地址管理 ==========

    /**
     * 获取地址列表
     */
    @GetMapping("/address/list")
    public Result<?> addressList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Address> addresses = addressService.findByUserId(userId);
        return Result.success(addresses);
    }

    /**
     * 添加地址
     */
    @PostMapping("/address/add")
    public Result<?> addAddress(@RequestBody Address address, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        address.setUserId(userId);
        address.setId(null);

        // 如果是默认地址，取消其他默认
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            List<Address> addresses = addressService.findByUserId(userId);
            for (Address a : addresses) {
                if (a.getIsDefault() == 1) {
                    a.setIsDefault(0);
                    addressService.updateById(a);
                }
            }
        }

        addressService.save(address);
        return Result.success("添加成功", address);
    }

    /**
     * 更新地址
     */
    @PutMapping("/address/update/{id}")
    public Result<?> updateAddress(@PathVariable Long id, @RequestBody Address address, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Address existing = addressService.getById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error("地址不存在");
        }

        address.setId(id);
        address.setUserId(userId);

        // 如果设为默认地址，取消其他默认
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            List<Address> addresses = addressService.findByUserId(userId);
            for (Address a : addresses) {
                if (!a.getId().equals(id) && a.getIsDefault() == 1) {
                    a.setIsDefault(0);
                    addressService.updateById(a);
                }
            }
        }

        addressService.updateById(address);
        return Result.success("更新成功");
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/address/delete/{id}")
    public Result<?> deleteAddress(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Address address = addressService.getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return Result.error("地址不存在");
        }

        addressService.removeById(id);
        return Result.success("删除成功");
    }
}
