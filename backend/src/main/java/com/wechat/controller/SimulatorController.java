package com.wechat.controller;

import com.wechat.service.AudioPushService;
import com.wechat.service.DeviceService;
import com.wechat.service.MeetingService;
import com.wechat.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/simulator")
public class SimulatorController {

  @Autowired
  private DeviceService deviceService;

  @Autowired
  private MeetingService meetingService;

  @Autowired
  private AudioPushService audioPushService;

  @PostMapping("/heartbeat")
  public Result<Map<String, Object>> heartbeat(@RequestBody Map<String, String> params) {
    try {
      String deviceSerial = params.get("deviceSerial");

      deviceService.updateDeviceStatus(deviceSerial, "online");

      Map<String, Object> result = new HashMap<>();
      result.put("deviceSerial", deviceSerial);
      result.put("status", "online");
      result.put("timestamp", new Date());

      return Result.success(result);
    } catch (Exception e) {
      log.error("心跳失败: {}", e.getMessage(), e);
      return Result.error("心跳失败: " + e.getMessage());
    }
  }

  @PostMapping("/simulate-recording")
  public Result<Map<String, Object>> simulateRecording(@RequestBody Map<String, Object> params) {
    try {
      String deviceSerial = (String) params.get("deviceSerial");
      String title = (String) params.getOrDefault("title", "模拟会议_" + System.currentTimeMillis());
      Integer duration = (Integer) params.getOrDefault("duration", 30);

      log.info("开始模拟录音推送 - 设备: {}, 标题: {}, 时长: {}分钟", deviceSerial, title, duration);

      Map<String, Object> result = new HashMap<>();

      result.put("deviceSerial", deviceSerial);
      result.put("title", title);
      result.put("duration", duration);
      result.put("simulatedAt", new Date());

      return Result.success(result);
    } catch (Exception e) {
      log.error("模拟失败: {}", e.getMessage(), e);
      return Result.error("模拟失败: " + e.getMessage());
    }
  }
}
