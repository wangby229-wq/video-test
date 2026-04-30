import api from "./index";

export const authApi = {
  getOAuthUrl(redirectUri) {
    return api.get("/auth/wechat/oauth-url", { params: { redirectUri } });
  },

  getQrConnectUrl(redirectUri) {
    return api.get("/auth/wechat/qr-url", { params: { redirectUri } });
  },

  wechatCallback(code) {
    return api.get("/auth/wechat/callback", { params: { code } });
  },

  wechatSilentLogin(code) {
    return api.get("/auth/wechat/silent-login", { params: { code } });
  },

  getUserInfo() {
    return api.get("/auth/user/info");
  },

  logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("userInfo");
    return api.post("/auth/logout");
  },

  isInWechat() {
    const ua = navigator.userAgent.toLowerCase();
    return ua.includes("wxwork") || ua.includes("micromessenger");
  },

  mockLogin() {
    return api.get("/auth/mock-login");
  },
};
