<template>
  <div>
    <el-header class="header-container">
      <el-button-group>
        <el-button
          :type="selectedType === 1 ? 'primary' : 'primary'"
          @click="selectType(1)"
          class="type-button"
        >
          辅导
        </el-button>
        <el-button
          :type="selectedType === 2 ? 'warning' : 'warning'"
          @click="selectType(2)"
          class="type-button"
        >
          申诉
        </el-button>
        <el-button
          :type="selectedType === 4 ? 'success' : 'success'"
          @click="selectType(4)"
          class="type-button"
        >
          国际课程
        </el-button>
      </el-button-group>

      <div class="right-buttons">
        <el-popconfirm
          :title="`确定要清空所有${getTypeLabel(selectedType)}数据吗？`"
          @confirm="removeAllByType(selectedType)"
        >
          <el-button
            slot="reference"
            type="danger"
            icon="el-icon-delete"
            class="clear-button"
          >清空当前数据</el-button>
        </el-popconfirm>

        <el-button
          type="danger"
          class="delete-button"
          @click="handleAdd"
        >新增</el-button>
      </div>
    </el-header>

    <el-dialog :visible.sync="dialogVisible" title="新增补客资">
      <el-form :model="formData" ref="form">
        <el-form-item label="粘贴名单">
          <el-input
            type="textarea"
            v-model="formData.names"
            :rows="5"
            placeholder="请粘贴社群名单，每行一个名字"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="checkMatch">匹配社群</el-button>
          <el-button type="success" @click="submitForm" :disabled="!canSubmit">确认添加</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <div>
      <h1 style="font-size: 100px;">
        {{ selectedType === 1 ? '辅导' : selectedType === 2 ? '申诉' : selectedType === 4 ? '国际课程' : '' }}
      </h1>
    </div>

    <div class="table-container">
      <div class="table-section">
        <el-table :data="filteredConsultantsCompl" ref="normalTable">
          <el-table-column prop="name" label="姓名" width="150"></el-table-column>
          <el-table-column label="操作" width="200">
            <template slot-scope="scope">
              <el-button v-if="!scope.row.waiting" type="primary" @click="handleFenpei(scope.row)">分配</el-button>
              <template v-else>
                <el-button type="success" @click="handleDelete(scope.row)">已@，等回复</el-button>
                <el-button type="warning" @click="handleSkip(scope.row)">跳过</el-button>
              </template>
            </template>
          </el-table-column>
          <el-table-column prop="count" label="客资" width="150"></el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request';
import webSocket from '@/utils/websocket';
import { Message } from 'element-ui';

export default {
  data() {
    return {
      dialogVisible: false,
      allConsultants: [],
      formData: {
        names: ''
      },
      consultantcompl: [],
      filteredConsultantsCompl: [],
      selectedType: 1,
      canSubmit: false,
      matchedData: [],
      wsMessageHandler: null
    };
  },
  mounted() {
    this.show();
    this.initializeWebSocket();
    this.filterConsultants();
    this.$emit('data-ready', this.filteredConsultantsCompl);
    this.updateComplementStatus();
  },
  beforeDestroy() {
    if (this.wsMessageHandler) {
      webSocket.removeMessageHandler(this.wsMessageHandler);
      this.wsMessageHandler = null;
    }
  },
  watch: {
    consultantcompl: {
      handler() {
        this.updateComplementStatus();
      },
      deep: true
    }
  },
  methods: {
    getTypeLabel(type) {
      if (type === 1) return '辅导';
      if (type === 2) return '申诉';
      if (type === 4) return '国际课程';
      return '未知';
    },
    handleFenpei(row) {
      row.waiting = 1;
      this.$forceUpdate();
      request.post('/updateComplement', {
        complementId: row.complementId,
        waiting: row.waiting
      });
    },
    handleDelete(row) {
      request.post('/deleteComplement', {
        complementId: row.complementId
      });
      const index = this.consultantcompl.findIndex(item => item.complementId === row.complementId);
      if (index !== -1) {
        this.consultantcompl.splice(index, 1);
        this.filterConsultants();
      }
    },
    handleSkip(row) {
      row.waiting = 0;
      this.$forceUpdate();
      request.post('/updateComplement', {
        complementId: row.complementId,
        waiting: row.waiting
      });
    },
    async getAllConsultants() {
      try {
        const res = await request.get('/consultants');
        if (res.data && Array.isArray(res.data)) {
          this.allConsultants = res.data.map(consultant => ({
            id: consultant.id,
            name: consultant.name,
            type: parseInt(this.selectedType)
          }));
        } else {
          this.$message.error('获取顾问列表失败');
        }
      } catch (error) {
        console.error('获取顾问列表失败:', error);
        this.$message.error('获取顾问列表失败');
      }
    },
    handleAdd() {
      this.dialogVisible = true;
      this.getAllConsultants();
    },
    checkMatch() {
      if (!this.formData.names.trim()) {
        Message.warning('请粘贴顾问名单');
        return;
      }

      const names = this.formData.names.split('\n').map(name => name.trim()).filter(name => name);
      const selectedData = [];
      const notFound = [];

      names.forEach((name, index) => {
        const numbers = name.match(/\d+/g);
        let consultant = null;

        if (numbers) {
          const number = numbers[0];
          consultant = this.allConsultants.find(c => c.name.includes(number) || c.id === number);
        }

        if (!consultant) {
          consultant = this.allConsultants.find(c => c.name === name)
            || this.allConsultants.find(c => c.name.includes(name));
        }

        if (consultant) {
          selectedData.push({
            id: consultant.id,
            name: consultant.name,
            type: consultant.type,
            waiting: 0,
            displayId: `${consultant.id}_${index}`
          });
        } else {
          notFound.push(name);
        }
      });

      this.matchedData = selectedData;

      if (notFound.length > 0) {
        Message.warning(`以下顾问未找到：${notFound.join(', ')}`);
        this.canSubmit = false;
      } else {
        Message.success(`成功匹配 ${selectedData.length} 个顾问`);
        this.canSubmit = true;
      }
    },
    async submitForm() {
      if (!this.matchedData.length) {
        Message.warning('请先匹配顾问');
        return;
      }

      try {
        const res = await request.post('/addComplement', this.matchedData);
        if (res.data.code === 200) {
          Message.success('添加成功');
          this.dialogVisible = false;
          this.formData.names = '';
          this.canSubmit = false;
          this.matchedData = [];
          this.show();
        } else {
          Message.error(res.data.msg || '添加失败');
        }
      } catch (error) {
        console.error('添加失败:', error);
        Message.error('添加失败');
      }
    },
    show() {
      request.get('/complement').then(res => {
        if (res.data.code === 200) {
          this.consultantcompl = res.data.data;
          this.filterConsultants();
        }
      });
    },
    initializeWebSocket() {
      this.wsMessageHandler = (data) => {
        try {
          const parsedData = JSON.parse(data);
          if (Array.isArray(parsedData)) {
            parsedData.forEach(this.updateConsultantData);
          } else {
            this.updateConsultantData(parsedData);
          }
        } catch (error) {
          console.error('处理WebSocket消息失败:', error);
        }
      };
      webSocket.connect(this.wsMessageHandler);
    },
    filterConsultants() {
      this.filteredConsultantsCompl = this.consultantcompl.filter(item =>
        this.selectedType === '' || item.type === parseInt(this.selectedType)
      );
    },
    updateConsultantData(data) {
      const index = this.consultantcompl.findIndex(c => c.complementId === data.complementId);
      if (index !== -1) {
        this.$set(this.consultantcompl, index, {
          ...this.consultantcompl[index],
          ...data
        });
      }
      this.filterConsultants();
    },
    selectType(type) {
      this.selectedType = type;
      this.filterConsultants();
      this.updateComplementStatus();
    },
    async removeAllByType(type) {
      try {
        const response = await request.post('/clearComplementByType', { type });
        if (response.data.code === 200) {
          this.$message.success(`已清空所有${this.getTypeLabel(type)}数据`);
          this.consultantcompl = this.consultantcompl.filter(item => item.type !== type);
          this.filterConsultants();
          this.updateComplementStatus();
        } else {
          this.$message.error(response.data.msg || '清除失败');
        }
      } catch (error) {
        if (!error.message.includes('Authentication failed')) {
          this.$message.error('清除失败：' + error.message);
        }
      }
    },
    updateComplementStatus() {
      const hasActive = this.consultantcompl.some(item => item.type === this.selectedType);
      this.$store.commit('setHasActiveComplement', {
        value: hasActive,
        type: this.selectedType
      });
    }
  }
};
</script>

<style>
.el-header {
  background-color: #B3C0D1;
  color: #333;
  line-height: 60px;
}

.el-aside {
  color: #333;
}

.table-container {
  display: flex;
  justify-content: space-between;
  gap: 20px;
}

.table-section {
  flex: 1;
}

.el-table--striped .el-table__body tr.el-table__row--striped.current-row td, .el-table__body tr.current-row>td {
  color: #fff;
  background-color: red!important;
}

.type-select {
  width: 200px;
}

.delete-button {
  background-color: #F56C6C;
  color: white;
  box-shadow: 0 2px 4px rgba(245, 108, 108, 0.3);
  transition: transform 0.2s ease;
}

.delete-button:hover {
  background-color: #D73737;
  transform: scale(1.1);
}

.delete-button:active {
  transform: scale(0.95);
}

.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background-color: #F5F7FA;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  height: 60px;
  border-bottom: 1px solid #E4E7ED;
}

.el-button-group {
  margin-right: 16px;
}

.el-button-group .el-button {
  margin-left: 0 !important;
}

.type-button {
  min-width: 80px;
}

.right-buttons {
  display: flex;
  gap: 10px;
  align-items: center;
}

.clear-button {
  background-color: #F56C6C;
  color: white;
  box-shadow: 0 2px 4px rgba(245, 108, 108, 0.3);
  transition: all 0.2s ease;
}

.clear-button:hover {
  background-color: #D73737;
  transform: scale(1.05);
}

.clear-button:active {
  transform: scale(0.95);
}
</style>
