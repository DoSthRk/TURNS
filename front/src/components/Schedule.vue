<template>
  <div class="schedule-container">
    <div class="header-actions">
      <el-switch
        v-model="onlyShowMine"
        active-text="仅看我的"
        inactive-text="查看全部"
        @change="handleSwitchChange"
      >
      </el-switch>
    </div>

    <el-calendar v-model="date">
      <template #dateCell="{ data }">
        <div 
          class="calendar-cell" 
          @click.stop="handleDateClick(data.day)"
          style="cursor: pointer;"
        >
          <div class="date-text">{{ data.day.split('-').slice(2).join('') }}</div>
          <div 
            v-for="schedule in getSchedulesByDate(data.day)" 
            :key="schedule.id"
            class="schedule-item"
            :style="{ backgroundColor: schedule.shiftType.color }"
          >
            <span>{{ schedule.shiftType.name }} - {{ schedule.user.account }}</span>
            <el-button
              v-if="$store.getters.userRole === 'admin'"
              type="text"
              size="mini"
              @click.stop="handleDelete(schedule.id)"
              class="delete-btn"
            >
              <i class="el-icon-delete"></i>
            </el-button>
          </div>
        </div>
      </template>
    </el-calendar>

    <!-- 排班弹窗 -->
    <el-dialog 
      :title="`${selectedDate} 排班设置`" 
      :visible.sync="dialogVisible"
      width="500px"
    >
      <div v-for="type in shiftTypes" :key="type.id" class="shift-type-row">
        <div class="shift-type-label" :style="{ color: type.color }">
          {{ type.name }}：
        </div>
        <el-select
          v-model="scheduleForm[type.id]"
          multiple
          collapse-tags
          placeholder="请选择人员"
          style="width: 300px;"
          @change="handleSelectionChange"
        >
          <el-option
            v-for="user in users"
            :key="user.id"
            :label="user.account"
            :value="user.id"
          ></el-option>
        </el-select>
      </div>
      
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitSchedule">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 批量排班弹窗 -->
    <el-dialog 
      title="批量排班" 
      :visible.sync="batchDialogVisible"
      width="50%"
    >
      <el-form :model="batchForm" ref="batchForm" :rules="batchRules" label-width="100px">
        <!-- 日期范围选择 -->
        <el-form-item label="日期范围" prop="dateRange">
          <el-date-picker
            v-model="batchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
          ></el-date-picker>
        </el-form-item>

        <!-- 选择工作日 -->
        <el-form-item label="工作日">
          <el-checkbox-group v-model="batchForm.workDays">
            <el-checkbox label="1">周一</el-checkbox>
            <el-checkbox label="2">周二</el-checkbox>
            <el-checkbox label="3">周三</el-checkbox>
            <el-checkbox label="4">周四</el-checkbox>
            <el-checkbox label="5">周五</el-checkbox>
            <el-checkbox label="6">周六</el-checkbox>
            <el-checkbox label="0">周日</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <!-- 班次和人员选择 -->
        <el-form-item label="排班设置">
          <div class="batch-schedule-items">
            <div v-for="(item, index) in batchForm.scheduleItems" :key="index" class="batch-item">
              <el-select v-model="item.shiftTypeId" placeholder="选择班次" style="width: 120px; margin-right: 10px">
                <el-option
                  v-for="type in shiftTypes"
                  :key="type.id"
                  :label="type.name"
                  :value="type.id"
                ></el-option>
              </el-select>
              
              <el-select v-model="item.userId" placeholder="选择人员" style="width: 120px; margin-right: 10px">
                <el-option
                  v-for="user in users"
                  :key="user.id"
                  :label="user.account"
                  :value="user.id"
                ></el-option>
              </el-select>
              
              <el-button 
                type="text" 
                icon="el-icon-delete"
                @click="removeScheduleItem(index)"
                v-if="batchForm.scheduleItems.length > 1"
              ></el-button>
            </div>
          </div>
          <el-button type="text" icon="el-icon-plus" @click="addScheduleItem">添加排班项</el-button>
        </el-form-item>
      </el-form>
      
      <span slot="footer" class="dialog-footer">
        <el-button @click="batchDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitBatchSchedule">确 定</el-button>
      </span>
    </el-dialog>

    <el-table
      :data="filteredTableData"
      v-loading="loading"
      style="width: 100%"
    >
      <el-table-column prop="date" label="日期" width="120"></el-table-column>
      <el-table-column label="班次" width="120">
        <template slot-scope="scope">
          {{ getShiftTypeName(scope.row.shift_type_id) }}
        </template>
      </el-table-column>
      <!-- 其他列保持不变 -->
    </el-table>

    <!-- 修改排班按钮 -->
    <el-button 
      type="primary" 
      @click="handleScheduleClick"
    >
      排班
    </el-button>
  </div>
</template>

<script>
import axios from 'axios'
import moment from 'moment'
import request from '@/utils/request';

export default {
  name: 'Schedule',
  data() {
    return {
      date: new Date(),
      schedules: [],
      shiftTypes: [],
      users: [],
      dialogVisible: false,
      selectedDate: '',
      scheduleForm: {},  // 用于存储每个班次选择的人员
      batchDialogVisible: false,
      batchForm: {
        dateRange: [],
        workDays: ['1', '2', '3', '4', '5'],  // 默认选中周一到周五
        scheduleItems: [{
          shiftTypeId: '',
          userId: ''
        }]
      },
      batchRules: {
        dateRange: [{ required: true, message: '请选择日期范围', trigger: 'change' }]
      },
      onlyShowMine: false,  // 切换状态
      tableData: [],        // 原始数据
      loading: false,
      userId: localStorage.getItem('userId'),
      shiftTypes: {
        1: '早班',
        2: '中班',
        3: '晚班',
        4: '夜班'
      }
    }
  },
  
  created() {
    if (!this.userId) {
      this.userId = localStorage.getItem('userId')
    }
    this.fetchSchedules()
    this.fetchShiftTypes()
    this.fetchUsers()
  },
  
  computed: {
    isAdmin() {
      return this.$store.getters.userRole === 'admin'
    },
    filteredTableData() {
      if (!this.onlyShowMine) {
        return this.tableData
      }
      
      return this.tableData.filter(row => {
        // 确保进行数字比较
        return row.user_id === parseInt(this.userId)
      })
    }
  },
  mounted(){
    console.log('Current role:', this.$store.state.userRole);
    console.log('Is admin?', this.isAdmin);
  
  },
  methods: {
    // 获取排班数据
    async fetchSchedules() {
      try {
        const response = await request.get('/schedule/list', {
          headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        })
        if (response.data.code === 200) {
          this.schedules = response.data.data
        } else {
          this.$message.error(response.data.msg || '获取排班数据失败')
        }
      } catch (error) {
        console.error('获取排班数据失败:', error)
        this.$message.error('获取排班数据失败')
      }
    },
    
    // 获取班次类型
    async fetchShiftTypes() {
      try {
        const response = await request.get('/schedule/shift-types', {
          headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        })
        if (response.data.code === 200) {
          this.shiftTypes = response.data.data
        } else {
          this.$message.error(response.data.msg || '获取班次类型失败')
        }
      } catch (error) {
        console.error('获取班次类型失败:', error)
        this.$message.error('获取班次类型失败')
      }
    },
    
    // 获取用户列表
    async fetchUsers() {
      try {
        const response = await request.get('/user/list', {
          headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        })
        if (response.data.code === 200) {
          this.users = response.data.data
        } else {
          this.$message.error(response.data.msg || '获取用户列表失败')
        }
      } catch (error) {
        console.error('获取用户列表失败:', error)
        this.$message.error('获取用户列表失败')
      }
    },
    
    // 根据日期获取排班
    getSchedulesByDate(date) {
        return this.schedules.filter(schedule => schedule.date === date)
    },
    
    // 点击日期块
    handleDateClick(date) {
      this.selectedDate = date;
      this.dialogVisible = true;
    },
    
    // 处理选择变化
    handleSelectionChange(value, shiftTypeId) {
      console.log('Selection changed:', value, shiftTypeId);
    },
    
    // 提交排班
    async submitSchedule() {
      try {
        // 将表单数据转换为排班记录
        const schedules = [];
        Object.entries(this.scheduleForm).forEach(([shiftTypeId, userIds]) => {
          if (Array.isArray(userIds)) {
            userIds.forEach(userId => {
              schedules.push({
                date: this.selectedDate,
                shiftTypeId: parseInt(shiftTypeId),
                userId: userId
              });
            });
          }
        });

        if (schedules.length === 0) {
          this.$message.warning('请至少选择一个排班');
          return;
        }

        // 发送请求
        const response = await request.post(
          '/schedule/batch-add',
          { schedules },
          {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
          }
        );

        if (response.data.code === 200) {
          this.$message.success('排班成功');
          this.dialogVisible = false;
          this.fetchSchedules();
        } else {
          this.$message.error(response.data.msg || '排班失败');
        }
      } catch (error) {
        console.error('排班失败:', error);
        this.$message.error('排班失败，请稍后重试');
      }
    },
    
    // 删除排班
    async handleDelete(id) {
      try {
        await this.$confirm('确认删除该排班?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        
        const response = await request.delete(`/schedule/delete/${id}`, {
          headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        })
        
        if (response.data.code === 200) {
          this.$message.success('删除成功')
          this.fetchSchedules()
        } else {
          this.$message.error(response.data.msg || '删除失败')
        }
      } catch (error) {
        console.error('删除失败:', error);
      }
    },
    
    showBatchDialog() {
      this.batchDialogVisible = true;
      this.batchForm = {
        dateRange: [],
        workDays: ['1', '2', '3', '4', '5'],
        scheduleItems: [{
          shiftTypeId: '',
          userId: ''
        }]
      };
    },
    
    addScheduleItem() {
      this.batchForm.scheduleItems.push({
        shiftTypeId: '',
        userId: ''
      });
    },
    
    removeScheduleItem(index) {
      this.batchForm.scheduleItems.splice(index, 1);
    },
    
    async submitBatchSchedule() {
      try {
        const [startDate, endDate] = this.batchForm.dateRange;
        const schedules = [];
        
        // 生成日期范围内的所有日期
        let currentDate = new Date(startDate);
        const lastDate = new Date(endDate);
        
        while (currentDate <= lastDate) {
          const dayOfWeek = currentDate.getDay().toString();
          
          // 检查是否是选中的工作日
          if (this.batchForm.workDays.includes(dayOfWeek)) {
            // 为每个排班项生成排班记录
            this.batchForm.scheduleItems.forEach(item => {
              schedules.push({
                date: this.$moment(currentDate).format('YYYY-MM-DD'),
                shiftTypeId: item.shiftTypeId,
                userId: item.userId
              });
            });
          }
          
          currentDate.setDate(currentDate.getDate() + 1);
        }
        
        // 发送批量排班请求
        const response = await request.post(
          '/schedule/batch-add',
          { schedules },
          {
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
          }
        );
        
        if (response.data.code === 200) {
          this.$message.success('批量排班成功');
          this.batchDialogVisible = false;
          this.fetchSchedules();
        } else {
          this.$message.error(response.data.msg || '批量排班失败');
        }
      } catch (error) {
        console.error('批量排班失败:', error);
        this.$message.error('批量排班失败，请稍后重试');
      }
    },
    // 切换处理
    handleSwitchChange(val) {
      console.log('切换到:', val ? '仅看我的' : '查看全部')
    },
    getShiftTypeName(typeId) {
      return this.shiftTypes[typeId] || '未知班次'
    },
    async fetchScheduleData() {
      this.loading = true
      try {
        const response = await this.$http.get('/schedule/list')
        if (response.data.code === 200) {
          this.tableData = response.data.data
        }
      } catch (error) {
        console.error('获取排班数据失败:', error)
        this.$message.error('获取排班数据失败')
      } finally {
        this.loading = false
      }
    },
    handleScheduleClick() {
      this.dialogVisible = true;
    }
  }
}
</script>

<style scoped>
.schedule-container {
  padding: 20px;
}

.schedule-header {
  margin-bottom: 20px;
}

.calendar-cell {
  height: 100%;
  padding: 4px;
  cursor: pointer;
  min-height: 100px;  /* 确保单元格有足够的高度 */
}

.calendar-cell:hover {
  background-color: #f5f7fa;  /* 添加悬停效果 */
}

.date-text {
  font-size: 14px;
  margin-bottom: 4px;
}

.schedule-item {
  font-size: 12px;
  padding: 4px 8px;
  margin-bottom: 4px;
  border-radius: 4px;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.delete-btn {
  color: #fff;
  padding: 0 4px;
}

.delete-btn:hover {
  color: #ff4949;
}

:deep(.el-calendar-table .el-calendar-day) {
  height: auto;
  padding: 4px;
}

:deep(.el-calendar-table td) {
  border: 1px solid #ebeef5;
}

.batch-schedule-items {
  margin-bottom: 10px;
}

.batch-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.shift-type-row {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.shift-type-label {
  width: 80px;
  font-weight: bold;
}

:deep(.el-select) {
  width: 100%;
}

.header-actions {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
