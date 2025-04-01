<template>
  <div class="log-container">
    <div class="header">
      <h2>操作日志</h2>
      <div class="filter-section">
        <el-input
          v-model="searchAccount"
          placeholder="搜索操作人"
          clearable
          style="width: 200px; margin-right: 10px"
          @clear="handleFilter"
          @input="handleFilter"
        />
        <el-select
          v-model="selectedModule"
          placeholder="选择模块"
          clearable
          style="width: 200px; margin-right: 10px"
          @change="handleFilter"
        >
          <el-option
            v-for="module in modules"
            :key="module"
            :label="module"
            :value="module"
          />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :shortcuts="dateShortcuts"
          @change="handleFilter"
        />
      </div>
    </div>

    <el-table
      v-loading="loading"
      :data="paginatedLogs"
      style="width: 100%"
      border
      stripe
    >
      <el-table-column prop="account" label="操作人" width="120" />
      <el-table-column label="操作内容" min-width="300">
        <template slot-scope="scope">
          {{ scope.row.operation }}
        </template>
      </el-table-column>
      <el-table-column prop="module" label="操作模块" width="120" />
      <el-table-column prop="ip" label="IP地址" width="140" />
      <el-table-column label="操作时间" width="180">
        <template slot-scope="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button
            type="primary"
            size="small"
            @click="showDetail(scope.row)"
          >
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="filteredLogs.length"
      >
      </el-pagination>
    </div>

    <el-dialog
      :visible.sync="dialogVisible"
      title="操作详情"
      width="60%"
    >
      <div v-if="selectedLog" class="log-detail">
        <div class="detail-item">
          <span class="label">操作人：</span>
          <span>{{ selectedLog.account }}</span>
        </div>
        <div class="detail-item">
          <span class="label">操作内容：</span>
          <span>{{ selectedLog.operation }}</span>
        </div>
        <div class="detail-item">
          <span class="label">操作模块：</span>
          <span>{{ selectedLog.module }}</span>
        </div>
        <div class="detail-item">
          <span class="label">操作方法：</span>
          <span>{{ selectedLog.method }}</span>
        </div>
        <div class="detail-item">
          <span class="label">操作参数：</span>
          <pre>{{ formatParams(selectedLog.params) }}</pre>
        </div>
        <div class="detail-item">
          <span class="label">IP地址：</span>
          <span>{{ selectedLog.ip }}</span>
        </div>
        <div class="detail-item">
          <span class="label">操作时间：</span>
          <span>{{ formatTime(selectedLog.createTime) }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'LogList',
  data() {
    return {
      logs: [],
      loading: false,
      searchAccount: '',
      selectedModule: '',
      dateRange: null,
      dialogVisible: false,
      selectedLog: null,
      dateShortcuts: [
        {
          text: '最近一周',
          value() {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
            return [start, end]
          }
        },
        {
          text: '最近一个月',
          value() {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
            return [start, end]
          }
        }
      ],
      currentPage: 1,
      pageSize: 20
    }
  },
  computed: {
    modules() {
      const moduleSet = new Set(this.logs.map(log => log.module))
      return Array.from(moduleSet)
    },
    filteredLogs() {
      return this.logs.filter(log => {
        const matchAccount = !this.searchAccount || 
          log.account.toLowerCase().includes(this.searchAccount.toLowerCase())
        
        const matchModule = !this.selectedModule || 
          log.module === this.selectedModule
        
        let matchDate = true
        if (this.dateRange && this.dateRange[0] && this.dateRange[1]) {
          const logDate = this.$dayjs(log.createTime)
          const start = this.$dayjs(this.dateRange[0])
          const end = this.$dayjs(this.dateRange[1])
          matchDate = logDate.isAfter(start) && logDate.isBefore(end)
        }
        
        return matchAccount && matchModule && matchDate
      })
    },
    paginatedLogs() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredLogs.slice(start, end);
    }
  },
  methods: {
    async fetchLogs() {
      this.loading = true
      try {
        const response = await request.get('/log/list')
        if (response.data.code === 200) {
          this.logs = response.data.data
        } else {
          this.$message.error(response.data.msg || '获取日志失败')
        }
      } catch (error) {
        console.error('获取日志失败:', error)
        this.$message.error('获取日志失败')
      } finally {
        this.loading = false
      }
    },
    formatTime(time) {
      return this.$dayjs(time).format('YYYY-MM-DD HH:mm:ss')
    },
    formatParams(params) {
      try {
        return JSON.stringify(JSON.parse(params), null, 2)
      } catch {
        return params
      }
    },
    showDetail(log) {
      this.selectedLog = log
      this.dialogVisible = true
    },
    handleFilter() {
      // 筛选逻辑已通过计算属性实现
    },
    formatOperation(log) {
      try {
        const params = JSON.parse(log.params)
        if (params && params.length > 0) {
          const consultant = params[0]  // 获取第一个参数，即顾问对象
          let operation = ''
          
          switch(log.method.split('.').pop()) {  // 获取方法名
            case 'addCountNormal':
              operation = `顾问[${consultant.name}]: 分配普通客资 ${consultant.countNormal-1}→${consultant.countNormal}`
              break
            case 'addCountSem':
              operation = `顾问[${consultant.name}]: 分配SEM客资 ${consultant.countSem-1}→${consultant.countSem}`
              break
            case 'skipNormal':
              operation = `顾问[${consultant.name}]: 跳过普通客资 (当前客资数: ${consultant.countNormal})`
              break
            case 'skipSem':
              operation = `顾问[${consultant.name}]: 跳过SEM客资 (当前客资数: ${consultant.countSem})`
              break
            case 'confirmNormal':
              operation = `顾问[${consultant.name}]: 确认普通客资 ${consultant.countNormal-1}→${consultant.countNormal}`
              break
            case 'confirmSem':
              operation = `顾问[${consultant.name}]: 确认SEM客资 ${consultant.countSem-1}→${consultant.countSem}`
              break
            default:
              operation = log.operation
          }
          return operation
        }
        return log.operation
      } catch (e) {
        return log.operation
      }
    },
    handleSizeChange(val) {
      this.pageSize = val;
      this.currentPage = 1; // 切换每页显示数量时重置到第一页
    },
    handleCurrentChange(val) {
      this.currentPage = val;
    }
  },
  mounted() {
    this.fetchLogs()
  }
}
</script>

<style scoped>
.log-container {
  padding: 20px;
}

.header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-section {
  display: flex;
  gap: 10px;
}

.log-detail {
  padding: 20px;
}

.detail-item {
  margin-bottom: 15px;
  line-height: 1.5;
}

.label {
  font-weight: bold;
  margin-right: 10px;
  color: #606266;
}

pre {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 5px 0;
}

.el-table .operation-cell {
  white-space: pre-wrap;
  word-break: break-all;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
