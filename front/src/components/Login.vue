<template>
 <div class="login-container">
    <el-row type="flex" justify="center" align="middle" style="height: 100vh;">
      <el-col :span="8">
        <div class="login-box">
          <!-- 头像区域 -->
          <div class="text-center avatar-box">
            <img src="../assets/logo.png" class="avatar" alt="Logo" />
          </div>

          <!-- 表单区域 -->
          <el-form :model="form" ref="form" label-width="100px" class="form-login">
            <el-form-item label="用户名" :rules="[{ required: true, message: '请输入用户名', trigger: 'blur' }]">
              <el-input v-model="form.account" placeholder="请输入登录名称" autocomplete="off" />
            </el-form-item>

            <el-form-item label="密码" :rules="[{ required: true, message: '请输入密码', trigger: 'blur' }]">
              <el-input v-model="form.password" type="password" placeholder="请输入登录密码" />
            </el-form-item>

            <el-row>
              <el-col :span="24">
                <el-button type="primary" @click="handleLogin" style="width: 100%;">登录</el-button>
              </el-col>
            </el-row>
            <div class="form-footer">
              <span>没有账号？</span>
              <el-button type="text" @click="goToRegister">去注册</el-button>
            </div>
          </el-form>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import axios from 'axios';
import request from '@/utils/request';
export default {
    name: 'Login',
		data(){
			return {
                form: {
        account: '',
        password: '',
      },
      accountRules: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
      ],
      passwordRules: [
        { required: true, message: '请输入密码', trigger: 'blur' },
      ],
			}
		},
        methods: {
            handleLogin() {
      const loginData = {
        account: this.form.account,
        password: this.form.password
      };

      request.post('/user/login', loginData)
        .then(response => {
          if (response.data.code === 200) {
            const { token, avatarUrl, role } = response.data.data;
            
            // 保存token
            localStorage.setItem('token', token);
            
            // 保存用户信息到vuex
            this.$store.dispatch('login', {
              account: this.form.account,
              avatarUrl: avatarUrl || '',
              role: role || 'user'  // 如果后端没返回role，默认为user
            });

            // 登录成功提示
            this.$message.success('登录成功');

          
              this.$router.replace('/main');
            
          } else {
            this.$message.error(response.data.msg);
          }
        })
        .catch(error => {
          console.error('登录失败:', error);
          if (error.response) {
            // 服务器返回了错误响应
            const errorMsg = error.response.data.msg || error.response.data.message || '服务器错误';
            this.$message.error(`登录失败: ${errorMsg}`);
          } else if (error.request) {
            // 请求发送了但没有收到响应
            this.$message.error('无法连接到服务器，请检查网络');
          } else {
            // 设置请求时发生了错误
            this.$message.error(error.message || '登录失败，请稍后重试');
          }
        });
    },
    goToRegister() {
      this.$router.replace('/register');
    },
},
mounted(){
    // 如果已经登录，直接跳转
    if (this.$store.getters.isAuthenticated) {
      const role = this.$store.getters.userRole;
      if (role === 'admin') {
        this.$router.replace('/admin');
      } else {
        this.$router.replace('/main');
      }
    }
}
}
</script>

<style scoped>
.login-container {
  background-color: #f4f7fc;
  min-height: 100vh;
  width: 100%;
  position: fixed;
  top: 0;
  left: 0;
}

.login-box {
  background-color: #fff;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
}

.avatar-box {
  margin-bottom: 20px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  display: block;
}

.form-login {
  margin-top: 20px;
}

.el-button {
  margin-top: 10px;
  width: 100%;
}

.form-footer {
  margin-top: 15px;
  text-align: center;
}

@media screen and (max-width: 768px) {
  .el-col {
    width: 90% !important;
  }
}
</style>