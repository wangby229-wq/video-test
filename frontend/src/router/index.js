import { createRouter, createWebHistory } from "vue-router";
import Layout from "@/layout/index.vue";

const routes = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/login/index.vue"),
    meta: { title: "登录" },
  },
  {
    path: "/",
    component: Layout,
    redirect: "/meetings",
    children: [
      {
        path: "meetings",
        name: "Meetings",
        component: () => import("@/views/meetings/index.vue"),
        meta: { title: "会议记录", requiresAuth: true },
      },
      {
        path: "meetings/:id",
        name: "MeetingDetail",
        component: () => import("@/views/meetings/detail.vue"),
        meta: { title: "会议详情" },
      },
      {
        path: "upload",
        name: "Upload",
        component: () => import("@/views/upload/index.vue"),
        meta: { title: "上传录音", requiresAuth: true },
      },
      {
        path: "device",
        name: "Device",
        component: () => import("@/views/device/index.vue"),
        meta: { title: "设备管理", requiresAuth: true },
      },
      {
        path: "simulator",
        name: "Simulator",
        component: () => import("@/views/device/Simulator.vue"),
        meta: { title: "音频推送模拟", requiresAuth: true },
      },
      {
        path: "profile",
        name: "Profile",
        component: () => import("@/views/profile/index.vue"),
        meta: { title: "个人中心", requiresAuth: true },
      },
    ],
  },
  {
    path: "/device-bind",
    name: "DeviceBind",
    component: () => import("@/views/device-bind/index.vue"),
    meta: { title: "设备绑定", requiresAuth: false },
  },
];

const router = createRouter({
  history: createWebHistory("/meeting/"),
  routes,
});

router.beforeEach((to, from, next) => {
  // 暂时跳过登录验证，直接访问所有页面
  // const token = localStorage.getItem('token')
  // if (to.meta.requiresAuth && !token) {
  //   next('/login')
  // } else if (to.path === '/login' && token) {
  //   next('/meetings')
  // } else {
  //   next()
  // }
  next();
});

export default router;
