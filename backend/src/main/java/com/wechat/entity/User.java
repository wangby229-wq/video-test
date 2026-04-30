package com.wechat.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "wechat_user")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", unique = true, nullable = false, length = 64)
  private String userId;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 500)
  private String avatar;

  @Column(length = 20)
  private String mobile;

  @Column(length = 100)
  private String email;

  @Column(length = 200)
  private String department;

  @Column(length = 100)
  private String position;

  @Column(name = "create_time")
  private Date createTime;

  @Column(name = "update_time")
  private Date updateTime;

  @PrePersist
  protected void onCreate() {
    createTime = new Date();
    updateTime = new Date();
  }

  @PreUpdate
  protected void onUpdate() {
    updateTime = new Date();
  }
}
