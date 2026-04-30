package com.wechat.repository;

import com.wechat.entity.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
  Optional<Meeting> findByMeetingId(String meetingId);

  Page<Meeting> findByUserId(String userId, Pageable pageable);

  @Query("SELECT m FROM Meeting m WHERE m.userId = :userId AND " +
      "(:keyword IS NULL OR m.title LIKE %:keyword% OR m.summary LIKE %:keyword%) AND " +
      "(:status IS NULL OR :status = '' OR m.status = :status)")
  Page<Meeting> searchMeetings(@Param("userId") String userId,
      @Param("keyword") String keyword,
      @Param("status") String status,
      Pageable pageable);

  @Query("SELECT m FROM Meeting m WHERE " +
      "(:keyword IS NULL OR m.title LIKE %:keyword% OR m.summary LIKE %:keyword%) AND " +
      "(:status IS NULL OR :status = '' OR m.status = :status)")
  Page<Meeting> searchAllMeetings(@Param("keyword") String keyword,
      @Param("status") String status,
      Pageable pageable);

  void deleteByMeetingId(String meetingId);
}
