package com.wechat.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "meeting")
public class Meeting {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "meeting_id", unique = true, nullable = false, length = 64)
  private String meetingId;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(name = "meeting_date", nullable = false)
  private Date meetingDate;

  @Column(length = 50)
  private String duration;

  private Integer participants = 0;

  @Column(length = 20)
  private String status = "processing";

  @Column(columnDefinition = "TEXT")
  private String summary;

  @Column(name = "summary_status", length = 20)
  private String summaryStatus = "idle";

  @Column(name = "audio_url", length = 500)
  private String audioUrl;

  @Column(name = "audio_file_path", length = 500)
  private String audioFilePath;

  @Column(name = "device_serial", length = 100)
  private String deviceSerial;

  @Column(name = "user_id", length = 64)
  private String userId;

  @Column(name = "create_time")
  private Date createTime;

  @Column(name = "update_time")
  private Date updateTime;

  @PrePersist
  protected void onCreate() {
    createTime = new Date();
    updateTime = new Date();
    if (meetingId == null || meetingId.isEmpty()) {
      meetingId = "MEETING_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 10000);
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updateTime = new Date();
  }
}
