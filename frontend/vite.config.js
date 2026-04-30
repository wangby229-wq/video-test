import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import path from "path";

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "src"),
    },
  },
  base: "/meeting/",
  build: {
    assetsDir: "assets",
    assetsInlineLimit: 4096,
    cssCodeSplit: true,
    rollupOptions: {
      output: {
        manualChunks: undefined,
      },
    },
  },
  define: {
    // 在构建时注入API配置
    __API_BASE_URL__: JSON.stringify(process.env.VITE_API_BASE_URL || "/api"),
    __FRONTEND_URL__: JSON.stringify(process.env.VITE_FRONTEND_URL || ""),
  },
  server: {
    port: 3001,
    host: "0.0.0.0",
    proxy: {
      "/api": {
        target: process.env.VITE_API_BASE_URL || "http://192.168.11.16:8080",
        changeOrigin: true,
      },
    },
  },
});
