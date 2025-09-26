<template>
  
  <div>
    <el-header class="header-container">
      <!-- 左侧按钮组 -->
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
        <!-- <el-button
          
          class="type-button"
          @click="selectedType(3)"
        >
          AP
          
        </el-button> -->
      </el-button-group>
      
      <!-- 右侧按钮组 -->
      <div class="right-buttons">
<!-- 
        <el-button 
        @click="handleAddAP">
          新增AP
        </el-button> -->


        <el-popconfirm
          :title="`确定要清空所有${selectedType === 1 ? '辅导' : '申诉'}数据吗？`"
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

    <!-- 新增对话框 -->
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

        <!-- 新增对话框 -->
        <el-dialog :visible.sync="dialogVisibleAp" title="新增AP补客资">
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
          <el-button type="success" @click="submitFormAp" :disabled="!canSubmit">确认添加</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <div>
      <h1 style="font-size: 100px;">
        {{ 
  selectedType === 1 
    ? '辅导' 
    : selectedType === 2 
      ? '申诉' 
      : selectedType === 3 
        ? 'AP' 
        : '' 
}}
    </h1>
    </div>
  
    <!-- 使用 Flexbox 将表格并排显示 -->
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
          <el-table-column prop="count" label="客资" width="150">
            
          </el-table-column>
        </el-table>
        <!-- <el-button type="danger" @click="removeAll()">清空所有条目</el-button> -->
      </div>
    </div>
  </div>
</template>


<script>
import axios from 'axios';
import request from '@/utils/request';
import webSocket from '@/utils/websocket';
import { Message } from 'element-ui';

export default {

    data() {
        return{
          dialogVisible: false,
          dialogVisibleAp: false,
          selectedConsultants: [], // 选中的顾问ID数组
          allConsultants: [],     // 所有可选的顾问
          formData: {
        names: ''  // 用于存储粘贴的名单
      },
            consultantcompl:[],
            activeMenu: '1', 
            switchStatus:0,
            ws:null, //webSocket
            filteredConsultantsCompl: [],
            selectedType: 1,
            canSubmit: false,  // 控制确认添加按钮
            matchedData: [],    // 存储匹配到的顾问数据
        };
    },
    mounted(){
        this.show();
        this.initializeWebSocket();
        this.filterConsultants();
        this.$emit("data-ready", this.filteredConsultantsCompl);
        // 设置全局状态
        this.updateComplementStatus();
    },
    computed:{
      availableConsultants() {
        // 过滤掉已经在补充列表中的顾问
        return this.allConsultants.filter(consultant => 
          !this.consultantcompl.some(comp => comp.id === consultant.id)
        )
      }
    },
    watch: {
      filteredConsultants: function() {
        this.$nextTick(function() {
          this.$refs.normalTable.setCurrentRow(this.filteredConsultantsCompl[0])
        })
      },
      consultantcompl: {
        handler(newVal) {
          // 当补客资列表变化时更新全局状态
          this.updateComplementStatus();
        },
        deep: true
      }
    },
    methods:{
      addItem() {
      // 新增条目
      this.formData.items.push({ type: 1, name: '' });
    },
    removeItem(index) {
      // 删除条目
      this.formData.items.splice(index, 1);
    },
    handleFenpei(row) {
      row.waiting = 1
      this.$forceUpdate()
      request.post('/updateComplement', {
        complementId: row.complementId,
        waiting: row.waiting
      })
    },
    handleDelete(row) {
      request.post('/deleteComplement', {
        complementId: row.complementId
      })
      const index = this.consultantcompl.findIndex(item => item.complementId === row.complementId)
      if (index !== -1) {
        this.consultantcompl.splice(index, 1)
        this.filterConsultants()  // 重新过滤数据
      }
    },
    handleSkip(row) {
      row.waiting = 0
      this.$forceUpdate()
      request.post('/updateComplement', {
        complementId: row.complementId,
        waiting: row.waiting
      })
    },
   
    // 获取所有可选顾问
    async getAllConsultants() {
      try {
        console.log('开始获取顾问列表')
        const res = await request.get('/consultants')
        console.log('接口返回数据:', res)
        
        if (res.data && Array.isArray(res.data)) {  // 直接检查是否是数组
          this.allConsultants = res.data.map(consultant => ({
            id: consultant.id,
            name: consultant.name,
            type: parseInt(this.selectedType)  // 使用当前选中的类型
          }))
          console.log('设置顾问列表成功:', this.allConsultants)
        } else {
          console.error('接口返回格式错误:', res)
          this.$message.error('获取顾问列表失败')
        }
      } catch (error) {
        console.error('获取顾问列表失败, 详细错误:', error)
        this.$message.error('获取顾问列表失败')
      }
    },
    
    // 打开新增对话框
    handleAdd() {
      console.log('打开新增对话框')
      this.dialogVisible = true
      this.getAllConsultants() // 打开对话框时获取最新的顾问列表
    },
    handleAddAP() {
      console.log('打开新增对话框')
      this.dialogVisibleAp = true
      this.getAllConsultants()
    },

    // 检查匹配
checkMatch() {
  if (!this.formData.names.trim()) {
    Message.warning('请粘贴顾问名单')
    return
  }

  const names = this.formData.names.split('\n')
    .map(name => name.trim())
    .filter(name => name)

  const selectedData = []
  const notFound = []

  names.forEach((name, index) => {
    const numbers = name.match(/\d+/g)
    let consultant = null

    if (numbers) {
      const number = numbers[0]
      consultant = this.allConsultants.find(c => 
        c.name.includes(number) || c.id === number
      )
    }

    if (!consultant) {
      consultant = this.allConsultants.find(c => c.name === name)
        || this.allConsultants.find(c => c.name.includes(name))
    }

    if (consultant) {
      console.log(`匹配到顾问: ${consultant.name}, type: ${consultant.type}`)
      selectedData.push({
        id: consultant.id,
        name: consultant.name,
        type: consultant.type,
        waiting: 0,
        displayId: `${consultant.id}_${index}`
      })
    } else {
      notFound.push(name)
    }
  })

  this.matchedData = selectedData

  if (notFound.length > 0) {
    Message.warning(`以下顾问未找到：${notFound.join(', ')}`)
    this.canSubmit = false
  } else {
    Message.success(`成功匹配 ${selectedData.length} 个顾问`)
    this.canSubmit = true
  }

  console.log('最终匹配结果:', selectedData)
},

    // 提交表单
    async submitForm() {
      if (!this.matchedData.length) {
        Message.warning('请先匹配顾问')
        return
      }

      try {
        const res = await request.post('/addComplement', this.matchedData)
        if (res.data.code === 200) {
          Message.success('添加成功')
          this.dialogVisible = false
          this.formData.names = ''
          this.canSubmit = false
          this.matchedData = []
          this.show()
        } else {
          Message.error(res.data.msg || '添加失败')
        }
      } catch (error) {
        console.error('添加失败:', error)
        Message.error('添加失败')
      }
    },
    async submitFormAp() {
      if (!this.matchedData.length) {
        Message.warning('请先匹配顾问')
        return
      }
      this.matchedData = this.matchedData.map(item => {
        item.type = 3;
        return item;
      });

      try {
        const res = await request.post('/addComplement', this.matchedData)
        if (res.data.code === 200) {
          Message.success('添加成功')
          this.dialogVisible = false
          this.formData.names = ''
          this.canSubmit = false
          this.matchedData = []
          this.show()
        } else {
          Message.error(res.data.msg || '添加失败')
        }
      } catch (error) {
        console.error('添加失败:', error)
        Message.error('添加失败')
      }
    },
    
        show(){
            request.get('/complement')
            .then(res=>{
                if (res.data.code === 200) {
                    this.consultantcompl = res.data.data;
                    this.filterConsultants();
                }
            })
        },
      

        formatType(type){
            if(type==1){
                return '辅导';
            }
            else if(type==2){
                return '申诉';
            }
            else{
                return '未知';
            }
        },
       
    //WebSocket初始化
    initializeWebSocket() {
      webSocket.connect((data) => {
        try {
          const parsedData = JSON.parse(data)
          console.log('收到WebSocket消息:', parsedData)
          
          if (Array.isArray(parsedData)) {
            parsedData.forEach(this.updateConsultantData)
          } else {
            this.updateConsultantData(parsedData)
          }
        } catch (error) {
          console.error('处理WebSocket消息失败:', error)
        }
      })
    },
    filterConsultants() {
      this.filteredConsultantsCompl = this.consultantcompl.filter(item => 
        this.selectedType === "" || item.type === parseInt(this.selectedType)
      );
    },

    // 排序顾问数据的方法
    sortConsultants(data, countField) {
      return data.sort((a, b) => {
        if (a[countField] !== b[countField]) {
          return a[countField] - b[countField];  // 按照指定字段排序
        }
        return a.orderStatus - b.orderStatus;  // 如果指定字段相同，再按照 orderStatus 排序
      });
    },
    removeAll() {
        // 调用后端接口清除所有数据
        request.post('/clearAllConsCompl')
          .then(response => {
            this.$message.success('所有数据已清除');
            
            // 清除前端表格中的数据
            this.consultantcompl = [];
            this.filterConsultants(); // 更新显示
          })
          .catch(error => {
            console.error('清除失败', error.response || error);
            this.$message.error('清除失败');
          });
      },

    // WebSocket 消息处理
    updateConsultantData(data) {
      // 只更新complementId完全匹配的单条数据
      const index = this.consultantcompl.findIndex(c => c.complementId === data.complementId)
      if (index !== -1) {
        this.$set(this.consultantcompl, index, {
          ...this.consultantcompl[index],
          ...data
        })
        console.log(`更新顾问数据: complementId=${data.complementId}, name=${data.name}, waiting=${data.waiting}`)
      }
      
      // 更新过滤后的列表
      this.filterConsultants()
    },
    selectType(type) {
      this.selectedType = type
      this.filterConsultants()  // 调用原有的过滤方法
    },
    async removeAllByType(type) {
      try {
        const response = await request.post('/clearComplementByType', { type })
        if (response.data.code === 200) {
          this.$message.success(`已清空所有${type === 1 ? '辅导' : '申诉'}数据`)
          // 清除前端对应类型的数据
          this.consultantcompl = this.consultantcompl.filter(item => item.type !== type)
          this.filterConsultants()
        } else {
          this.$message.error(response.data.msg || '清除失败')
        }
      } catch (error) {
        if (!error.message.includes('Authentication failed')) {
          this.$message.error('清除失败：' + error.message)
        }
      }
    },
    updateComplementStatus() {
      const hasActive = this.consultantcompl.length > 0;
      this.$store.commit('setHasActiveComplement', {
        value: hasActive,
        type: this.selectedType
      });
    },
  }
  
        
}
  
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

  /* 分割线：设置表格的行间距 */

  /* 其他表格样式 */
  /* .divider {
    width: 100%;
    height: 1px;
    background-color: #ccc;
    margin-top: 20px;
    margin-bottom: 20px;
  } */

  
  .el-table--striped .el-table__body tr.el-table__row--striped.current-row td, .el-table__body tr.current-row>td {
    color: #fff;
    background-color: red!important;
}

.type-select {
  width: 200px; /* 固定宽度 */
}

/* 删除按钮样式 */
.delete-button {
  background-color: #F56C6C; /* 按钮背景色 */
  color: white; /* 图标颜色 */
  box-shadow: 0 2px 4px rgba(245, 108, 108, 0.3); /* 按钮阴影 */
  transition: transform 0.2s ease; /* 添加点击时的动画 */
}

.delete-button:hover {
  background-color: #D73737; /* 鼠标悬停变暗 */
  transform: scale(1.1); /* 悬停放大 */
}

.delete-button:active {
  transform: scale(0.95);
}

.header-container {
  display: flex;
  align-items: center; /* 垂直居中 */
  justify-content: space-between; /* 左右内容分散对齐 */
  padding: 0 20px;
  background-color: #F5F7FA; /* 浅灰背景 */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); /* 添加阴影效果 */
  height: 60px; /* 固定高度 */
  border-bottom: 1px solid #E4E7ED; /* 添加底部边框 */
}

.el-button-group {
  margin-right: 16px;
}

.el-button-group .el-button {
  margin-left: 0 !important;  /* 覆盖默认的按钮间距 */
}

.type-button {
  min-width: 80px;  /* 设置最小宽度使按钮大小一致 */
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