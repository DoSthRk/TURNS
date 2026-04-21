<template>
  <el-tabs type="border-card">
    <el-tab-pane label="轮班" class="turn">
      <div class="type-buttons">
        <el-button
          v-for="typeItem in enabledTypeOptions"
          :key="typeItem.consultantType"
          :type="selectedType === typeItem.consultantType ? 'primary' : 'success'"
          class="type-button"
          @click="selectType(typeItem.consultantType)"
        >
          {{ typeItem.typeLabel }}
        </el-button>

      </div>

      <div class="title-section">
        <h1 class="title-text">{{ currentTypeTitle }}</h1>
        <div class="inactive-consultants-panel" v-if="inactiveConsultants.length > 0">
          <div
            v-for="consultantItem in inactiveConsultants"
            :key="consultantItem.id"
            :class="{
              'pause-status': consultantItem.status === 3,
              'rest-status': consultantItem.status === 2
            }"
          >
            <span class="consultant-status">
              {{ consultantItem.name }}
              {{ consultantItem.status === 2 ? '正在休息' : '正在暂停' }}
              {{ consultantItem.status === 3 && consultantItem.description ? ': ' + consultantItem.description : '' }}
            </span>
          </div>
        </div>
      </div>

      <div v-if="activeModules.length === 0" class="empty-modules">
        当前类型暂无启用的轮班表格，请在轮班配置中添加并启用。
      </div>

      <template v-for="(moduleItem, moduleIndex) in activeModules">
        <el-table
          :key="moduleItem.moduleKey"
          :data="getModuleTableData(moduleItem)"
          class="module-table"
        >
          <el-table-column prop="name" :label="moduleItem.moduleLabel"></el-table-column>
          <el-table-column width="260">
            <template slot-scope="scope">
              <div v-if="isModuleSkipped(scope.row, moduleItem)">
                <el-button type="success" @click="restoreModule(scope.row, moduleItem)">补</el-button>
              </div>
              <div v-else>
                <el-button type="success" @click="assignModule(scope.row, moduleItem)">分配</el-button>
                <el-button type="danger" @click="skipModule(scope.row, moduleItem)">跳过</el-button>
                <el-button
                  v-if="Number(scope.row[moduleItem.waitingFieldName]) === 1"
                  type="info"
                  @click="confirmModule(scope.row, moduleItem)"
                >
                  已聊，等回复
                </el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column :prop="moduleItem.countFieldName" label="客资" width="120"></el-table-column>
        </el-table>
        <el-divider v-if="moduleIndex < activeModules.length - 1" :key="moduleItem.moduleKey + '-divider'">
          <i class="el-icon-paperclip"></i>
        </el-divider>
      </template>
    </el-tab-pane>

    <el-tab-pane label="总表">
      <Info ref="infoData"></Info>
    </el-tab-pane>

    <el-dialog
      title="模块与分类配置（管理员）"
      :visible.sync="configDialogVisible"
      width="860px"
    >
      <el-tabs v-model="configTab">
        <el-tab-pane label="模块配置" name="types">
          <el-alert
            title="模块即顶部按钮。可新增、启用/禁用和排序。"
            type="info"
            :closable="false"
            show-icon
          ></el-alert>
          <div class="config-actions">
            <el-button type="primary" plain icon="el-icon-plus" @click="addTypeOption">新增模块</el-button>
            <el-button type="success" :loading="savingTypeConfig" @click="saveTypeOptions">保存模块配置</el-button>
          </div>
          <el-table :data="typeConfigDraft">
            <el-table-column label="模块值" width="130">
              <template slot-scope="scope">
                <el-input-number v-model="scope.row.consultantType" :min="1" :max="999" size="small"></el-input-number>
              </template>
            </el-table-column>
            <el-table-column label="模块名称" min-width="220">
              <template slot-scope="scope">
                <el-input v-model.trim="scope.row.typeLabel" maxlength="30" show-word-limit></el-input>
              </template>
            </el-table-column>
            <el-table-column label="启用" width="120">
              <template slot-scope="scope">
                <el-switch v-model="scope.row.enabled" :active-value="1" :inactive-value="0"></el-switch>
              </template>
            </el-table-column>
            <el-table-column label="排序" width="120">
              <template slot-scope="scope">
                <el-input-number v-model="scope.row.displayOrder" :min="1" :max="200" size="small"></el-input-number>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template slot-scope="scope">
                <el-button type="text" class="danger-text" @click="removeTypeOption(scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="分类配置" name="modules">
          <!--
          <el-alert
            title="分类即模块下的表格。分类配置会同时影响“轮班”和“总表”。"
            type="info"
            :closable="false"
            show-icon
          ></el-alert>
          -->

          <div class="config-actions">
            <el-select
              v-model="moduleTargetType"
              placeholder="请选择要配置的模块"
              style="width: 260px;"
              @change="refreshModuleDraft"
            >
              <el-option
                v-for="typeItem in typeOptions"
                :key="'module-type-' + typeItem.consultantType"
                :label="`${typeItem.typeLabel}（${typeItem.consultantType}）`"
                :value="typeItem.consultantType"
              ></el-option>
            </el-select>
            <el-button type="primary" plain icon="el-icon-plus" @click="addModuleRow">新增分类</el-button>
            <el-button type="success" :loading="savingModuleConfig" @click="saveModuleConfig">保存分类配置</el-button>
          </div>

          <el-table :data="moduleDraft">
            <el-table-column label="分类来源" width="170">
              <template slot-scope="scope">
                {{ getModuleName(scope.row.moduleKey) }}
              </template>
            </el-table-column>
            <el-table-column label="分类标题" min-width="220">
              <template slot-scope="scope">
                <el-input v-model.trim="scope.row.moduleLabel" maxlength="30" show-word-limit></el-input>
              </template>
            </el-table-column>
            <el-table-column label="启用" width="120">
              <template slot-scope="scope">
                <el-switch v-model="scope.row.enabled" :active-value="1" :inactive-value="0"></el-switch>
              </template>
            </el-table-column>
            <el-table-column label="排序" width="120">
              <template slot-scope="scope">
                <el-input-number v-model="scope.row.displayOrder" :min="1" :max="20" size="small"></el-input-number>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template slot-scope="scope">
                <el-button type="text" class="danger-text" @click="removeModuleRow(scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </el-tabs>
</template>

<script>
import Info from './Info'
import request from '@/utils/request'
import webSocket from '@/utils/websocket'
import {
  MODULE_CATALOG,
  DEFAULT_TYPE_CONFIGS,
  normalizeTypeConfigs,
  buildModuleConfig,
  defaultModulesByType
} from '@/utils/shiftConfig'

export default {
  name: 'WorkDay',
  components: {
    Info
  },
  data() {
    return {
      consultant: [],
      selectedType: 1,
      typeOptions: [],
      moduleConfigMap: {},
      wsMessageHandler: null,
      hasComplementType1: false,
      hasComplementType2: false,
      hasComplementType4: false,

      configDialogVisible: false,
      configTab: 'types',
      typeConfigDraft: [],
      moduleTargetType: 1,
      moduleDraft: [],
      savingTypeConfig: false,
      savingModuleConfig: false
    }
  },
  computed: {
    isAdmin() {
      return this.$store.getters.isAdmin
    },
    enabledTypeOptions() {
      return this.typeOptions
        .filter(item => Number(item.enabled) === 1)
        .sort((a, b) => Number(a.displayOrder) - Number(b.displayOrder))
    },
    currentTypeConfig() {
      return this.typeOptions.find(item => item.consultantType === this.selectedType) || null
    },
    currentTypeTitle() {
      const baseTitle = this.currentTypeConfig ? this.currentTypeConfig.typeLabel : `类型${this.selectedType}`
      if (this.selectedType === 1 && this.hasComplementType1) {
        return `${baseTitle}正在补客资`
      }
      if (this.selectedType === 2 && this.hasComplementType2) {
        return `${baseTitle}正在补客资`
      }
      if (this.selectedType === 4 && this.hasComplementType4) {
        return `${baseTitle}正在补客资`
      }
      return baseTitle
    },
    inactiveConsultants() {
      return this.consultant
        .filter(item => Number(item.type) === this.selectedType && (item.status === 2 || item.status === 3))
        .sort((a, b) => b.status - a.status)
    },
    activeModules() {
      return this.getModulesForType(this.selectedType)
        .filter(item => Number(item.enabled) === 1)
        .sort((a, b) => Number(a.displayOrder) - Number(b.displayOrder))
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
      await Promise.all([this.fetchConsultants(), this.loadTypeOptions(), this.loadModuleConfigs(), this.checkComplementData()])
      this.ensureSelectedTypeValid()
    },
    async fetchConsultants() {
      try {
        const res = await request.get('/consultants')
        this.consultant = Array.isArray(res.data) ? res.data : []
      } catch (error) {
        this.consultant = []
        this.$message.error('获取顾问列表失败')
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
      if (!data || !data.id) {
        return
      }
      const index = this.consultant.findIndex(item => item.id === data.id)
      if (index !== -1) {
        this.$set(this.consultant, index, { ...this.consultant[index], ...data })
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
    ensureSelectedTypeValid() {
      if (this.enabledTypeOptions.length === 0) {
        if (this.typeOptions.length > 0) {
          this.selectedType = this.typeOptions[0].consultantType
        }
        return
      }
      const exists = this.enabledTypeOptions.some(item => item.consultantType === this.selectedType)
      if (!exists) {
        this.selectedType = this.enabledTypeOptions[0].consultantType
      }
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
    getModuleName(moduleKey) {
      const moduleItem = MODULE_CATALOG.find(item => item.moduleKey === moduleKey)
      return moduleItem ? moduleItem.moduleName : moduleKey
    },
    getModuleTableData(moduleItem) {
      const currentTypeConsultants = this.consultant.filter(
        item => Number(item.type) === this.selectedType && Number(item.status) === 1
      )

      const waitingList = currentTypeConsultants
        .filter(item => Number(item[moduleItem.waitingFieldName]) === 1)
        .sort((a, b) => this.compareByModule(a, b, moduleItem))

      const skippedList = currentTypeConsultants
        .filter(item => Number(item[moduleItem.orderStatusFieldName]) === 0)
        .sort((a, b) => this.compareByModule(a, b, moduleItem))

      const activeList = currentTypeConsultants
        .filter(
          item =>
            Number(item[moduleItem.orderStatusFieldName]) !== 0 &&
            Number(item[moduleItem.waitingFieldName]) !== 1
        )
        .sort((a, b) => this.compareByModule(a, b, moduleItem))

      const rows = []
      if (activeList.length > 0) {
        rows.push(activeList[0])
      }
      rows.push(...waitingList)
      rows.push(...skippedList)
      return rows
    },
    compareByModule(a, b, moduleItem) {
      const countA = Number(a[moduleItem.countFieldName] || 0)
      const countB = Number(b[moduleItem.countFieldName] || 0)
      if (countA !== countB) {
        return countA - countB
      }
      return Number(a.sortOrder || 0) - Number(b.sortOrder || 0)
    },
    isModuleSkipped(consultantItem, moduleItem) {
      return Number(consultantItem[moduleItem.orderStatusFieldName]) === 0
    },
    assignModule(consultantItem, moduleItem) {
      consultantItem[moduleItem.waitingFieldName] = 1
      this.updateConsultant(consultantItem, `${moduleItem.moduleKey}_add`)
    },
    skipModule(consultantItem, moduleItem) {
      consultantItem[moduleItem.orderStatusFieldName] = 0
      consultantItem[moduleItem.waitingFieldName] = 0
      this.updateConsultant(consultantItem, `${moduleItem.moduleKey}_skip`)
    },
    confirmModule(consultantItem, moduleItem) {
      consultantItem[moduleItem.waitingFieldName] = 0
      consultantItem[moduleItem.countFieldName] = Number(consultantItem[moduleItem.countFieldName] || 0) + 1
      this.updateConsultant(consultantItem, `${moduleItem.moduleKey}_confirm`)
    },
    restoreModule(consultantItem, moduleItem) {
      consultantItem[moduleItem.orderStatusFieldName] = 1
      consultantItem[moduleItem.waitingFieldName] = 1
      this.updateConsultant(consultantItem, `${moduleItem.moduleKey}_restore`)
    },
    async updateConsultant(consultantItem, operationType) {
      try {
        const response = await request.post('/updateConsultants', consultantItem, {
          params: { operationType }
        })
        if (response.data.code === 200) {
          this.$message.success(this.buildSuccessMessage(operationType))
        } else {
          this.$message.error(response.data.msg || '更新失败')
        }
      } catch (error) {
        console.error('更新顾问失败:', error)
        this.$message.error('更新失败')
      }
    },
    buildSuccessMessage(operationType) {
      if (!operationType) {
        return '更新成功'
      }
      if (operationType.includes('_add')) {
        return '分配客资成功'
      }
      if (operationType.includes('_skip')) {
        return '跳过客资成功'
      }
      if (operationType.includes('_confirm')) {
        return '确认客资成功'
      }
      if (operationType.includes('_restore')) {
        return '恢复分配状态成功'
      }
      return '更新成功'
    },
    selectType(typeValue) {
      this.selectedType = typeValue
    },
    openConfigDialog() {
      if (!this.isAdmin) {
        return
      }
      this.configDialogVisible = true
      this.configTab = 'types'
      this.typeConfigDraft = this.typeOptions.map(item => ({ ...item }))
      this.moduleTargetType = this.selectedType
      this.refreshModuleDraft()
    },
    addTypeOption() {
      const maxType = this.typeConfigDraft.reduce((max, item) => Math.max(max, Number(item.consultantType || 0)), 0)
      this.typeConfigDraft.push({
        consultantType: maxType + 1,
        typeLabel: `新类型${maxType + 1}`,
        enabled: 1,
        displayOrder: this.typeConfigDraft.length + 1
      })
    },
    removeTypeOption(index) {
      this.typeConfigDraft.splice(index, 1)
    },
    async saveTypeOptions() {
      this.savingTypeConfig = true
      try {
        const draft = this.typeConfigDraft.map(item => ({
          consultantType: Number(item.consultantType),
          typeLabel: item.typeLabel,
          enabled: Number(item.enabled) === 1 ? 1 : 0,
          displayOrder: Number(item.displayOrder)
        }))

        const duplicated = this.findDuplicateTypes(draft)
        if (duplicated.length > 0) {
          this.$message.error(`模块值重复：${duplicated.join('、')}`)
          return
        }

        const invalid = draft.filter(item => !Number.isInteger(item.consultantType) || item.consultantType <= 0)
        if (invalid.length > 0) {
          this.$message.error('模块值必须是大于 0 的整数')
          return
        }

        const payload = normalizeTypeConfigs(draft)
        if (payload.length === 0) {
          this.$message.error('请至少保留一个模块')
          return
        }

        const response = await request.post('/shift-config/type-options', payload)
        if (response.data.code === 200) {
          await this.loadTypeOptions()
          this.ensureSelectedTypeValid()
          this.$message.success('模块配置保存成功')
          this.typeConfigDraft = this.typeOptions.map(item => ({ ...item }))
          if (!this.typeOptions.some(item => item.consultantType === this.moduleTargetType)) {
            this.moduleTargetType = this.typeOptions[0].consultantType
            this.refreshModuleDraft()
          }
          if (this.$refs.infoData && typeof this.$refs.infoData.initPage === 'function') {
            this.$refs.infoData.initPage()
          }
        } else {
          this.$message.error(response.data.msg || '保存失败')
        }
      } catch (error) {
        console.error('保存模块配置失败:', error)
        this.$message.error('保存失败')
      } finally {
        this.savingTypeConfig = false
      }
    },
    findDuplicateTypes(payload) {
      const seen = new Set()
      const duplicates = []
      payload.forEach(item => {
        const key = Number(item.consultantType)
        if (seen.has(key) && !duplicates.includes(key)) {
          duplicates.push(key)
        } else {
          seen.add(key)
        }
      })
      return duplicates
    },
    refreshModuleDraft() {
      this.moduleDraft = this.getModulesForType(this.moduleTargetType).map(item => ({ ...item }))
    },
    addModuleRow() {
      const existingKeys = new Set(this.moduleDraft.map(item => item.moduleKey))
      const nextModule = MODULE_CATALOG.find(item => !existingKeys.has(item.moduleKey))
      if (!nextModule) {
        this.$message.warning('可添加的表格模块已全部添加')
        return
      }
      this.moduleDraft.push({
        consultantType: this.moduleTargetType,
        moduleKey: nextModule.moduleKey,
        moduleName: nextModule.moduleName,
        moduleLabel: nextModule.moduleName,
        countFieldName: nextModule.countFieldName,
        orderStatusFieldName: nextModule.orderStatusFieldName,
        waitingFieldName: nextModule.waitingFieldName,
        enabled: 1,
        displayOrder: this.moduleDraft.length + 1
      })
    },
    removeModuleRow(index) {
      this.moduleDraft.splice(index, 1)
    },
    async saveModuleConfig() {
      this.savingModuleConfig = true
      try {
        const payload = this.moduleDraft
          .map(item => buildModuleConfig(this.moduleTargetType, item))
          .filter(Boolean)
          .sort((a, b) => Number(a.displayOrder) - Number(b.displayOrder))
          .map(item => ({
            consultantType: this.moduleTargetType,
            moduleKey: item.moduleKey,
            moduleLabel: item.moduleLabel,
            enabled: Number(item.enabled) === 1 ? 1 : 0,
            displayOrder: Number(item.displayOrder) > 0 ? Number(item.displayOrder) : 1
          }))

        const response = await request.post(`/shift-config/types/${this.moduleTargetType}`, payload)
        if (response.data.code === 200) {
          await this.loadModuleConfigs()
          this.$message.success('分类配置保存成功')
          this.refreshModuleDraft()
          if (this.$refs.infoData && typeof this.$refs.infoData.initPage === 'function') {
            this.$refs.infoData.initPage()
          }
        } else {
          this.$message.error(response.data.msg || '保存失败')
        }
      } catch (error) {
        console.error('保存表格配置失败:', error)
        this.$message.error('保存失败')
      } finally {
        this.savingModuleConfig = false
      }
    },
    async checkComplementData() {
      try {
        const response = await request.get('/complement')
        if (response.data.code === 200) {
          const complementData = response.data.data || []
          this.hasComplementType1 = complementData.some(item => item.type === 1)
          this.hasComplementType2 = complementData.some(item => item.type === 2)
          this.hasComplementType4 = complementData.some(item => item.type === 4)
        }
      } catch (error) {
        console.error('获取补客资状态失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.type-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.type-button {
  margin: 0;
}

.title-section {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 12px;
}

.title-text {
  font-size: 64px;
  margin: 0;
  line-height: 1.2;
}

.inactive-consultants-panel {
  min-width: 320px;
  max-width: 520px;
  text-align: right;
  padding-top: 8px;
}

.consultant-status {
  font-weight: bold;
  line-height: 1.6;
  display: block;
}

.pause-status {
  color: #f56c6c;
  margin-bottom: 8px;
}

.rest-status {
  color: #909399;
  margin-bottom: 8px;
}

.module-table {
  margin-bottom: 6px;
}

.empty-modules {
  margin: 20px 0;
  color: #909399;
  font-size: 16px;
}

.config-actions {
  margin: 12px 0;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.danger-text {
  color: #f56c6c;
}

@media (max-width: 1200px) {
  .title-section {
    flex-direction: column;
  }

  .inactive-consultants-panel {
    text-align: left;
    min-width: auto;
    max-width: 100%;
  }
}
</style>
