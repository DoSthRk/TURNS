<template>
    <div id="profile-page">
      <el-container style="height: 100vh;">
        <el-aside width="220px" style="background-color: #fafafa; padding-top: 20px; border-right: 1px solid #ddd;">
          <el-menu :default-active="activeMenu" @select="handleMenuSelect" style="font-size: 16px;">
            <el-menu-item index="1">
              <span class="menu-item">工作日</span>
            </el-menu-item>
            <el-menu-item index="2">
              <span class="menu-item">周末</span>
            </el-menu-item>
            <el-menu-item index="3">
              <span class="menu-item">补客资</span>
            </el-menu-item>
            <el-menu-item index="4">
              <span class="menu-item">留言板</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
  
        <el-main style="padding: 20px; background-color: #fff; overflow: auto;">
          <!-- 个人信息部分 -->
          <el-row>
            <el-col :span="12">
              <el-avatar :src="avatar" size="large" style="cursor: pointer;" @click="handleAvatarClick"></el-avatar>
              <p style="margin-top: 20px;">点击头像修改头像</p>
            </el-col>
            <el-col :span="12">
              <el-form ref="form" :model="form" label-width="120px">
                <!-- 修改密码 -->
                <el-form-item label="新密码">
                  <el-input v-model="form.password" type="password" placeholder="请输入新密码"></el-input>
                </el-form-item>
  
                <el-form-item label="确认密码">
                  <el-input v-model="form.confirmPassword" type="password" placeholder="确认新密码"></el-input>
                </el-form-item>
  
                <el-form-item>
                  <el-button type="primary" @click="saveChanges">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-col>
          </el-row>
        </el-main>
      </el-container>
    </div>
  </template>
  
  <script>
  export default {
    data() {
      return {
        activeMenu: '1',
        avatar: 'https://avatars.githubusercontent.com/u/48797185?v=4', // 默认头像
        form: {
          password: '',
          confirmPassword: '',
        },
      };
    },
    methods: {
      // 处理菜单选择
      handleMenuSelect(index) {
        this.activeMenu = index;
      },
  
      // 点击头像触发的操作
      handleAvatarClick() {
        this.$message.info('这里可以上传新头像');
        // 你可以在这里使用 el-upload 组件实现头像上传
      },
  
      // 保存修改的密码
      saveChanges() {
        if (this.form.password !== this.form.confirmPassword) {
          this.$message.error('密码和确认密码不匹配');
          return;
        }
  
        // 保存密码修改的 API 调用
        this.$axios.post('/save-profile', {
          avatar: this.avatar,
          password: this.form.password,
        })
        .then(response => {
          if (response.data.success) {
            this.$message.success('修改成功');
          } else {
            this.$message.error('修改失败');
          }
        })
        .catch(error => {
          this.$message.error('保存失败，请稍后再试');
        });
      },
    },
  }
  </script>
  
  <style scoped>
  .menu-item {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 12px 20px;
    font-size: 16px;
    font-weight: 500;
    color: #333;
    border-radius: 4px;
    transition: background-color 0.3s ease;
  }
  </style>