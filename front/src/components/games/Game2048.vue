<template>
  <div class="game-2048">
    <div class="game-header">
      <div class="score">分数: {{ score }}</div>
      <el-button type="primary" size="small" @click="resetGame">重新开始</el-button>
    </div>
    <div class="game-board">
      <div v-for="(row, i) in board" :key="i" class="row">
        <div v-for="(cell, j) in row" :key="j" 
             class="cell" 
             :class="[
               'cell-' + cell,
               { 'cell-new': isNewTile(i, j) }
             ]">
          {{ cell || '' }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'Game2048',
  data() {
    return {
      board: Array(4).fill().map(() => Array(4).fill(0)),
      score: 0,
      isPlaying: true,
      newTilePosition: null  // 添加新位置标记
    }
  },
  mounted() {
    this.initGame()
    window.addEventListener('keydown', this.handleKeyDown)
  },
  beforeDestroy() {
    window.removeEventListener('keydown', this.handleKeyDown)
  },
  methods: {
    initGame() {
      this.board = Array(4).fill().map(() => Array(4).fill(0))
      this.score = 0
      this.addNewTile()
      this.addNewTile()
    },
    addNewTile() {
      const emptyCells = []
      for (let i = 0; i < 4; i++) {
        for (let j = 0; j < 4; j++) {
          if (this.board[i][j] === 0) {
            emptyCells.push({x: i, y: j})
          }
        }
      }
      if (emptyCells.length > 0) {
        const {x, y} = emptyCells[Math.floor(Math.random() * emptyCells.length)]
        this.board[x][y] = Math.random() < 0.9 ? 2 : 4
        this.newTilePosition = {x, y}  // 记录新生成的位置
        
        // 300ms后清除新位置标记
        setTimeout(() => {
          this.newTilePosition = null
        }, 300)
      }
    },
    isNewTile(i, j) {
      return this.newTilePosition && 
             this.newTilePosition.x === i && 
             this.newTilePosition.y === j
    },
    handleKeyDown(e) {
      if (!this.isPlaying) return
      
      if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(e.key)) {
        e.preventDefault()  // 阻止默认行为
        e.stopPropagation()  // 阻止事件冒泡
        let moved = false
        
        switch(e.key) {
          case 'ArrowLeft': moved = this.moveLeft(); break
          case 'ArrowRight': moved = this.moveRight(); break
          case 'ArrowUp': moved = this.moveUp(); break
          case 'ArrowDown': moved = this.moveDown(); break
        }

        if (moved) {
          setTimeout(() => {
            this.addNewTile()
            if (this.isGameOver()) {
              this.gameOver()
            }
          }, 150)
        }
      }
    },
    moveLeft() {
      let moved = false
      for (let i = 0; i < 4; i++) {
        // 保存移动前的行状态用于比较
        const originalRow = [...this.board[i]]
        
        // 1. 先去除空格，把所有数字靠左
        let row = this.board[i].filter(cell => cell !== 0)
        
        // 2. 合并相同的数字
        for (let j = 0; j < row.length - 1;) {
          if (row[j] === row[j + 1]) {
            row[j] *= 2
            this.score += row[j]
            row.splice(j + 1, 1)
            j++
          } else {
            j++
          }
        }
        
        // 3. 补充0到4个格子
        while (row.length < 4) {
          row.push(0)
        }
        
        // 4. 检查是否发生了移动（包括位置移动和数字合并）
        moved = moved || row.some((val, index) => val !== originalRow[index])
        
        this.board[i] = row
      }
      return moved
    },
    moveRight() {
      let moved = false
      for (let i = 0; i < 4; i++) {
        const originalRow = [...this.board[i]]
        
        // 1. 去除空格，把所有数字靠右
        let row = this.board[i].filter(cell => cell !== 0)
        
        // 2. 从右向左合并
        for (let j = row.length - 1; j > 0;) {
          if (row[j] === row[j - 1]) {
            row[j] *= 2
            this.score += row[j]
            row.splice(j - 1, 1)
            j--
          } else {
            j--
          }
        }
        
        // 3. 在左侧补充0
        while (row.length < 4) {
          row.unshift(0)
        }
        
        // 4. 检查移动
        moved = moved || row.some((val, index) => val !== originalRow[index])
        
        this.board[i] = row
      }
      return moved
    },
    moveUp() {
      let moved = false
      for (let j = 0; j < 4; j++) {
        let column = this.board.map(row => row[j]).filter(cell => cell !== 0)
        
        for (let i = 0; i < column.length - 1;) {
          if (column[i] === column[i + 1]) {
            column[i] *= 2
            this.score += column[i]
            column.splice(i + 1, 1)
            moved = true
          } else {
            i++
          }
        }
        
        while (column.length < 4) {
          column.push(0)
        }
        
        const oldColumn = this.board.map(row => row[j])
        if (!moved) {
          moved = !column.every((val, index) => val === oldColumn[index])
        }
        
        for (let i = 0; i < 4; i++) {
          this.board[i][j] = column[i]
        }
      }
      return moved
    },
    moveDown() {
      let moved = false
      for (let j = 0; j < 4; j++) {
        let column = this.board.map(row => row[j]).filter(cell => cell !== 0)
        
        for (let i = column.length - 1; i > 0;) {
          if (column[i] === column[i - 1]) {
            column[i] *= 2
            this.score += column[i]
            column.splice(i - 1, 1)
            moved = true
            i--
          } else {
            i--
          }
        }
        
        while (column.length < 4) {
          column.unshift(0)
        }
        
        const oldColumn = this.board.map(row => row[j])
        if (!moved) {
          moved = !column.every((val, index) => val === oldColumn[index])
        }
        
        for (let i = 0; i < 4; i++) {
          this.board[i][j] = column[i]
        }
      }
      return moved
    },
    isGameOver() {
      for (let i = 0; i < 4; i++) {
        for (let j = 0; j < 4; j++) {
          if (this.board[i][j] === 0) return false
        }
      }
      
      for (let i = 0; i < 4; i++) {
        for (let j = 0; j < 3; j++) {
          if (this.board[i][j] === this.board[i][j + 1]) return false
        }
      }
      for (let j = 0; j < 4; j++) {
        for (let i = 0; i < 3; i++) {
          if (this.board[i][j] === this.board[i + 1][j]) return false
        }
      }
      return true
    },
    resetGame() {
      this.initGame()
    },
    async submitScore(score) {
      try {
        const response = await request.post('/game/score', {
          type: '2048',
          score: score
        })
        
        if (response.data.code === 200) {
          this.$message.success('分数提交成功！')
          if (this.$parent.$refs.leaderboard) {
            await this.$parent.$refs.leaderboard.fetchLeaderboard('2048')
          }
        }
      } catch (error) {
        if (!error.message.includes('Authentication failed')) {
          this.$message.error('提交分数失败：' + error.message)
        }
      }
    },
    gameOver() {
      console.log('游戏结束 - 当前状态:', {
        score: this.score,
        isPlaying: this.isPlaying,
        board: this.board
      });
      
      this.isPlaying = false;
      this.$message.error(`游戏结束！得分：${this.score}`);
      
      // 提交分数
      this.submitScore(this.score);
    },
    // 添加更多数字的样式
    getCellStyle(value) {
      return {
        '2': { background: '#eee4da', color: '#776e65' },
        '4': { background: '#ede0c8', color: '#776e65' },
        '8': { background: '#f2b179', color: '#f9f6f2' },
        '16': { background: '#f59563', color: '#f9f6f2' },
        '32': { background: '#f67c5f', color: '#f9f6f2' },
        '64': { background: '#f65e3b', color: '#f9f6f2' },
        '128': { background: '#edcf72', color: '#f9f6f2' },
        '256': { background: '#edcc61', color: '#f9f6f2' },
        '512': { background: '#edc850', color: '#f9f6f2' },
        '1024': { background: '#edc53f', color: '#f9f6f2' },
        '2048': { background: '#edc22e', color: '#f9f6f2' }
      }[value] || { background: '#cdc1b4', color: '#776e65' }
    }
  }
}
</script>

<style scoped>
.game-2048 {
  width: 400px;
  padding: 20px;
  background: #faf8ef;
  border-radius: 8px;
}

.game-board {
  background: #bbada0;
  padding: 10px;
  border-radius: 6px;
  position: relative;
}

.row {
  display: flex;
  margin-bottom: 10px;
}

.row:last-child {
  margin-bottom: 0;
}

.cell {
  width: 80px;
  height: 80px;
  margin: 5px;
  background: rgba(238, 228, 218, 0.35);
  display: inline-flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
  font-weight: bold;
  border-radius: 4px;
  transition: all 0.15s ease;
}

.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.score {
  font-size: 20px;
  font-weight: bold;
  color: #776e65;
}

/* 添加不同数字的颜色样式 */
.cell-2 { background: #eee4da; }
.cell-4 { background: #ede0c8; }
.cell-8 { background: #f2b179; color: white; }
/* ... 其他数字的样式 ... */

/* 新增：新生成数字的动画效果 */
.cell-new {
  animation: appear 0.3s ease;
}

@keyframes appear {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

/* 数字移动的动画 */
.cell-2, .cell-4, .cell-8, .cell-16, .cell-32, .cell-64, 
.cell-128, .cell-256, .cell-512, .cell-1024, .cell-2048 {
  transition: all 0.15s ease-in-out;
}
</style> 