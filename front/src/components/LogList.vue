<template>
  <div class="log-container">
    <div class="header">
      <h2>Operation Logs</h2>
      <div class="filter-section">
        <el-input
          v-model="searchAccount"
          placeholder="Search account"
          clearable
          style="width: 200px; margin-right: 10px"
          @clear="handleFilter"
          @input="handleFilter"
        />
        <el-input
          v-model="selectedModule"
          placeholder="Module (exact)"
          clearable
          style="width: 200px; margin-right: 10px"
          @clear="handleFilter"
          @input="handleFilter"
        />
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="to"
          start-placeholder="Start date"
          end-placeholder="End date"
          :shortcuts="dateShortcuts"
          @change="handleFilter"
        />
      </div>
    </div>

    <el-table
      v-loading="loading"
      :data="logs"
      style="width: 100%"
      border
      stripe
    >
      <el-table-column prop="account" label="Account" width="120" />
      <el-table-column prop="operation" label="Operation" min-width="300" />
      <el-table-column prop="module" label="Module" width="120" />
      <el-table-column prop="ip" label="IP" width="140" />
      <el-table-column label="Time" width="180">
        <template slot-scope="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="Action">
        <template slot-scope="scope">
          <el-button type="primary" size="small" @click="showDetail(scope.row)">Detail</el-button>
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
        :total="total"
      />
    </div>

    <el-dialog :visible.sync="dialogVisible" title="Operation Detail" width="60%">
      <div v-if="selectedLog" class="log-detail">
        <div class="detail-item"><span class="label">Account:</span><span>{{ selectedLog.account }}</span></div>
        <div class="detail-item"><span class="label">Operation:</span><span>{{ selectedLog.operation }}</span></div>
        <div class="detail-item"><span class="label">Module:</span><span>{{ selectedLog.module }}</span></div>
        <div class="detail-item"><span class="label">Method:</span><span>{{ selectedLog.method }}</span></div>
        <div class="detail-item"><span class="label">Params:</span><pre>{{ formatParams(selectedLog.params) }}</pre></div>
        <div class="detail-item"><span class="label">IP:</span><span>{{ selectedLog.ip }}</span></div>
        <div class="detail-item"><span class="label">Time:</span><span>{{ formatTime(selectedLog.createTime) }}</span></div>
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
      total: 0,
      loading: false,
      searchAccount: '',
      selectedModule: '',
      dateRange: null,
      dialogVisible: false,
      selectedLog: null,
      currentPage: 1,
      pageSize: 20,
      dateShortcuts: [
        {
          text: 'Last 7 days',
          value() {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
            return [start, end]
          }
        },
        {
          text: 'Last 30 days',
          value() {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
            return [start, end]
          }
        }
      ]
    }
  },
  methods: {
    buildQueryParams() {
      const params = {
        page: this.currentPage,
        size: this.pageSize
      }

      if (this.searchAccount && this.searchAccount.trim()) {
        params.account = this.searchAccount.trim()
      }
      if (this.selectedModule && this.selectedModule.trim()) {
        params.module = this.selectedModule.trim()
      }
      if (this.dateRange && this.dateRange[0] && this.dateRange[1]) {
        params.startTime = this.$dayjs(this.dateRange[0]).startOf('day').format('YYYY-MM-DD HH:mm:ss')
        params.endTime = this.$dayjs(this.dateRange[1]).endOf('day').format('YYYY-MM-DD HH:mm:ss')
      }

      return params
    },
    async fetchLogs() {
      this.loading = true
      try {
        const response = await request.get('/log/page', { params: this.buildQueryParams() })
        if (response.data.code === 200) {
          const data = response.data.data || {}
          this.logs = data.records || []
          this.total = data.total || 0
        } else {
          this.$message.error(response.data.msg || 'Failed to fetch logs')
        }
      } catch (error) {
        console.error('Failed to fetch logs:', error)
        this.$message.error('Failed to fetch logs')
      } finally {
        this.loading = false
      }
    },
    handleFilter() {
      this.currentPage = 1
      this.fetchLogs()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
      this.fetchLogs()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchLogs()
    },
    showDetail(log) {
      this.selectedLog = log
      this.dialogVisible = true
    },
    formatTime(time) {
      if (!time) {
        return '-'
      }
      return this.$dayjs(time).format('YYYY-MM-DD HH:mm:ss')
    },
    formatParams(params) {
      try {
        return JSON.stringify(JSON.parse(params), null, 2)
      } catch {
        return params || '-'
      }
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
