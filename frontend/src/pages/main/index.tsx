import { useEffect, useState } from 'react'
import { Client } from '@stomp/stompjs'

function MainPage() {
  const [connected, setConnected] = useState(false)

  useEffect(() => {
    const client = new Client({
      brokerURL: 'ws://localhost:8080/ws/connect',
      onConnect: () => {
        setConnected(true)
        console.log('Connected to WebSocket')
      },
      onDisconnect: () => {
        setConnected(false)
        console.log('Disconnected from WebSocket')
      },
      onStompError: (frame) => {
        console.error('STOMP Error:', frame)
        setConnected(false)
      }
    })

    client.activate()

    return () => {
      client.deactivate()
    }
  }, [])

  return (
    <div>
      <h1>Crypto WebSocket Test</h1>
      <div>
        Connection Status: {connected ? 'Connected' : 'Disconnected'}
      </div>
    </div>
  )
}

export default MainPage 