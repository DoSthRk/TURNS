<template>
  <div class="snake-game">
    <div class="game-header">
      <div class="score">分数: {{ score }}</div>
      <el-button type="primary" size="small" @click="resetGame">重新开始</el-button>
    </div>
    <div class="game-board" ref="board">
      <div v-for="(row, i) in board" :key="i" class="row">
        <div v-for="(cell, j) in row" :key="j" 
             class="cell"
             :class="{
               'snake': isSnake(i, j),
               'food': isFood(i, j),
               'snake-head': isSnakeHead(i, j)
             }">
        </div>
      </div>
    </div>
    <div class="game-tip">
      使用方向键控制蛇的移动
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'Snake',
  data() {
    return {
      board: Array(20).fill().map(() => Array(20).fill(0)),
      snake: [{x: 10, y: 10}],  // 蛇的身体，第一个元素是头
      food: {x: 15, y: 15},
      direction: 'right',
      score: 0,
      isPlaying: true,
      gameInterval: null
    }
  },
  mounted() {
    this.initGame()
    window.addEventListener('keydown', this.handleKeyDown)
  },
  beforeDestroy() {
    window.removeEventListener('keydown', this.handleKeyDown)
    if (this.gameInterval) {
      clearInterval(this.gameInterval)
    }
  },
  methods: {
    initGame() {
      this.snake = [{x: 10, y: 10}]
      this.direction = 'right'
      this.score = 0
      this.isPlaying = true
      this.generateFood()
      
      if (this.gameInterval) {
        clearInterval(this.gameInterval)
      }
      
      this.gameInterval = setInterval(() => {
        if (this.isPlaying) {
          this.moveSnake()
        }
      }, 200)  // 每200ms移动一次
    },
    
    generateFood() {
      do {
        this.food = {
          x: Math.floor(Math.random() * 20),
          y: Math.floor(Math.random() * 20)
        }
      } while (this.isSnake(this.food.x, this.food.y))
    },
    
    handleKeyDown(e) {
      if (!this.isPlaying) return
      
      const newDirection = {
        'ArrowUp': 'up',
        'ArrowDown': 'down',
        'ArrowLeft': 'left',
        'ArrowRight': 'right'
      }[e.key]
      
      if (newDirection) {
        e.preventDefault()  // 阻止默认行为
        e.stopPropagation()  // 阻止事件冒泡
        // 防止180度转向
        const opposites = {
          'up': 'down',
          'down': 'up',
          'left': 'right',
          'right': 'left'
        }
        if (opposites[newDirection] !== this.direction) {
          this.direction = newDirection
        }
      }
    },
    
    moveSnake() {
      const head = {...this.snake[0]}
      
      // 根据方向移动蛇头
      switch(this.direction) {
        case 'up': head.x--; break
        case 'down': head.x++; break
        case 'left': head.y--; break
        case 'right': head.y++; break
      }
      
      // 检查是否撞墙
      if (head.x < 0 || head.x >= 20 || head.y < 0 || head.y >= 20) {
        this.gameOver()
        return
      }
      
      // 检查是否撞到自己
      if (this.isSnake(head.x, head.y)) {
        this.gameOver()
        return
      }
      
      // 移动蛇
      this.snake.unshift(head)
      
      // 检查是否吃到食物
      if (head.x === this.food.x && head.y === this.food.y) {
        this.score += 10
        this.generateFood()
      } else {
        this.snake.pop()
      }
    },
    
    isSnake(x, y) {
      return this.snake.some(segment => segment.x === x && segment.y === y)
    },
    
    isFood(x, y) {
      return this.food.x === x && this.food.y === y
    },
    
    isSnakeHead(x, y) {
      return this.snake[0].x === x && this.snake[0].y === y
    },
    
    resetGame() {
      this.initGame()
    },
    
    async submitScore(score) {
      try {
        const token = localStorage.getItem('token');
        console.log('开始提交分数:', {
          score: score,
          type: 'snake',
          isPlaying: this.isPlaying,
          token: token ? token.substring(0, 20) + '...' : '未设置'  // 只显示部分token
        });
        
        if (!token) {
          throw new Error('未登录，请先登录');
        }
        
        const response = await request.post('/game/score', {
          type: 'snake',
          score: score
        });
        console.log('分数提交响应:', {
          code: response.data.code,
          msg: response.data.msg,
          headers: response.config.headers
        });
        
        if (response.data.code === 200) {
          this.$message.success('分数提交成功！');
          if (this.$parent.$refs.leaderboard) {
            console.log('开始刷新排行榜');
            await this.$parent.$refs.leaderboard.fetchLeaderboard('snake');
          }
        } else {
          throw new Error(response.data.msg || '提交失败');
        }
      } catch (error) {
        console.error('提交分数失败:', {
          message: error.message,
          response: error.response?.data
        });
        this.$message.error(`提交分数失败: ${error.message}`);
        throw error;
      }
    },
    
    gameOver() {
      console.log('游戏结束 - 当前状态:', {
        score: this.score,
        isPlaying: this.isPlaying,
        snake: this.snake
      });
      
      this.isPlaying = false;
      clearInterval(this.gameInterval);
      
      // 先提交分数
      this.submitScore(this.score).then(() => {
        // 提交成功后再显示游戏结束消息
        this.$message.error(`游戏结束！得分：${this.score}`);
      }).catch(() => {
        this.$message.error(`游戏结束！得分：${this.score} (分数提交失败)`);
      });
    }
  }
}
</script>

<style scoped>
.snake-game {
  width: 500px;
  padding: 20px;
  background: #faf8ef;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.game-board {
  background: #bbada0;
  padding: 10px;
  border-radius: 6px;
  position: relative;
  margin: 20px 0;
}

.row {
  display: flex;
  margin-bottom: 2px;
}

.row:last-child {
  margin-bottom: 0;
}

.cell {
  width: 20px;
  height: 20px;
  margin: 1px;
  background: #cdc1b4;
  border-radius: 3px;
  transition: all 0.15s ease;
}

.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.score {
  font-size: 24px;
  font-weight: bold;
  color: #776e65;
}

.snake {
  background: #4CAF50;
  box-shadow: 0 0 5px rgba(76, 175, 80, 0.5);
}

.snake-head {
  background: #388E3C;
  box-shadow: 0 0 8px rgba(56, 142, 60, 0.8);
}

.food {
  background: #f44336;
  border-radius: 50%;
  box-shadow: 0 0 8px rgba(244, 67, 54, 0.8);
  animation: pulse 1s infinite;
}

.game-tip {
  text-align: center;
  color: #776e65;
  margin-top: 10px;
  font-size: 14px;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

/* 响应式布局 */
@media (max-width: 600px) {
  .snake-game {
    width: 100%;
    padding: 10px;
  }
  
  .cell {
    width: 15px;
    height: 15px;
  }
}
</style> 