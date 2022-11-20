package com.example.myapplication.util

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.myapplication.ChatSocketMessage
import com.example.myapplication.dto.CustomerLogInInfoDto
import com.example.myapplication.dto.CustomerSignUpInfoDto
import com.example.myapplication.dto.ResponseDto
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.Stomp.ConnectionProvider
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompMessage
import ua.naiksoftware.stomp.provider.OkHttpConnectionProvider.TAG

class WebSocketResolver private constructor() : ViewModel() {
    private val mapper = jacksonObjectMapper()

    private val stompClient: StompClient = Stomp.over(ConnectionProvider.OKHTTP, SOCKET_URL)
        .withServerHeartbeat(30000)

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    private fun initConnection() {
        if (initFlag.get()) {
            return
        }

        initFlag.set(true)
        resetSubscriptions()

        val lifecycleSubscribe = stompClient.lifecycle()
            .subscribeOn(Schedulers.io(), false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                when (it.type!!) {
                    LifecycleEvent.Type.OPENED -> Log.d(TAG, "Stomp connection opened")
                    LifecycleEvent.Type.ERROR -> Log.e(TAG, "Error", it.exception)
                    LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT,
                    LifecycleEvent.Type.CLOSED,
                    -> {
                        initFlag.set(false)
                        Log.i(TAG, "Stomp connection closed")
                    }
                }
            }

        compositeDisposable.add(lifecycleSubscribe)

        if (!stompClient.isConnected) {
            stompClient.connect()
        }
    }

    private fun resetSubscriptions() {
        Log.i(TAG, "Reset subscriptions")
        compositeDisposable.dispose()
        compositeDisposable = CompositeDisposable()
    }

    fun sendMessage(text: String) {
        initConnection()
        val chatSocketMessage = ChatSocketMessage(text = text, author = "Me", datetime = LocalDateTime.now())
        sendCompletable(stompClient.send(CHAT_LINK_SOCKET, mapper.writeValueAsString(chatSocketMessage)))
    }

    fun logIn(
        credentials: CustomerLogInInfoDto,
        successful: Runnable,
        unsuccessful: Runnable,
        error: java.util.function.Consumer<in Throwable>,
    ) {
        initConnection()
        topicListenerResponseDto("/topic/login", successful, unsuccessful, error)

        webSocketSend(LOGIN_LINK_SOCKET, credentials)
    }

    fun signUp(
        credentials: CustomerSignUpInfoDto,
        successful: Runnable,
        unsuccessful: Runnable,
        error: java.util.function.Consumer<in Throwable>,
    ) {
        initConnection()

        topicListenerResponseDto("$TOPIC_URL/signup", successful, unsuccessful, error)
        webSocketSend(SIGNUP_LINK_SOCKET, credentials)
    }

    private fun topicListenerResponseDto(
        topicName: String, successful: Runnable,
        unsuccessful: Runnable,
        error: java.util.function.Consumer<in Throwable>,
    ) {
        val topicSubscribe = stompClient.topic(topicName)
            .subscribeOn(Schedulers.io(), false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.d(ContentValues.TAG, "Get response from topic [$topicName] ${it.payload}")
                val message = mapper.readValue<ResponseDto>(it.payload)

                if (message.status == 0) {
                    successful.run()
                } else {
                    unsuccessful.run()
                }
            }, {
                error.accept(it)
            })

        compositeDisposable.add(topicSubscribe)
    }

    private fun webSocketSend(url: String, data: Any) =
        sendCompletable(stompClient.send(url, mapper.writeValueAsString(data)))

    private fun topicListener(topicName: String, onNext: Consumer<in StompMessage>, onError: Consumer<in Throwable>) {
        val topicSubscribe = stompClient.topic(topicName)
            .subscribeOn(Schedulers.io(), false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext, onError)

        compositeDisposable.add(topicSubscribe)
    }

    private fun sendCompletable(request: Completable) {
        compositeDisposable.add(
            request.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { Log.d(TAG, "Stomp sent") },
                    { Log.e(TAG, "Stomp error", it) }
                )
        )
    }

    @Suppress("unused")
    private fun healthCheck() {
        val url = URL("$HTTP_PROTOCOL$URL/actuator/health")

        with(url.openConnection() as HttpURLConnection) {
            println("Sent 'GET' request to URL : $url; Response Code : $responseCode")
            inputStream.bufferedReader().use {
                it.lines().forEach(System.out::println)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        stompClient.disconnect()
        compositeDisposable.dispose()
    }

    init {
        initFlag.set(false)
        initConnection()
    }

    companion object {
        private val initFlag = AtomicBoolean()
        private val instance = WebSocketResolver()
        fun getInstance() = instance

        private val sessionId = SessionContext.CurrentSession.id

        private const val HTTP_PROTOCOL = "http://"
        private const val WS_PROTOCOL = "ws://"
        private const val URL = "192.168.0.108:5000"

        private const val PATH = "/api/v1/chat"
        private const val SOCKET_URL = "$WS_PROTOCOL$URL$PATH/websocket"
        private const val CHAT_LINK_SOCKET = "$PATH/sock"

        private val LOGIN_LINK_SOCKET = "$PATH/login/?token=$sessionId"
        private val SIGNUP_LINK_SOCKET = "$PATH/signup/?token=$sessionId"
        private val TOPIC_URL = "/topic/?token=$sessionId"
    }
}