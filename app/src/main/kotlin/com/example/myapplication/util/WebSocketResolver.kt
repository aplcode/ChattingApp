package com.example.myapplication.util

import android.content.ContentValues
import android.util.Log
import com.example.myapplication.dto.CustomerLogInInfoDto
import com.example.myapplication.dto.CustomerSignUpInfoDto
import com.example.myapplication.dto.DialogDto
import com.example.myapplication.dto.MessageDto
import com.example.myapplication.dto.ResponseDto
import com.example.myapplication.dto.UserDto
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.Stomp.ConnectionProvider
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.provider.OkHttpConnectionProvider.TAG

class WebSocketResolver private constructor() {
    private val mapper = jacksonObjectMapper()

    private val stompClient: StompClient = Stomp.over(ConnectionProvider.OKHTTP, SOCKET_URL)
        .withServerHeartbeat(30000)

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    fun logIn(
        credentials: CustomerLogInInfoDto,
        successful: Runnable,
        unsuccessful: Runnable,
        error: Consumer<in Throwable>,
    ) = listenTopicAndSend("$TOPIC_URL/login", LOGIN_LINK_SOCKET, credentials, successful, unsuccessful, error)

    fun signUp(
        credentials: CustomerSignUpInfoDto,
        successful: Runnable,
        unsuccessful: Runnable,
        error: Consumer<in Throwable>,
    ) = listenTopicAndSend("$TOPIC_URL/signup", SIGNUP_LINK_SOCKET, credentials, successful, unsuccessful, error)

    fun getDialogs(
        user: UserDto,
        successful: Consumer<List<DialogDto>>,
        unsuccessful: Runnable,
        error: Consumer<in Throwable>
    ) {
        initConnection()
        topicListenerDialogDto("$TOPIC_URL/getDialogs", successful, unsuccessful, error)
        webSocketSend(GET_DIALOGS_LINK_SOCKET, user)
    }

    fun getOldMessages(
        users: Pair<String, String>,
        successful: Consumer<List<MessageDto>>,
        unsuccessful: Runnable,
        error: Consumer<in Throwable>
    ) {
        initConnection()
        topicListenerMessageDto("$TOPIC_URL/getMessages", successful, unsuccessful, error)
        webSocketSend(GET_MESSAGES_LINK_SOCKET, users)
    }

    fun sendMessage(
        dto: MessageDto,
        successful: Runnable,
        unsuccessful: Runnable,
        error: Consumer<in Throwable>
    ) {
        initConnection()
        topicListenerResponseDto("$TOPIC_URL/sendMessage", successful, unsuccessful, error)
        webSocketSend(SEND_MESSAGE_LINK_SOCKET, dto)
    }

    private fun listenTopicAndSend(
        listenFrom: String, sendTo: String,
        credentials: Any,
        successful: Runnable,
        unsuccessful: Runnable,
        error: Consumer<in Throwable>,
    ) {
        initConnection()
        topicListenerResponseDto(listenFrom, successful, unsuccessful, error)
        webSocketSend(sendTo, credentials)
    }

    private fun topicListenerResponseDto(
        topicName: String,
        successful: Runnable,
        unsuccessful: Runnable,
        error: Consumer<in Throwable>,
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
            }, error::accept)

        resetSubscriptions()
        compositeDisposable.add(topicSubscribe)
    }

    private fun topicListenerDialogDto(
        topicName: String, successful: Consumer<List<DialogDto>>,
        unsuccessful: Runnable,
        error: Consumer<in Throwable>,
    ) {
        val topicSubscribe = stompClient.topic(topicName)
            .subscribeOn(Schedulers.io(), false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                try {
                    val dialogsList = mapper.readValue<List<DialogDto>>(it.payload)
                    Log.d(ContentValues.TAG, "Get response from topic [$topicName]. Receive list with [${dialogsList.size}] dialogs")
                    successful.accept(dialogsList)
                } catch (e: Exception) {
                    Log.e(ContentValues.TAG, "Get response from topic [$topicName] with exception:", e)
                    unsuccessful.run()
                }
            }, error::accept)

        resetSubscriptions()
        compositeDisposable.add(topicSubscribe)
    }

    private fun topicListenerMessageDto(
        topicName: String, successful: Consumer<List<MessageDto>>,
        unsuccessful: Runnable,
        error: Consumer<in Throwable>,
    ) {
        val topicSubscribe = stompClient.topic(topicName)
            .subscribeOn(Schedulers.io(), false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                try {
                    val messagesList = mapper.readValue<List<MessageDto>>(it.payload)
                    Log.d(ContentValues.TAG, "Get response from topic [$topicName]. Receive list with [${messagesList.size}] dialogs")
                    successful.accept(messagesList)
                } catch (e: Exception) {
                    Log.e(ContentValues.TAG, "Get response from topic [$topicName] with exception:", e)
                    unsuccessful.run()
                }
            }, error::accept)

        resetSubscriptions()
        compositeDisposable.add(topicSubscribe)
    }

    private fun webSocketSend(url: String, data: Any) =
        sendCompletable(stompClient.send(url, mapper.writeValueAsString(data)))

    private fun sendCompletable(request: Completable) {
        val callback = request.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { Log.d(TAG, "Stomp sent") },
                { Log.e(TAG, "Stomp error", it) }
            )

        compositeDisposable.add(callback)
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
        private const val URL = "192.168.0.107:5000"

        private const val PATH = "/api/v1/chat"
        private const val SOCKET_URL = "$WS_PROTOCOL$URL$PATH/websocket"

        private val LOGIN_LINK_SOCKET = "$PATH/login/?token=$sessionId"
        private val SIGNUP_LINK_SOCKET = "$PATH/signup/?token=$sessionId"
        private val GET_DIALOGS_LINK_SOCKET = "$PATH/getDialogs/?token=$sessionId"
        private val GET_MESSAGES_LINK_SOCKET = "$PATH/getMessages/?token=$sessionId"
        private val SEND_MESSAGE_LINK_SOCKET = "$PATH/sendMessage/?token=$sessionId"

        private val TOPIC_URL = "/topic/?token=$sessionId"
    }
}