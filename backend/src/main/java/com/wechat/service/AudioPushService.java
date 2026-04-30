package com.wechat.service;

import com.wechat.entity.DeviceBinding;
import com.wechat.entity.Meeting;
import com.wechat.entity.PushRecord;
import com.wechat.repository.MeetingRepository;
import com.wechat.repository.PushRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AudioPushService {

  @Autowired
  private MeetingRepository meetingRepository;

  @Autowired
  private PushRecordRepository pushRecordRepository;

  @Autowired
  private DeviceService deviceService;

  @Autowired
  private WechatService wechatService;

  @Value("${file.upload-path}")
  private String uploadPath;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  @Transactional
  public void pushAudioToBindingUser(String meetingId) {
    log.info("========== 开始推送消息 ==========");
    log.info("会议ID: {}", meetingId);

    Meeting meeting = meetingRepository.findByMeetingId(meetingId)
        .orElseThrow(() -> new RuntimeException("会议不存在"));

    if (meeting.getDeviceSerial() == null || meeting.getDeviceSerial().isEmpty()) {
      log.warn("会议 {} 没有关联设备", meetingId);
      return;
    }

    DeviceBinding binding = deviceService.getBindingByDeviceSerial(meeting.getDeviceSerial())
        .orElse(null);

    if (binding == null) {
      log.warn("设备 {} 未绑定用户", meeting.getDeviceSerial());
      return;
    }

    String userIds = binding.getUserIds();
    log.info("绑定用户IDs: {}", userIds);

    if (userIds != null && !userIds.isEmpty()) {
      for (String userId : userIds.split(",")) {
        if (userId != null && !userId.trim().isEmpty()) {
          log.info("准备推送给用户: {}", userId);
          pushAudioToUser(meetingId, userId.trim());
        }
      }
    }

    log.info("========== 推送完成 ==========");
  }

  @Transactional
  public Map<String, Object> pushAudioToUser(String meetingId, String userId) {
    log.info(">>> pushAudioToUser 被调用 - meetingId: {}, userId: {}", meetingId, userId);

    Meeting meeting = meetingRepository.findByMeetingId(meetingId)
        .orElseThrow(() -> new RuntimeException("会议不存在"));

    PushRecord pushRecord = new PushRecord();
    pushRecord.setMeetingId(meetingId);
    pushRecord.setUserId(userId);

    Map<String, Object> result = new HashMap<>();
    result.put("meetingId", meetingId);
    result.put("userId", userId);
    result.put("status", "failed");

    try {
      log.info("开始推送录音文件，会议ID: {}, 用户ID: {}", meetingId, userId);

      String linkUrl = frontendUrl + "/meetings/" + meetingId;
      String content = "会议录音通知\n" +
          "会议标题：" + meeting.getTitle() + "\n" +
          "点击查看详情：" + linkUrl;

      log.info("准备发送文本消息，内容: {}", content);
      wechatService.sendTextMessage(userId, content);

      pushRecord.setPushStatus("success");
      result.put("status", "success");
      log.info("录音文件推送成功，会议ID: {}, 用户ID: {}", meetingId, userId);
    } catch (Exception e) {
      pushRecord.setPushStatus("failed");
      pushRecord.setErrorMessage(e.getMessage());
      result.put("errorMessage", e.getMessage());
      log.error("录音文件推送失败: {}", e.getMessage(), e);
    }

    pushRecordRepository.save(pushRecord);
    return result;
  }

  @Transactional
  public Map<String, Object> pushAudioToUsers(String meetingId, List<String> userIds) {
    Map<String, Object> result = new HashMap<>();
    result.put("meetingId", meetingId);
    result.put("total", userIds.size());
    result.put("success", 0);
    result.put("failed", 0);
    result.put("details", new java.util.ArrayList<Map<String, Object>>());

    for (String userId : userIds) {
      try {
        Map<String, Object> userResult = pushAudioToUser(meetingId, userId);
        ((java.util.List<Map<String, Object>>) result.get("details")).add(userResult);

        if ("success".equals(userResult.get("status"))) {
          result.put("success", (Integer) result.get("success") + 1);
        } else {
          result.put("failed", (Integer) result.get("failed") + 1);
        }
      } catch (Exception e) {
        log.error("推送给用户 {} 失败: {}", userId, e.getMessage());
        Map<String, Object> userResult = new HashMap<>();
        userResult.put("userId", userId);
        userResult.put("status", "failed");
        userResult.put("errorMessage", e.getMessage());
        ((java.util.List<Map<String, Object>>) result.get("details")).add(userResult);
        result.put("failed", (Integer) result.get("failed") + 1);
      }
    }

    return result;
  }

  @Transactional
  public void pushAudioToUserName(String meetingId, String userName) {
    // 这里简化处理，直接使用userName作为userId
    // 实际情况中，需要根据userName查询对应的企业微信用户ID
    pushAudioToUser(meetingId, userName);
  }

  public List<PushRecord> getPushRecordsByMeeting(String meetingId) {
    return pushRecordRepository.findByMeetingId(meetingId);
  }

  public List<PushRecord> getPushRecordsByUser(String userId) {
    return pushRecordRepository.findByUserId(userId);
  }
}
