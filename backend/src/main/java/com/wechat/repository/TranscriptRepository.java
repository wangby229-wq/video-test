package com.wechat.repository;

import com.wechat.entity.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TranscriptRepository extends JpaRepository<Transcript, Long> {
    List<Transcript> findByMeetingId(String meetingId);
    void deleteByMeetingId(String meetingId);
}
