<template>
  <div class="module-config-page">
    <h2 class="page-title">模块与分类配置</h2>

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
          title="分类即模块下的表格。分类配置会同时影响"轮班"和"总表"。"
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
          <el-table-column label="#" width="60" type="index" :index="i => i + 1"></el-table-column>
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
  </div>
</template>

<script>
import request from '@/utils/request'
import {
  MODULE_CATALOG,
  DEFAULT_TYPE_CONFIGS,
  normalizeTypeConfigs,
  buildModuleConfig,
  defaultModulesByType
} from '@/utils/shiftConfig'

export default {
  name: 'ModuleConfig',
  data() {
    return {
      configTab: 'types',
      typeOptions: [],
      moduleConfigMap: {},
      typeConfigDraft: [],
      moduleTargetType: 1,
      moduleDraft: [],
      savingTypeConfig: false,
      savingModuleConfig: false
    }
  },
  mounted() {
    this.init()
  },
  methods: {
    async init() {
      await Promise.all([this.loadTypeOptions(), this.loadModuleConfigs()])
      this.typeConfigDraft = this.typeOptions.map(item => ({ ...item }))
      this.refreshModuleDraft()
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
    addTypeOption() {
      const maxType = this.typeConfigDraft.reduce(
        (max, item) => Math.max(max, Number(item.consultantType || 0)), 0
      )
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
          this.$message.success('模块配置保存成功')
          this.typeConfigDraft = this.typeOptions.map(item => ({ ...item }))
          if (!this.typeOptions.some(item => item.consultantType === this.moduleTargetType)) {
            this.moduleTargetType = this.typeOptions[0]?.consultantType || 1
            this.refreshModuleDraft()
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
      const slot = MODULE_CATALOG[this.moduleDraft.length % MODULE_CATALOG.length]
      this.moduleDraft.push({
        consultantType: this.moduleTargetType,
        moduleKey: slot.moduleKey,
        moduleName: slot.moduleName,
        moduleLabel: '',
        countFieldName: slot.countFieldName,
        orderStatusFieldName: slot.orderStatusFieldName,
        waitingFieldName: slot.waitingFieldName,
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
        } else {
          this.$message.error(response.data.msg || '保存失败')
        }
      } catch (error) {
        console.error('保存表格配置失败:', error)
        this.$message.error('保存失败')
      } finally {
        this.savingModuleConfig = false
      }
    }
  }
}
</script>

<style scoped>
.module-config-page {
  padding: 8px 0;
}

.page-title {
  margin: 0 0 20px;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
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
</style>
