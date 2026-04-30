package com.wechat.controller;

import com.wechat.entity.Device;
import com.wechat.entity.DeviceBinding;
import com.wechat.entity.User;
import com.wechat.service.DeviceService;
import com.wechat.service.JwtService;
import com.wechat.service.WechatService;
import com.wechat.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/devices")
public class DeviceController {

  @Autowired
  private DeviceService deviceService;

  @Autowired
  private JwtService jwtService;

  @Autowired
  private WechatService wechatService;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  @PostMapping("/register")
  public Result<Device> registerDevice(@RequestBody Map<String, String> params) {
    try {
      String deviceSerial = params.get("deviceSerial");
      String deviceName = params.get("deviceName");

      Device device = deviceService.registerDevice(deviceSerial, deviceName);
      return Result.success(device);
    } catch (Exception e) {
      return Result.error("注册失败: " + e.getMessage());
    }
  }

  @PostMapping("/bind")
  public Result<DeviceBinding> bindDevice(@RequestBody Map<String, Object> params) {
    try {
      String deviceSerial = (String) params.get("deviceSerial");
      Object userIdsObj = params.get("userIds");
      Object userNamesObj = params.get("userNames");

      String userIds = userIdsObj != null ? userIdsObj.toString() : "";
      String userNames = userNamesObj != null ? userNamesObj.toString() : "";

      DeviceBinding binding = deviceService.bindDevice(deviceSerial, userIds, userNames, "manual");
      return Result.success(binding);
    } catch (Exception e) {
      return Result.error("绑定失败: " + e.getMessage());
    }
  }

  @PostMapping("/unbind")
  public Result<Void> unbindDevice(@RequestBody Map<String, Object> params) {
    try {
      String deviceSerial = (String) params.get("deviceSerial");
      deviceService.unbindDevice(deviceSerial);
      return Result.success();
    } catch (Exception e) {
      return Result.error("解绑失败: " + e.getMessage());
    }
  }

  @PostMapping("/update-binding")
  public Result<DeviceBinding> updateBinding(@RequestBody Map<String, Object> params) {
    try {
      String deviceSerial = (String) params.get("deviceSerial");
      Object userIdsObj = params.get("userIds");
      Object userNamesObj = params.get("userNames");

      String userIds = userIdsObj != null ? userIdsObj.toString() : "";
      String userNames = userNamesObj != null ? userNamesObj.toString() : "";

      DeviceBinding binding = deviceService.updateBinding(deviceSerial, userIds, userNames);
      return Result.success(binding);
    } catch (Exception e) {
      return Result.error("更新绑定失败: " + e.getMessage());
    }
  }

  @GetMapping("/list")
  public Result<List<Device>> getAllDevices() {
    List<Device> devices = deviceService.getAllDevices();
    return Result.success(devices);
  }

  @GetMapping("/bindings")
  public Result<List<DeviceBinding>> getAllBindings() {
    List<DeviceBinding> bindings = deviceService.getAllBindings();
    return Result.success(bindings);
  }

  @GetMapping("/corp-users")
  public Result<List<Map<String, String>>> getCorpUsers() {
    try {
      List<Map<String, String>> users = wechatService.getCorpUsers();
      return Result.success(users);
    } catch (Exception e) {
      return Result.error("获取企业成员失败: " + e.getMessage());
    }
  }

  @GetMapping("/my-binding")
  public Result<DeviceBinding> getMyBinding(@RequestHeader("Authorization") String token) {
    try {
      String userId = jwtService.getUserIdFromToken(token);
      DeviceBinding binding = deviceService.getBindingByUserId(userId).orElse(null);
      return Result.success(binding);
    } catch (Exception e) {
      return Result.error("获取绑定信息失败: " + e.getMessage());
    }
  }

  @PostMapping("/heartbeat")
  public Result<Void> heartbeat(@RequestBody Map<String, String> params) {
    try {
      String deviceSerial = params.get("deviceSerial");
      deviceService.updateDeviceStatus(deviceSerial, "online");
      return Result.success();
    } catch (Exception e) {
      return Result.error("心跳更新失败: " + e.getMessage());
    }
  }

  @GetMapping("/qrcode-info")
  public Result<Map<String, Object>> getQrcodeInfo(@RequestParam String serial) {
    try {
      Device device = deviceService.getDeviceBySerial(serial).orElse(null);
      if (device == null) {
        return Result.error("设备不存在");
      }

      DeviceBinding binding = deviceService.getBindingByDeviceSerial(serial).orElse(null);

      Map<String, Object> result = new java.util.HashMap<>();
      result.put("deviceSerial", device.getDeviceSerial());
      result.put("deviceName", device.getDeviceName());
      result.put("deviceId", device.getDeviceId());
      result.put("status", device.getStatus());
      result.put("isBound", binding != null);
      if (binding != null) {
        result.put("bindTime", binding.getBindTime());
        result.put("userNames", binding.getUserNames());
      }

      return Result.success(result);
    } catch (Exception e) {
      return Result.error("获取设备信息失败: " + e.getMessage());
    }
  }

  @GetMapping("/qrcode-bind-url")
  public Result<Map<String, String>> getQrcodeBindUrl(@RequestParam String serial,
      @RequestParam(required = false) String redirectUri) {
    try {
      String baseUrl;
      if (redirectUri != null && !redirectUri.isEmpty()) {
        baseUrl = redirectUri;
      } else {
        baseUrl = frontendUrl + "/device-bind?serial=" + serial;
      }
      String oauthUrl = wechatService.getQrConnectUrl(baseUrl);
      Map<String, String> result = new java.util.HashMap<>();
      result.put("bindUrl", frontendUrl + "/device-bind?serial=" + serial);
      result.put("oauthUrl", oauthUrl);
      return Result.success(result);
    } catch (Exception e) {
      return Result.error("生成绑定URL失败: " + e.getMessage());
    }
  }

  @GetMapping("/oauth-url")
  public Result<Map<String, String>> getOAuthUrl(@RequestParam String serial) {
    try {
      String bindUrl = frontendUrl + "/device-bind?serial=" + serial;
      String encodedRedirectUri = java.net.URLEncoder.encode(bindUrl, "UTF-8");
      String oauthUrl = wechatService.getOAuthUrl(encodedRedirectUri);

      Map<String, String> result = new java.util.HashMap<>();
      result.put("oauthUrl", oauthUrl);
      result.put("bindUrl", bindUrl);
      return Result.success(result);
    } catch (Exception e) {
      return Result.error("生成授权URL失败: " + e.getMessage());
    }
  }

  @PostMapping("/qrcode-bind")
  public Result<DeviceBinding> qrcodeBind(@RequestParam String serial,
      @RequestParam(required = false) String code) {
    try {
      String userId;
      String userName;

      if (code != null && !code.isEmpty()) {
        User user = wechatService.getUserInfoByOAuth(code);
        userId = user.getUserId();
        userName = user.getName();
      } else {
        return Result.error("用户身份验证失败");
      }

      Device device = deviceService.getDeviceBySerial(serial).orElse(null);
      if (device == null) {
        device = deviceService.registerDevice(serial, "设备_" + serial);
      }

      DeviceBinding binding = deviceService.bindDevice(serial, userId, userName, "qrcode");

      return Result.success(binding);
    } catch (Exception e) {
      log.error("绑定异常: ", e);
      String errorMsg = e.getMessage();
      if (errorMsg == null || errorMsg.isEmpty()) {
        errorMsg = "未知错误";
      }
      return Result.error("绑定失败: " + errorMsg);
    }
  }
}
