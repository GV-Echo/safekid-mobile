package com.example.safekidsmobile.data.websocket

import com.example.safekidsmobile.data.model.LocationData
import com.example.safekidsmobile.data.model.WebSocketMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

interface LocationUpdateListener {
    fun onLocationUpdate(location: LocationData)
    fun onConnectionOpen()
    fun onConnectionClosed()
    fun onError(error: String)
}

@Singleton
class WebSocketLocationService @Inject constructor() {
    private var webSocketClient: LocationWebSocketClient? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val _locationUpdates = MutableSharedFlow<LocationData>(replay = 0)
    val locationUpdates: SharedFlow<LocationData> = _locationUpdates.asSharedFlow()
    
    private val _connectionStatus = MutableSharedFlow<ConnectionStatus>(replay = 1)
    val connectionStatus: SharedFlow<ConnectionStatus> = _connectionStatus.asSharedFlow()
    
    private val listeners = mutableListOf<LocationUpdateListener>()

    enum class ConnectionStatus {
        DISCONNECTED, CONNECTING, CONNECTED, ERROR
    }

    fun connect(wsUrl: String = "ws://localhost:8000/ws/locations") {
        if (webSocketClient != null && webSocketClient!!.isOpen) {
            return  // Already connected
        }

        scope.launch {
            try {
                _connectionStatus.emit(ConnectionStatus.CONNECTING)
                webSocketClient = LocationWebSocketClient(
                    URI(wsUrl),
                    onMessage = { message ->
                        handleWebSocketMessage(message)
                    },
                    onOpen = {
                        scope.launch {
                            _connectionStatus.emit(ConnectionStatus.CONNECTED)
                            listeners.forEach { it.onConnectionOpen() }
                        }
                    },
                    onClose = {
                        scope.launch {
                            _connectionStatus.emit(ConnectionStatus.DISCONNECTED)
                            listeners.forEach { it.onConnectionClosed() }
                            // Attempt reconnection after 5 seconds
                            kotlinx.coroutines.delay(5000)
                            connect(wsUrl)
                        }
                    },
                    onError = { error ->
                        scope.launch {
                            _connectionStatus.emit(ConnectionStatus.ERROR)
                            listeners.forEach { it.onError(error) }
                        }
                    }
                )
                webSocketClient?.connect()
            } catch (e: Exception) {
                scope.launch {
                    _connectionStatus.emit(ConnectionStatus.ERROR)
                    listeners.forEach { it.onError(e.message ?: "Unknown error") }
                }
            }
        }
    }

    fun disconnect() {
        webSocketClient?.close()
        webSocketClient = null
        scope.launch {
            _connectionStatus.emit(ConnectionStatus.DISCONNECTED)
        }
    }

    fun addListener(listener: LocationUpdateListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: LocationUpdateListener) {
        listeners.remove(listener)
    }

    private fun handleWebSocketMessage(message: String) {
        scope.launch {
            try {
                val wsMessage = Json.decodeFromString<WebSocketMessage>(message)
                
                if (wsMessage.type == "location.update" && wsMessage.data != null) {
                    _locationUpdates.emit(wsMessage.data!!)
                    listeners.forEach { it.onLocationUpdate(wsMessage.data!!) }
                }
            } catch (e: Exception) {
                listeners.forEach { it.onError("Failed to parse message: ${e.message}") }
            }
        }
    }

    private class LocationWebSocketClient(
        uri: URI,
        private val onMessage: (String) -> Unit,
        private val onOpen: () -> Unit,
        private val onClose: () -> Unit,
        private val onError: (String) -> Unit
    ) : WebSocketClient(uri) {
        
        override fun onOpen(handshakedata: ServerHandshake?) {
            onOpen()
        }

        override fun onMessage(message: String?) {
            message?.let { onMessage(it) }
        }

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            onClose()
        }

        override fun onError(ex: Exception?) {
            onError(ex?.message ?: "Unknown WebSocket error")
        }
    }
}
