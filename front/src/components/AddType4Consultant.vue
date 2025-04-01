<template>
  <div>
    <el-button type="primary" @click="addType4Consultant">添加国际课程顾问</el-button>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'AddType4Consultant',
  methods: {
    addType4Consultant() {
      const consultant = {
        name: '国际课程顾问',
        description: '国际课程顾问',
        status: 1,
        type: 4,
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
      }
      
      request.post('/addConsultants', consultant)
        .then(res => {
          console.log('添加国际课程顾问成功:', res.data)
          this.$message.success('添加国际课程顾问成功')
          // 刷新页面
          this.$emit('refresh')
        })
        .catch(err => {
          console.error('添加国际课程顾问失败:', err)
          this.$message.error('添加国际课程顾问失败')
        })
    }
  }
}
</script>

<style scoped>
</style>
