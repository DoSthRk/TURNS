<template>
  <div class="message-container">
    

    <!-- 留言板部分 -->
    <div class="message-board">
      <div class="message-input">
        <el-input
          type="textarea"
          :rows="3"
          placeholder="说点什么吧..."
          v-model="messageContent"
          class="message-textarea"
        ></el-input>
        <el-button type="primary" @click="submitAdvice" class="submit-btn">发布留言</el-button>
      </div>

      <!-- 留言列表 -->
      <div class="message-list">
        <div v-for="message in currentPageMessages" :key="message.id" class="message-item">
          <div class="message-header">
            <el-avatar 
              :size="40" 
              :src="message.user?.avatarUrl || defaultAvatar"
              @error="handleAvatarError"
            ></el-avatar>
            <div class="message-info">
              <div class="message-user">{{ message.user?.account || '未知用户' }}</div>
              <div class="message-time">{{ formatTime(message.createTime) }}</div>
            </div>
            <!-- 如果是当前用户的留言，显示删除按钮 -->
            <el-button 
              v-if="message.user?.account === currentUser"
              type="text" 
              class="delete-btn"
              @click="deleteMessage(message.id)"
            >
              <i class="el-icon-delete"></i>
            </el-button>
          </div>
          <div class="message-content">{{ message.text }}</div>
        </div>
      </div>
    </div>

    <!-- 分页组件 -->
    <div class="pagination-container">
      <el-pagination
        @current-change="handlePageChange"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="messages.length"
        layout="prev, pager, next"
      >
      </el-pagination>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import moment from 'moment'; // 需要安装 moment
import request from '@/utils/request';

export default {
  data() {
    return {
      dialogVisible: false,
      text: "",
      messageContent: "",
      messages: [], // 存储留言列表
      defaultAvatar: require('@/assets/logo.png'),
      currentPage: 1,
      pageSize: 10,
    };
  },
  
  computed: {
    // 获取当前登录用户的账号
    currentUser() {
      return this.$store.state.userAccount;
    },
    
    // 获取当前页的留言
    currentPageMessages() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.messages.slice(start, end);
    },
  },
  
  created() {
    this.fetchMessages(); // 组件创建时获取留言列表
  },

  methods: {
    // 原有的建议相关方法保持不变
    async submitAdvice() {
      if (this.messageContent.trim() === "") {
        this.$message.error("留言内容不能为空！");
        return;
      }

      try {
        const response = await request.post("/advice/add", {
          text: this.messageContent
        }, {
          headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
          }
        });

        if (response.data.code === 200) {
          this.$message.success("发布成功！");
          this.messageContent = "";
          this.fetchMessages(); // 重新获取留言列表
        } else {
          this.$message.error(response.data.msg || "发布失败！");
        }
      } catch (error) {
        console.error("提交失败:", error);
        this.$message.error("提交失败，请稍后再试！");
      }
    },
    
    clearInput() {
      this.messageContent = "";
    },

    // 新增留言相关方法
    async fetchMessages() {
      try {
        const response = await request.get('/advice/list', {
          headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
          }
        });
        
        if (response.data.code === 200) {
          this.messages = response.data.data;
        } else {
          this.$message.error(response.data.msg || "获取留言失败！");
        }
      } catch (error) {
        console.error('获取留言失败:', error);
        this.$message.error('获取留言失败，请稍后重试！');
      }
    },

    // 删除留言
    async deleteMessage(id) {
      try {
        const response = await request.delete(`/advice/delete/${id}`, {
          headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
          }
        });

        if (response.data.code === 200) {
          this.$message.success("删除成功！");
          this.fetchMessages(); // 重新获取留言列表
        } else {
          this.$message.error(response.data.msg || "删除失败！");
        }
      } catch (error) {
        console.error('删除失败:', error);
        this.$message.error('删除失败，请稍后重试！');
      }
    },

    formatTime(time) {
      if (!time) return '';
      return moment(time).format('YYYY-MM-DD HH:mm');
    },

    // 头像加载失败时使用默认头像
    handleAvatarError() {
      return true; // 使用默认头像
    },

    // 处理页码改变
    handlePageChange(newPage) {
      this.currentPage = newPage;
    },
  },
};
</script>

<style scoped>
.message-container {
  padding: 20px;
}

.message-board {
  margin-top: 20px;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.message-input {
  margin-bottom: 30px;
}

.message-textarea {
  margin-bottom: 10px;
}

.submit-btn {
  float: right;
}

.message-list {
  margin-top: 20px;
}

.message-item {
  padding: 20px;
  border-bottom: 1px solid #eee;
  position: relative;
}

.message-item:last-child {
  border-bottom: none;
}

.message-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.message-info {
  margin-left: 10px;
  flex-grow: 1;
}

.message-user {
  font-weight: bold;
  color: #303133;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.message-content {
  color: #606266;
  line-height: 1.6;
  margin-left: 50px;
  word-break: break-all;
}

.delete-btn {
  position: absolute;
  right: 20px;
  top: 20px;
  color: #F56C6C;
}

.delete-btn:hover {
  color: #f78989;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>