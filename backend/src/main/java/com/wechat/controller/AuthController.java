package com.wechat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechat.entity.User;
import com.wechat.service.JwtService;
import com.wechat.service.WechatService;
import com.wechat.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  @Autowired
  private WechatService wechatService;

  @Autowired
  private JwtService jwtService;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @GetMapping("/wechat/oauth-url")
  public Result<Map<String, String>> getOAuthUrl(@RequestParam String redirectUri) {
    String oauthUrl = wechatService.getOAuthUrl(redirectUri);
    Map<String, String> data = new HashMap<>();
    data.put("oauthUrl", oauthUrl);
    return Result.success(data);
  }

  @GetMapping("/wechat/qr-url")
  public Result<Map<String, String>> getQrConnectUrl(@RequestParam String redirectUri) {
    String qrUrl = wechatService.getQrConnectUrl(redirectUri);
    Map<String, String> data = new HashMap<>();
    data.put("qrUrl", qrUrl);
    return Result.success(data);
  }

  @GetMapping("/wechat/callback")
  public void wechatCallback(@RequestParam String code, HttpServletResponse response) {
    try {
      User user = wechatService.getUserInfo(code);
      String token = jwtService.generateToken(user.getUserId());

      // 重定向到前端登录页，并携带token和用户信息
      String userJson = objectMapper.writeValueAsString(user);
      String encodedUser = java.net.URLEncoder.encode(userJson, "UTF-8");
      String redirectUrl = String.format(
          frontendUrl + "/login?token=%s&user=%s",
          token, encodedUser);

      response.sendRedirect(redirectUrl);
    } catch (Exception e) {
      try {
        response.sendRedirect(
            frontendUrl + "/login?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  @GetMapping("/wechat/silent-login")
  public Result<Map<String, Object>> wechatSilentLogin(@RequestParam String code) {
    try {
      User user = wechatService.getUserInfo(code);
      String token = jwtService.generateToken(user.getUserId());

      Map<String, Object> data = new HashMap<>();
      data.put("token", token);
      data.put("user", user);

      return Result.success(data);
    } catch (Exception e) {
      return Result.error("静默登录失败: " + e.getMessage());
    }
  }

  @GetMapping("/user/info")
  public Result<User> getUserInfo(@RequestHeader("Authorization") String token) {
    try {
      String userId = jwtService.getUserIdFromToken(token);
      User user = wechatService.getUserDetail(userId);
      return Result.success(user);
    } catch (Exception e) {
      return Result.error("获取用户信息失败: " + e.getMessage());
    }
  }

  @GetMapping("/user-info")
  public Result<User> getUserInfoByCode(@RequestParam String code) {
    try {
      User user = wechatService.getUserInfoByOAuth(code);
      return Result.success(user);
    } catch (Exception e) {
      return Result.error("获取用户信息失败: " + e.getMessage());
    }
  }

  @PostMapping("/logout")
  public Result<Void> logout() {
    return Result.success();
  }

  @GetMapping("/mock-login")
  public Result<Map<String, Object>> mockLogin() {
    try {
      // 创建模拟用户
      User user = new User();
      user.setUserId("mock_user_001");
      user.setName("测试用户");
      user.setEmail("test@example.com");
      user.setMobile("13800138000");
      user.setDepartment("技术部");
      user.setCreateTime(new java.util.Date());

      String token = jwtService.generateToken(user.getUserId());

      Map<String, Object> data = new HashMap<>();
      data.put("token", token);
      data.put("user", user);

      return Result.success(data);
    } catch (Exception e) {
      return Result.error("登录失败: " + e.getMessage());
    }
  }
}
