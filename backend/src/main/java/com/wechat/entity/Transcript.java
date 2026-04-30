package com.wechat.entity;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "transcript")
public class Transcript {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "meeting_id", nullable = false, length = 64)
    private String meetingId;
    
    @Column(length = 100)
    private String speaker;
    
    @Column(length = 20)
    private String time;
    
    @Column(columnDefinition = "TEXT")
    private String text;
    
    @Column(name = "create_time")
    private Date createTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = new Date();
    }
}
