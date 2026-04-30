import api from "./index";

export const userApi = {
  // 用户登录
  login(data) {
    return api.post("/auth/user/login", data);
  },

  // 获取用户信息
  getUserInfo() {
    return api.get("/auth/user/info");
  },

  // 退出登录
  logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("userInfo");
    return Promise.resolve();
  },
};
