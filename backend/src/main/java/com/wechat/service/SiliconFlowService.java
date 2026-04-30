package com.wechat.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Service
public class SiliconFlowService {

  private static final Logger log = LoggerFactory.getLogger(SiliconFlowService.class);

  @Value("${siliconflow.api-key}")
  private String apiKey;

  @Value("${siliconflow.base-url}")
  private String baseUrl;

  @Value("${siliconflow.asr-model}")
  private String asrModel;

  @Value("${siliconflow.summary-model}")
  private String summaryModel;

  public String recognizeSpeech(String filePath) throws IOException {
    log.info("使用硅基流动开始语音识别，文件路径: {}", filePath);

    File file = new File(filePath);
    if (!file.exists()) {
      throw new IOException("文件不存在: " + filePath);
    }

    String url = baseUrl + "/v1/audio/transcriptions";
    log.info("语音识别API地址: {}", url);

    String boundary = "----WebKitFormBoundary" + UUID.randomUUID().toString();

    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);
    conn.setRequestProperty("Authorization", "Bearer " + apiKey);
    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
    conn.setConnectTimeout(30000);
    conn.setReadTimeout(120000);

    try (OutputStream os = conn.getOutputStream()) {
      StringBuilder body = new StringBuilder();

      body.append("--").append(boundary).append("\r\n");
      body.append("Content-Disposition: form-data; name=\"model\"\r\n\r\n");
      body.append(asrModel).append("\r\n");

      body.append("--").append(boundary).append("\r\n");
      body.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
          .append(file.getName()).append("\"\r\n");

      String contentType = getContentType(filePath);
      body.append("Content-Type: ").append(contentType).append("\r\n\r\n");

      os.write(body.toString().getBytes(StandardCharsets.UTF_8));

      try (FileInputStream fis = new FileInputStream(file)) {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
          os.write(buffer, 0, bytesRead);
        }
      }

      os.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
    }

    int responseCode = conn.getResponseCode();
    log.info("语音识别响应码: {}", responseCode);

    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(
            responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream(),
            StandardCharsets.UTF_8))) {
      StringBuilder response = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        response.append(line);
      }

      String responseStr = response.toString();
      log.info("语音识别响应: {}", responseStr);

      JSONObject json = new JSONObject(responseStr);

      if (json.has("text")) {
        String text = json.getString("text");
        log.info("语音识别成功，识别文本长度: {} 字符", text.length());
        return text;
      } else if (json.has("error")) {
        String error = json.getJSONObject("error").optString("message", "未知错误");
        log.error("语音识别失败: {}", error);
        throw new IOException("语音识别失败: " + error);
      } else {
        log.error("语音识别响应格式异常: {}", responseStr);
        throw new IOException("语音识别响应格式异常: " + responseStr);
      }
    }
  }

  public String generateSummary(String transcript, String meetingTitle) throws IOException {
    log.info("使用硅基流动生成摘要，原始文本长度: {} 字符", transcript.length());

    String url = baseUrl + "/v1/chat/completions";
    log.info("AI摘要API地址: {}", url);

    JSONObject requestBody = new JSONObject();
    requestBody.put("model", summaryModel);

    JSONObject systemMessage = new JSONObject();
    systemMessage.put("role", "system");
    systemMessage.put("content", "你是一个专业的会议记录助手，负责从会议录音转写的文本中提取关键信息，生成HTML格式的会议摘要。要求：\n" +
        "1. 直接输出HTML代码，不要加任何markdown标记\n" +
        "2. 使用<b>标签加粗标题\n" +
        "3. 使用<ol>和<li>标签创建有序列表\n" +
        "4. 使用<ul>和<li>标签创建无序列表\n" +
        "5. 使用<br>标签换行\n" +
        "6. 不要使用**或#等markdown符号\n" +
        "7. 摘要内容用中文回复\n" +
        "8. 不要输出任何解释说明，直接输出HTML代码");

    JSONObject userMessage = new JSONObject();
    userMessage.put("role", "user");
    String content = String.format("会议标题：%s\n\n会议录音转写内容：\n%s\n\n请根据以上内容生成会议摘要。",
        meetingTitle, transcript);
    userMessage.put("content", content);

    requestBody.put("messages", new org.json.JSONArray().put(systemMessage).put(userMessage));

    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);
    conn.setRequestProperty("Authorization", "Bearer " + apiKey);
    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

    try (OutputStream os = conn.getOutputStream()) {
      os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
    }

    int responseCode = conn.getResponseCode();
    log.info("AI摘要响应码: {}", responseCode);

    try (BufferedReader br = new BufferedReader(
        new InputStreamReader(
            responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream(),
            StandardCharsets.UTF_8))) {
      StringBuilder response = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) {
        response.append(line);
      }

      String responseStr = response.toString();
      log.info("AI摘要响应: {}", responseStr);

      JSONObject json = new JSONObject(responseStr);

      if (json.has("choices") && json.getJSONArray("choices").length() > 0) {
        JSONObject choice = json.getJSONArray("choices").getJSONObject(0);
        JSONObject message = choice.getJSONObject("message");
        String summary = message.getString("content");
        log.info("AI摘要生成成功，摘要长度: {} 字符", summary.length());
        return summary;
      } else if (json.has("error")) {
        String error = json.getJSONObject("error").optString("message", "未知错误");
        log.error("AI摘要生成失败: {}", error);
        throw new IOException("AI摘要生成失败: " + error);
      } else {
        log.error("AI摘要响应格式异常: {}", responseStr);
        throw new IOException("AI摘要响应格式异常: " + responseStr);
      }
    }
  }

  public String processAudioAndGenerateSummary(String filePath, String meetingTitle) throws IOException {
    String transcript = recognizeSpeech(filePath);

    if (transcript == null || transcript.trim().isEmpty()) {
      log.warn("语音识别结果为空");
      return "语音识别结果为空，无法生成摘要。";
    }

    String summary = generateSummary(transcript, meetingTitle);
    return summary;
  }

  public String processAudioAndGenerateSummary(String filePath) throws IOException {
    return processAudioAndGenerateSummary(filePath, "未知会议");
  }

  private String getContentType(String filePath) {
    if (filePath.endsWith(".mp3")) {
      return "audio/mpeg";
    } else if (filePath.endsWith(".wav")) {
      return "audio/wav";
    } else if (filePath.endsWith(".m4a")) {
      return "audio/mp4";
    } else if (filePath.endsWith(".amr")) {
      return "audio/amr";
    } else if (filePath.endsWith(".ogg")) {
      return "audio/ogg";
    } else {
      return "application/octet-stream";
    }
  }
}
