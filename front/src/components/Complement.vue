<template>
  
  <el-tabs type="border-card">

      <el-tab-pane label="补客资">
        <ComplementInfo></ComplementInfo>
      </el-tab-pane>
    </el-tabs>

</template>

<script>
import ComplementInfo from './ComplementInfo.vue';
import request from '../utils/request';

export default {
    components:{
        ComplementInfo
    },
    methods: {
        async fetchComplementList() {
            try {
                const response = await request.get('/complement/list')
                if (response.data.code === 200) {
                    this.complementList = response.data.data || []
                }
            } catch (error) {
                if (!error.message.includes('Authentication failed')) {
                    this.$message.error('获取补卡记录失败：' + error.message)
                }
            }
        },
        async submitComplement(data) {
            try {
                const response = await request.post('/complement/submit', data)
                if (response.data.code === 200) {
                    this.$message.success('提交成功')
                    await this.fetchComplementList()
                }
            } catch (error) {
                if (!error.message.includes('Authentication failed')) {
                    this.$message.error('提交失败：' + error.message)
                }
            }
        }
    }
}
</script>

<style>

</style>