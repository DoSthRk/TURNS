<template>
  <div>
    <el-button type="primary" @click="openDialog">添加顾问</el-button>

    <el-dialog
      title="添加顾问"
      :visible.sync="dialogVisible"
      width="36%"
      :before-close="handleClose"
    >
      <el-form ref="advisorForm" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="顾问类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择顾问类型">
            <el-option
              v-for="typeItem in enabledTypeOptions"
              :key="`new-advisor-type-${typeItem.consultantType}`"
              :label="typeItem.typeLabel"
              :value="typeItem.consultantType"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="顾问姓名" prop="name">
          <el-input v-model.trim="form.name" placeholder="请输入顾问姓名"></el-input>
        </el-form-item>
      </el-form>

      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'
import { DEFAULT_TYPE_CONFIGS, normalizeTypeConfigs } from '@/utils/shiftConfig'

export default {
  name: 'AddAdvisor',
  data() {
    return {
      dialogVisible: false,
      submitting: false,
      typeOptions: [],
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
        type: [{ required: true, message: '请选择顾问类型', trigger: 'change' }],
        name: [
          { required: true, message: '请输入顾问姓名', trigger: 'blur' },
          { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    enabledTypeOptions() {
      return this.typeOptions
        .filter(item => Number(item.enabled) === 1)
        .sort((a, b) => Number(a.displayOrder) - Number(b.displayOrder))
    }
  },
  mounted() {
    this.loadTypeOptions()
  },
  methods: {
    async loadTypeOptions() {
      try {
        const response = await request.get('/shift-config/type-options')
        if (response.data.code === 200 && Array.isArray(response.data.data)) {
          this.typeOptions = normalizeTypeConfigs(response.data.data)
        } else {
          this.typeOptions = normalizeTypeConfigs(DEFAULT_TYPE_CONFIGS)
        }
      } catch (error) {
        console.warn('读取顾问类型失败，使用默认配置', error)
        this.typeOptions = normalizeTypeConfigs(DEFAULT_TYPE_CONFIGS)
      }

      if (this.enabledTypeOptions.length > 0) {
        this.form.type = this.enabledTypeOptions[0].consultantType
      }
    },
    openDialog() {
      this.dialogVisible = true
      if (this.enabledTypeOptions.length > 0) {
        this.form.type = this.enabledTypeOptions[0].consultantType
      }
    },
    handleClose(done) {
      if (this.$refs.advisorForm) {
        this.$refs.advisorForm.resetFields()
      }
      done()
    },
    submitForm() {
      if (!this.$refs.advisorForm) return
      this.$refs.advisorForm.validate(async valid => {
        if (!valid) {
          return
        }
        this.submitting = true
        try {
          const payload = {
            ...this.form,
            description: null
          }
          await request.post('/addConsultants', payload)
          this.$message.success('添加顾问成功')
          this.dialogVisible = false
          this.$refs.advisorForm.resetFields()
          this.$emit('refresh')
        } catch (error) {
          console.error('添加顾问失败:', error)
          this.$message.error('添加顾问失败')
        } finally {
          this.submitting = false
        }
      })
    }
  }
}
</script>

<style scoped>
.el-select {
  width: 100%;
}
</style>

