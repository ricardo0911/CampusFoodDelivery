package com.campus.ordering.controller;

import com.campus.ordering.dto.LoginDTO;
import com.campus.ordering.dto.RegisterDTO;
import com.campus.ordering.entity.Merchant;
import com.campus.ordering.entity.Shop;
import com.campus.ordering.entity.User;
import com.campus.ordering.service.MerchantService;
import com.campus.ordering.service.ShopService;
import com.campus.ordering.service.UserService;
import com.campus.ordering.util.JwtUtil;
import com.campus.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO dto) {
        String userType = dto.getUserType();

        if ("user".equals(userType) || "admin".equals(userType)) {
            User user = userService.findByUsername(dto.getUsername());
            if (user == null) {
                return Result.error("用户不存在");
            }
            if (!dto.getPassword().equals(user.getPassword())) {
                return Result.error("密码错误");
            }
            if (user.getStatus() == 0) {
                return Result.error("账号已被禁用");
            }
            if ("admin".equals(userType) && user.getRole() != 1) {
                return Result.error("无管理员权限");
            }

            String token = jwtUtil.generateToken(user.getId(), userType);
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            user.setPassword(null);
            return Result.success("登录成功", data);

        } else if ("merchant".equals(userType)) {
            Merchant merchant = merchantService.findByUsername(dto.getUsername());
            if (merchant == null) {
                return Result.error("商家账号不存在");
            }
            if (!dto.getPassword().equals(merchant.getPassword())) {
                return Result.error("密码错误");
            }
            if (merchant.getStatus() == 0) {
                return Result.error("账号审核中，请耐心等待");
            }
            if (merchant.getStatus() == 2) {
                return Result.error("账号审核未通过：" + merchant.getRejectReason());
            }
            if (merchant.getStatus() == 3) {
                return Result.error("账号已被禁用");
            }

            Shop shop = shopService.findByMerchantId(merchant.getId());

            String token = jwtUtil.generateToken(merchant.getId(), "merchant");
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("merchant", merchant);
            data.put("shop", shop);
            merchant.setPassword(null);
            return Result.success("登录成功", data);
        }

        return Result.error("未知的用户类型");
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterDTO dto) {
        if (userService.findByUsername(dto.getUsername()) != null) {
            return Result.error("用户名已存在");
        }
        if (dto.getPhone() != null && userService.findByPhone(dto.getPhone()) != null) {
            return Result.error("手机号已被注册");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setStudentId(dto.getStudentId());
        user.setRole(0);
        user.setStatus(1);

        userService.save(user);
        user.setPassword(null);

        String token = jwtUtil.generateToken(user.getId(), "user");
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        return Result.success("注册成功", data);
    }

    /**
     * 商家注册
     */
    @PostMapping("/merchant/register")
    public Result<?> merchantRegister(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String contactName = params.get("contactName");
        String phone = params.get("phone");
        String shopName = params.get("shopName");

        if (merchantService.findByUsername(username) != null) {
            return Result.error("商家用户名已存在");
        }

        Merchant merchant = new Merchant();
        merchant.setUsername(username);
        merchant.setPassword(password);
        merchant.setContactName(contactName);
        merchant.setPhone(phone);
        merchant.setStatus(0); // 待审核
        merchantService.save(merchant);

        // 创建店铺
        Shop shop = new Shop();
        shop.setMerchantId(merchant.getId());
        shop.setName(shopName != null ? shopName : contactName + "的店铺");
        shop.setStatus(0); // 休息中
        shopService.save(shop);

        return Result.success("注册成功，等待管理员审核");
    }
}
