<script setup lang="ts">
import { RouterView, RouterLink } from 'vue-router'
</script>

<template>
  <div id="app-wrapper">
    <header class="app-header">
      <nav class="app-nav">
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/dashboard">实时监控</RouterLink>
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

.app-main {
  flex: 1;
}
</style>
