<template>
  <div class="compensation-container">
    <div class="header">
      <h2>待补客资管理</h2>
      <el-button 
        type="primary" 
        @click="calculateDifferences('normal')"
        :disabled="hasUncompletedNormal"
      >
        统计辅导补客资
      </el-button>
      <el-button 
        type="danger" 
        @click="calculateDifferences('sem')"
        :disabled="hasUncompletedSem"
      >
        统计申诉补客资
      </el-button>
      <el-button 
        type="warning" 
        @click="clearNormalCount('normal')"
      >
        清零辅导官号客资
      </el-button>
      <el-button 
        type="warning" 
        @click="clearNormalCount('sem')"
      >
        清零申诉官号客资
      </el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="compensationList"
      style="width: 100%"
      border
      stripe
    >
      <el-table-column prop="consultantName" label="顾问姓名" width="120" />
      <el-table-column label="类型" width="100">
        <template slot-scope="scope">
          {{ scope.row.type === 1 ? '辅导' : '申诉' }}
        </template>
      </el-table-column>
      <el-table-column prop="normalCount" label="客资数量" width="120">
        <template slot-scope="scope">
          {{ scope.row.normalCount }}
        </template>
      </el-table-column>
      <el-table-column prop="difference" label="需补" width="120">
        <template slot-scope="scope">
          <span :class="{ 'highlight': scope.row.difference > 0 }">
            {{ scope.row.difference || 0 }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template slot-scope="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加清除按钮 -->
    <div class="clear-all-button">
      <el-button 
        type="danger" 
        @click="clearAllCompensation"
        :loading="clearLoading"
      >
        清除所有统计数据
      </el-button>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'CompensationList',
  props: {
    compensationData: {
      type: Object,
      default: () => ({ normalStats: [], semStats: [] })
    }
  },
  data() {
    return {
      loading: false,
      compensationList: [],
      clearLoading: false
    }
  },
  watch: {
    compensationData: {
      immediate: true,
      handler(newData) {
        this.compensationList = [...newData.normalStats, ...newData.semStats];
      }
    }
  },
  computed: {
    // 检查是否有未完成的辅导补偿记录
    hasUncompletedNormal() {
      return this.compensationList.some(item => 
        item.type === 1 && item.status === 1
      );
    },
    // 检查是否有未完成的申诉补偿记录
    hasUncompletedSem() {
      return this.compensationList.some(item => 
        item.type === 2 && item.status === 1
      );
    }
  },
  methods: {
    async fetchCompensationList() {
      this.loading = true;
      try {
        const response = await request.get('/compensation/list');
        console.log('补偿列表响应:', response);
        if (response.data.code === 200) {
          const data = response.data.data;
          
          // 按类型分组计算最大值
          const normalData = data.filter(item => item.type === 1);
          const semData = data.filter(item => item.type === 2);
          
          const normalMax = Math.max(...normalData.map(item => item.normalCount), 0);
          const semMax = Math.max(...semData.map(item => item.normalCount), 0);
          
          // 重新计算差值
          this.compensationList = data.map(item => ({
            ...item,
            difference: (item.type === 1 ? normalMax : semMax) - item.normalCount
          }));
          
          console.log('处理后的补偿列表:', this.compensationList);
        } else {
          this.$message.error(response.data.msg || '获取待补客资列表失败');
        }
      } catch (error) {
        console.error('获取待补客资列表失败:', error);
        this.$message.error('获取待补客资列表失败');
      } finally {
        this.loading = false;
      }
    },

    async calculateDifferences(type) {
      try {
        const response = await request.get('/consultants');
        const consultants = response.data;
        console.log('获取到的原始顾问数据:', consultants);
        
        // 检查每个顾问的详细信息
        consultants.forEach(c => {
          console.log(`顾问 ${c.name} 的详细信息:`, {
            status: c.status,  // 使用 status 替代 workStatus
            type: c.type,
            countNormal: c.countNormal
          });
        });
        
        // 筛选状态正常的顾问（使用 status 替代 workStatus）
        const activeConsultants = consultants.filter(c => {
          const isActive = c.status === 1;
          console.log(`顾问 ${c.name} 的状态 ${c.status} ${isActive ? '符合' : '不符合'}条件`);
          return isActive;
        });
        console.log('状态正常的顾问数量:', activeConsultants.length);
        
        // 按类型筛选顾问
        const targetType = type === 'normal' ? 1 : 2;
        console.log('目标类型:', targetType);
        
        const filteredConsultants = activeConsultants.filter(c => {
          const matchesType = c.type === targetType;
          console.log(`顾问 ${c.name} 的类型 ${c.type} ${matchesType ? '符合' : '不符合'}目标类型 ${targetType}`);
          return matchesType;
        });
        
        if (filteredConsultants.length === 0) {
          console.log('筛选后没有符合条件的顾问，原因可能是：');
          console.log('1. 没有状态为1的顾问');
          console.log('2. 没有对应类型的顾问');
          this.$message.warning(`没有找到状态正常且类型为${type === 'normal' ? '辅导' : '申诉'}的顾问`);
          return;
        }

        // 计算最高客资数
        const counts = filteredConsultants.map(c => {
          const count = parseInt(c.countNormal) || 0; // 确保转换为数字
          console.log(`顾问 ${c.name} 的客资数: ${c.countNormal}, 转换后: ${count}`);
          return count;
        });
        const maxCount = Math.max(...counts);
        console.log('所有客资数:', counts);
        console.log('最高客资数:', maxCount);

        // 计算每个顾问的差值并构建显示数据
        const calculatedStats = filteredConsultants.map(c => {
          const currentCount = parseInt(c.countNormal) || 0;
          const diff = maxCount - currentCount;
          console.log(`顾问 ${c.name} 的差值计算: ${maxCount} - ${currentCount} = ${diff}`);
          
          const stat = {
            consultantId: c.id,
            consultantName: c.name,
            normalCount: currentCount,
            difference: diff,
            type: c.type,
            status: 1,
            createTime: new Date()
          };
          console.log(`顾问 ${c.name} 的最终统计数据:`, stat);
          return stat;
        });

        // 更新表格数据
        this.compensationList = calculatedStats;
        console.log('最终计算结果:', calculatedStats);

        // 保存统计结果到后端
        console.log('准备保存到后端的数据:', calculatedStats);
        const saveResponse = await request.post('/compensation/statistics', calculatedStats);

        if (saveResponse.data.code === 200) {
          console.log('保存成功，重新获取数据');
          await this.fetchCompensationList();
          console.log('重新获取的数据:', this.compensationList);
          this.$message.success('统计完成');
        } else {
          this.$message.error(saveResponse.data.msg || '统计失败');
        }

      } catch (error) {
        console.error('统计失败:', error);
        this.$message.error('统计失败');
      }
    },

    formatTime(time) {
      return this.$dayjs(time).format('YYYY-MM-DD HH:mm:ss')
    },

    async handleComplete(compensation) {
      console.log('待完成的补偿记录:', compensation);

      if (!compensation || !compensation.id) {
        this.$message.error('补偿记录无效');
        return;
      }

      try {
        const response = await request({
          url: '/compensation/complete',
          method: 'post',
          data: { 
            compensationId: String(compensation.id) // 确保发送字符串类型的 ID
          }
        });

        if (response.data.code === 200) {
          this.$message.success('标记完成成功');
          await this.fetchCompensationList();
        } else {
          this.$message.error(response.data.msg || '操作失败');
        }
      } catch (error) {
        console.error('操作失败:', error);
        this.$message.error('操作失败');
      }
    },

    async clearNormalCount(type) {
      try {
        const confirmResult = await this.$confirm(
          `确定要清零${type === 'normal' ? '辅导' : '申诉'}顾问的官号客资吗？`, 
          '警告', 
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        );

        if (confirmResult) {
          const response = await request({
            url: '/clear-normal-count',
            method: 'post',
            data: { type: type === 'normal' ? 1 : 2 }
          });

          if (response.data.code === 200) {
            this.$message.success(`${type === 'normal' ? '辅导' : '申诉'}顾问的官号客资已清零`);
            await this.fetchCompensationList();
          } else {
            this.$message.error(response.data.msg || '清零失败');
          }
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('清零失败:', error);
          this.$message.error('清零失败');
        }
      }
    },

    // 添加清除所有数据的方法
    async clearAllCompensation() {
      try {
        const confirmResult = await this.$confirm(
          '确定要清除所有统计数据吗？此操作不可恢复',
          '警告',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        );

        if (confirmResult === 'confirm') {
          this.clearLoading = true;
          const response = await request.post('/compensation/clear-all');
          
          if (response.data.code === 200) {
            this.$message.success('所有统计数据已清除');
            this.compensationList = []; // 清空本地数据
          } else {
            this.$message.error(response.data.msg || '清除失败');
          }
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('清除失败:', error);
          this.$message.error('清除失败：' + error.message);
        }
      } finally {
        this.clearLoading = false;
      }
    }
  },
  mounted() {
    this.fetchCompensationList()
  }
}
</script>

<style scoped>
.compensation-container {
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.highlight {
  color: #E6A23C;
  font-weight: bold;
}

.clear-all-button {
  margin-top: 20px;
  text-align: center;
}

.el-button + .el-button {
  margin-left: 10px;
}
</style> 