<template>
  <div class="games-container">
    <div v-if="showGames" class="games-content">
      <el-tabs v-model="activeGame" type="card" @tab-click="handleGameChange">
        <!-- 2048游戏 -->
        <el-tab-pane label="2048" name="2048">
          <div class="game-box">
            <Game2048 v-if="activeGame === '2048'" />
          </div>
        </el-tab-pane>
        
        <!-- 贪吃蛇 -->
        <el-tab-pane label="贪吃蛇" name="snake">
          <div class="game-box">
            <Snake v-if="activeGame === 'snake'" />
          </div>
        </el-tab-pane>
        
        <!-- 记忆翻牌 -->
        <el-tab-pane label="记忆翻牌" name="memory">
          <div class="game-box">
            <MemoryCard v-if="activeGame === 'memory'" />
          </div>
        </el-tab-pane>
      </el-tabs>
      
      <div class="leaderboard-container">
        <GameLeaderboard ref="leaderboard" :active-game="activeGame" />
      </div>
    </div>
    
    <div v-else class="document-content">
      <h2>常见申诉类型及处理方法</h2>
      <div class="text-content">
        <h3>特殊情况申诉 (Special Consideration)</h3>
        <p>
          在学习过程中，我们可能会遇到需要延期考试或提交作业的情况。
          常见的特殊情况申请类型包括：
        </p>
        <ul>
          <li>MC (Mitigation Circumstances) - 主要用于美英地区</li>
          <li>SC (Special Circumstances) - 主要用于澳洲地区</li>
          <li>EC (Extenuating Circumstances) - 主要用于英国地区</li>
          <li>GC (Good Cause) - 主要用于英国地区</li>
        </ul>

        <h3>学术诚信 (Academic Integrity)</h3>
        <p>学术不端问题需要格外注意，主要包括：</p>
        <ul>
          <li>Plagiarism (抄袭) - 未注明引用来源或过度引用</li>
          <li>Collusion (共谋) - 与他人不当合作</li>
          <li>Falsification (数据造假) - 篡改研究数据或结论</li>
          <li><span class="hidden-trigger" @click="triggerGame('2048')">Cheating</span> (作弊) - 考试中的不当行为</li>
        </ul>
        <p>一旦被发现可能面临严重后果，包括：</p>
        <ul>
          <li>轻微情况：重新提交作业或评分为零分</li>
          <li>严重情况：课程挂科或学术记过</li>
          <li>极严重情况：停学或开除学籍</li>
        </ul>

        <h3>成绩申诉 (Grade Appeal)</h3>
        <p>对于考试成绩，我们需要记住以下几点：</p>
        <ol>
          <li>及时查看成绩并确认申诉期限</li>
          <li>收集相关证据（作业、试卷、评分标准等）</li>
          <li>准备申诉材料（申诉信、支持文件）</li>
        </ol>

        <h3>撤课申请 (Course Withdrawal)</h3>
        <p>主要包括以下类型：</p>
        <ul>
          <li>常规撤课 (Withdraw/Drop Course)</li>
          <li>延迟撤课 (Late Withdrawal)</li>
          <li>特殊撤课 (Special Withdrawal)</li>
        </ul>

        <h3>补考与重修</h3>
        <p>不同情况的处理方式：</p>
        <ul>
          <li>Supplementary Exams (补考)</li>
          <li>Retake Course (重修课程)</li>
          <li>Resit/Retake Exam (重考)</li>
          <li>Pass by Compensation (软挂通过)</li>
        </ul>

        <h3>学籍变更</h3>
        <p>常见的学籍变更类型：</p>
        <ul>
          <li>Leave of Absence (休学)</li>
          <li>Suspension of Studies (停学)</li>
          <li>Academic Probation (学业观察)</li>
          <li>Re-enrollment (复学)</li>
        </ul>

        <div class="tips-box">
          <h4>申诉小贴士</h4>
          <ol>
            <li>注意申诉截止日期</li>
            <li>保留所有相关证据</li>
            <li>使用正式、专业的语言</li>
            <li>清晰说明申诉理由</li>
            <li>提供充分的支持材料</li>
          </ol>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Game2048 from './games/Game2048.vue'
import Snake from './games/Snake.vue'
import MemoryCard from './games/MemoryCard.vue'
import GameLeaderboard from './games/GameLeaderboard.vue'

export default {
  name: 'Games',
  components: {
    Game2048,
    Snake,
    MemoryCard,
    GameLeaderboard
  },
  data() {
    return {
      activeGame: '2048',
      showGames: false
    }
  },
  mounted() {
    window.addEventListener('keydown', this.handleKeyDown)
  },
  beforeDestroy() {
    window.removeEventListener('keydown', this.handleKeyDown)
  },
  methods: {
    handleKeyDown(e) {
      if (e.key === 'Escape' && this.showGames) {
        this.showGames = false
      }
    },
    triggerGame(gameType) {
      this.activeGame = gameType
      this.showGames = true
    },
    handleGameChange(tab) {
      if (this.$refs.leaderboard) {
        this.$refs.leaderboard.fetchLeaderboard(tab.name)
      }
    }
  }
}
</script>

<style scoped>
.games-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.games-content {
  display: flex;
  gap: 20px;
}

.el-tabs {
  flex: 1;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

.game-box {
  margin-top: 20px;
  min-height: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.leaderboard-container {
  width: 300px;
}

.document-content {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  line-height: 1.8;
  font-size: 16px;
}

.text-content {
  max-width: 800px;
  margin: 0 auto;
}

h2 {
  color: #303133;
  margin-bottom: 20px;
  text-align: center;
}

h3 {
  color: #409EFF;
  margin: 25px 0 15px;
}

.hidden-trigger {
  cursor: pointer;
  color: inherit;  /* 继承父元素的颜色 */
  text-decoration: none;  /* 移除下划线 */
}

/* 移除悬停效果 */
.hidden-trigger:hover {
  color: inherit;
}

/* 移除游戏触发器的原有样式 */
.game-trigger {
  display: none;
}

.text-content ul, 
.text-content ol {
  padding-left: 20px;
  margin: 10px 0;
}

.text-content li {
  margin: 8px 0;
  line-height: 1.6;
}

.tips-box {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 6px;
  margin-top: 30px;
}

.tips-box h4 {
  color: #409EFF;
  margin: 0 0 15px;
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .games-content {
    flex-direction: column;
  }
  
  .leaderboard-container {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .document-content {
    padding: 20px;
  }
  
  .text-content {
    font-size: 14px;
  }
}
</style> 