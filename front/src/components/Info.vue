<template>
  <div>
    <el-header class="header-container">
      <el-select v-model="selectedType" @change="filterConsultants" placeholder="选择类型" class="type-select">
        <el-option label="辅导" value="1"></el-option>
        <el-option label="申诉" value="2"></el-option>
        <el-option label="抖音/B站国际课程" value="3"></el-option>
        <el-option label="B站申诉" value="4"></el-option>

      </el-select>
      
      <el-button
        type="danger"
        icon="el-icon-setting"
        @click="dialogVisible=true"
      >轮班设置</el-button>
    </el-header>

    <el-dialog
  title="轮班设置"
  :visible.sync="dialogVisible"
  width="90%"
  :before-close="handleClose">

  <div class="consultants-container">
    <!-- 辅导顾问 -->
    <div class="consultant-group">
      <div class="group-title">辅导社群</div>
      <draggable 
        v-model="consultantType1" 
        :options="{group:'consultant-group1', ghostClass: 'ghost-item', animation: 200}"
        @end="onDragEnd(1)"
        class="draggable-list"
        :move="checkMove"
      >
        <div 
          v-for="element in consultantType1" 
          :key="element.id"
          class="draggable-item"
        >
          <span class="drag-handle"><i class="el-icon-rank"></i></span>
          <span class="consultant-id">{{ element.sortOrder }}</span>
          <span class="consultant-name">{{ element.name }}</span>
          <el-select v-model="element.workStatus" @change="handleWorkStatusChange(element)" class="status-select" 
            :class="{'status-rest': element.workStatus === 2, 'status-pause': element.workStatus === 3}">
            <el-option label="正常" :value="1"></el-option>
            <el-option label="休息" :value="2" class="option-rest"></el-option>
            <el-option label="暂停" :value="3" class="option-pause"></el-option>
          </el-select>
        </div>
      </draggable>
    </div>

    <!-- 申诉顾问 -->
    <div class="consultant-group">
      <div class="group-title">申诉社群</div>
      <draggable 
        v-model="consultantType2" 
        :options="{group:'consultant-group2', ghostClass: 'ghost-item', animation: 200}"
        @end="onDragEnd(2)"
        class="draggable-list"
        :move="checkMove"
      >
        <div 
          v-for="element in consultantType2" 
          :key="element.id"
          class="draggable-item"
        >
          <span class="drag-handle"><i class="el-icon-rank"></i></span>
          <span class="consultant-id">{{ element.sortOrder }}</span>
          <span class="consultant-name">{{ element.name }}</span>
          <el-select v-model="element.workStatus" @change="handleWorkStatusChange(element)" class="status-select" 
            :class="{'status-rest': element.workStatus === 2, 'status-pause': element.workStatus === 3}">
            <el-option label="正常" :value="1"></el-option>
            <el-option label="休息" :value="2" class="option-rest"></el-option>
            <el-option label="暂停" :value="3" class="option-pause"></el-option>
          </el-select>
        </div>
      </draggable>
    </div>

    <!-- 抖音顾问 -->
    <div class="consultant-group">
      <div class="group-title">抖音/B站国际课程社群</div>
      <draggable 
        v-model="consultantType3" 
        :options="{group:'consultant-group3', ghostClass: 'ghost-item', animation: 200}"
        @end="onDragEnd(3)"
        class="draggable-list"
        :move="checkMove"
      >
        <div 
          v-for="element in consultantType3" 
          :key="element.id"
          class="draggable-item"
        >
          <span class="drag-handle"><i class="el-icon-rank"></i></span>
          <span class="consultant-id">{{ element.sortOrder }}</span>
          <span class="consultant-name">{{ element.name }}</span>
          <el-select v-model="element.workStatus" @change="handleWorkStatusChange(element)" class="status-select" 
            :class="{'status-rest': element.workStatus === 2, 'status-pause': element.workStatus === 3}">
            <el-option label="正常" :value="1"></el-option>
            <el-option label="休息" :value="2" class="option-rest"></el-option>
            <el-option label="暂停" :value="3" class="option-pause"></el-option>
          </el-select>
        </div>
      </draggable>
    </div>

    <div class="consultant-group">
      <div class="group-title">B站申诉社群</div>
      <draggable 
        v-model="consultantType4" 
        :options="{group:'consultant-group3', ghostClass: 'ghost-item', animation: 200}"
        @end="onDragEnd(4)"
        class="draggable-list"
        :move="checkMove"
      >
        <div 
          v-for="element in consultantType4" 
          :key="element.id"
          class="draggable-item"
        >
          <span class="drag-handle"><i class="el-icon-rank"></i></span>
          <span class="consultant-id">{{ element.sortOrder }}</span>
          <span class="consultant-name">{{ element.name }}</span>
          <el-select v-model="element.workStatus" @change="handleWorkStatusChange(element)" class="status-select" 
            :class="{'status-rest': element.workStatus === 2, 'status-pause': element.workStatus === 3}">
            <el-option label="正常" :value="1"></el-option>
            <el-option label="休息" :value="2" class="option-rest"></el-option>
            <el-option label="暂停" :value="3" class="option-pause"></el-option>
          </el-select>
        </div>
      </draggable>
    </div>

    <add-advisor></add-advisor>

    
  </div>
</el-dialog>

<!-- 暂停原因输入对话框 -->
<el-dialog
  title="请输入补充提醒信息"
  :visible.sync="pauseReasonDialogVisible"
  width="30%"
  :before-close="handlePauseReasonDialogClose">
  <el-form>
    <el-form-item label="补充提醒信息">
      <el-input 
        v-model="pauseReason" 
        type="textarea" 
        :rows="3"
        placeholder="请输入补充提醒信息...">
      </el-input>
    </el-form-item>
  </el-form>
  <span slot="footer" class="dialog-footer">
    <el-button @click="cancelPauseStatusChange">取 消</el-button>
    <el-button type="primary" @click="confirmPauseStatusChange">确 定</el-button>
  </span>
</el-dialog>

    <!-- 表格容器，使用flex布局并排显示 -->
    <div class="tables-flex-container">
      <!-- 官号表格 - 辅导和申诉类型显示 -->
      <div v-if="selectedType === '1' || selectedType === '2'" class="table-container">
        <div class="table-section">
          <el-table :data="filteredConsultantsByNormal" ref="normalTable" highlight-current-row>
            <el-table-column prop="name" label="姓名" width="150"></el-table-column>
            <el-table-column prop="countNormal" label="官号客资" width="150">
              <template slot-scope="scope">
                <el-input-number
                  v-model="scope.row.countNormal"
                  :min="0"
                  :max="1000"
                  :disabled="scope.row.status === 0"  
                  @change="handleNormalCountChange(scope.row)"
                  size="small"
                ></el-input-number>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- SEM表格 - 辅导和申诉类型显示 -->
      <div v-if="selectedType === '1' || selectedType === '2'" class="table-container">
        <div class="table-section">
          <el-table :data="filteredConsultantsBySem" ref="semTable" highlight-current-row>
            <el-table-column prop="name" label="姓名" width="150"></el-table-column>
            <el-table-column prop="countSem" label="SEM客资" width="150">
              <template slot-scope="scope">
                <el-input-number
                  v-model="scope.row.countSem"
                  :min="0"
                  :max="1000"
                  :disabled="scope.row.status === 0"  
                  @change="handleSemCountChange(scope.row)"
                  size="small"
                ></el-input-number>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- AP/Alevel表格 - 只有辅导类型显示 -->
      <div v-if="selectedType === '1'" class="table-container">
        <div class="table-section">
          <el-table :data="filteredConsultantsBySingle1" ref="single1Table" highlight-current-row>
            <el-table-column prop="name" label="姓名" width="150"></el-table-column>
            <el-table-column prop="countSingle1" label="AP/Alevel客资" width="150">
              <template slot-scope="scope">
                <el-input-number
                  v-model="scope.row.countSingle1"
                  :min="0"
                  :max="1000"
                  :disabled="scope.row.status === 0"  
                  @change="handleSingle1CountChange(scope.row)"
                  size="small"
                ></el-input-number>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 抖音表格 - 只有抖音类型显示 -->
      <div v-if="selectedType === '3'" class="table-container" style="flex: 1;">
        <div class="table-section">
          <el-table :data="filteredConsultantsBySingle1" ref="single1Table" highlight-current-row>
            <el-table-column prop="name" label="姓名" width="150"></el-table-column>
            <el-table-column prop="countSingle1" label="抖音/B站国际课程" width="150">
              <template slot-scope="scope">
                <el-input-number
                  v-model="scope.row.countSingle1"
                  :min="0"
                  :max="1000"
                  :disabled="scope.row.status === 0"  
                  @change="handleDyCountChange(scope.row)"
                  size="small"
                ></el-input-number>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 国际课程表格-->
      <div v-if="selectedType === '4'" class="table-container" style="flex: 1;">
        <div class="table-section">
          <el-table :data="filteredConsultantsBySingle1" ref="single1Table" highlight-current-row>
            <el-table-column prop="name" label="姓名" width="150"></el-table-column>
            <el-table-column prop="countSingle1" label="B站申诉客资" width="150">
              <template slot-scope="scope">
                <el-input-number
                  v-model="scope.row.countSingle1"
                  :min="0"
                  :max="1000"
                  :disabled="scope.row.status === 0"  
                  @change="handleICCountChange(scope.row)"
                  size="small"
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

export default {
  components: {
    draggable,
    AddAdvisor
  },

    data() {
        return{
            dialogVisible:false,
            consultant:[],
            consultantType1: [],
            consultantType2: [],
            consultantType3: [],
            consultantType4: [],
            activeMenu: '1', 
            switchStatus:0,
            filteredConsultantsByNormal: [] ,
            filteredConsultantsBySem:[],
            filteredConsultantsBySingle1: [],
            filteredConsultantsByDy: [], 
            tempConsultants: [],
            selectedType: "1",
            pauseReasonDialogVisible: false,
            pauseReason: '',
            currentConsultant: null,
        };
    },
    
    mounted(){
        this.show();
        this.initializeWebSocket();
        this.filterConsultants();
        this.$emit("data-ready", this.filteredConsultantsByNormal);
        
    },
    beforeDestroy() {
        webSocket.close();
    },
    computed:{

    },
    watch: {
      filteredConsultantsByNormal: function() {
        this.$nextTick(function() {
          this.$refs.normalTable.setCurrentRow(this.filteredConsultantsByNormal[0])
        })
      },
      filteredConsultantsBySem:function(){
        this.$nextTick(function(){
          this.$refs.semTable.setCurrentRow(this.filteredConsultantsBySem[0])
        })
      },
      filteredConsultantsBySingle1:function(){
        this.$nextTick(function(){
          this.$refs.single1Table.setCurrentRow(this.filteredConsultantsBySingle1[0])
        })
      },
      dialogVisible: function(newVal) {
        if (newVal) {
          // 对话框打开时，重新获取顾问数据并更新分组
          console.log('对话框已打开，重新获取顾问数据');
          this.show();
        }
      },
      consultant: {
        deep: true,
        immediate: true,
        handler(val) {
          console.log('consultant watch触发，数据长度:', val ? val.length : 0);
          if (val && val.length > 0) {
            const type1 = val.filter(c => c.type === 1);
            const type2 = val.filter(c => c.type === 2);
            const type3 = val.filter(c => c.type === 3);
            const type4 = val.filter(c => c.type === 4);
            
            console.log('过滤后的顾问数量 - 辅导:', type1.length, '申诉:', type2.length, '对客:', type3.length, type4.length);
            
            this.consultantType1 = type1.sort((a, b) => a.sortOrder - b.sortOrder);
            this.consultantType2 = type2.sort((a, b) => a.sortOrder - b.sortOrder);
            this.consultantType3 = type3.sort((a, b) => a.sortOrder - b.sortOrder);
            this.consultantType4 = type4.sort((a, b) => a.sortOrder - b.sortOrder);
            
            console.log('consultantType1:', this.consultantType1);
            console.log('consultantType2:', this.consultantType2);
            console.log('consultantType3:', this.consultantType3);
            console.log('consultantType4:', this.consultantType4);
          }
        }
      }
    },
    methods:{
        show(){
            console.log('开始获取顾问数据');
            request.get('/consultants')
            .then(res=>{
                console.log('获取顾问数据成功，原始数据：', res.data);
                if (!res.data || res.data.length === 0) {
                    console.warn('顾问数据为空！');
                    return;
                }
                this.consultant = res.data.map(c => ({
                  ...c,
                  workStatus: c.status
                }));
                console.log('处理后的顾问数据：', this.consultant);
                
                // 检查是否有type=4的顾问
                const type4Consultants = this.consultant.filter(c => c.type === 4);
                console.log('Type=4顾问数量:', type4Consultants.length);
                console.log('Type=4顾问详情:', type4Consultants);
                
                // 更新顾问类型分组
                this.updateConsultantGroups();
                
                // 过滤顾问数据
                this.filterConsultants();
            })
            .catch(err => {
                console.error('获取顾问数据失败:', err);
            });
        },
        toggleConsultantStatus(consultant) {
          if (consultant.status === 0) {
            const index = this.consultant.indexOf(consultant);
            if (index !== -1) {
                this.consultant.splice(index, 1);  // 从顾问数组中移除
                this.tempConsultants.push(consultant);  // 放入临时数组
                console.log('tempconsultants:'+this.tempConsultants)
            }
        } else {
            // 如果状态为1，恢复顾问到主数组
            const index = this.tempConsultants.indexOf(consultant);
            if (index !== -1) {
                this.tempConsultants.splice(index, 1);  // 从临时数组中移除
                this.consultant.push(consultant);  // 恢复到顾问数组
                console.log('tempconsultants:'+this.tempConsultants)
            }
        }
        
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
        handleMenuSelect(index) {
      if (index === '1') {
        this.$router.push('/WorkDay'); 
      } else if (index === '2') {
        this.$router.push('/Info');
      } else if (index === '3'){
        this.$router.push('/Order')
      }
    },

   
      
    // },
    //WebSocket初始化
    initializeWebSocket(){
      webSocket.connect((data) => {
        try {
          const parsedData = JSON.parse(data)
          console.log('收到数据', parsedData)
          this.updateConsultantData(parsedData)
        } catch (error) {
          console.error('解析WebSocket消息失败:', error)
        }
      })
    },
    updateConsultantData(data){
      console.log('处理WebSocket数据:', data);
      
      // 检查数据是否为数组
      if (Array.isArray(data)) {
        // 处理批量更新
        data.forEach(item => {
          this.updateSingleConsultant(item);
        });
      } else {
        // 处理单个顾问更新
        this.updateSingleConsultant(data);
      }
      
      // 更新顾问类型分组
      this.updateConsultantGroups();
      
      // 重新过滤顾问数据以更新表格
      this.filterConsultants();
    },
    
    // 更新单个顾问数据
    updateSingleConsultant(data) {
      const index = this.consultant.findIndex(item => item.id === data.id);
      if (index !== -1) {
        this.$set(this.consultant, index, {
          ...data,
          workStatus: data.status
        });
        console.log(`已更新顾问ID:${data.id}, 名称:${data.name}, 排序:${data.sortOrder}`);
      } else {
        // 如果是新顾问，添加到数组
        this.consultant.push({
          ...data,
          workStatus: data.status
        });
        console.log(`添加新顾问ID:${data.id}, 名称:${data.name}`);
      }
    },
    
    // 更新顾问类型分组
    updateConsultantGroups() {
      console.log('开始更新顾问分组，当前顾问数据:', this.consultant);
      
      // 更新辅导顾问组
      this.consultantType1 = this.consultant
        .filter(c => c.type === 1)
        .sort((a, b) => a.sortOrder - b.sortOrder);
      
      // 更新申诉顾问组
      this.consultantType2 = this.consultant
        .filter(c => c.type === 2)
        .sort((a, b) => a.sortOrder - b.sortOrder);
      
      // 更新对客顾问组
      this.consultantType3 = this.consultant
        .filter(c => c.type === 3)
        .sort((a, b) => a.sortOrder - b.sortOrder);

      // 更新国际课程顾问组
      this.consultantType4 = this.consultant
        .filter(c => c.type === 4)
        .sort((a, b) => a.sortOrder - b.sortOrder);
        
      // 检查各类型顾问数量
      const type1Count = this.consultantType1.length;
      const type2Count = this.consultantType2.length;
      const type3Count = this.consultantType3.length;
      const type4Count = this.consultantType4.length;
      
      console.log('更新后的顾问分组:', {
        '辅导': type1Count,
        '申诉': type2Count,
        '对客': type3Count,
        '国际课程': type4Count
      });
      
      // 详细日志
      if (type1Count > 0) console.log('辅导顾问详情:', this.consultantType1);
      if (type2Count > 0) console.log('申诉顾问详情:', this.consultantType2);
      if (type3Count > 0) console.log('对客顾问详情:', this.consultantType3);
      if (type4Count > 0) console.log('国际课程顾问详情:', this.consultantType4);
      else console.log('警告: 没有国际课程顾问数据!');
    },
    
    addCountNormal(consultant) {
      consultant.countNormal += 1;
      this.updateConsultant(consultant, 'updateNormalCount');
    },
    addCountSem(consultant){
      consultant.countSem += 1;
      this.updateConsultant(consultant, 'updateSemCount');
    },
    reduceCountNormal(consultant){
      consultant.countNormal -= 1;
      this.updateConsultant(consultant, 'updateNormalCount');

    },
    reduceCountSem(consultant){
      consultant.countSem -= 1;
      this.updateConsultant(consultant, 'updateSemCount');
    },
    updateConsultant(consultant, operationType) {
      const requestData = { ...consultant };
      
      // For pause reason updates, include the pauseReason in the request
      if (operationType === 'updatePauseReason') {
        requestData.description = consultant.pauseReason;
      }
      
      request.post('/updateConsultants', requestData, {
        params: { operationType }
      })
      .then(response => {
        if (response.data.code === 200) {
          this.$message.success('更新成功')
          this.updateConsultantData(consultant)
        } else {
          this.$message.error(response.data.msg || '更新失败')
          // 如果是状态更新，失败时恢复状态
          if (operationType === 'updateStatus') {
            consultant.status = consultant.status === 1 ? 0 : 1
          }
        }
      })
    },
    handleNormalCountChange(consultant) {
      this.updateConsultant(consultant, 'updateNormalCount');
    },

    handleSemCountChange(consultant) {
      this.updateConsultant(consultant, 'updateSemCount');
    },

    handleSingle1CountChange(consultant) {
      this.updateConsultant(consultant, 'updateSingle1Count');
    },

    handleDyCountChange(consultant) {
      this.updateConsultant(consultant, 'updateDyCount');
    },

    handleICCountChange(consultant) {
      this.updateConsultant(consultant, 'updateICCount');
    },

    // 添加处理状态变化的方法
    handleStatusChange(consultant) {
      this.updateConsultant(consultant, 'updateStatus');
    },

    async handleWorkStatusChange(consultant) {
      if (consultant.workStatus === 3) {
        this.pauseReasonDialogVisible = true;
        this.currentConsultant = consultant;
      } else {
        try {
          if (consultant.workStatus === 1) {
            const response = await request({
              url: '/restore',
              method: 'post',
              params: {
                consultantId: consultant.id,
                fromStatus: consultant.status
              }
            });

            if (response.data.code === 200) {
              this.$message.success('状态恢复成功');
            } else {
              this.$message.error(response.data.msg || '状态恢复失败');
            }
          } else {
            const response = await request({
              url: '/status',
              method: 'post',
              params: {
                consultantId: consultant.id,
                status: consultant.workStatus
              }
            });

            if (response.data.code === 200) {
              this.$message.success('状态更新成功');
            } else {
              this.$message.error(response.data.msg || '状态更新失败');
            }
          }
        } catch (error) {
          console.error('状态更新失败:', error);
          this.$message.error('状态更新失败');
        }
      }
    },

    handleClose(done) {
      // 确保关闭对话框时保存修改
      done();
    },
    
    // 处理拖拽结束事件
    async onDragEnd(type) {
      try {
        const consultantList = type === 1 ? this.consultantType1 :
                              type === 2 ? this.consultantType2 :
                              type ===3 ? this.consultantType3 :
                              this.consultantType4;
        
        // 更新本地显示的排序顺序
        consultantList.forEach((item, index) => {
          item.sortOrder = index + 1;
        });
        
        // 生成新的排序顺序
        const updates = consultantList.map((item, index) => ({
          id: item.id,
          newOrder: index + 1,
          type: type
        }));
        
        console.log('发送排序更新:', updates);
        
        // 发送请求到后端
        const response = await request.post('/consultants/reorder', { updates });
        
        if (response.data.code === 200) {
          this.$message.success('顾问排序更新成功');
          
          // 更新主数据列表中对应顾问的排序
          updates.forEach(update => {
            const consultant = this.consultant.find(c => c.id === update.id);
            if (consultant) {
              consultant.sortOrder = update.newOrder;
              console.log(`本地更新顾问ID:${consultant.id}, 名称:${consultant.name}, 新排序:${update.newOrder}`);
            }
          });
          
          // 更新顾问类型分组
          this.updateConsultantGroups();
          
          // 重新过滤顾问数据以更新表格
          this.filterConsultants();
        } else {
          this.$message.error(response.data.msg || '更新顺序失败');
          // 如果失败，重新获取数据以恢复正确的排序
          this.show();
        }
      } catch (error) {
        console.error('更新顺序失败:', error);
        this.$message.error('更新顺序失败');
        // 发生错误时重新获取数据
        this.show();
      }
    },

    checkMove(evt) {
      // 确保只能在同一个组内拖动
      return true; // 允许所有移动，因为我们已经通过group属性限制了只能在同一组内拖动
    },

    handlePauseReasonDialogClose(done) {
      this.pauseReasonDialogVisible = false;
      this.pauseReason = '';
      this.currentConsultant = null;
      done();
    },

    cancelPauseStatusChange() {
      this.pauseReasonDialogVisible = false;
      this.pauseReason = '';
      this.currentConsultant = null;
    },

    async confirmPauseStatusChange() {
      if (this.currentConsultant) {
        try {
          // 保存暂停原因
          this.currentConsultant.pauseReason = this.pauseReason;
          
          // 更新顾问的状态属性
          this.currentConsultant.status = 3;  // 设置状态为暂停
          
          // 发送状态更新请求
          const statusResponse = await request({
            url: '/status',
            method: 'post',
            params: {
              consultantId: this.currentConsultant.id,
              status: 3  // 明确设置为暂停状态 (3)
            }
          });
          
          if (statusResponse.data.code === 200) {
            // 在前端更新顾问的状态
            const consultantIndex = this.consultant.findIndex(c => c.id === this.currentConsultant.id);
            if (consultantIndex !== -1) {
              // 使用 Vue 的响应式更新方法
              this.$set(this.consultant, consultantIndex, {
                ...this.consultant[consultantIndex],
                status: 3,
                workStatus: 3,
                description: this.pauseReason
              });
            }
            
            // 状态更新成功后，再更新暂停原因
            this.updateConsultant(this.currentConsultant, 'updatePauseReason');
            
            // 刷新顾问列表
            this.filterConsultants();
            
            this.$message.success('状态更新成功');
          } else {
            this.$message.error(statusResponse.data.msg || '状态更新失败');
          }
        } catch (error) {
          console.error('状态更新失败:', error);
          this.$message.error('状态更新失败');
        } finally {
          this.pauseReasonDialogVisible = false;
          this.pauseReason = '';
          this.currentConsultant = null;
        }
      }
    },
    filterConsultants() {
      console.log('过滤类型:', this.selectedType); 
      console.log('所有顾问数据:', this.consultant);
      
      const baseConsultants = this.consultant.filter(consultant => {
        const matchesType = consultant.type === parseInt(this.selectedType);
        const isActive = consultant.status !== 0;
        const isNormalStatus = consultant.workStatus === 1;
        
        console.log(`顾问 ${consultant.name || consultant.id} - 类型匹配: ${matchesType}, 状态激活: ${isActive}, 工作状态正常: ${isNormalStatus}`);
        
        return matchesType && isActive && isNormalStatus;
      });
      
      console.log('过滤后的基础顾问数据:', baseConsultants);

      // 辅导或申诉顾问的表格数据
      if (this.selectedType === '1' || this.selectedType === '2') {
        this.filteredConsultantsByNormal = this.sortConsultants(
          baseConsultants.map(c => ({...c, accountType: 'normal'})),
          'countNormal'
        );
        console.log('官号表格数据:', this.filteredConsultantsByNormal);

        this.filteredConsultantsBySem = this.sortConsultants(
          baseConsultants.map(c => ({...c, accountType: 'sem'})),
          'countSem'
        );
        console.log('SEM表格数据:', this.filteredConsultantsBySem);
      }

      // AP/Alevel表格数据 - 只在辅导类型时显示
      if (this.selectedType === '1') {
        this.filteredConsultantsBySingle1 = this.sortConsultants(
          baseConsultants.map(c => ({...c, accountType: 'ap'})),
          'countSingle1'
        );
        console.log('AP/Alevel表格数据:', this.filteredConsultantsBySingle1);
      } 
      // 抖音表格数据 - 只在抖音类型时显示
      else if (this.selectedType === '3') {
        this.filteredConsultantsBySingle1 = this.sortConsultants(
          this.consultantType3.filter(c => c.status === 1),
          'countSingle1'
        );
        console.log('抖音表格数据:', this.filteredConsultantsBySingle1);
      }
      // 国际课程表格数据 - 只在国际课程类型时显示
      else if (this.selectedType === '4') {
        this.filteredConsultantsBySingle1 = this.sortConsultants(
          this.consultantType4.filter(c => c.status === 1),
          'countSingle1'
        );
        console.log('国际课程表格数据:', this.filteredConsultantsBySingle1);
      }
    },

    // 排序顾问数据的方法
    sortConsultants(data, countField) {
      return data.sort((a, b) => {
        if (a[countField] !== b[countField]) {
          return a[countField] - b[countField];  // 按照指定字段排序
        }
        return a.sortOrder - b.sortOrder;  // 如果指定字段相同，再按照 sortOrder 排序
      });
    },
    
    handleNormalCountChange(consultant) {
      this.updateConsultant(consultant, 'updateNormalCount');
    },
  }
        
}
  
</script>

<style scoped>
.header-container {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
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
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 10px;
}

.divider {
  height: 20px;
}

.group-title {
  font-size: 14px; /* 减小字体大小 */
  font-weight: bold;
  margin-bottom: 5px; /* 减小底部边距 */
  padding-left: 5px; /* 减小左侧内边距 */
  color: #409EFF;
}

.consultants-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  max-width: 100%;
}

.consultant-group {
  flex: 1;
  min-width: 200px; /* 增加最小宽度 */
  max-width: 320px; /* 增加最大宽度 */
}

.status-select {
  width: 90px; /* 增加宽度，使文字完全显示 */
}

.type-select {
  width: 120px;
}

.status-rest {
  background-color: #909399 !important; /* 灰色 */
  color: white !important;
}

.status-rest /deep/ .el-input__inner {
  background-color: #909399 !important;
  color: white !important;
  border-color: #909399 !important;
}

.status-rest /deep/ .el-input__icon {
  color: white !important;
}

.status-pause {
  background-color: #f56c6c !important; /* 红色 */
  color: white !important;
}

.status-pause /deep/ .el-input__inner {
  background-color: #f56c6c !important;
  color: white !important;
  border-color: #f56c6c !important;
}

.status-pause /deep/ .el-input__icon {
  color: white !important;
}

.option-rest {
  color: #909399 !important; /* 灰色 */
  background-color: #f5f7fa !important;
}

.option-pause {
  color: #f56c6c !important; /* 红色 */
  background-color: #f5f7fa !important;
}

/* 拖拽相关样式 */
.draggable-list {
  min-height: 50px;
  border: 1px solid #ebeef5;
  border-radius: 3px;
  padding: 2px;
  background-color: #f5f7fa;
}

.draggable-item {
  display: flex;
  align-items: center;
  padding: 6px;
  margin-bottom: 3px;
  background-color: white;
  border-radius: 3px;
  border: 1px solid #dcdfe6;
  cursor: move;
  min-height: 36px; /* 确保最小高度 */
}

.drag-handle {
  margin-right: 5px;
  color: #909399;
  cursor: move;
  font-size: 14px;
}

.consultant-id {
  width: 18px;
  margin-right: 5px;
  font-weight: bold;
  font-size: 13px;
}

.consultant-name {
  flex: 1;
  margin-right: 8px; /* 增加右侧边距，为状态选择框留出更多空间 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 13px;
}

.ghost-item {
  opacity: 0.5;
  background: #c8ebfb;
}

.chosen-item {
  background: #e6f7ff;
}
</style>