<template>
  <div>
    <el-button type="primary" @click="dialogVisible = true">添加顾问</el-button>
    
    <el-dialog
      title="添加顾问"
      :visible.sync="dialogVisible"
      width="30%"
      :before-close="handleClose">
      <el-form :model="form" :rules="rules" ref="advisorForm" label-width="100px">
        <el-form-item label="顾问类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择顾问类型">
            <el-option label="辅导" :value="1"></el-option>
            <el-option label="申诉" :value="2"></el-option>
            <el-option label="国际课程" :value="4"></el-option>
            <el-option label="推月申诉" :value="3"></el-option>
            <el-option label="推月辅导" :value="5"></el-option>
            <el-option label="汇诺辅导" :value="6"></el-option>
            <el-option label="汇诺申诉" :value="7"></el-option>
            <el-option label="智云辅导" :value="8"></el-option>
            <el-option label="留学堂辅导" :value="9"></el-option>
            <el-option label="留学堂申诉" :value="12"></el-option>
            <el-option label="集好家辅导" :value="10"></el-option>
            <el-option label="集好家申诉" :value="11"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="顾问姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入顾问姓名"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'AddAdvisor',
  data() {
    return {
      dialogVisible: false,
      form: {
        name: '',
        type: 1,
        description: '',
        status: 1,
        sortOrder: 1,
        countNormal: 0,
        orderStatusNormal: 1,
        waitingNormal: 0,
        countSem: 0,
        orderStatusSem: 1,
        waitingSem: 0,
        countSingle1: 0,
        orderStatusSingle1: 1,
        waitingSingle1: 0
      },
      rules: {
        type: [
          { required: true, message: '请选择顾问类型', trigger: 'change' }
        ],
        name: [
          { required: true, message: '请输入顾问姓名', trigger: 'blur' },
          { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    handleClose(done) {
      this.$refs.advisorForm.resetFields();
      done();
    },
    submitForm() {
      this.$refs.advisorForm.validate((valid) => {
        if (valid) {
          // 设置描述与名称相同
          this.form.description =null;
          request.post('/addConsultants', this.form)
            .then(res => {
              console.log('添加顾问成功:', res.data);
              this.$message.success('添加顾问成功');
              this.dialogVisible = false;
              this.$refs.advisorForm.resetFields();
              // 刷新页面
              this.$emit('refresh');
            })
            .catch(err => {
              console.error('添加顾问失败:', err);
              this.$message.error('添加顾问失败');
            });
        } else {
          return false;
        }
      });
    }
  }
}
</script>

<style scoped>
.el-select {
  width: 100%;
}
</style>
