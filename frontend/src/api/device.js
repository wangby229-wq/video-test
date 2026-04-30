import api from "./index";

export const deviceApi = {
  register(data) {
    return api.post("/devices/register", data);
  },

  bind(data) {
    return api.post("/devices/bind", data);
  },

  unbind(deviceSerial) {
    return api.post("/devices/unbind", { deviceSerial });
  },

  updateBinding(data) {
    return api.post("/devices/update-binding", data);
  },

  getList() {
    return api.get("/devices/list");
  },

  getBindings() {
    return api.get("/devices/bindings");
  },

  getCorpUsers() {
    return api.get("/devices/corp-users");
  },

  getMyBinding() {
    return api.get("/devices/my-binding");
  },

  heartbeat(deviceSerial) {
    return api.post("/devices/heartbeat", { deviceSerial });
  },
};
