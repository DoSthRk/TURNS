class WebSocketClient {
  constructor() {
    this.ws = null
    this.url = process.env.VUE_APP_WS_API
  }

  connect(onMessage) {
    this.ws = new WebSocket(this.url)
    
    this.ws.onopen = () => {
      console.log('WebSocket连接成功')
    }
    
    this.ws.onmessage = (event) => {
      if (onMessage) {
        onMessage(event.data)
      }
    }
    
    this.ws.onerror = (error) => {
      console.error('WebSocket错误:', error)
    }
    
    this.ws.onclose = () => {
      console.log('WebSocket连接关闭')
      // 可以在这里添加重连逻辑
    }
  }

  send(data) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(data)
    } else {
      console.error('WebSocket未连接')
    }
  }

  close() {
    if (this.ws) {
      this.ws.close()
    }
  }
}

export default new WebSocketClient() 