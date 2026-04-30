package com.wechat.service;

import com.wechat.entity.Meeting;
import com.wechat.entity.MeetingTag;
import com.wechat.entity.Transcript;
import com.wechat.repository.MeetingRepository;
import com.wechat.repository.MeetingTagRepository;
import com.wechat.repository.TranscriptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MeetingService {

  @Autowired
  private MeetingRepository meetingRepository;

  @Autowired
  private MeetingTagRepository meetingTagRepository;

  @Autowired
  private TranscriptRepository transcriptRepository;

  @Autowired
  private AudioPushService audioPushService;

  public Page<Meeting> getMeetingList(String userId, String keyword, String status, int page, int size) {
    Pageable pageable = PageRequest.of(page - 1, size);
    return meetingRepository.searchMeetings(userId, keyword, status, pageable);
  }

  public Page<Meeting> getAllMeetings(String keyword, String status, int page, int size) {
    Pageable pageable = PageRequest.of(page - 1, size);
    return meetingRepository.searchAllMeetings(keyword, status, pageable);
  }

  public Optional<Meeting> getMeetingById(String meetingId) {
    return meetingRepository.findByMeetingId(meetingId);
  }

  public List<MeetingTag> getMeetingTags(String meetingId) {
    return meetingTagRepository.findByMeetingId(meetingId);
  }

  public List<Transcript> getMeetingTranscripts(String meetingId) {
    return transcriptRepository.findByMeetingId(meetingId);
  }

  @Transactional
  public Meeting createMeeting(Meeting meeting, List<String> tags) {
    Meeting savedMeeting = meetingRepository.save(meeting);

    if (tags != null && !tags.isEmpty()) {
      for (String tagName : tags) {
        MeetingTag tag = new MeetingTag();
        tag.setMeetingId(savedMeeting.getMeetingId());
        tag.setTagName(tagName);
        meetingTagRepository.save(tag);
      }
    }

    return savedMeeting;
  }

  @Transactional
  public Meeting updateMeetingStatus(String meetingId, String status) {
    Meeting meeting = meetingRepository.findByMeetingId(meetingId)
        .orElseThrow(() -> new RuntimeException("会议不存在"));

    meeting.setStatus(status);
    Meeting savedMeeting = meetingRepository.save(meeting);

    if ("completed".equals(status)) {
      audioPushService.pushAudioToBindingUser(meetingId);
    }

    return savedMeeting;
  }

  @Transactional
  public void updateMeetingAudio(String meetingId, String audioFilePath, String audioUrl) {
    Meeting meeting = meetingRepository.findByMeetingId(meetingId)
        .orElseThrow(() -> new RuntimeException("会议不存在"));

    meeting.setAudioFilePath(audioFilePath);
    meeting.setAudioUrl(audioUrl);
    meetingRepository.save(meeting);
  }

  @Transactional
  public void updateMeetingSummary(String meetingId, String summary) {
    Meeting meeting = meetingRepository.findByMeetingId(meetingId)
        .orElseThrow(() -> new RuntimeException("会议不存在"));

    meeting.setSummary(summary);
    meetingRepository.save(meeting);
  }

  @Transactional
  public void updateMeetingSummaryStatus(String meetingId, String summaryStatus) {
    Meeting meeting = meetingRepository.findByMeetingId(meetingId)
        .orElseThrow(() -> new RuntimeException("会议不存在"));

    meeting.setSummaryStatus(summaryStatus);
    meetingRepository.save(meeting);
  }

  @Transactional
  public void saveTranscripts(String meetingId, List<Transcript> transcripts) {
    transcriptRepository.deleteByMeetingId(meetingId);

    for (Transcript transcript : transcripts) {
      transcript.setMeetingId(meetingId);
      transcriptRepository.save(transcript);
    }
  }

  @Transactional
  public void deleteMeeting(String meetingId) {
    transcriptRepository.deleteByMeetingId(meetingId);
    meetingRepository.deleteByMeetingId(meetingId);
  }
}
