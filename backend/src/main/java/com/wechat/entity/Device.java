package com.wechat.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "device")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "device_id", unique = true, nullable = false, length = 64)
    private String deviceId;
    
    @Column(name = "device_serial", unique = true, nullable = false, length = 100)
    private String deviceSerial;
    
    @Column(name = "device_name", nullable = false, length = 200)
    private String deviceName;
    
    @Column(name = "device_key", length = 200)
    private String deviceKey;
    
    @Column(length = 20)
    private String status = "offline";
    
    @Column(name = "last_online_time")
    private Date lastOnlineTime;
    
    @Column(name = "create_time")
    private Date createTime;
    
    @Column(name = "update_time")
    private Date updateTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = new Date();
        updateTime = new Date();
        if (deviceKey == null || deviceKey.isEmpty()) {
            deviceKey = generateDeviceKey();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
    }
    
    private String generateDeviceKey() {
        return "KEY_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }
}
