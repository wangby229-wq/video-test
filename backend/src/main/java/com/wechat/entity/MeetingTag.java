package com.wechat.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "meeting_tag")
public class MeetingTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "meeting_id", nullable = false, length = 64)
    private String meetingId;
    
    @Column(name = "tag_name", nullable = false, length = 50)
    private String tagName;
}
