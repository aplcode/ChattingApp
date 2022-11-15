package com.example.myapplication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.work.Logger
import com.example.myapplication.dto.CustomerLogInInfoDto
import com.example.myapplication.dto.CustomerSignUpInfoDto
import com.example.myapplication.dto.ResponseDto
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompMessage
import ua.naiksoftware.stomp.provider.OkHttpConnectionProvider.TAG
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class WebSocketResolver private constructor() : ViewModel() {
    private val gson: Gson = GsonBuilder().registerTypeAdapter(
        LocalDateTime::class.java,
        GsonLocalDateTimeAdapter()
    ).create()

    private val mStompClient: StompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, SOCKET_URL)
        .withServerHeartbeat(30000)

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    private fun initConnection() {
        if (initFlag.get()) {
            return
        }

        initFlag.set(true)
        resetSubscriptions()

        val lifecycleSubscribe = mStompClient.lifecycle()
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
                        Log.d(TAG, "Stomp connection closed")
                    }
                }
            }

        compositeDisposable.add(lifecycleSubscribe)

        if (!mStompClient.isConnected) {
            mStompClient.connect()
        }
    }

    private fun resetSubscriptions() {
        compositeDisposable.dispose()
        compositeDisposable = CompositeDisposable()
    }

    fun sendMessage(text: String) {
        initConnection()
        val chatSocketMessage = ChatSocketMessage(text = text, author = "Me", datetime = LocalDateTime.now())
        sendCompletable(mStompClient.send(CHAT_LINK_SOCKET, gson.toJson(chatSocketMessage)))
    }

    fun login(email: String, password: String, success: Consumer<in StompMessage>, error: Consumer<in Throwable>) {
        initConnection()
        val credentials = CustomerLogInInfoDto(login = email, password = password)
        topicListener("/topic/login", success, error)

        webSocketSend(LOGIN_LINK_SOCKET, credentials)
    }

    private fun webSocketSend(url: String, data: Any) =
        sendCompletable(mStompClient.send(url, gson.toJson(data)))

    private fun topicListenerResponseDto(topicName: String) =
        topicListener(topicName, {
            Log.d(TAG, it.payload)
            val message = gson.fromJson(it.payload, ResponseDto::class.java)
            if (message.status == 0) {
                authFlag.set(1)
            } else {
                authFlag.set(-1)
            }
            logger.info("", Thread.currentThread().toString())
        }, { Log.e(TAG, "Error!", it) })

    private fun topicListener(topicName: String, onNext: Consumer<in StompMessage>, onError: Consumer<in Throwable>) {
        val topicSubscribe = mStompClient.topic(topicName)
            .subscribeOn(Schedulers.io(), false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onNext, onError)

        compositeDisposable.add(topicSubscribe)
    }

    fun signup(firstname: String, lastname: String, email: String, password: String) {
        initConnection()
        val credentials =
            CustomerSignUpInfoDto(firstname = firstname, lastname = lastname, emailAddress = email, password = password)
        topicListenerResponseDto("/topic/signup")

        webSocketSend(SIGNUP_LINK_SOCKET, credentials)
        logger.info("", Thread.currentThread().toString())
    }

    private fun sendCompletable(request: Completable) {
        compositeDisposable.add(
            request.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { Log.d(TAG, "Stomp sended") },
                    { Log.e(TAG, "Stomp error", it) }
                )
        )
    }

    @Suppress("unused")
    fun healthCheck() {
        val url = URL("http://37.192.212.41:5000/actuator/health")

        with(url.openConnection() as HttpURLConnection) {
            println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")
            inputStream.bufferedReader().use {
                it.lines().forEach(System.out::println)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        mStompClient.disconnect()
        compositeDisposable.dispose()
    }

    init {
        initFlag.set(false)
        authFlag.set(0)

        initConnection()
    }

    companion object {
        private val initFlag = AtomicBoolean()
        private val authFlag = AtomicInteger()
        private val instance = WebSocketResolver()

        private const val URL = "ws://37.192.212.41:5000"
        private const val PATH = "/api/v1/chat"
        private const val SOCKET_URL = "$URL$PATH/websocket"
        private const val CHAT_LINK_SOCKET = "$PATH/sock"
        private const val LOGIN_LINK_SOCKET = "$PATH/login"
        private const val SIGNUP_LINK_SOCKET = "$PATH/signup"

        private val logger = Logger.LogcatLogger(Log.DEBUG)

        fun getInstance() = instance

        fun getAuthFlag() = authFlag
    }
}
