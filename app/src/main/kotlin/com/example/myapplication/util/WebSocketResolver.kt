package com.example.myapplication.util

import android.content.ContentValues
import android.util.Log
import com.example.myapplication.dto.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.Stomp.ConnectionProvider
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.provider.OkHttpConnectionProvider.TAG
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer

class WebSocketResolver private constructor() {
    private val mapper = jacksonObjectMapper()

    private val stompClient: StompClient = Stomp.over(ConnectionProvider.OKHTTP, SOCKET_URL)
        .withServerHeartbeat(30000)

    private var compositeDisposable = CompositeDisposable()
    private var compositeDisposableChats = CompositeDisposable()

    fun logIn(
        credentials: CustomerLogInInfoDto,
        successful: Runnable,
        unsuccessful: Runnable,
        error: Consumer<in Throwable>,
    ) = listenTopicAndSend(
        "$TOPIC_URL_SESSION_ID/login",
        LOGIN_LINK_SOCKET,
        credentials,
        successful,
        unsuccessful,
        error
    )

    fun signUp(
        credentials: CustomerSignUpInfoDto,
        successful: Runnable,
        unsuccessful: Runnable,
        error: Consumer<in Throwable>,
    ) = listenTopicAndSend(
        "$TOPIC_URL_SESSION_ID/signup",
        SIGNUP_LINK_SOCKET,
        credentials,
        successful,
        unsuccessful,
        error
    )

    fun getDialogs(
        user: UserDto,
        successful: Consumer<List<DialogDto>>,
        unsuccessful: Runnable,
        error: Consumer<in Throwable>,
    ) {
        initConnection()
        topicListenerDialogDto("${getTopicUrlPersonalToken()}/getDialogs", successful, unsuccessful, error)
        webSocketSend(getDialogsLinkSocket(), user)
    }

    fun getOldMessages(
        users: Pair<String, String>,
        successful: Consumer<List<MessageDto>>,
        unsuccessful: Runnable,
        error: Consumer<in Throwable>,
    ) {
        initConnection()
        topicListenerMessageDto("${getTopicUrlPersonalToken()}/getMessages", successful, unsuccessful, error)
        webSocketSend(getMessagesLinkSocket(), users)
    }

    fun sendMessage(
        dto: MessageDto,
        successful: Runnable,
        unsuccessful: Runnable,
        error: Consumer<in Throwable>,
    ) {
        initConnection()
        topicListenerResponseDto("${getTopicUrlPersonalToken()}/sendMessage", successful, unsuccessful, error)
        webSocketSend(sendMessageLinkSocket(), dto)
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
                    currentUserToken = message.personalToken ?: throw RuntimeException("Personal token not defined")
                } else {
                    unsuccessful.run()
                }
            }, error::accept)

        resetTemporarySubscriptions()
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
                    Log.d(
                        ContentValues.TAG,
                        "Get response from topic [$topicName]. Receive list with [${dialogsList.size}] dialogs"
                    )
                    successful.accept(dialogsList)
                } catch (e: Exception) {
                    Log.e(ContentValues.TAG, "Get response from topic [$topicName] with exception:", e)
                    unsuccessful.run()
                }
            }, error::accept)

        resetTemporarySubscriptions()
        compositeDisposable.add(topicSubscribe)

        val subscribeToReceiveMessages = stompClient.topic(getTopicUrlPersonalToken() + "/getNewDialog")
            .subscribeOn(Schedulers.io(), false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                try {
                    val dialogDto = mapper.readValue<DialogDto>(it.payload)
                    Log.d(ContentValues.TAG, "Receive new dialog with [${dialogDto.getPartner(getCurrentUsername())}]")
                    successful.accept(listOf(dialogDto))
                } catch (e: Exception) {
                    Log.e(ContentValues.TAG, "Get response from topic [$topicName] with exception:", e)
                    unsuccessful.run()
                }
            }, error::accept)

        resetChatSubscriptions()
        compositeDisposableChats.add(subscribeToReceiveMessages)
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
                    Log.d(
                        ContentValues.TAG,
                        "Get response from topic [$topicName]. Receive list with [${messagesList.size}] dialogs"
                    )
                    successful.accept(messagesList)
                } catch (e: Exception) {
                    Log.e(ContentValues.TAG, "Get response from topic [$topicName] with exception:", e)
                    unsuccessful.run()
                }
            }, error::accept)

        resetTemporarySubscriptions()
        compositeDisposable.add(topicSubscribe)

        val subscribeToReceiveMessages = stompClient.topic(getTopicUrlPersonalToken() + "/getNewMessage")
            .subscribeOn(Schedulers.io(), false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                try {
                    val messagesDto = mapper.readValue<MessageDto>(it.payload)
                    Log.d(ContentValues.TAG, "Receive new message from [${messagesDto.fromEmailAddress}]")
                    successful.accept(listOf(messagesDto))
                } catch (e: Exception) {
                    Log.e(ContentValues.TAG, "Get response from topic [$topicName] with exception:", e)
                    unsuccessful.run()
                }
            }, error::accept)

        resetChatSubscriptions()
        compositeDisposableChats.add(subscribeToReceiveMessages)
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
        resetTemporarySubscriptions()

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

    private fun resetTemporarySubscriptions() {
        Log.i(TAG, "Reset temporary subscriptions")
        compositeDisposable.dispose()
        compositeDisposable = CompositeDisposable()
    }

    private fun resetChatSubscriptions() {
        Log.i(TAG, "Reset chat subscriptions")
        compositeDisposableChats.dispose()
        compositeDisposableChats = CompositeDisposable()
    }

    init {
        initFlag.set(false)
        initConnection()
    }

    companion object {
        private var currentUserToken: String? = null

        private val initFlag = AtomicBoolean()
        private val instance = WebSocketResolver()
        fun getInstance() = instance

        private val sessionId = SessionContext.CurrentSession.id

        private const val HTTP_PROTOCOL = "http://"
        private const val WS_PROTOCOL = "ws://"
        private const val URL = "37.192.212.41:5000"

        private const val PATH = "/api/v1/chat"
        private const val SOCKET_URL = "$WS_PROTOCOL$URL$PATH/websocket"

        private val LOGIN_LINK_SOCKET = "$PATH/login/?token=$sessionId"
        private val SIGNUP_LINK_SOCKET = "$PATH/signup/?token=$sessionId"

        private val TOPIC_URL_SESSION_ID = "/topic/?token=$sessionId"
    }

    private fun getDialogsLinkSocket() = "$PATH/getDialogs/?token=$currentUserToken"
    private fun getMessagesLinkSocket() = "$PATH/getMessages/?token=$currentUserToken"
    private fun sendMessageLinkSocket() = "$PATH/sendMessage/?token=$currentUserToken"
    private fun getTopicUrlPersonalToken() = "/topic/?token=$currentUserToken"
}