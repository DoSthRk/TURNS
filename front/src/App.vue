<template>
  <div id="app">
<router-view></router-view>

  </div>
</template>

<script>
import Vue from 'vue';
import Router from './router'; // 导入路由配置

export default {
  name: 'App',
  data() {
    return {
     
    
    };
  },

  methods:{

  }

 
  
};
</script>

<style scoped>
/* 统一调整el-menu-item的样式 */
.menu-item {
  display: flex;
  justify-content: center;  /* 水平居中 */
  align-items: center;      /* 垂直居中 */
  padding: 12px 20px;       /* 控制内边距 */
  font-size: 16px;          /* 字体大小 */
  font-weight: 500;         /* 字体加粗 */
  color: #333;              /* 字体颜色 */
  border-radius: 4px;       /* 圆角 */
  transition: background-color 0.3s ease;  /* 背景色过渡效果 */
}

/* 鼠标悬浮时变色 */
.menu-item:hover {
  background-color: #f5f5f5; /* 鼠标悬浮时背景颜色 */
  color: #409EFF; /* 鼠标悬浮时字体颜色 */
}
/* 样式 */
</style>