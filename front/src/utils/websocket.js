class WebSocketClient {
  constructor() {
    this.ws = null
    this.url = process.env.VUE_APP_WS_API
    this.messageHandlers = new Set()
    this.reconnectTimer = null
    this.manualClose = false
  }

  connect(onMessage) {
    if (typeof onMessage === 'function') {
      this.messageHandlers.add(onMessage)
    }

    if (!this.ws || this.ws.readyState === WebSocket.CLOSED) {
      this.manualClose = false
      this.createConnection()
    }

    return () => this.removeMessageHandler(onMessage)
  }

  createConnection() {
    if (!this.url) {
      console.error('WebSocket URL is empty')
      return
    }

    if (this.ws && (this.ws.readyState === WebSocket.OPEN || this.ws.readyState === WebSocket.CONNECTING)) {
      return
    }

    this.ws = new WebSocket(this.url)

    this.ws.onopen = () => {
      console.log('WebSocket connected')
    }

    this.ws.onmessage = (event) => {
      this.messageHandlers.forEach((handler) => {
        try {
          handler(event.data)
        } catch (error) {
          console.error('WebSocket message handler failed:', error)
        }
      })
    }

    this.ws.onerror = (error) => {
      console.error('WebSocket error:', error)
    }

    this.ws.onclose = () => {
      this.ws = null
      console.log('WebSocket closed')

      const shouldReconnect = !this.manualClose && this.messageHandlers.size > 0
      if (shouldReconnect && !this.reconnectTimer) {
        this.reconnectTimer = setTimeout(() => {
          this.reconnectTimer = null
          this.createConnection()
        }, 2000)
      }
    }
  }

  removeMessageHandler(onMessage) {
    if (typeof onMessage === 'function') {
      this.messageHandlers.delete(onMessage)
    }

    if (this.messageHandlers.size === 0) {
      this.close(false)
    }
  }

  send(data) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(data)
      return
    }
    console.error('WebSocket is not connected')
  }

  close(clearHandlers = true) {
    this.manualClose = true
    if (clearHandlers) {
      this.messageHandlers.clear()
    }

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }
}

export default new WebSocketClient()
