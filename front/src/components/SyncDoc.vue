<template>
  <div class="sync-doc">
    <div class="doc-header">
      <h2>{{ docTitle }}</h2>
      <!-- 管理员才显示编辑按钮 -->
      <el-button 
        v-if="isAdmin" 
        type="primary" 
        @click="toggleEdit"
      >
        {{ isEditing ? '保存' : '编辑' }}
      </el-button>
    </div>
    
    <div class="doc-container">
      <!-- 目录区域 -->
      <div class="catalog" :class="{ 'catalog-fixed': isScrolled }">
        <div class="catalog-title">目录</div>
        <div class="catalog-content">
          <div 
            v-for="(item, index) in catalog" 
            :key="index"
            class="catalog-item"
            :class="{ 
              'active': currentHeading === item.id,
              [`level-${item.level}`]: true 
            }"
            @click="scrollToHeading(item.id)"
          >
            {{ item.text }}
          </div>
        </div>
      </div>

      <!-- 编辑器区域 -->
      <div class="content-container">
        <div v-if="isAdmin && isEditing" class="editor">
          <Toolbar
            style="border-bottom: 1px solid #ccc"
            :editor="editor"
            :defaultConfig="toolbarConfig"
            :mode="mode"
          />
          <Editor
            style="height: 500px; overflow-y: hidden;"
            v-model="content"
            :defaultConfig="editorConfig"
            :mode="mode"
            @onCreated="handleCreated"
            @onChange="handleChange"
          />
        </div>
        <!-- 阅读模式 -->
        <div v-else ref="contentView" class="content-view" v-html="content"></div>
      </div>
    </div>
  </div>
</template>

<script>
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { mapState } from 'vuex'
import request from '@/utils/request'
import { Message } from 'element-ui'
import { throttle } from 'lodash'  // 需要安装 lodash

export default {
  name: 'SyncDoc',
  components: {
    Editor,
    Toolbar
  },
  data() {
    return {
      docTitle: '同步文档',
      content: '',
      isEditing: false,
      lastSavedContent: '',
      editor: null,
      toolbarConfig: {
        excludeKeys: []
      },
      editorConfig: {
        placeholder: '请输入内容...',
        MENU_CONF: {
          uploadImage: {
            customUpload(file, insertFn) {
              // 创建 FormData
              const formData = new FormData()
              formData.append('file', file)

              // 使用 axios 上传
              request.post('/user/uploadAvatar', formData, {
                headers: {
                  'Content-Type': 'multipart/form-data',
                  'Authorization': 'Bearer ' + localStorage.getItem('token')
                }
              }).then(res => {
                if (res.data.code === 200) {
                  // 上传成功，插入图片
                  insertFn(res.data.data.avatarUrl)
                  Message.success('图片上传成功')
                } else {
                  Message.error(res.data.msg || '图片上传失败')
                }
              }).catch(err => {
                console.error('图片上传失败:', err)
                Message.error('图片上传失败')
              })

              // 返回 true 表示使用自定义上传
              return true
            }
          }
        }
      },
      mode: 'default',
      catalog: [],  // 目录数据
      currentHeading: '',  // 当前激活的标题
      isScrolled: false,  // 是否已滚动
    }
  },
  computed: {
    ...mapState(['userRole']),
    isAdmin() {
      return this.userRole === 'admin'
    }
  },
  created() {
    this.fetchContent()
  },
  mounted() {
    // 监听滚动事件
    window.addEventListener('scroll', this.handleScroll)
    // 初始化目录
    this.$nextTick(() => {
      this.generateCatalog()
    })
  },
  methods: {
    handleCreated(editor) {
      this.editor = editor
    },
    async fetchContent() {
      try {
        const response = await request.get('/sync-doc/content')
        if (response.data.code === 200) {
          this.content = response.data.data.content || ''
          this.lastSavedContent = this.content
          // 获取内容后更新目录
          this.$nextTick(() => {
            this.generateCatalog()
          })
        } else {
          this.$message.error(response.data.msg || '获取文档内容失败')
        }
      } catch (error) {
        console.error('获取文档内容失败:', error)
        this.$message.error('获取文档内容失败')
      }
    },
    async toggleEdit() {
      if (this.isEditing) {
        try {
          const response = await request.post('/sync-doc/content', {
            content: this.content
          })
          if (response.data.code === 200) {
            this.lastSavedContent = this.content
            this.isEditing = false
            this.$message.success('保存成功')
            // 保存后更新目录
            this.$nextTick(() => {
              this.generateCatalog()
            })
          } else {
            this.$message.error(response.data.msg || '保存失败')
          }
        } catch (error) {
          console.error('保存失败:', error)
          this.$message.error('保存失败')
        }
      } else {
        this.isEditing = true
      }
    },
    // 生成目录
    generateCatalog() {
      if (!this.$refs.contentView) return
      
      const headings = this.$refs.contentView.querySelectorAll('h1, h2, h3, h4, h5, h6')
      console.log('找到的标题:', headings.length)  // 添加日志
      
      this.catalog = Array.from(headings).map((heading, index) => {
        const id = `heading-${index}`
        heading.id = id  // 给标题添加 id
        return {
          id,
          text: heading.textContent,
          level: parseInt(heading.tagName.charAt(1)),
          offsetTop: heading.offsetTop
        }
      })
      
      console.log('生成的目录:', this.catalog)  // 添加日志
    },

    // 处理编辑器内容变化
    handleChange(editor) {
      console.log('内容变化了:', editor.getHtml())  // 添加日志
      this.$nextTick(() => {
        if (this.isEditing) {
          // 编辑模式下，从编辑器获取内容
          const tempDiv = document.createElement('div')
          tempDiv.innerHTML = editor.getHtml()
          const headings = tempDiv.querySelectorAll('h1, h2, h3, h4, h5, h6')
          this.catalog = Array.from(headings).map((heading, index) => {
            const id = `heading-${index}`
            return {
              id,
              text: heading.textContent,
              level: parseInt(heading.tagName.charAt(1))
            }
          })
        } else {
          // 阅读模式下，从 DOM 获取内容
          this.generateCatalog()
        }
      })
    },

    // 滚动到指定标题
    scrollToHeading(id) {
      const element = document.getElementById(id)
      if (element) {
        element.scrollIntoView({ behavior: 'smooth' })
      }
    },

    // 处理滚动事件
    handleScroll: throttle(function() {
      // 设置目录固定状态
      this.isScrolled = window.scrollY > 100

      // 更新当前标题
      if (this.catalog.length === 0) return
      
      const scrollTop = window.scrollY + 100 // 偏移量
      for (let i = this.catalog.length - 1; i >= 0; i--) {
        if (scrollTop >= this.catalog[i].offsetTop) {
          this.currentHeading = this.catalog[i].id
          break
        }
      }
    }, 100)
  },
  beforeDestroy() {
    window.removeEventListener('scroll', this.handleScroll)
    if (this.editor == null) return
    this.editor.destroy() // 组件销毁时，及时销毁编辑器
  },
  beforeRouteLeave(to, from, next) {
    if (this.isEditing && this.content !== this.lastSavedContent) {
      this.$confirm('有未保存的更改，确定要离开吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        next()
      }).catch(() => {
        next(false)
      })
    } else {
      next()
    }
  },
  watch: {
    content: {
      handler(newVal) {
        this.$nextTick(() => {
          this.generateCatalog()
        })
      },
      deep: true
    }
  }
}
</script>

<style scoped>
.sync-doc {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.doc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.doc-container {
  display: flex;
  gap: 20px;
  position: relative;
}

.catalog {
  width: 250px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  padding: 16px;
  max-height: calc(100vh - 120px);
  overflow-y: auto;
}

.catalog-fixed {
  position: fixed;
  top: 20px;
}

.catalog-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.catalog-content {
  font-size: 14px;
}

.catalog-item {
  cursor: pointer;
  padding: 4px 8px;
  margin: 4px 0;
  border-radius: 4px;
  transition: all 0.3s;
}

.catalog-item:hover {
  background: #f5f7fa;
}

.catalog-item.active {
  background: #ecf5ff;
  color: #409eff;
}

.level-1 { padding-left: 8px; font-weight: bold; }
.level-2 { padding-left: 16px; }
.level-3 { padding-left: 24px; }
.level-4 { padding-left: 32px; }
.level-5 { padding-left: 40px; }
.level-6 { padding-left: 48px; }

.content-container {
  flex: 1;
  min-width: 0;
}

/* 确保内容区域不会被目录遮挡 */
.editor-container {
  margin-left: 270px;
}

@media (max-width: 768px) {
  .catalog {
    display: none;
  }
  
  .editor-container {
    margin-left: 0;
  }
}

/* 自定义阅读模式的样式 */
.content-view :deep(h1) {
  font-size: 28px;
  margin-bottom: 20px;
}

.content-view :deep(h2) {
  font-size: 24px;
  margin-bottom: 16px;
}

.content-view :deep(p) {
  line-height: 1.6;
  margin-bottom: 16px;
}

.content-view :deep(ul), 
.content-view :deep(ol) {
  padding-left: 20px;
  margin-bottom: 16px;
}
</style>
