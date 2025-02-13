<template>
  <div class="leaderboard">
    <el-tabs v-model="currentGame">
      <el-tab-pane label="2048" name="2048">
        <el-table :data="leaderboards['2048']" style="width: 100%">
          <el-table-column prop="rank" label="排名" width="80" />
          <el-table-column prop="username" label="用户" />
          <el-table-column prop="score" label="分数" />

        </el-table>
      </el-tab-pane>
      
      <el-tab-pane label="贪吃蛇" name="snake">
        <el-table :data="leaderboards.snake" style="width: 100%">
          <el-table-column prop="rank" label="排名" width="80" />
          <el-table-column prop="username" label="用户" />
          <el-table-column prop="score" label="分数" />

        </el-table>
      </el-tab-pane>
      
      <el-tab-pane label="记忆翻牌" name="memory">
        <el-table :data="leaderboards.memory" style="width: 100%">
          <el-table-column prop="rank" label="排名" width="80" />
          <el-table-column prop="username" label="用户" />
          <el-table-column prop="score" label="步数" />

        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import request from '@/utils/request'
import moment from 'moment'

export default {
  name: 'GameLeaderboard',
  props: {
    activeGame: {
      type: String,
      default: '2048'
    }
  },
  data() {
    return {
      currentGame: this.activeGame,
      leaderboards: {
        '2048': [],
        snake: [],
        memory: []
      }
    }
  },
  watch: {
    activeGame: {
      immediate: true,
      handler(newVal) {
        this.currentGame = newVal
        this.fetchLeaderboard(newVal)
      }
    }
  },
  mounted() {
    this.fetchLeaderboard(this.activeGame)
  },
  methods: {
    async fetchLeaderboard(gameType) {
      try {
        console.log('开始获取排行榜数据:', {
          gameType: gameType,
          token: localStorage.getItem('token') ? '已设置' : '未设置'
        });
        
        const response = await request.get(`/game/leaderboard/${gameType}`);
        console.log('排行榜数据:', response.data);
        
        if (response.data.code === 200) {
          this.leaderboards[gameType] = response.data.data.map((item, index) => ({
            ...item,
            rank: index + 1
          }));
          console.log('更新后的排行榜:', this.leaderboards[gameType]);
        }
      } catch (error) {
        console.error('获取排行榜失败:', error);
        this.$message.error('获取排行榜失败')
      }
    },
    formatTime(time) {
      return moment(time).format('YYYY-MM-DD HH:mm')
    }
  }
}
</script>

<style scoped>
.leaderboard {
  margin-top: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}
</style> 