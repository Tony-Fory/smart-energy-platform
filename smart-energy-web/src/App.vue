<script setup lang="ts">
import { useRouter, useRoute, RouterView, RouterLink } from 'vue-router'
import { isAuthenticated, clearAuthToken, initAuth, hasPermission } from './api/auth'

// Initialize auth from localStorage on app startup
initAuth()

const router = useRouter()
const route = useRoute()

function handleLogout() {
  clearAuthToken()
  router.replace('/login')
}
</script>

<template>
  <div id="app-wrapper">
    <header v-if="isAuthenticated() && route.path !== '/login'" class="app-header">
      <nav class="app-nav">
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/dashboard">实时监控</RouterLink>
        <RouterLink to="/device">设备管理</RouterLink>
        <RouterLink v-if="hasPermission('ALARM_VIEW')" to="/alarm">告警中心</RouterLink>
        <RouterLink v-if="hasPermission('ALARM_RULE_VIEW')" to="/alarm/rules">告警规则</RouterLink>
        <span class="nav-spacer"></span>
        <a class="logout-link" @click.prevent="handleLogout">退出登录</a>
      </nav>
    </header>
    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
#app-wrapper {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
  padding: 0 20px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.app-nav {
  display: flex;
  gap: 4px;
  height: 48px;
  align-items: center;
}

.app-nav a {
  padding: 8px 16px;
  border-radius: 6px;
  color: #606266;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.2s;
}

.app-nav a:hover {
  background: #f0f2f5;
  color: #303133;
}

.app-nav a.router-link-active {
  background: #ecf5ff;
  color: #409eff;
}

.nav-spacer {
  flex: 1;
}

.logout-link {
  cursor: pointer;
  color: #f56c6c !important;
}

.logout-link:hover {
  background: #fef0f0 !important;
  color: #f56c6c !important;
}

.app-main {
  flex: 1;
}
</style>
