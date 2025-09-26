<template>
  <el-tabs type="border-card">
    <el-tab-pane label="轮班" class="turn">
      <!-- 类型选择按钮组 -->
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
        :type="selectedType === 4 ? 'primary' : 'success'"
        @click="selectType(4)"
        class="type-button"
      >
        国际课程
      </el-button>

      <el-button
        :type="selectedType === 3 ? 'primary' : 'success'"
        @click="selectType(3)"
        class="type-button"
      >
        推月申诉
      </el-button>

      <el-button
        :type="selectedType === 5 ? 'primary' : 'success'"
        @click="selectType(5)"
        class="type-button"
      >
        推月辅导
      </el-button>
            <el-button
        :type="selectedType === 6 ? 'primary' : 'success'"
        @click="selectType(6)"
        class="type-button"
      >
        汇诺辅导
      </el-button>
            <el-button
        :type="selectedType === 7 ? 'primary' : 'success'"
        @click="selectType(7)"
        class="type-button"
      >
        汇诺申诉
      </el-button>
      <el-button
        :type="selectedType === 8 ? 'primary' : 'success'"
        @click="selectType(8)"
        class="type-button"
      >
        智云辅导
      </el-button>
      <el-button
        :type="selectedType === 9 ? 'primary' : 'success'"
        @click="selectType(9)"
        class="type-button"
      >
        留学堂辅导
      </el-button>
      <el-button
        :type="selectedType === 12 ? 'primary' : 'success'"
        @click="selectType(12)"
        class="type-button"
      >
        留学堂申诉
      </el-button>
      <el-button
        :type="selectedType === 10 ? 'primary' : 'success'"
        @click="selectType(10)"
        class="type-button"
      >
        集好家辅导
      </el-button>
      <el-button
        :type="selectedType === 11 ? 'primary' : 'success'"
        @click="selectType(11)"
        class="type-button"
      >
        集好家申诉
      </el-button>
      
      <!-- 显示选择类型的内容 -->
      <el-col>
        <div class="title-section">
          <h1 style="font-size: 100px;">
            {{ 
              selectedType === 1 ? 
                (hasComplementType1 ? '辅导正在补客资' : '辅导') : 
              selectedType === 2 ? 
                (hasComplementType2 ? '申诉正在补客资' : '申诉') : 
              selectedType === 3 ? '推月申诉':
              selectedType === 5 ? '推月辅导': 
              selectedType === 4 ? '国际课程':
              selectedType === 6 ? '汇诺辅导':
              selectedType === 7 ? '汇诺申诉':
              selectedType === 8 ? '智云辅导':
              selectedType === 9 ? '留学堂辅导':
              selectedType === 10 ? '集好家辅导':
              selectedType === 11 ?'集好家申诉' :
              '留学堂申诉'
            }}
          </h1>
          <!-- 修改休息/暂停顾问显示 -->
          <div class="inactive-consultants-container">
            <div 
              class="inactive-consultants" 
              v-if="inactiveConsultants.length > 0"
            >
              <div 
                v-for="consultant in inactiveConsultants" 
                :key="consultant.id"
                :class="{'pause-status': consultant.status === 3, 'rest-status': consultant.status === 2}"
              >
                <span class="consultant-status">
                  {{ consultant.name }}{{ consultant.status === 2 ? '正在休息' : '正在暂停' }}{{ consultant.status === 3 && consultant.description ? ': ' + consultant.description : '' }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 辅导和申诉相关表格 -->
      
        <template v-if="selectedType === 1 || selectedType === 2 || selectedType ===4">
          <!-- 官号表格 -->
          <el-table :data="normalTableData" class="normalTable">
            <el-table-column prop="name" :label="getNormalLabel()"></el-table-column>
            <el-table-column>
              <template slot-scope="scope">
                <div v-if="scope.row.orderStatusNormal === 0">
                  <el-button slot="reference" type="success" @click="restoreConsultantNormal(scope.row)">补</el-button>
                </div>
                <div v-else>
                  <el-button @click="addCountNormal(scope.row)" type="success">分配</el-button>
                  <el-button type="danger" @click="skipNormal(scope.row)">跳过</el-button>
                  <el-button v-if="scope.row.waitingNormal === 1" type="info" @click="confirmNormal(scope.row)">
                    已@，等回复
                  </el-button>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="countNormal" label="客资" width="150"></el-table-column>
          </el-table>
        </template>
      
          <el-divider><i class="el-icon-paperclip"></i></el-divider>


        <!-- AP/Alevel客资表格，在type=1, 3, 4时显示 ,-->
      <template v-if="selectedType === 1 ||selectedType === 3 || selectedType === 4 || selectedType ===5 || selectedType ===6 || selectedType ===7 || selectedType === 8
       || selectedType === 9 || selectedType === 10 || selectedType === 11 || selectedType === 12">
        <el-table :data="single1TableData">
          <el-table-column prop="name" :label="getSingle1Label()"></el-table-column>
          <el-table-column>
            <template slot-scope="scope">
              <div v-if="scope.row.orderStatusSingle1 === 0">
                <el-button slot="reference" type="success" @click="selectedType === 3 ? restoreConsultantDy(scope.row) : restoreConsultantIC(scope.row)">补</el-button>
              </div>
              <div v-else>
                <el-button @click="selectedType === 3 ? addCountDy(scope.row) : addCountIC(scope.row)" type="success">分配</el-button>
                <el-button type="danger" @click="selectedType === 3 ? skipDy(scope.row) : skipIC(scope.row)">跳过</el-button>
                <el-button v-if="scope.row.waitingSingle1 === 1" type="info" @click="selectedType === 3 ? confirmDy(scope.row) : confirmIC(scope.row)">
                  已@，等回复
                </el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="countSingle1" label="客资" width="150"></el-table-column>
        </el-table>
      </template>


      <el-divider><i class="el-icon-paperclip"></i></el-divider>

      
        <template v-if="selectedType === 1 || selectedType === 2">
          <!-- SEM表格 -->
          <el-table :data="semTableData">
            <el-table-column prop="name" label="SEM"></el-table-column>
            <el-table-column>
              <template slot-scope="scope">
                <div v-if="scope.row.orderStatusSem === 0">
                  <el-button slot="reference" type="success" @click="restoreConsultantSem(scope.row)">补</el-button>
                </div>
                <div v-else>
                  <el-button @click="addCountSem(scope.row)" type="success">分配</el-button>
                  <el-button type="danger" @click="skipSem(scope.row)">跳过</el-button>
                  <el-button v-if="scope.row.waitingSem === 1" type="info" @click="confirmSem(scope.row)">
                    已@，等回复
                  </el-button>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="countSem" label="客资" width="150"></el-table-column>
          </el-table>
        </template>
      
    </el-tab-pane>
    
    <el-tab-pane label="总表">
      <Info ref="infoData" @data-loaded="updateData"></Info>
    </el-tab-pane>
  </el-tabs>
</template>

<script>

import Info from './Info';
import request from '@/utils/request';
import webSocket from '@/utils/websocket'

export default {
  components:{
    Info
  },
  
  data(){
    return{
      workDays: [],
      loading: false,
      isCountdownVisible: false, // 是否显示倒计时按钮
      timeLeft: 180, // 倒计时时间（秒）
      intervalId: null, // 定时器 ID
            consultant:[],
            switchStatus:0,
            ws:null, //webSocket
            filteredConsultantsByNormal: [] ,
            filteredConsultantsBySem:[],
            filteredConsultantsBySingle1: [],
            firstNormalData: {}, // 第一行数据
            firstSemData:{},
            firstSingle1Data:{},
            secNormalData:{},
            secSemData:{},
            secSingle1Data:{},
            thirdNormalData:{},
            thirdSemData:{},
            thirdSingle1Data:{},
            selectedType: 1,
            normalTableData:[],
            semTableData:[],
            single1TableData:[],
            prepConsultantsSem: [],
            hasComplementType1: false, // 是否有辅导补客资
            hasComplementType2: false,  // 是否有申诉补客资
            complementType2List: [] // 申诉补客资列表
    }
  },
  computed:{
    // 获取当前类型下所有休息或暂停的顾问
    inactiveConsultants() {
      return this.consultant.filter(c => 
        c.type === this.selectedType && 
        (c.status === 2 || c.status === 3)
      ).sort((a, b) => b.status - a.status); // 暂停的排在前面
    }
  },
  mounted() {
    this.show();
    this.initializeWebSocket();
    this.filterConsultants();
    this.updateNormalTableData();
    this.updateSemTableData();
    this.updateSingle1TableData();
    this.checkComplementData(); // 初始检查补客资状态
  },

  methods:{
    

    show(){
            request.get('/consultants')
            .then(res=>{
                this.consultant=res.data;
                this.filterConsultants();
                this.updateNormalTableData();
                this.updateSemTableData();
                this.updateSingle1TableData();

                console.log(1111111111111111111111111111)
                console.log(this.normalTableData);
                console.log(this.semTableData);
                console.log(this.single1TableData)
            })
        },
initializeWebSocket() {
      webSocket.connect((data) => {
        try {
          const parsedData = JSON.parse(data)
          console.log('收到数据', parsedData)
          this.updateConsultantData(parsedData)
          this.updateNormalTableData()
          this.updateSingle1TableData()
          this.updateSemTableData()
          this.checkComplementData() // 添加这行
        } catch (error) {
          console.error('解析WebSocket消息失败:', error)
        }
      })
    },
updateConsultantData(data) {
  const index = this.consultant.findIndex(item => item.id === data.id);
  if (index !== -1) {
    // 只更新特定字段，不覆盖整个对象
    this.$set(this.consultant, index, {
      ...this.consultant[index], // 保留原有数据
      ...data, // 更新新数据
      // 确保这些字段不被覆盖
      countNormal: data.countNormal ?? this.consultant[index].countNormal,
      countSem: data.countSem ?? this.consultant[index].countSem,
      countSingle1: data.countSingle1 ?? this.consultant[index].countSingle1,
      orderStatusNormal: data.orderStatusNormal ?? this.consultant[index].orderStatusNormal,
      orderStatusSem: data.orderStatusSem ?? this.consultant[index].orderStatusSem,
      orderStatusSingle1: data.orderStatusSingle1 ?? this.consultant[index].orderStatusSingle1
    });
    console.log('更新后的顾问数据:', {
      id: this.consultant[index].id,
      countNormal: this.consultant[index].countNormal,
      countSem: this.consultant[index].countSem,
      orderStatusNormal: this.consultant[index].orderStatusNormal,
      orderStatusSem: this.consultant[index].orderStatusSem
    });
  }
  this.filterConsultants();
  this.updateNormalTableData();
  this.updateSemTableData();
  this.updateSingle1TableData();
},
queding(consultant){

  consultant.waiting=0;
  this.addCountSem(consultant);
  this.prepConsultantsSem.shift()


},

addCountNormal(consultant) {
  consultant.waitingNormal = 1;
  this.updateConsultant(consultant, 'addCountNormal');
  this.updateNormalTableData();
},

addCountSem(consultant) {
  consultant.waitingSem = 1;
  this.prepConsultantsSem = this.prepConsultantsSem || [];
  this.prepConsultantsSem.push(consultant);
  this.updateConsultant(consultant, 'addCountSem');
  this.updateSemTableData();
},
addCountSingle1(consultant) {
  consultant.waitingSingle1 = 1;
  this.updateConsultant(consultant, 'addCountSingle1');
  this.updateSingle1TableData();
},
addCountDy(consultant) {
  consultant.waitingSingle1 = 1;
  this.updateConsultant(consultant, 'addCountDy');
  this.updateSingle1TableData();
},
addCountIC(consultant) {
  consultant.waitingSingle1 = 1;
  this.updateConsultant(consultant, 'addCountIC');
  this.updateSingle1TableData();
},
reduceCountSingle1(consultant){
  consultant.countSingle1-=1;
  this.updateConsultant(consultant)
},
reduceCountNormal(consultant){
consultant.countNormal -= 1;
this.updateConsultant(consultant);
},
reduceCountSem(consultant){
consultant.countSem -= 1;
this.updateConsultant(consultant);
},
reduceCountDy(consultant){
  consultant.countSingle1 -= 1;
  this.updateConsultant(consultant, 'updateDyCount');
},
reduceCountIC(consultant){
  consultant.countSingle1 -= 1;
  this.updateConsultant(consultant, 'updateICCount');
},
updateSemTableData(){
  const skippedConsultants = this.consultant.filter(c => 
    c.orderStatusSem === 0 && 
    c.type === this.selectedType  // 添加类型过滤
  );
  const waitingConsultants = this.consultant.filter(c => 
    c.waitingSem === 1 && 
    c.type === this.selectedType  // 添加类型过滤
  );
  this.semTableData = [
    this.firstSemData,
    ...waitingConsultants,
    ...skippedConsultants
  ];

  if(skippedConsultants.length === 1) {
    this.firstSemData = this.secSemData;
  } else if(skippedConsultants.length === 2) {
    this.firstSemData = this.thirdSemData;
  } else if(skippedConsultants.length === 3) {
    this.firstSemData = this.forthSemData;
  } else if(skippedConsultants.length === 4) {
    this.firstSemData = this.fifthSemData;
  } else if(skippedConsultants.length === 5) {
    this.firstSemData = this.sixthSemData;
  } else if(skippedConsultants.length === 6) {
    this.firstSemData = this.seventhSemData;
  } else if(skippedConsultants.length === 7) {
    this.firstSemData = this.eighthSemData;
  } else if(skippedConsultants.length === 8) {
    this.firstSemData = this.ninthSemData;
  } else if(skippedConsultants.length === 9) {
    this.firstSemData = this.tenthSemData;
  }
  // ... 其他位置更新逻辑
},

updateSingle1TableData(){
  const skippedConsultants = this.consultant.filter(c => 
    c.orderStatusSingle1 === 0 && 
    c.type === this.selectedType  // 添加类型过滤
  );
  const waitingConsultants = this.consultant.filter(c => 
    c.waitingSingle1 === 1 && 
    c.type === this.selectedType  // 添加类型过滤
  );
  this.single1TableData = [
    this.firstSingle1Data,
    ...waitingConsultants,  // 添加等待中的顾问
    ...skippedConsultants
  ];

  if(skippedConsultants.length === 1) {
    this.firstSingle1Data = this.secSingle1Data;
  } else if(skippedConsultants.length === 2) {
    this.firstSingle1Data = this.thirdSingle1Data;
  } else if(skippedConsultants.length === 3) {
    this.firstSingle1Data = this.forthSingle1Data;
  } else if(skippedConsultants.length === 4) {
    this.firstSingle1Data = this.fifthSingle1Data;
  } else if(skippedConsultants.length === 5) {
    this.firstSingle1Data = this.sixthSingle1Data;
  } else if(skippedConsultants.length === 6) {
    this.firstSingle1Data = this.seventhSingle1Data;
  } else if(skippedConsultants.length === 7) {
    this.firstSingle1Data = this.eighthSingle1Data;
  } else if(skippedConsultants.length === 8) {
    this.firstSingle1Data = this.ninthSingle1Data;
  } else if(skippedConsultants.length === 9) {
    this.firstSingle1Data = this.tenthSingle1Data;
  }
  // ... 其他位置更新逻辑
},
updateNormalTableData(){
  const skippedConsultants = this.consultant.filter(c => 
    c.orderStatusNormal === 0 && 
    c.type === this.selectedType  // 添加类型过滤
  );
  const waitingConsultants = this.consultant.filter(c => 
    c.waitingNormal === 1 && 
    c.type === this.selectedType  // 添加类型过滤
  );
  this.normalTableData = [
    this.firstNormalData, 
    ...waitingConsultants,
    ...skippedConsultants
  ];

  // 更新第一位顾问的逻辑
  if (skippedConsultants.length === 1) {
    this.firstNormalData = this.secNormalData;
  } else if (skippedConsultants.length === 2) {
    this.firstNormalData = this.thirdNormalData;
  } else if(skippedConsultants.length === 3) {
    this.firstNormalData = this.forthNormalData;
  } else if(skippedConsultants.length === 4) {
    this.firstNormalData = this.fifthNormalData;
  } else if(skippedConsultants.length === 5) {
    this.firstNormalData = this.sixthNormalData;
  } else if(skippedConsultants.length === 6) {
    this.firstNormalData = this.seventhNormalData;
  } else if(skippedConsultants.length === 7) {
    this.firstNormalData = this.eighthNormalData;
  } else if(skippedConsultants.length === 8) {
    this.firstNormalData = this.ninthNormalData;
  } else if(skippedConsultants.length === 9) {
    this.firstNormalData = this.tenthNormalData;
  }
  // ... 其他位置的更新逻辑
},
restoreConsultantNormal(consultant) {
  consultant.orderStatusNormal = 1;
  consultant.waitingNormal = 1;  // 设置等待状态
  this.prepConsultantsNormal = this.prepConsultantsNormal || [];
  this.prepConsultantsNormal.push(consultant);
  this.updateConsultant(consultant, 'restoreNormal');
  this.updateNormalTableData();
},

restoreConsultantSem(consultant) {
  consultant.orderStatusSem = 1;
  consultant.waitingSem = 1;  // 设置等待状态
  this.prepConsultantsSem = this.prepConsultantsSem || [];
  this.prepConsultantsSem.push(consultant);
  this.updateConsultant(consultant, 'restoreSem');
  this.updateSemTableData();
},

restoreConsultantSingle1(consultant) {
  consultant.orderStatusSingle1 = 1;
  consultant.waitingSingle1 = 1;  // 添加这行，设置waiting状态为1
  this.updateConsultant(consultant, 'restoreSingle1');
  this.updateSingle1TableData();
},
restoreConsultantDy(consultant) {
  consultant.orderStatusSingle1 = 1;
  consultant.waitingSingle1 = 1;  // 添加这行，设置waiting状态为1
  this.updateConsultant(consultant, 'restoreDy');
  this.updateSingle1TableData();
},
restoreConsultantIC(consultant) {
  consultant.orderStatusSingle1 = 1;
  consultant.waitingSingle1 = 1;  // 添加这行，设置waiting状态为1
  this.updateConsultant(consultant, 'restoreIC');
  this.updateSingle1TableData();
},


updateConsultant(consultant, operationType) {
  request.post('/updateConsultants', consultant, {
    params: {
      operationType: operationType
    },
    headers: {
      'Authorization': `Bearer ${localStorage.getItem('token')}`
    }
  })
  .then(response => {
    if (response.data.code === 200) {
      // 根据操作类型显示更具体的成功信息
      let successMessage = '更新成功';
      if (operationType) {
        if (operationType.includes('add')) {
          successMessage = '分配客资成功';
        } else if (operationType.includes('skip')) {
          successMessage = '跳过客资成功';
        } else if (operationType.includes('confirm')) {
          successMessage = '确认客资成功';
        } else if (operationType.includes('restore')) {
          successMessage = '恢复分配状态成功';
        }
      }
      this.$message.success(successMessage);
    } else {
      this.$message.error(response.data.msg || '更新失败');
    }
  })
  .catch(error => {
    console.error('更新失败:', error);
    this.$message.error('更新失败');
  });
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
this.filteredConsultantsByNormal = this.consultant.filter(consultant => 
  consultant.type === parseInt(this.selectedType) && 
  consultant.status === 1 &&
  consultant.waitingNormal !== 1
);
this.filteredConsultantsBySem = this.consultant.filter(consultant => 
  consultant.type === parseInt(this.selectedType) && 
  consultant.status === 1 &&
  consultant.waitingSem !== 1
);
this.filteredConsultantsBySingle1=this.consultant.filter(consultant => 
  consultant.type === parseInt(this.selectedType) && 
  consultant.status === 1 &&
  consultant.waitingSingle1 !== 1
);
}

// 排序 Normal 顾问数据
this.filteredConsultantsByNormal = this.sortConsultants(this.filteredConsultantsByNormal, 'countNormal');
// 排序 SEM 顾问数据
this.filteredConsultantsBySem = this.sortConsultants(this.filteredConsultantsBySem, 'countSem');
this.filteredConsultantsBySingle1=this.sortConsultants(this.filteredConsultantsBySingle1,'countSingle1');

console.log('过滤后的 Normal 顾问数据:', this.filteredConsultantsByNormal);
  console.log('过滤后的 SEM 顾问数据:', this.filteredConsultantsBySem);
  console.log('过滤后的 Single1 顾问数据:', this.filteredConsultantsBySingle1);

this.firstNormalData = { ...this.filteredConsultantsByNormal[0] };
this.secNormalData = { ...this.filteredConsultantsByNormal[1]};
this.thirdNormalData = { ...this.filteredConsultantsByNormal[2]};
this.forthNormalData = { ...this.filteredConsultantsByNormal[3]};
this.fifthNormalData = { ...this.filteredConsultantsByNormal[4]};
this.sixthNormalData = { ...this.filteredConsultantsByNormal[5]};
this.seventhNormalData = { ...this.filteredConsultantsByNormal[6]};
this.eighthNormalData = { ...this.filteredConsultantsByNormal[7]};
this.ninthNormalData = { ...this.filteredConsultantsByNormal[8]};
this.tenthNormalData = { ...this.filteredConsultantsByNormal[9]};



this.firstSemData = { ...this.filteredConsultantsBySem[0] };
this.secSemData = { ...this.filteredConsultantsBySem[1]};
this.thirdSemData = { ... this.filteredConsultantsBySem[2]};
this.forthSemData = { ...this.filteredConsultantsBySem[3]};
this.fifthSemData = { ...this.filteredConsultantsBySem[4]};
this.sixthSemData = { ...this.filteredConsultantsBySem[5]};
this.seventhSemData = { ...this.filteredConsultantsBySem[6]};
this.eighthSemData = { ...this.filteredConsultantsBySem[7]};
this.ninthSemData = { ...this.filteredConsultantsBySem[8]};
this.tenthSemData = { ...this.filteredConsultantsBySem[9]};

this.firstSingle1Data = { ...this.filteredConsultantsBySingle1[0] };
this.secSingle1Data = { ...this.filteredConsultantsBySingle1[1]};
this.thirdSingle1Data = {...this.filteredConsultantsBySingle1[2]}
this.forthSingle1Data = { ...this.filteredConsultantsBySingle1[3]};
this.fifthSingle1Data = { ...this.filteredConsultantsBySingle1[4]};
this.sixthSingle1Data = { ...this.filteredConsultantsBySingle1[5]};
this.seventhSingle1Data = { ...this.filteredConsultantsBySingle1[6]};
this.eighthSingle1Data = { ...this.filteredConsultantsBySingle1[7]};
this.ninthSingle1Data = { ...this.filteredConsultantsBySingle1[8]};
this.tenthSingle1Data = { ...this.filteredConsultantsBySingle1[9]};


          
      this.updateNormalTableData();
      this.updateSemTableData();
      this.updateSingle1TableData();


},

// 排序顾问数据的方法
sortConsultants(data, countField) {
return data.sort((a, b) => {
if (a[countField] !== b[countField]) {
return a[countField] - b[countField];  // 按照指定字段排序
}
return a.sortOrder - b.sortOrder; 
});
},

skipNormal(consultant) {
  consultant.orderStatusNormal = 0;
  consultant.waitingNormal = 0;
  this.updateConsultant(consultant, 'skipNormal');
  this.updateNormalTableData();
},
skipSem(consultant) {
  consultant.orderStatusSem = 0;
  consultant.waitingSem = 0;
  this.updateConsultant(consultant, 'skipSem');
  this.updateSemTableData();
},
skipSingle1(consultant) {
  consultant.orderStatusSingle1 = 0;
  consultant.waitingSingle1 = 0;
  this.updateConsultant(consultant, 'skipSingle1');
  this.updateSingle1TableData();
},
skipDy(consultant) {
  consultant.orderStatusSingle1 = 0;
  consultant.waitingSingle1 = 0;
  this.updateConsultant(consultant, 'skipDy');
  this.updateSingle1TableData();
},
skipIC(consultant) {
  consultant.orderStatusSingle1 = 0;
  consultant.waitingSingle1 = 0;
  this.updateConsultant(consultant, 'skipIC');
  this.updateSingle1TableData();
},

selectType(type) {
  this.selectedType = type;
  this.filterConsultants();
  this.updateNormalTableData();
  this.updateSemTableData();
  this.updateSingle1TableData();
},

confirmNormal(consultant) {
  consultant.waitingNormal = 0;
  consultant.countNormal += 1;
  this.updateConsultant(consultant, 'confirmNormal');
  this.updateNormalTableData();
},

confirmSem(consultant) {
  consultant.waitingSem = 0;
  consultant.countSem += 1;
  this.updateConsultant(consultant, 'confirmSem');
  this.updateSemTableData();
},

confirmSingle1(consultant) {
  consultant.waitingSingle1 = 0;
  consultant.countSingle1 += 1;
  this.updateConsultant(consultant, 'confirmSingle1');
  this.updateSingle1TableData();
},
confirmDy(consultant) {
  consultant.waitingSingle1 = 0;
  consultant.countSingle1 += 1;
  this.updateConsultant(consultant, 'confirmDy');
  this.updateSingle1TableData();
},
confirmIC(consultant) {
  consultant.waitingSingle1 = 0;
  consultant.countSingle1 += 1;
  this.updateConsultant(consultant, 'confirmIC');
  this.updateSingle1TableData();
},

  // checkAndSkipConsultants(consultants, countField, statusField) {
  //   for (let i = 1; i < consultants.length; i++) {
  //     const prevConsultant = consultants[i-1];
  //     const currentConsultant = consultants[i];
      
  //     if (currentConsultant[countField] + 2 <= prevConsultant[countField]) {
  //       // 如果当前顾问的count比前一个顾问小2以上
  //       currentConsultant[statusField] = 0;
  //       this.updateConsultant(currentConsultant);
  //     }
  //   }
  // },

  async fetchWorkDays() {
    this.loading = true
    try {
      const response = await request.get('/workday/list')
      if (response.data.code === 200) {
        this.workDays = response.data.data || []
      }
    } catch (error) {
      // 不过滤认证错误，让所有错误都显示
      this.$message.error(error.message || '获取工作日失败')
    } finally {
      this.loading = false
    }
  },

  async updateWorkDay(date, isWorkDay) {
    try {
      const response = await request.post('/workday/update', {
        date,
        isWorkDay
      })
      if (response.data.code === 200) {
        this.$message.success('更新成功')
        await this.fetchWorkDays()
      }
    } catch (error) {
      this.$message.error(error.message || '更新失败')
    }
  },

  // 检查补客资数据
  async checkComplementData() {
    try {
      const response = await request.get('/complement');
      if (response.data.code === 200) {
        const complementData = response.data.data;
        this.hasComplementType1 = complementData.some(item => item.type === 1);
        this.hasComplementType2 = complementData.some(item => item.type === 2);
        
        // 不直接修改顾问数据
        console.log('补客资状态:', {
          type1: this.hasComplementType1,
          type2: this.hasComplementType2
        });
      }
    } catch (error) {
      console.error('获取补客资数据失败:', error);
    }
  },

  // 获取申诉补客资列表
  async getComplementType2List() {
    try {
      const response = await request.get('/complement/type2/list')
      if (response.data.code === 200) {
        this.complementType2List = response.data.data || []
        this.hasComplementType2 = this.complementType2List.length > 0
      }
    } catch (error) {
      console.error('获取申诉补客资列表失败:', error)
      this.$message.error('获取申诉补客资列表失败')
    }
  },

  // 获取Single1表格标题的方法
  getSingle1Label() {
    if (this.selectedType === 3) {
      return '推月申诉';
    } else if (this.selectedType === 4) {
      return '国际课程主动留资';
    } else if (this.selectedType === 1){
      return '主动添加';
    } else if (this.selectedType === 5){
      return '推月辅导'
    } else if (this.selectedType === 6){
      return '汇诺辅导'
    } else if (this.selectedType === 7){
      return '汇诺申诉'
    } else if (this.selectedType === 8){
      return '智云辅导'
    } else if (this.selectedType === 9){
      return '留学堂辅导'
    }else if (this.selectedType === 10){
      return '集好家辅导'
    }else if (this.selectedType === 11){
      return '集好家申诉'
    }else if (this.selectedType === 12){
      return '留学堂申诉'
    }
  },

  getNormalLabel(){
    if(this.selectedType === 1){
      return '官号(拉群)';
    }
    if(this.selectedType ===4){
      return '国际课程'
    }
    else{
      return '官号';
    }
  }
}
}
</script>

<style scoped>
.el-row {
  margin-bottom: 20px;
}

.left-section,
.right-section {
  padding: 20px;
}

.action-buttons {
  margin-top: 10px;
}

.undo-button {
  margin-top: 20px;
  text-align: center;
}
.normalTable{
  background-color: black;
}

/* 添加禁用状态的样式 */
.el-button.is-disabled {
  cursor: not-allowed !important;
  opacity: 0.7 !important;
}

/* 添加警告提示的样式 */
.el-message--warning {
  background-color: #fdf6ec !important;
  border-color: #faecd8 !important;
  color: #e6a23c !important;
}

/* 修改标题区域样式 */
.title-section {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;  /* 改为两端对齐 */
  gap: 20px;
  position: relative;  /* 添加相对定位 */
}

/* 休息/暂停顾问容器样式 */
.inactive-consultants-container {
  position: absolute;  /* 绝对定位 */
  right: 50px;        /* 距离右侧50px */
  top: 20px;          /* 距离顶部20px */
}

/* 休息/暂停顾问显示样式 */
.inactive-consultants {
  margin-top: 10px;
  font-size: 24px;  /* 增大字体 */
  text-align: right;
}

/* 顾问状态文字样式 */
.consultant-status {
  font-weight: bold;
  line-height: 1.5;
  display: block;
}

.pause-status {
  color: #F56C6C;  /* 红色 */
  margin-bottom: 10px;
}

.rest-status {
  color: #909399;  /* 灰色 */
  margin-bottom: 10px;
}
</style>