<template>
  <div class="memory-card">
    <div class="game-header">
      <div class="score">步数: {{ moves }}</div>
      <el-button type="primary" size="small" @click="resetGame">重新开始</el-button>
    </div>
    <div class="game-board">
      <div class="card-grid">
        <div v-for="(card, index) in cards" 
             :key="index"
             class="card"
             :class="{ flipped: card.isFlipped, matched: card.isMatched }"
             @click="flipCard(index)">
          <div class="card-inner">
            <div class="card-front">?</div>
            <div class="card-back">{{ card.value }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'MemoryCard',
  data() {
    return {
      cards: [],
      moves: 0,
      flippedCards: [],
      canFlip: true
    }
  },
  mounted() {
    this.initGame()
  },
  methods: {
    initGame() {
      // 创建配对的卡片
      const values = ['🎮', '🎲', '🎯', '🎨', '🎭', '🎪', '🎫', '🎪']
      this.cards = [...values, ...values]
        .sort(() => Math.random() - 0.5)
        .map(value => ({
          value,
          isFlipped: false,
          isMatched: false
        }))
      this.moves = 0
      this.flippedCards = []
      this.canFlip = true
    },

    async flipCard(index) {
      if (!this.canFlip || this.cards[index].isFlipped || this.cards[index].isMatched) {
        return
      }

      this.cards[index].isFlipped = true
      this.flippedCards.push(index)

      if (this.flippedCards.length === 2) {
        this.moves++
        this.canFlip = false
        
        const [firstIndex, secondIndex] = this.flippedCards
        if (this.cards[firstIndex].value === this.cards[secondIndex].value) {
          // 匹配成功
          this.cards[firstIndex].isMatched = true
          this.cards[secondIndex].isMatched = true
          this.canFlip = true
          this.flippedCards = []
          
          // 检查是否完成游戏
          if (this.cards.every(card => card.isMatched)) {
            // 计算得分：基础分1000减去翻牌次数 * 10
            const score = Math.max(1000 - this.moves * 10, 0)
            this.$message.success(`恭喜完成！总共用了 ${this.moves} 步，得分：${score}`)
            this.submitScore(score)
          }
        } else {
          // 匹配失败，翻回去
          await new Promise(resolve => setTimeout(resolve, 1000))
          this.cards[firstIndex].isFlipped = false
          this.cards[secondIndex].isFlipped = false
          this.canFlip = true
          this.flippedCards = []
        }
      }
    },

    async submitScore(steps) {
      try {
        const response = await request.post('/game/score', {
          type: 'memory',
          score: steps
        });
        
        if (response.data.code === 200) {
          this.$message.success('记录提交成功！');
          if (this.$parent.$refs.leaderboard) {
            await this.$parent.$refs.leaderboard.fetchLeaderboard('memory');
          }
        }
      } catch (error) {
        if (!error.message.includes('Authentication failed')) {
          this.$message.error('提交记录失败：' + error.message);
        }
      }
    },

    resetGame() {
      this.initGame()
    }
  }
}
</script>

<style scoped>
.memory-card {
  width: 600px;
  padding: 20px;
  background: #faf8ef;
  border-radius: 8px;
}

.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  perspective: 1000px;
}

.card {
  aspect-ratio: 1;
  cursor: pointer;
  position: relative;
  transform-style: preserve-3d;
  transition: transform 0.5s;
}

.card.flipped {
  transform: rotateY(180deg);
}

.card-inner {
  position: relative;
  width: 100%;
  height: 100%;
  text-align: center;
  transition: transform 0.8s;
  transform-style: preserve-3d;
}

.card-front,
.card-back {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2em;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.card-front {
  background: #bbada0;
  color: #f9f6f2;
}

.card-back {
  background: #eee4da;
  transform: rotateY(180deg);
}

.matched {
  opacity: 0.7;
  cursor: default;
}
</style> 