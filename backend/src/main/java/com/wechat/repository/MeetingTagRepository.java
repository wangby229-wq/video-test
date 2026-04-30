package com.wechat.repository;

import com.wechat.entity.MeetingTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingTagRepository extends JpaRepository<MeetingTag, Long> {
    List<MeetingTag> findByMeetingId(String meetingId);
    void deleteByMeetingId(String meetingId);
}
