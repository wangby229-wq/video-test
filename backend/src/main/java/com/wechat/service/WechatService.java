package com.wechat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechat.config.WechatConfig;
import com.wechat.entity.User;
import com.wechat.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WechatService {

  @Autowired
  private WechatConfig wechatConfig;

  @Autowired
  private UserRepository userRepository;

  private final OkHttpClient httpClient = new OkHttpClient.Builder()
      .connectTimeout(10, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .build();

  private final ObjectMapper objectMapper = new ObjectMapper();

  private static String accessToken;
  private static Long tokenExpireTime;

  public String getAccessToken() throws Exception {
    if (accessToken != null && tokenExpireTime != null && System.currentTimeMillis() < tokenExpireTime) {
      return accessToken;
    }

    String url = wechatConfig.getTokenUrl() +
        "?corpid=" + wechatConfig.getId() +
        "&corpsecret=" + wechatConfig.getAgent().getSecret();

    Request request = new Request.Builder().url(url).build();

    try (Response response = httpClient.newCall(request).execute()) {
      String responseBody = response.body().string();
      JsonNode jsonNode = objectMapper.readTree(responseBody);

      if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() != 0) {
        throw new RuntimeException("获取access_token失败: " + jsonNode.get("errmsg").asText());
      }

      accessToken = jsonNode.get("access_token").asText();
      Integer expiresIn = jsonNode.get("expires_in").asInt();
      tokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000L;

      return accessToken;
    }
  }

  public User getUserInfo(String code) throws Exception {
    String accessToken = getAccessToken();

    String url = wechatConfig.getUserInfoUrl() +
        "?access_token=" + accessToken +
        "&code=" + code;

    Request request = new Request.Builder().url(url).build();

    try (Response response = httpClient.newCall(request).execute()) {
      String responseBody = response.body().string();
      JsonNode jsonNode = objectMapper.readTree(responseBody);

      if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() != 0) {
        throw new RuntimeException("获取用户信息失败: " + jsonNode.get("errmsg").asText());
      }

      String userId = jsonNode.get("UserId").asText();

      return getUserDetail(userId);
    }
  }

  public User getUserInfoByOAuth(String code) throws Exception {
    String accessToken = getAccessToken();

    // 【核心修复】接口换成 getuserinfo！！！ 这才是OAuth用的接口
    String userIdUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo" +
        "?access_token=" + accessToken +
        "&code=" + code;

    log.info("OAuth获取用户信息请求: {}", userIdUrl);

    // 【修复】这个接口是 GET 请求！直接拼参数即可，不需要POST
    Request request = new Request.Builder().url(userIdUrl).build();

    try (Response response = httpClient.newCall(request).execute()) {
      String responseBody = response.body().string();
      log.info("OAuth获取用户信息响应: {}", responseBody);

      JsonNode jsonNode = objectMapper.readTree(responseBody);

      if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() != 0) {
        throw new RuntimeException("invalid code, hint: " + jsonNode);
      }

      // 【修复】getuserinfo 接口返回的是 UserId 字段，和之前逻辑一致
      if (!jsonNode.has("UserId")) {
        if (jsonNode.has("OpenId")) {
          log.warn("获取到的是OpenId而不是UserId，用户可能不在通讯录中: {}", jsonNode.get("OpenId"));
          throw new RuntimeException("用户不在企业通讯录中，请联系管理员");
        }
        throw new RuntimeException("无法获取用户ID");
      }

      String userId = jsonNode.get("UserId").asText();
      log.info("OAuth获取到userId: {}", userId);

      return getUserDetail(userId);
    }
  }

  public User getUserDetail(String userId) throws Exception {
    String accessToken = getAccessToken();

    String url = wechatConfig.getUserDetailUrl() +
        "?access_token=" + accessToken +
        "&userid=" + userId;

    log.info("获取用户详情请求: {}", url);

    Request request = new Request.Builder().url(url).build();

    try (Response response = httpClient.newCall(request).execute()) {
      String responseBody = response.body().string();
      log.info("获取用户详情响应: {}", responseBody);

      JsonNode jsonNode = objectMapper.readTree(responseBody);

      if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() != 0) {
        throw new RuntimeException("获取用户详情失败: " + jsonNode.get("errmsg").asText());
      }

      User user = userRepository.findByUserId(userId).orElse(new User());

      user.setUserId(userId);
      user.setName(jsonNode.get("name").asText());

      if (jsonNode.has("avatar")) {
        user.setAvatar(jsonNode.get("avatar").asText());
      }

      if (jsonNode.has("mobile")) {
        user.setMobile(jsonNode.get("mobile").asText());
      }

      if (jsonNode.has("email")) {
        user.setEmail(jsonNode.get("email").asText());
      }

      if (jsonNode.has("department") && jsonNode.get("department").isArray() && jsonNode.get("department").size() > 0) {
        user.setDepartment(jsonNode.get("department").get(0).asText());
      }

      if (jsonNode.has("position") && !jsonNode.get("position").isNull()) {
        user.setPosition(jsonNode.get("position").asText());
      }

      return userRepository.save(user);
    }
  }

  public String getOAuthUrl(String redirectUri) {
    return "https://open.work.weixin.qq.com/connect/oauth2/authorize" +
        "?appid=" + wechatConfig.getId() +
        "&redirect_uri=" + redirectUri +
        "&response_type=code" +
        "&scope=snsapi_base" +
        "&state=STATE#wechat_redirect";
  }

  public String getQrConnectUrl(String redirectUri) {
    return wechatConfig.getQrConnectUrl() +
        "?appid=" + wechatConfig.getId() +
        "&agentid=" + wechatConfig.getAgent().getId() +
        "&redirect_uri=" + redirectUri +
        "&state=STATE";
  }

  public void sendFileMessage(String touser, String mediaId) throws Exception {
    String accessToken = getAccessToken();

    String url = wechatConfig.getSendMessageUrl() + "?access_token=" + accessToken;

    String json = String.format(
        "{\"touser\":\"%s\",\"msgtype\":\"file\",\"agentid\":%s,\"file\":{\"media_id\":\"%s\"}}",
        touser, wechatConfig.getAgent().getId(), mediaId);

    Request request = new Request.Builder()
        .url(url)
        .post(okhttp3.RequestBody.create(json, okhttp3.MediaType.parse("application/json")))
        .build();

    try (Response response = httpClient.newCall(request).execute()) {
      String responseBody = response.body().string();
      JsonNode jsonNode = objectMapper.readTree(responseBody);

      if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() != 0) {
        throw new RuntimeException("发送文件消息失败: " + jsonNode.get("errmsg").asText());
      }
    }
  }

  public void sendTextMessage(String touser, String content) throws Exception {
    String accessToken = getAccessToken();
    log.info("获取到access_token: {}", accessToken.substring(0, 20) + "...");

    String url = wechatConfig.getSendMessageUrl() + "?access_token=" + accessToken;
    log.info("发送消息URL: {}", url);

    String agentId = wechatConfig.getAgent().getId();
    log.info("Agent ID: {}", agentId);
    log.info("接收用户: {}", touser);
    log.info("消息内容: {}", content);

    String json = String.format(
        "{\"touser\":\"%s\",\"msgtype\":\"text\",\"agentid\":%s,\"text\":{\"content\":\"%s\"}}",
        touser, agentId, content);
    log.info("发送的JSON: {}", json);

    Request request = new Request.Builder()
        .url(url)
        .post(okhttp3.RequestBody.create(json, okhttp3.MediaType.parse("application/json")))
        .build();

    try (Response response = httpClient.newCall(request).execute()) {
      String responseBody = response.body().string();
      log.info("企业微信API响应: {}", responseBody);

      JsonNode jsonNode = objectMapper.readTree(responseBody);

      if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() != 0) {
        throw new RuntimeException("发送文本消息失败: " + jsonNode.get("errmsg").asText());
      }
      log.info("发送文本消息成功");
    }
  }

  public void sendLinkMessage(String touser, String title, String description, String url, String mediaId)
      throws Exception {
    String accessToken = getAccessToken();

    String urlStr = wechatConfig.getSendMessageUrl() + "?access_token=" + accessToken;

    String json;
    if (mediaId != null && !mediaId.isEmpty()) {
      json = String.format(
          "{\"touser\":\"%s\",\"msgtype\":\"link\",\"agentid\":%s,\"link\":{\"title\":\"%s\",\"description\":\"%s\",\"url\":\"%s\",\"picurl\":\"%s\"}}",
          touser, wechatConfig.getAgent().getId(), title, description, url, "");
    } else {
      json = String.format(
          "{\"touser\":\"%s\",\"msgtype\":\"link\",\"agentid\":%s,\"link\":{\"title\":\"%s\",\"description\":\"%s\",\"url\":\"%s\"}}",
          touser, wechatConfig.getAgent().getId(), title, description, url);
    }

    Request request = new Request.Builder()
        .url(urlStr)
        .post(okhttp3.RequestBody.create(json, okhttp3.MediaType.parse("application/json")))
        .build();

    try (Response response = httpClient.newCall(request).execute()) {
      String responseBody = response.body().string();
      JsonNode jsonNode = objectMapper.readTree(responseBody);

      if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() != 0) {
        throw new RuntimeException("发送链接消息失败: " + jsonNode.get("errmsg").asText());
      }
    }
  }

  public String uploadMedia(String filePath, String type) throws Exception {
    String accessToken = getAccessToken();

    String url = wechatConfig.getMediaUrl() +
        "?access_token=" + accessToken +
        "&type=" + type;

    java.io.File file = new java.io.File(filePath);
    okhttp3.RequestBody requestBody = new okhttp3.MultipartBody.Builder()
        .setType(okhttp3.MultipartBody.FORM)
        .addFormDataPart("media", file.getName(),
            okhttp3.RequestBody.create(file, okhttp3.MediaType.parse("application/octet-stream")))
        .build();

    Request request = new Request.Builder()
        .url(url)
        .post(requestBody)
        .build();

    try (Response response = httpClient.newCall(request).execute()) {
      String responseBody = response.body().string();
      JsonNode jsonNode = objectMapper.readTree(responseBody);

      if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() != 0) {
        throw new RuntimeException("上传临时素材失败: " + jsonNode.get("errmsg").asText());
      }

      return jsonNode.get("media_id").asText();
    }
  }

  public List<Map<String, String>> getCorpUsers() throws Exception {
    String accessToken = getAccessToken();
    String url = "https://qyapi.weixin.qq.com/cgi-bin/user/simplelist?access_token=" + accessToken
        + "&department_id=1&fetch_child=1";

    Request request = new Request.Builder().url(url).build();

    try (Response response = httpClient.newCall(request).execute()) {
      String responseBody = response.body().string();
      JsonNode jsonNode = objectMapper.readTree(responseBody);

      if (jsonNode.has("errcode") && jsonNode.get("errcode").asInt() != 0) {
        throw new RuntimeException("获取成员列表失败: " + jsonNode.get("errmsg").asText());
      }

      List<Map<String, String>> users = new java.util.ArrayList<>();
      if (jsonNode.has("userlist")) {
        JsonNode userlist = jsonNode.get("userlist");
        for (JsonNode user : userlist) {
          Map<String, String> userMap = new java.util.HashMap<>();
          userMap.put("userId", user.has("userid") ? user.get("userid").asText() : "");
          userMap.put("name", user.has("name") ? user.get("name").asText() : "");
          users.add(userMap);
        }
      }

      return users;
    }
  }
}
