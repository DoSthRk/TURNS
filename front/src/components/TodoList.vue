<template>
  <div class="todo-container">
    <div class="todo-header">
      <span class="date">{{ currentDate }}</span>
      <el-badge :value="uncompletedCount" :hidden="uncompletedCount === 0" class="todo-badge">
        <span class="shift-type" :style="{ color: currentShift ? currentShift.color : '' }">
          {{ currentShift ? currentShift.name : '今日无班次' }}
        </span>
      </el-badge>
    </div>

    <div v-if="currentShift" class="todo-list">
      <el-card class="todo-card">
        <div slot="header" class="todo-card-header">
          <span>待办清单</span>
          <el-button 
            type="text"
            @click="refreshTodos"
            :loading="loading"
          >
            <i class="el-icon-refresh"></i>
          </el-button>
        </div>
        
        <el-checkbox-group v-model="completedTodos" @change="handleTodoChange">
          <div v-for="todo in todos" :key="todo.id" class="todo-item">
            <el-checkbox 
              :label="todo.id"
              :disabled="loading"
            >
              <span :class="{ 'completed': isCompleted(todo.id) }">
                {{ todo.todoType ? todo.todoType.content : '加载中...' }}
              </span>
            </el-checkbox>
            <span class="complete-time" v-if="isCompleted(todo.id)">
              {{ formatTime(todo.completeTime) }}
            </span>
          </div>
        </el-checkbox-group>
      </el-card>
    </div>
    
    <div v-else class="no-shift">
      <el-empty description="今日无班次安排"></el-empty>
    </div>
  </div>
</template>

<script>
import moment from 'moment'
import request from '@/utils/request'

export default {
  name: 'TodoList',
  
  data() {
    return {
      currentDate: moment().format('YYYY-MM-DD'),
      currentShift: null,
      todos: [],
      completedTodos: [],
      loading: false
    }
  },

  computed: {
    uncompletedCount() {
      return this.todos.length - this.completedTodos.length
    }
  },

  methods: {
    async fetchTodayShift() {
      try {
        const response = await request.get('/schedule/today')
        
        if (response.data.code === 200 && response.data.data) {
          this.currentShift = response.data.data;
          await this.fetchTodos();
        }
      } catch (error) {
        if (error.message === 'Authentication failed') {
          return
        }
        this.$message.error('获取今日班次失败：' + error.message)
      }
    },

    async fetchTodos() {
      this.loading = true;
      try {
        const response = await request.get('/todo/list', {
          headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        
        if (response.data.code === 200) {
          this.todos = response.data.data;
          this.completedTodos = this.todos
            .filter(todo => todo.isCompleted)
            .map(todo => todo.id);
          console.log('待办事项:', this.todos);
        }
      } catch (error) {
        console.error('获取待办事项失败:', error);
        this.$message.error('获取待办事项失败');
      } finally {
        this.loading = false;
      }
    },

    async handleTodoChange(value) {
      const todoId = value[value.length - 1];
      if (!todoId) return;
      
      try {
        const response = await request.post(`/todo/complete/${todoId}`, null, {
          headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        
        if (response.data.code === 200) {
          this.$message.success('更新成功');
          await this.fetchTodos();
        } else {
          this.$message.error(response.data.msg || '更新失败');
        }
      } catch (error) {
        console.error('更新待办事项失败:', error);
        this.$message.error('更新失败');
      }
    },

    isCompleted(todoId) {
      return this.completedTodos.includes(todoId);
    },

    formatTime(time) {
      return moment(time).format('HH:mm:ss');
    },

    refreshTodos() {
      this.fetchTodos();
    }
  },

  created() {
    this.fetchTodayShift();
  }
}
</script>

<style scoped>
.todo-container {
  padding: 20px;
}

.todo-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.date {
  font-size: 18px;
  font-weight: bold;
  margin-right: 20px;
}

.shift-type {
  font-size: 16px;
  font-weight: bold;
}

.todo-card {
  max-width: 600px;
  margin: 0 auto;
}

.todo-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.todo-item:last-child {
  border-bottom: none;
}

.completed {
  text-decoration: line-through;
  color: #909399;
}

.complete-time {
  font-size: 12px;
  color: #909399;
}

.no-shift {
  margin-top: 40px;
}

.todo-badge {
  margin-top: 10px;
}
</style> 