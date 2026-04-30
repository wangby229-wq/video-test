import api from "./index";

export const meetingApi = {
  getList(params) {
    return api.get("/meetings/list", { params });
  },

  getDetail(meetingId) {
    return api.get("/meetings/detail", { params: { meetingId } });
  },

  create(data) {
    return api.post("/meetings/create", data);
  },

  uploadAudio(formData) {
    return api.post("/meetings/upload-audio", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
  },

  updateStatus(meetingId, status) {
    return api.post("/meetings/update-status", { meetingId, status });
  },

  saveTranscripts(meetingId, transcripts) {
    return api.post("/meetings/save-transcripts", { meetingId, transcripts });
  },

  pushAudio(meetingId) {
    return api.post("/meetings/push-audio", null, { params: { meetingId } });
  },

  delete(meetingId) {
    return api.post("/meetings/delete", { meetingId });
  },

  generateSummary(meetingId) {
    return api.post(`/meetings/generate-summary?meetingId=${meetingId}`);
  },

  getSummaryStatus(meetingId) {
    return api.get("/meetings/summary-status", { params: { meetingId } });
  },

  simulateRecording(data) {
    return api.post("/meetings/simulate-recording", data);
  },
};
