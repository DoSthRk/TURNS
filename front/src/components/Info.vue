<template>
  <div>
    <el-header class="header-container">
      <el-select v-model="selectedType" placeholder="选择类型" class="type-select">
        <el-option
          v-for="typeItem in enabledTypeOptions"
          :key="`type-${typeItem.consultantType}`"
          :label="typeItem.typeLabel"
          :value="typeItem.consultantType"
        ></el-option>
      </el-select>

    </el-header>

    <el-dialog
      title="轮班设置"
      :visible.sync="dialogVisible"
      width="56%"
      :before-close="handleSettingDialogClose"
    >
      <div class="dialog-header">
        <el-select
          v-model="selectedSettingType"
          placeholder="选择顾问类型"
          class="setting-type-select"
          @change="refreshSettingConsultants"
        >
          <el-option
            v-for="typeItem in typeOptions"
            :key="`setting-type-${typeItem.consultantType}`"
            :label="`${typeItem.typeLabel}（${typeItem.consultantType}）`"
            :value="typeItem.consultantType"
          ></el-option>
        </el-select>
      </div>

      <div class="consultants-container">
        <div class="consultant-group">
          <div class="group-title">{{ getTypeLabel(selectedSettingType) }}</div>
          <draggable
            v-model="settingConsultants"
            :options="{ ghostClass: 'ghost-item', animation: 200 }"
            @end="onDragEnd"
            class="draggable-list"
          >
            <div v-for="element in settingConsultants" :key="element.id" class="draggable-item">
              <span class="drag-handle"><i class="el-icon-rank"></i></span>
              <span class="consultant-id">{{ element.sortOrder }}</span>
              <span class="consultant-name">{{ element.name }}</span>
              <el-select
                v-model="element.workStatus"
                class="status-select"
                :class="{ 'status-rest': element.workStatus === 2, 'status-pause': element.workStatus === 3 }"
                @change="handleWorkStatusChange(element)"
              >
                <el-option label="正常" :value="1"></el-option>
                <el-option label="休息" :value="2"></el-option>
                <el-option label="暂停" :value="3"></el-option>
              </el-select>
              <el-button
                type="danger"
                size="mini"
                icon="el-icon-delete"
                circle
                class="delete-consultant-btn"
                @click.stop="handleDeleteConsultant(element)"
              />
            </div>
          </draggable>
          <add-advisor @refresh="handleAdvisorRefresh"></add-advisor>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      title="请输入暂停说明"
      :visible.sync="pauseReasonDialogVisible"
      width="30%"
      :before-close="handlePauseReasonDialogClose"
    >
      <el-form>
        <el-form-item label="补充提醒信息">
          <el-input
            v-model="pauseReason"
            type="textarea"
            :rows="3"
            placeholder="请输入暂停原因..."
          ></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="cancelPauseStatusChange">取消</el-button>
        <el-button type="primary" @click="confirmPauseStatusChange">确定</el-button>
      </span>
    </el-dialog>

    <div class="tables-flex-container">
      <div
        v-for="moduleItem in activeModules"
        :key="`table-${selectedType}-${moduleItem.moduleKey}`"
        class="table-container"
      >
        <div class="table-section">
          <el-table :data="getModuleRows(moduleItem)" :row-key="'id'" highlight-current-row>
            <el-table-column prop="name" label="姓名" width="160"></el-table-column>
            <el-table-column :prop="moduleItem.countFieldName" :label="moduleItem.moduleLabel" width="180">
              <template slot-scope="scope">
                <el-input-number
                  v-model="scope.row[moduleItem.countFieldName]"
                  :min="0"
                  :max="1000"
                  :disabled="scope.row.status === 0"
                  size="small"
                  @change="handleModuleCountChange(scope.row, moduleItem)"
                ></el-input-number>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'
import webSocket from '@/utils/websocket'
import draggable from 'vuedraggable'
import AddAdvisor from './AddAdvisor.vue'
import {
  DEFAULT_TYPE_CONFIGS,
  normalizeTypeConfigs,
  buildModuleConfig,
  defaultModulesByType
} from '@/utils/shiftConfig'

export default {
  name: 'InfoPanel',
  components: {
    draggable,
    AddAdvisor
  },
  data() {
    return {
      consultant: [],
      selectedType: 1,
      typeOptions: [],
      moduleConfigMap: {},

      dialogVisible: false,
      selectedSettingType: 1,
      settingConsultants: [],

      pauseReasonDialogVisible: false,
      pauseReason: '',
      currentConsultant: null,
      currentConsultantOldStatus: null,
      wsMessageHandler: null
    }
  },
  computed: {
    enabledTypeOptions() {
      return this.typeOptions
        .filter(item => Number(item.enabled) === 1)
        .sort((a, b) => Number(a.displayOrder) - Number(b.displayOrder))
    },
    activeModules() {
      return this.getModulesForType(this.selectedType)
        .filter(item => Number(item.enabled) === 1)
        .sort((a, b) => Number(a.displayOrder) - Number(b.displayOrder))
    }
  },
  watch: {
    selectedType() {
      this.ensureSelectedTypeValid()
    },
    dialogVisible(val) {
      if (val) {
        this.selectedSettingType = this.selectedType
        this.refreshSettingConsultants()
      }
    }
  },
  mounted() {
    this.initPage()
    this.initializeWebSocket()
  },
  beforeDestroy() {
    if (this.wsMessageHandler) {
      webSocket.removeMessageHandler(this.wsMessageHandler)
      this.wsMessageHandler = null
    }
  },
  methods: {
    async initPage() {
      await Promise.all([this.fetchConsultants(), this.loadTypeOptions(), this.loadModuleConfigs()])
      this.ensureSelectedTypeValid()
      this.refreshSettingConsultants()
    },
    async fetchConsultants() {
      try {
        const response = await request.get('/consultants')
        const source = Array.isArray(response.data) ? response.data : []
        this.consultant = source.map(item => ({
          ...item,
          workStatus: item.status
        }))
      } catch (error) {
        console.error('获取顾问数据失败:', error)
        this.$message.error('获取顾问数据失败')
      }
    },
    initializeWebSocket() {
      this.wsMessageHandler = (data) => {
        try {
          const parsedData = JSON.parse(data)
          this.updateConsultantData(parsedData)
        } catch (error) {
          console.error('解析WebSocket消息失败:', error)
        }
      }
      webSocket.connect(this.wsMessageHandler)
    },
    updateConsultantData(data) {
      const items = Array.isArray(data) ? data : [data]
      items.forEach(item => {
        if (!item || !item.id) return
        const index = this.consultant.findIndex(c => c.id === item.id)
        if (index >= 0) {
          this.$set(this.consultant, index, {
            ...this.consultant[index],
            ...item,
            workStatus: item.status
          })
        } else {
          this.consultant.push({
            ...item,
            workStatus: item.status
          })
        }
      })
      if (this.dialogVisible) {
        this.refreshSettingConsultants()
      }
    },
    async loadTypeOptions() {
      try {
        const response = await request.get('/shift-config/type-options')
        if (response.data.code === 200 && Array.isArray(response.data.data)) {
          this.typeOptions = normalizeTypeConfigs(response.data.data)
          return
        }
      } catch (error) {
        console.warn('读取按钮配置失败，使用默认配置', error)
      }
      this.typeOptions = normalizeTypeConfigs(DEFAULT_TYPE_CONFIGS)
    },
    async loadModuleConfigs() {
      try {
        const response = await request.get('/shift-config/all')
        if (response.data.code === 200 && response.data.data) {
          const loadedMap = {}
          Object.keys(response.data.data).forEach(key => {
            const typeValue = Number(key)
            loadedMap[typeValue] = this.normalizeModuleConfigsForType(typeValue, response.data.data[key])
          })
          this.moduleConfigMap = loadedMap
          return
        }
      } catch (error) {
        console.warn('读取表格配置失败，使用默认配置', error)
      }
      this.moduleConfigMap = {}
    },
    normalizeModuleConfigsForType(typeValue, configs) {
      const source = Array.isArray(configs) && configs.length > 0 ? configs : defaultModulesByType(typeValue)
      return source
        .map(item => buildModuleConfig(typeValue, item))
        .filter(Boolean)
        .sort((a, b) => Number(a.displayOrder) - Number(b.displayOrder))
    },
    getModulesForType(typeValue) {
      if (this.moduleConfigMap[typeValue] && this.moduleConfigMap[typeValue].length > 0) {
        return this.moduleConfigMap[typeValue]
      }
      return defaultModulesByType(typeValue)
    },
    ensureSelectedTypeValid() {
      const enabled = this.enabledTypeOptions
      if (enabled.length === 0) {
        if (this.typeOptions.length > 0) {
          this.selectedType = this.typeOptions[0].consultantType
        }
        return
      }
      if (!enabled.some(item => item.consultantType === this.selectedType)) {
        this.selectedType = enabled[0].consultantType
      }
      if (!this.typeOptions.some(item => item.consultantType === this.selectedSettingType)) {
        this.selectedSettingType = this.selectedType
      }
    },
    getTypeLabel(typeValue) {
      const current = this.typeOptions.find(item => item.consultantType === Number(typeValue))
      return current ? current.typeLabel : `类型${typeValue}`
    },
    getModuleRows(moduleItem) {
      return this.consultant
        .filter(item => Number(item.type) === Number(this.selectedType) && Number(item.status) === 1 && Number(item.workStatus) === 1)
        .sort((a, b) => {
          const countA = Number(a[moduleItem.countFieldName] || 0)
          const countB = Number(b[moduleItem.countFieldName] || 0)
          if (countA !== countB) return countA - countB
          return Number(a.sortOrder || 0) - Number(b.sortOrder || 0)
        })
    },
    handleModuleCountChange(consultant, moduleItem) {
      const operationMap = {
        normal: 'updateNormalCount',
        sem: 'updateSemCount',
        single1: 'updateSingle1Count'
      }
      const operationType = operationMap[moduleItem.moduleKey] || 'updateCount'
      this.updateConsultant(consultant, operationType)
    },
    async updateConsultant(consultant, operationType) {
      try {
        const response = await request.post('/updateConsultants', consultant, {
          params: { operationType }
        })
        if (response.data.code === 200) {
          this.$message.success('更新成功')
        } else {
          this.$message.error(response.data.msg || '更新失败')
        }
      } catch (error) {
        console.error('更新顾问失败:', error)
        this.$message.error('更新失败')
      }
    },
    openSettingDialog() {
      this.dialogVisible = true
      this.selectedSettingType = this.selectedType
      this.refreshSettingConsultants()
    },
    refreshSettingConsultants() {
      this.settingConsultants = this.consultant
        .filter(item => Number(item.type) === Number(this.selectedSettingType))
        .sort((a, b) => Number(a.sortOrder || 0) - Number(b.sortOrder || 0))
        .map(item => ({ ...item }))
    },
    async onDragEnd() {
      try {
        const updates = this.settingConsultants.map((item, index) => ({
          id: item.id,
          newOrder: index + 1,
          type: Number(this.selectedSettingType)
        }))

        const response = await request.post('/consultants/reorder', { updates })
        if (response.data.code === 200) {
          this.settingConsultants.forEach((item, index) => {
            item.sortOrder = index + 1
            const target = this.consultant.find(c => c.id === item.id)
            if (target) {
              target.sortOrder = index + 1
            }
          })
          this.$message.success('顾问排序更新成功')
        } else {
          this.$message.error(response.data.msg || '更新顺序失败')
          this.refreshSettingConsultants()
        }
      } catch (error) {
        console.error('更新顺序失败:', error)
        this.$message.error('更新顺序失败')
        this.refreshSettingConsultants()
      }
    },
    async handleWorkStatusChange(consultant) {
      if (consultant.workStatus === 3) {
        this.currentConsultant = consultant
        this.currentConsultantOldStatus = consultant.status
        this.pauseReason = consultant.description || ''
        this.pauseReasonDialogVisible = true
        return
      }

      if (consultant.workStatus === 1) {
        await this.restoreConsultantStatus(consultant)
        return
      }

      await this.updateConsultantStatus(consultant, consultant.workStatus)
    },
    async updateConsultantStatus(consultant, status) {
      try {
        const response = await request({
          url: '/status',
          method: 'post',
          params: {
            consultantId: consultant.id,
            status
          }
        })

        if (response.data.code === 200) {
          consultant.status = status
          consultant.workStatus = status
          const target = this.consultant.find(c => c.id === consultant.id)
          if (target) {
            target.status = status
            target.workStatus = status
          }
          this.$message.success('状态更新成功')
        } else {
          consultant.workStatus = consultant.status
          this.$message.error(response.data.msg || '状态更新失败')
        }
      } catch (error) {
        consultant.workStatus = consultant.status
        console.error('状态更新失败:', error)
        this.$message.error('状态更新失败')
      }
    },
    async restoreConsultantStatus(consultant) {
      try {
        if (consultant.status !== 2 && consultant.status !== 3) {
          consultant.workStatus = consultant.status
          return
        }
        const response = await request({
          url: '/restore',
          method: 'post',
          params: {
            consultantId: consultant.id,
            fromStatus: consultant.status
          }
        })

        if (response.data.code === 200) {
          consultant.status = 1
          consultant.workStatus = 1
          consultant.description = ''
          const target = this.consultant.find(c => c.id === consultant.id)
          if (target) {
            target.status = 1
            target.workStatus = 1
            target.description = ''
          }
          this.$message.success('状态恢复成功')
        } else {
          consultant.workStatus = consultant.status
          this.$message.error(response.data.msg || '状态恢复失败')
        }
      } catch (error) {
        consultant.workStatus = consultant.status
        console.error('状态恢复失败:', error)
        this.$message.error('状态恢复失败')
      }
    },
    handlePauseReasonDialogClose(done) {
      this.cancelPauseStatusChange()
      done()
    },
    cancelPauseStatusChange() {
      if (this.currentConsultant) {
        this.currentConsultant.workStatus = this.currentConsultantOldStatus
      }
      this.pauseReasonDialogVisible = false
      this.pauseReason = ''
      this.currentConsultant = null
      this.currentConsultantOldStatus = null
    },
    async confirmPauseStatusChange() {
      if (!this.currentConsultant) return

      const consultant = this.currentConsultant
      try {
        const statusResponse = await request({
          url: '/status',
          method: 'post',
          params: {
            consultantId: consultant.id,
            status: 3
          }
        })

        if (statusResponse.data.code !== 200) {
          consultant.workStatus = this.currentConsultantOldStatus
          this.$message.error(statusResponse.data.msg || '状态更新失败')
          return
        }

        consultant.status = 3
        consultant.workStatus = 3
        consultant.description = this.pauseReason

        await this.updateConsultant(consultant, 'updatePauseReason')

        const target = this.consultant.find(c => c.id === consultant.id)
        if (target) {
          target.status = 3
          target.workStatus = 3
          target.description = this.pauseReason
        }
        this.$message.success('状态更新成功')
      } catch (error) {
        consultant.workStatus = this.currentConsultantOldStatus
        console.error('状态更新失败:', error)
        this.$message.error('状态更新失败')
      } finally {
        this.pauseReasonDialogVisible = false
        this.pauseReason = ''
        this.currentConsultant = null
        this.currentConsultantOldStatus = null
      }
    },
    async handleDeleteConsultant(consultant) {
      try {
        await this.$confirm(`确认删除顾问「${consultant.name}」吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await request.post('/deleteConsultants', { id: consultant.id })
        this.consultant = this.consultant.filter(item => item.id !== consultant.id)
        this.settingConsultants = this.settingConsultants.filter(item => item.id !== consultant.id)
        this.$message.success('删除成功')
      } catch (error) {
        if (error !== 'cancel' && error !== 'close') {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      }
    },
    handleAdvisorRefresh() {
      this.initPage()
    },
    handleSettingDialogClose(done) {
      this.dialogVisible = false
      done()
    }
  }
}
</script>

<style scoped>
.header-container {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.type-select {
  width: 220px;
}

.tables-flex-container {
  display: flex;
  justify-content: flex-start;
  gap: 20px;
  flex-wrap: wrap;
}

.table-container {
  margin-bottom: 20px;
  flex: 0 0 auto;
}

.table-section {
  background-color: #fff;
  border-radius: 5px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 10px;
}

.dialog-header {
  margin-bottom: 20px;
}

.setting-type-select {
  width: 260px;
}

.consultants-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  max-width: 100%;
}

.consultant-group {
  flex: 1;
  min-width: 320px;
}

.group-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #409eff;
}

.draggable-list {
  min-height: 60px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 8px;
  background-color: #f5f7fa;
  margin-bottom: 12px;
}

.draggable-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  margin-bottom: 6px;
  background-color: #fff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.drag-handle {
  color: #909399;
  cursor: move;
}

.consultant-id {
  width: 28px;
  text-align: center;
  color: #909399;
}

.consultant-name {
  flex: 1;
}

.status-select {
  width: 100px;
}

.status-rest /deep/ .el-input__inner {
  background-color: #909399 !important;
  color: #fff !important;
  border-color: #909399 !important;
}

.status-pause /deep/ .el-input__inner {
  background-color: #f56c6c !important;
  color: #fff !important;
  border-color: #f56c6c !important;
}

.ghost-item {
  opacity: 0.45;
}
</style>

