<template>
  <div id="chat-container">
    <el-card id="chat-card">
      <div slot="header" class="chat-header">
        <span><i class="el-icon-chat-line-square"></i>&nbsp;聊天室</span>
        <el-tag size="small" :type="onlineCount > 0 ? 'success' : 'info'" effect="dark">
          在线 {{ onlineCount }}
        </el-tag>
      </div>

      <div id="messages-area" ref="messagesArea">
        <div v-if="loading" class="loading-tip">
          <i class="el-icon-loading"></i>&nbsp;加载聊天记录...
        </div>
        <div v-else-if="messages.length === 0" class="empty-tip">
          暂无消息，开始聊天吧！
        </div>
        <div
          v-for="msg in messages"
          :key="msg.id || msg._tempId"
          class="message-item"
          :class="{ 'is-self': msg.userId === currentUserId }"
        >
          <div class="message-avatar">
            <img
              v-if="msg.userAvatar"
              :src="msg.userAvatar"
              @error="onAvatarError($event)"
            >
            <i v-else class="el-icon-user-solid"></i>
          </div>
          <div class="message-body">
            <div class="message-meta">
              <span class="message-user">{{ msg.userName }}</span>
              <span class="message-time">{{ msg.createdAt ? formatTime(msg.createdAt) : '' }}</span>
            </div>
            <div class="message-content">{{ msg.content }}</div>
          </div>
        </div>
      </div>

      <div id="input-area">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="3"
          placeholder="按 Enter 发送，Shift+Enter 换行"
          @keydown.enter.native="onSend"
          :disabled="!isConnected"
          maxlength="500"
          show-word-limit
        >
        </el-input>
        <div class="input-actions">
          <span class="connection-status" :class="{ connected: isConnected }">
            <i :class="isConnected ? 'el-icon-success' : 'el-icon-warning'"></i>
            {{ isConnected ? '已连接' : '未连接' }}
          </span>
          <el-button
            type="primary"
            @click="sendMessage"
            :disabled="!isConnected || !inputText.trim()"
          >
            发送
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import ws from '@/utils/websocket'

export default {
  name: 'chat',
  data() {
    return {
      messages: [],
      inputText: '',
      onlineCount: 0,
      isConnected: false,
      currentUserId: null,
      loading: true,
      tempIdCounter: 0
    }
  },
  created() {
    this.currentUserId = this.getUserIdFromToken()
    this.initWebSocket()
  },
  beforeDestroy() {
    this.unbindEvents()
    ws.disconnect()
  },
  methods: {
    initWebSocket() {
      if (this.$store.state.token === '') {
        this.$message.warning('请先登录')
        this.$router.push('/')
        return
      }

      ws.on('open', this.onOpen)
      ws.on('chat', this.onChat)
      ws.on('history', this.onHistory)
      ws.on('online', this.onOnline)
      ws.on('close', this.onClose)
      ws.on('error', this.onError)

      ws.connect()
    },
    unbindEvents() {
      ws.off('open', this.onOpen)
      ws.off('chat', this.onChat)
      ws.off('history', this.onHistory)
      ws.off('online', this.onOnline)
      ws.off('close', this.onClose)
      ws.off('error', this.onError)
    },
    onOpen() {
      this.isConnected = true
    },
    onClose() {
      this.isConnected = false
    },
    onError() {
      this.isConnected = false
    },
    onHistory(data) {
      this.loading = false
      if (data.messages && Array.isArray(data.messages)) {
        this.messages = data.messages
        this.$nextTick(() => this.scrollToBottom())
      }
    },
    onChat(data) {
      this.messages.push({
        id: data.id,
        _tempId: null,
        userId: data.userId,
        userName: data.userName,
        userAvatar: data.userAvatar,
        content: data.content,
        createdAt: data.createdAt
      })
      this.$nextTick(() => this.scrollToBottom())
    },
    onOnline(data) {
      this.onlineCount = data.count
    },
    sendMessage() {
      const content = this.inputText.trim()
      if (!content) return
      if (content.length > 500) {
        this.$message.warning('消息不能超过500字')
        return
      }
      ws.sendChat(content)
      this.inputText = ''
      this.$nextTick(() => this.scrollToBottom())
    },
    onSend(e) {
      if (e.shiftKey) return
      e.preventDefault()
      this.sendMessage()
    },
    scrollToBottom() {
      const area = this.$refs.messagesArea
      if (area) {
        area.scrollTop = area.scrollHeight
      }
    },
    formatTime(timeStr) {
      if (!timeStr) return ''
      try {
        const date = new Date(timeStr)
        const pad = n => String(n).padStart(2, '0')
        return `${pad(date.getHours())}:${pad(date.getMinutes())}`
      } catch (e) {
        return timeStr
      }
    },
    getUserIdFromToken() {
      // 从 JWT token 中提取 userId
      const token = this.$store.state.token
      if (!token) return null
      try {
        const payload = JSON.parse(atob(token.split('.')[1]))
        return payload.userId
      } catch (e) {
        return null
      }
    },
    onAvatarError(e) {
      e.target.style.display = 'none'
    }
  }
}
</script>

<style scoped>
#chat-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 15px;
}

#chat-card {
  min-height: 600px;
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

#messages-area {
  height: 400px;
  overflow-y: auto;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 15px;
}

.loading-tip, .empty-tip {
  text-align: center;
  color: #909399;
  padding: 40px 0;
  font-size: 14px;
}

.message-item {
  display: flex;
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
  font-size: 18px;
  color: #909399;
}

.message-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.message-body {
  margin-left: 10px;
  flex: 1;
  min-width: 0;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.message-user {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
}

.message-time {
  font-size: 11px;
  color: #c0c4cc;
}

.message-content {
  background: #fff;
  padding: 8px 12px;
  border-radius: 4px 12px 12px 12px;
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
  word-break: break-word;
  display: inline-block;
  max-width: 80%;
}

#input-area {
  border-top: 1px solid #ebeef5;
  padding-top: 10px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.connection-status {
  font-size: 12px;
  color: #e6a23c;
}

.connection-status.connected {
  color: #67c23a;
}
</style>
