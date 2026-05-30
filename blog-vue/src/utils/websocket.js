import store from '@/store/store'

class WebSocketService {
  constructor() {
    this.ws = null
    this.handlers = {}
    this.isConnected = false
    this.reconnectTimer = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
  }

  connect() {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) return
    if (store.state.token === '') {
      console.warn('未登录，无法建立聊天连接')
      return
    }

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host
    const url = `${protocol}//${host}/ws/chat?token=${encodeURIComponent(store.state.token)}`

    this.ws = new WebSocket(url)

    this.ws.onopen = () => {
      this.isConnected = true
      this.reconnectAttempts = 0
      console.log('聊天连接已建立')
      this._emit('open')
    }

    this.ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        this._emit(data.type, data)
      } catch (e) {
        console.error('聊天消息解析失败', e)
      }
    }

    this.ws.onclose = () => {
      this.isConnected = false
      console.log('聊天连接已关闭')
      this._emit('close')
      this._tryReconnect()
    }

    this.ws.onerror = (err) => {
      console.error('聊天连接错误', err)
      this._emit('error', err)
    }
  }

  disconnect() {
    this.reconnectAttempts = this.maxReconnectAttempts // 阻止重连
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    this.isConnected = false
  }

  sendChat(content) {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      console.warn('聊天连接未建立')
      return false
    }
    this.ws.send(JSON.stringify({
      type: 'chat',
      content: content
    }))
    return true
  }

  on(event, handler) {
    if (!this.handlers[event]) {
      this.handlers[event] = []
    }
    this.handlers[event].push(handler)
  }

  off(event, handler) {
    if (!this.handlers[event]) return
    this.handlers[event] = this.handlers[event].filter(h => h !== handler)
  }

  _emit(event, data) {
    const handlers = this.handlers[event] || []
    handlers.forEach(handler => handler(data))
  }

  _tryReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) return
    if (store.state.token === '') return
    this.reconnectAttempts++
    const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 10000)
    console.log(`聊天连接将在 ${delay}ms 后重连 (第${this.reconnectAttempts}次)`)
    this.reconnectTimer = setTimeout(() => {
      this.connect()
    }, delay)
  }
}

export default new WebSocketService()
