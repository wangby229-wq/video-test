package com.wechat.repository;

import com.wechat.entity.PushRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PushRecordRepository extends JpaRepository<PushRecord, Long> {
    List<PushRecord> findByMeetingId(String meetingId);
    List<PushRecord> findByUserId(String userId);
}
