<template>
  
  <div>
    <el-header class="header-container">
      <!-- 左侧选择框 -->
      <el-select v-model="selectedType" @change="filterConsultants" placeholder="选择类型" class="type-select">
        <el-option label="辅导" value="1"></el-option>
        <el-option label="申诉" value="2"></el-option>
      </el-select>
      
      
      <el-button
        type="danger"
        icon="el-icon-setting"

        @click="dialogVisible=true"
      >轮班设置</el-button>
    </el-header>

    <el-dialog
  title="提示"
  :visible.sync="dialogVisible"
  width="30%"
  :before-close="handleClose">
  <el-table :data="consultant">
    <el-table-column prop="name" label="姓名" width="150">
    </el-table-column>
    <el-table-column prop="status" label="状态" width="120">
        <template slot-scope="scope">
          <el-switch
        v-model="scope.row.status"
        :active-value="1"
        :inactive-value="0"
        @change="handleStatusChange(scope.row)"
    />
        </template>
      </el-table-column>
  </el-table>
</el-dialog>

    <!-- 使用 Flexbox 将表格并排显示 -->
    <div class="table-container">

       <!-- 官号表格 -->
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
      <div class="divider"></div> <!-- 分割线 -->
      <!-- SEM表格 -->
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

      <div class="divider"></div> <!-- 分割线 -->

      <!-- AP/Alevel表格 -->
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

      <div class="divider"></div> <!-- 分割线 -->

     
    </div>
  </div>
</template>


<script>
import request from '@/utils/request'
import webSocket from '@/utils/websocket'

export default {

    data() {
        return{
            dialogVisible:false,
            consultant:[],
            activeMenu: '1', 
            switchStatus:0,
            filteredConsultantsByNormal: [] ,
            filteredConsultantsBySem:[],
            filteredConsultantsBySingle1: [],
            tempConsultants: [],
            selectedType: "1",
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
      }

    },
    methods:{
        show(){
            request.get('/consultants')
            .then(res=>{
                this.consultant=res.data;
                this.filterConsultants();
                console.log(res.data)
            })
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


      const index= this.consultant.findIndex(item=>item.id===data.id);
      if(index!==-1){
        this.$set(this.consultant,index,data);
        console.log('更新后的顾问数据:', this.consultant);
      }
      this.filterConsultants();



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
      request.post('/updateConsultants', consultant, {
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
    filterConsultants() {
      console.log('过滤类型:', this.selectedType); 
      console.log('顾问数据:', this.consultant);

  // 如果没有选择任何类型，则直接复制原始数据
  if (this.selectedType === null  || this.selectedType === "") {
    this.filteredConsultantsByNormal = [...this.consultant];
    this.filteredConsultantsBySem = [...this.consultant];
    this.filteredConsultantsBySingle1=[...this.consultant];
  } else {
    // 根据 selectedType 过滤数据
    this.filteredConsultantsByNormal = this.consultant.filter(consultant => consultant.type === parseInt(this.selectedType)&& consultant.status !== 0);
    this.filteredConsultantsBySem = this.consultant.filter(consultant => consultant.type === parseInt(this.selectedType)&& consultant.status !== 0);
    this.filteredConsultantsBySingle1=this.consultant.filter(consultant => consultant.type === parseInt(this.selectedType)&& consultant.status !== 0);
  }

  // 排序 Normal 顾问数据
  this.filteredConsultantsByNormal = this.sortConsultants(this.filteredConsultantsByNormal, 'countNormal');

  // 排序 SEM 顾问数据
  this.filteredConsultantsBySem = this.sortConsultants(this.filteredConsultantsBySem, 'countSem');

  this.filteredConsultantsBySingle1=this.sortConsultants(this.filteredConsultantsBySingle1,'countSingle1');

  console.log('过滤并排序后的顾问数据:', this.filteredConsultantsByNormal);
  console.log('SEM过滤并排序后的顾问数据:', this.filteredConsultantsBySem);
  console.log('SEM过滤并排序后的顾问数据:', this.filteredConsultantsBySingle1);
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

handleNormalCountChange(consultant) {
  this.updateConsultant(consultant, 'updateNormalCount');
},

handleSemCountChange(consultant) {
  this.updateConsultant(consultant, 'updateSemCount');
},

handleSingle1CountChange(consultant) {
  this.updateConsultant(consultant, 'updateSingle1Count');
},

// 添加处理状态变化的方法
handleStatusChange(consultant) {
  this.updateConsultant(consultant, 'updateStatus');
}

  }
        
}
  
</script>

}

.delete-button:hover {
  background-color: #D73737; /* 鼠标悬停变暗 */
  transform: scale(1.1); /* 悬停放大 */
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


</style>