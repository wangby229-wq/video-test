package com.wechat.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "device_binding")
public class DeviceBinding {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "binding_id", unique = true, nullable = false, length = 64)
  private String bindingId;

  @Column(name = "device_id", nullable = false, length = 64)
  private String deviceId;

  @Column(name = "device_serial", nullable = false, length = 100)
  private String deviceSerial;

  @Column(name = "user_id", nullable = true, length = 64)
  private String userId;

  @Column(name = "user_ids", nullable = false, length = 500)
  private String userIds;

  @Column(name = "user_names", length = 1000)
  private String userNames;

  @Column(name = "binding_type", length = 20)
  private String bindingType = "manual";

  @Column(name = "bind_time")
  private Date bindTime;

  @Column(name = "last_update_time")
  private Date lastUpdateTime;

  @PrePersist
  protected void onCreate() {
    bindTime = new Date();
    lastUpdateTime = new Date();
    if (bindingId == null || bindingId.isEmpty()) {
      bindingId = "BIND_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 10000);
    }
    if (userId == null || userId.isEmpty()) {
      userId = userIds != null && userIds.contains(",") ? userIds.split(",")[0] : userIds;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    lastUpdateTime = new Date();
  }
}
