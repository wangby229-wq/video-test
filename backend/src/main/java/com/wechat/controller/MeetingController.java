package com.wechat.controller;

import com.wechat.entity.Meeting;
import com.wechat.entity.MeetingTag;
import com.wechat.entity.Transcript;
import com.wechat.service.AudioPushService;
import com.wechat.service.DeviceService;
import com.wechat.service.SiliconFlowService;
import com.wechat.service.JwtService;
import com.wechat.service.MeetingService;
import com.wechat.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

  @Autowired
  private MeetingService meetingService;

  @Autowired
  private DeviceService deviceService;

  @Autowired
  private JwtService jwtService;

  @Autowired
  private AudioPushService audioPushService;

  @Autowired
  private SiliconFlowService siliconFlowService;

  @Value("${file.upload-path}")
  private String uploadPath;

  @GetMapping("/list")
  public Result<Map<String, Object>> getMeetingList(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestHeader(value = "Authorization", required = false) String token) {
    try {
      // 暂时不按userId过滤，显示所有会议记录
      // String userId = "QianChen";
      // if (token != null) {
      // try {
      // userId = jwtService.getUserIdFromToken(token);
      // } catch (Exception e) {
      // // Token解析失败，使用默认用户
      // }
      // }
      Page<Meeting> meetingPage = meetingService.getAllMeetings(keyword, status, page, size);

      Map<String, Object> data = new HashMap<>();
      data.put("list", meetingPage.getContent());
      data.put("total", meetingPage.getTotalElements());
      data.put("page", page);
      data.put("size", size);

      return Result.success(data);
    } catch (Exception e) {
      return Result.error("获取会议列表失败: " + e.getMessage());
    }
  }

  @GetMapping("/detail")
  public Result<Map<String, Object>> getMeetingDetail(@RequestParam String meetingId) {
    try {
      Meeting meeting = meetingService.getMeetingById(meetingId)
          .orElseThrow(() -> new RuntimeException("会议不存在"));

      List<MeetingTag> tags = meetingService.getMeetingTags(meetingId);
      List<Transcript> transcripts = meetingService.getMeetingTranscripts(meetingId);

      Map<String, Object> data = new HashMap<>();
      data.put("meeting", meeting);
      data.put("tags", tags);
      data.put("transcripts", transcripts);

      return Result.success(data);
    } catch (Exception e) {
      return Result.error("获取会议详情失败: " + e.getMessage());
    }
  }

  @PostMapping("/create")
  public Result<Meeting> createMeeting(@RequestBody Map<String, Object> params,
      @RequestHeader(value = "Authorization", required = false) String token) {
    try {
      String userId = "QianChen";
      if (token != null) {
        try {
          userId = jwtService.getUserIdFromToken(token);
        } catch (Exception e) {
          // Token解析失败，使用默认用户
        }
      }

      Meeting meeting = new Meeting();
      meeting.setTitle((String) params.get("title"));
      meeting.setMeetingDate(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").parse((String) params.get("date")));
      meeting.setUserId(userId);

      if (params.containsKey("deviceSerial")) {
        meeting.setDeviceSerial((String) params.get("deviceSerial"));
      }

      List<String> tags = (List<String>) params.get("tags");
      Meeting savedMeeting = meetingService.createMeeting(meeting, tags);

      return Result.success(savedMeeting);
    } catch (Exception e) {
      return Result.error("创建会议失败: " + e.getMessage());
    }
  }

  @PostMapping("/upload-audio")
  public Result<Map<String, String>> uploadAudio(@RequestParam("file") MultipartFile file,
      @RequestParam("meetingId") String meetingId,
      @RequestParam(value = "title", required = false) String title,
      @RequestParam(value = "date", required = false) String date,
      @RequestParam(value = "deviceSerial", required = false) String deviceSerial,
      @RequestParam(value = "duration", required = false) String duration) {
    try {
      log.info("========== 收到录音上传请求 ==========");
      log.info("meetingId: {}", meetingId);
      log.info("title: {}", title);
      log.info("date: {}", date);
      log.info("deviceSerial: {}", deviceSerial);
      log.info("duration: {}", duration);
      log.info("file.getName(): {}", file.getName());
      log.info("file.getOriginalFilename(): {}", file.getOriginalFilename());
      log.info("file.getSize(): {}", file.getSize());
      log.info("file.getContentType(): {}", file.getContentType());
      log.info("uploadPath: {}", uploadPath);

      String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
      String filePath = uploadPath + fileName;
      log.info("Full file path: {}", filePath);

      File dest = new File(filePath);
      log.info("Dest file: {}", dest.getAbsolutePath());
      log.info("Parent directory exists: {}", dest.getParentFile().exists());

      if (!dest.getParentFile().exists()) {
        log.info("Creating parent directory: {}", dest.getParentFile().getAbsolutePath());
        boolean created = dest.getParentFile().mkdirs();
        log.info("Directory created: {}", created);
      }

      log.info("Transferring file...");
      file.transferTo(dest);
      log.info("File transferred successfully");

      String audioUrl = "/uploads/" + fileName;
      log.info("Audio URL: {}", audioUrl);
      meetingService.updateMeetingAudio(meetingId, fileName, audioUrl);
      log.info("Meeting audio updated successfully");

      log.info("自动更新会议状态为completed，触发推送...");
      meetingService.updateMeetingStatus(meetingId, "completed");
      log.info("会议状态更新完成，推送已触发");

      // 暂时注释掉自动生成摘要，用户可以手动点击按钮生成
      // try {
      // log.info("开始调用硅基流动AI服务生成会议摘要，文件路径: {}", filePath);
      // Meeting meeting = meetingService.getMeetingById(meetingId)
      // .orElseThrow(() -> new RuntimeException("会议不存在"));
      // log.info("会议标题: {}", meeting.getTitle());
      // String summary = siliconFlowService.processAudioAndGenerateSummary(filePath,
      // meeting.getTitle());
      // meetingService.updateMeetingSummary(meetingId, summary);
      // log.info("会议摘要生成成功: {}", summary);
      // } catch (Exception e) {
      // log.error("会议摘要生成失败: {}", e.getMessage(), e);
      // // 摘要生成失败不影响上传流程
      // }

      Map<String, String> data = new HashMap<>();
      data.put("audioUrl", audioUrl);
      data.put("filePath", fileName);

      log.info("========== 上传成功 ==========");
      return Result.success(data);
    } catch (IOException e) {
      log.error("========== 上传失败 ==========");
      log.error("meetingId: {}", meetingId);
      log.error("错误类型: {}", e.getClass().getName());
      log.error("错误信息: {}", e.getMessage());
      log.error("uploadPath: {}", uploadPath);
      log.error("堆栈跟踪:", e);
      e.printStackTrace();
      return Result.error("上传失败: " + e.getMessage());
    }
  }

  @PostMapping("/push-audio")
  public Result<Map<String, Object>> pushAudio(@RequestParam(required = false) String meetingId,
      @RequestBody(required = false) Map<String, Object> params) {
    try {
      log.info("push-audio接口开始处理");
      String meetingIdToUse = meetingId;

      if (meetingIdToUse == null && params != null) {
        meetingIdToUse = (String) params.get("meetingId");
      }

      if (meetingIdToUse == null) {
        return Result.error("会议ID不能为空");
      }

      Meeting meeting = meetingService.getMeetingById(meetingIdToUse).orElse(null);
      List<String> userIds = null;

      if (params != null && params.containsKey("userIds")) {
        userIds = (List<String>) params.get("userIds");
      }

      if ((userIds == null || userIds.isEmpty()) && meeting != null && meeting.getDeviceSerial() != null) {
        Optional<com.wechat.entity.DeviceBinding> binding = deviceService
            .getBindingByDeviceSerial(meeting.getDeviceSerial());
        if (binding.isPresent()) {
          String userIdsStr = binding.get().getUserIds();
          if (userIdsStr != null && !userIdsStr.isEmpty()) {
            userIds = new ArrayList<>();
            for (String uid : userIdsStr.split(",")) {
              if (uid != null && !uid.trim().isEmpty()) {
                userIds.add(uid.trim());
              }
            }
          }
        }
      }

      if (userIds == null || userIds.isEmpty()) {
        return Result.error("没有找到可推送的用户，请先绑定设备");
      }

      log.info("准备推送录音文件，会议ID: {}, 用户列表: {}", meetingIdToUse, userIds);

      Map<String, Object> result;
      if (userIds.size() == 1) {
        result = audioPushService.pushAudioToUser(meetingIdToUse, userIds.get(0));
      } else {
        result = audioPushService.pushAudioToUsers(meetingIdToUse, userIds);
      }

      return Result.success(result);
    } catch (Exception e) {
      log.error("推送录音文件失败: {}", e.getMessage(), e);
      return Result.error("推送录音文件失败: " + e.getMessage());
    }
  }

  @PostMapping("/update-status")
  public Result<String> updateStatus(@RequestBody Map<String, Object> params) {
    try {
      String meetingId = (String) params.get("meetingId");
      String status = (String) params.get("status");

      if (meetingId == null || status == null) {
        return Result.error("会议ID和状态不能为空");
      }

      meetingService.updateMeetingStatus(meetingId, status);
      return Result.success("状态更新成功");
    } catch (Exception e) {
      return Result.error("状态更新失败: " + e.getMessage());
    }
  }

  @GetMapping("/uploads/{filename:.+}")
  public Resource serveFile(@PathVariable String filename) {
    try {
      File file = new File(uploadPath + filename);
      Resource resource = new FileSystemResource(file);
      if (resource.exists() || resource.isReadable()) {
        return resource;
      } else {
        throw new RuntimeException("Could not read the file!");
      }
    } catch (Exception e) {
      throw new RuntimeException("Error: " + e.getMessage());
    }
  }

  @GetMapping("/files")
  public Result<List<Map<String, String>>> listUploadedFiles() {
    List<Map<String, String>> files = new ArrayList<>();
    File uploadDir = new File(uploadPath);
    if (uploadDir.exists() && uploadDir.isDirectory()) {
      File[] fileList = uploadDir.listFiles();
      if (fileList != null) {
        for (File file : fileList) {
          Map<String, String> fileInfo = new HashMap<>();
          fileInfo.put("name", file.getName());
          fileInfo.put("url", MvcUriComponentsBuilder
              .fromMethodName(MeetingController.class, "serveFile", file.getName()).build().toString());
          files.add(fileInfo);
        }
      }
    }
    return Result.success(files);
  }

  @PostMapping("/generate-summary")
  public Result<Map<String, String>> generateSummary(@RequestParam String meetingId) {
    try {
      log.info("开始异步生成会议摘要，会议ID: {}", meetingId);

      Meeting meeting = meetingService.getMeetingById(meetingId)
          .orElseThrow(() -> new RuntimeException("会议不存在"));

      if (meeting.getAudioFilePath() == null || meeting.getAudioFilePath().isEmpty()) {
        return Result.error("会议没有音频文件");
      }

      if ("generating".equals(meeting.getSummaryStatus())) {
        return Result.error("摘要正在生成中，请稍候");
      }

      String filePath = uploadPath + meeting.getAudioFilePath();
      meetingService.updateMeetingSummaryStatus(meetingId, "generating");

      new Thread(() -> {
        try {
          log.info("后台线程开始生成摘要，文件路径: {}", filePath);
          String summary = siliconFlowService.processAudioAndGenerateSummary(filePath, meeting.getTitle());
          meetingService.updateMeetingSummary(meetingId, summary);
          meetingService.updateMeetingSummaryStatus(meetingId, "completed");
          log.info("会议摘要生成成功");
        } catch (Exception e) {
          log.error("会议摘要生成失败: {}", e.getMessage(), e);
          meetingService.updateMeetingSummaryStatus(meetingId, "failed");
        }
      }).start();

      Map<String, String> data = new HashMap<>();
      data.put("status", "generating");
      data.put("message", "摘要生成已开始，请稍候");

      return Result.success(data);
    } catch (Exception e) {
      log.error("生成摘要失败: {}", e.getMessage(), e);
      return Result.error("生成摘要失败: " + e.getMessage());
    }
  }

  @GetMapping("/summary-status")
  public Result<Map<String, Object>> getSummaryStatus(@RequestParam String meetingId) {
    try {
      Meeting meeting = meetingService.getMeetingById(meetingId)
          .orElseThrow(() -> new RuntimeException("会议不存在"));

      Map<String, Object> data = new HashMap<>();
      data.put("status", meeting.getSummaryStatus());
      data.put("summary", meeting.getSummary());

      return Result.success(data);
    } catch (Exception e) {
      log.error("查询摘要状态失败: {}", e.getMessage(), e);
      return Result.error("查询摘要状态失败: " + e.getMessage());
    }
  }

  @PostMapping("/delete")
  public Result<String> deleteMeeting(@RequestBody Map<String, String> params) {
    try {
      String meetingId = params.get("meetingId");

      if (meetingId == null || meetingId.isEmpty()) {
        return Result.error("会议ID不能为空");
      }

      log.info("删除会议: {}", meetingId);
      meetingService.deleteMeeting(meetingId);

      return Result.success("删除成功");
    } catch (Exception e) {
      log.error("删除会议失败: {}", e.getMessage(), e);
      return Result.error("删除会议失败: " + e.getMessage());
    }
  }
}
