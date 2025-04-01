<template>
  <div id="main">
    <el-container style="height: 100vh;">
      <!-- 固定的 Sidebar -->
      <el-aside class="sidebar">
        <el-menu :default-active="activeMenu" @select="handleMenuSelect" class="sidebar-menu">
          <el-menu-item index="1">
            <i class="el-icon-menu"></i>
            <span class="menu-item">轮班</span>
          </el-menu-item>
          <el-menu-item index="3">
            <i class="el-icon-s-promotion"></i>
            <span class="menu-item">补客资</span>
          </el-menu-item>
          <el-menu-item index="4">
            <i class="el-icon-s-comment"></i>
            <span class="menu-item">留言</span>
          </el-menu-item>
          <el-menu-item index="5">
            <i class="el-icon-date"></i>
            <span class="menu-item">排班</span>
          </el-menu-item>
          <el-menu-item index="6">
            <i class="el-icon-s-claim"></i>
            <span class="menu-item">待办</span>
          </el-menu-item>
          <el-menu-item index="7">
            <i class="el-icon-s-data"></i>
            <span class="menu-item">日志</span>
          </el-menu-item>
          <el-menu-item index="8">
            <i class="el-icon-aim"></i>
            <span class="menu-item">同步文档</span>
          </el-menu-item>
          <el-menu-item index="9">
            <i class="el-icon-magic-stick"></i>
            <span class="menu-item">申诉知识</span>
          </el-menu-item>
          <el-menu-item index="compensation">
            <i class="el-icon-s-order"></i>
            <span class="menu-item">待补客资</span>
          </el-menu-item>

        </el-menu>
        <div class="profile-menu">
          <el-dropdown>
            <span class="el-dropdown-link">
              <img class="avatar" :src="userAvatar" alt="用户头像" />
              <span class="account-text">{{ $store.state.userAccount }}</span>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="showChangePasswordModal">修改密码</el-dropdown-item>
              <el-dropdown-item @click.native="showChangeAvatarModal">修改头像</el-dropdown-item>
              <el-dropdown-item divided @click.native="logout">登出</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-aside>
<!-- 修改密码弹窗 -->
<el-dialog title="修改密码" :visible.sync="isChangePasswordVisible">
      <el-form :model="passwordForm" ref="passwordForm" :rules="passwordRules">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="isChangePasswordVisible = false">取消</el-button>
        <el-button type="primary" @click="submitChangePassword">确定</el-button>
      </div>
    </el-dialog>
    <el-dialog title="修改头像" :visible.sync="isChangeAvatarVisible">
      <el-upload
        class="avatar-uploader"
        :action="uploadUrl"
        :headers="uploadHeaders"
        :show-file-list="false"
        :on-success="handleAvatarSuccess"
        :on-error="handleAvatarError"
        :before-upload="beforeAvatarUpload"
      >
        <img v-if="userAvatar" :src="userAvatar" class="avatar" />
        <i v-else class="el-icon-plus avatar-uploader-icon"></i>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button @click="isChangeAvatarVisible = false">关闭</el-button>
      </div>
    </el-dialog>
      

      <!-- 主体内容区域 -->
      <el-main style="padding: 20px; background-color: #fff; overflow: auto;">
        <!-- 动态加载不同的组件 -->
        <component :is="currentComponent"></component>
      </el-main>
    </el-container>
  </div>

  
  
</template>

<script>
import WorkDay from './WorkDay.vue'
import Complement from './Complement.vue'
import Message from './Message.vue'
import Schedule from './Schedule.vue'
import TodoList from './TodoList.vue'
import LogList from './LogList.vue'
import SyncDoc from './SyncDoc.vue'
import Games from './Games.vue'
import CompensationList from './CompensationList.vue'

import request from '@/utils/request';


export default {
  name: 'Main',
  components: {
    WorkDay,
    Complement,
    Message,
    Schedule,
    TodoList,
    LogList,
    SyncDoc,
    Games,
    CompensationList,

  },
  data() {
    // 密码确认验证
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'));
      } else {
        callback();
      }
    };

    return {
      activeMenu: '1',
      currentComponent: 'WorkDay',  // 默认显示的组件
      isChangePasswordVisible: false,
      isChangeAvatarVisible: false,
      passwordForm: {
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },
      uploadUrl: process.env.VUE_APP_BASE_API + '/user/uploadAvatar',
    }
  },
  computed: {
    userAccount() {
      return this.$store.state.userAccount;
    },
    uploadHeaders() {
      return {
        Authorization: `Bearer ${localStorage.getItem('token')}`
      }
    },
    userAvatar() {
      return this.$store.state.userAvatar || require('@/assets/logo.png')
    }
  },
  methods: {
    handleMenuSelect(index) {
      this.activeMenu = index;
      // 根据菜单选项切换组件
      switch(index) {
        case '1':
          this.currentComponent = 'WorkDay';
          break;
        case '3':
          this.currentComponent = 'Complement';
          break;
        case '4':
          this.currentComponent = 'Message';
          break;
        case '5':
          this.currentComponent = 'Schedule';
          break;
        case '6':
          this.currentComponent = 'TodoList';
          break;
        case '7':
          this.currentComponent = 'LogList';
          break;
        case '8':
          this.currentComponent = 'SyncDoc';
          break;
        case '9':
          this.currentComponent = 'Games';
          break;
        case 'compensation':
          this.currentComponent = 'CompensationList';
          break;
       
      }
    },
    // 修改密码
    submitChangePassword() {
      this.$refs.passwordForm.validate(valid => {
        if (valid) {
          const token = localStorage.getItem('token');
          request.post('/user/changePassword', 
            { newPassword: this.passwordForm.newPassword },
            { headers: { Authorization: `Bearer ${token}` }}
          )
          .then(response => {
            if (response.data.code === 200) {
              this.$message.success('密码修改成功');
              this.isChangePasswordVisible = false;
              this.passwordForm.newPassword = '';
              this.passwordForm.confirmPassword = '';
              // 密码修改成功后登出
              this.logout();
            } else {
              this.$message.error(response.data.msg || '修改失败');
            }
          })
          .catch(error => {
            console.error('修改密码失败:', error);
            this.$message.error('修改密码失败，请稍后再试');
          });
        }
      });
    },

    // 头像上传前的验证
    beforeAvatarUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isJPG) {
        this.$message.error('上传头像图片只能是 JPG/PNG 格式!');
      }
      if (!isLt2M) {
        this.$message.error('上传头像图片大小不能超过 2MB!');
      }
      return isJPG && isLt2M;
    },

    // 头像上传成功
    handleAvatarSuccess(response) {
      if (response.code === 200) {
        const avatarUrl = response.data.avatarUrl;
        this.$store.dispatch('login', {
          account: this.$store.state.userAccount,
          avatarUrl
        });
        this.$message.success('头像修改成功');
        this.isChangeAvatarVisible = false;
      } else {
        this.$message.error(response.msg || '上传失败');
      }
    },

    // 头像上传失败
    handleAvatarError() {
      this.$message.error('上传头像失败，请稍后重试');
    },

    // 显示修改密码弹窗
    showChangePasswordModal() {
      this.isChangePasswordVisible = true;
      this.passwordForm.newPassword = '';
      this.passwordForm.confirmPassword = '';
    },

    // 显示修改头像弹窗
    showChangeAvatarModal() {
      this.isChangeAvatarVisible = true;
    },

    // 登出
    logout() {
      this.$store.dispatch('logout');
      this.$router.push('/login');
      this.$message.success('已成功退出登录');
    },

    async fetchUserInfo() {
      try {
        const response = await request.get('/user/info')
        if (response.data.code === 200) {
          this.userInfo = response.data.data
        }
      } catch (error) {
        if (!error.message.includes('Authentication failed')) {
          this.$message.error('获取用户信息失败：' + error.message)
        }
      }
    },

    async updateUserInfo() {
      try {
        const response = await request.post('/user/update', this.userInfo)
        if (response.data.code === 200) {
          this.$message.success('更新成功')
        }
      } catch (error) {
        if (!error.message.includes('Authentication failed')) {
          this.$message.error('更新失败：' + error.message)
        }
      }
    }
  },
  mounted() {
    // 监听切换菜单事件
    this.$bus.$on('switchMenu', this.handleMenuSelect)
  },
  beforeDestroy() {
    // 移除事件监听
    this.$bus.$off('switchMenu', this.handleMenuSelect)
  }
}
</script>

<style scoped>
.avatar-uploader {
  display: inline-block;
}

.menu-item {

  justify-content: center;
  align-items: center;
  padding: 12px 20px;
  font-size: 15px;

}

.main-container {
  position: relative;
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.profile-menu {
  position: fixed;
  bottom: 20px;
  left: 20px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  object-fit: cover; /* 保持比例 */
}

.avatar-uploader {
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.avatar-uploader img {
  width: 100px;
  height: 100px;
  display: block;
}

.el-aside {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background-color: #fafafa;
  padding-top: 20px;
  border-right: 1px solid #ddd;
  position: sticky;
  top: 0;
}

.el-main {
  padding: 20px;
  background-color: #fff;
  overflow: auto;
}
</style>