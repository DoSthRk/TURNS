<template>
  <div class="register-container">
    <el-row type="flex" justify="center" align="middle" style="height: 100vh;">
      <el-col :span="8">
        <div class="register-box">
          <!-- 头像区域 -->
          <div class="text-center avatar-box">
            <img src="../assets/logo.png" class="avatar" alt="Logo" />
          </div>

          <!-- 表单区域 -->
          <el-form :model="form" ref="form" label-width="100px" class="form-register">
            <el-form-item label="用户名" :rules="[{ required: true, message: '请输入用户名', trigger: 'blur' }]">
              <el-input v-model="form.account" placeholder="请输入用户名" autocomplete="off" />
            </el-form-item>

            <el-form-item label="密码" :rules="[{ required: true, message: '请输入密码', trigger: 'blur' }]">
              <el-input v-model="form.password" type="password" placeholder="请输入密码" />
            </el-form-item>

            <el-form-item label="确认密码" :rules="[{ required: true, message: '请确认密码', trigger: 'blur' }]">
              <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" />
            </el-form-item>

            <el-row>
              <el-col :span="24">
                <el-button type="primary" @click="register" style="width: 100%;">注册</el-button>
              </el-col>
            </el-row>
            <div class="form-footer">
              <span>已有账号？</span>
              <el-button type="text" @click="goToLogin">去登录</el-button>
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
    data() {
    return {
      form: {
        account: '',
        password: '',
        confirmPassword: '',
      },
    };
  },
  methods: {
    register() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          if (this.form.password !== this.form.confirmPassword) {
            this.$message.error('密码和确认密码不匹配');
            return;
          }
          // 注册请求
          request
            .post('/user/register', {
              account: this.form.account,
              password: this.form.password,
            })
            .then((response) => {
              if (response.data.code===200) {
                this.$message.success('注册成功');
                this.$router.push('/login');
              } else {
                this.$message.error(response.data.message || '注册失败');
              }
            })
            .catch((error) => {
              console.error('注册失败:', error);
              this.$message.error('注册失败，请稍后再试');
            });
        }
      });
    },
    goToLogin() {
      this.$router.push('/login');
    },
  },
}
</script>

<style scoped>
.register-container {
    background-color: #f4f7fc;
    min-height: 100vh;
    width: 100%;
    position: fixed;
    top: 0;
    left: 0;
}
  
.register-box {
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
}
  
.avatar {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    display: inline-block;
}
  
.form-register {
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