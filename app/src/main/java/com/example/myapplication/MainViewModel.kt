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

class MainViewModel private constructor() : ViewModel() {
    companion object {
        private val initFlag = AtomicBoolean()
        val authFlag = AtomicInteger()
        private val instance = MainViewModel()
        //указываем endpoint, на который регистрировали сокет, не забываем добавить /websocket
        const val SOCKET_URL = "ws://37.192.212.41:5000/api/v1/chat/websocket"
        const val CHAT_TOPIC = "/topic/chat"
        const val CHAT_LINK_SOCKET = "/api/v1/chat/sock"
        const val LOGIN_LINK_SOCKET = "/api/v1/chat/login"
        const val SIGNUP_LINK_SOCKET = "/api/v1/chat/signup"

        private val logger = Logger.LogcatLogger(Log.DEBUG)


        fun getInstance() = instance

    }



    /*
    	инициализируем Gson для сериализации/десериализации
    	и регистрируем дополнительный TypeAdapter для LocalDateTime
    */
    private val gson: Gson = GsonBuilder().registerTypeAdapter(
        LocalDateTime::class.java,
        GsonLocalDateTimeAdapter()
    ).create()
    private var mStompClient: StompClient? = null
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()


    private fun initConnection() {
        if (initFlag.get()){
            return
        }
        initFlag.set(true)
        resetSubscriptions()
        if (mStompClient != null) {
            val topicSubscribe = mStompClient!!.topic(CHAT_TOPIC)
                .subscribeOn(Schedulers.io(), false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ topicMessage: StompMessage ->
                    Log.d(TAG, topicMessage.payload)
                    val message: ChatSocketMessage =
                        gson.fromJson(topicMessage.payload, ChatSocketMessage::class.java)
                },
                    {
                        Log.e(TAG, "Error!", it)
                    }
                )

            val lifecycleSubscribe = mStompClient!!.lifecycle()
                .subscribeOn(Schedulers.io(), false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { lifecycleEvent: LifecycleEvent ->
                    when (lifecycleEvent.type!!) {
                        LifecycleEvent.Type.OPENED -> Log.d(TAG, "Stomp connection opened")
                        LifecycleEvent.Type.ERROR -> Log.e(TAG, "Error", lifecycleEvent.exception)
                        LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT,
                        LifecycleEvent.Type.CLOSED -> {
                            initFlag.set(false)
                            Log.d(TAG, "Stomp connection closed")
                        }
                    }
                }

            compositeDisposable.add(lifecycleSubscribe)
            compositeDisposable.add(topicSubscribe)

            if (!mStompClient!!.isConnected) {
                mStompClient!!.connect()
            }


        } else {
            Log.e(TAG, "mStompClient is null!")
        }
    }

    private fun resetSubscriptions() {
        compositeDisposable.dispose()

        compositeDisposable = CompositeDisposable()
    }

    /*
    отправляем сообщение в общий чат
    */
    fun sendMessage(text: String) {
        initConnection()
        val chatSocketMessage = ChatSocketMessage(text = text, author = "Me", datetime = LocalDateTime.now())
        sendCompletable(mStompClient!!.send(CHAT_LINK_SOCKET, gson.toJson(chatSocketMessage)))

    }

    fun login(email: String, password: String){
        initConnection()
        val credentials = CustomerLogInInfoDto(login = email, password = password)
        val topicSubscribe = mStompClient!!.topic("/topic/login")
            .subscribeOn(Schedulers.io(), false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ topicMessage: StompMessage ->
                Log.d(TAG, topicMessage.payload)
                val message =
                    gson.fromJson(topicMessage.payload, ResponseDto::class.java)
                if (message.status == 0){
                    authFlag.set(1)
                } else {
                    authFlag.set(-1)
                }
                logger.info("", Thread.currentThread().toString())
            },
                {
                    Log.e(TAG, "Error!", it)
                }
            )
        sendCompletable(mStompClient!!.send(LOGIN_LINK_SOCKET, gson.toJson(credentials)))
        logger.info("",Thread.currentThread().toString())

    }

    fun signup(firstname: String, lastname : String, email: String, password: String){
        initConnection()
        val credentials = CustomerSignUpInfoDto(firstname = firstname, lastname = lastname, emailAddress = email, password = password)
        val topicSubscribe = mStompClient!!.topic("/topic/signup")
            .subscribeOn(Schedulers.io(), false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ topicMessage: StompMessage ->
                Log.d(TAG, topicMessage.payload)
                val message =
                    gson.fromJson(topicMessage.payload, ResponseDto::class.java)
                if (message.status == 0){
                    authFlag.set(1)
                } else {
                    authFlag.set(-1)
                }
                logger.info("", Thread.currentThread().toString())
            },
                {
                    Log.e(TAG, "Error!", it)
                }
            )
        sendCompletable(mStompClient!!.send(SIGNUP_LINK_SOCKET, gson.toJson(credentials)))
        logger.info("",Thread.currentThread().toString())

    }

    private fun sendCompletable(request: Completable) {
        compositeDisposable.add(
            request.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        Log.d(TAG, "Stomp sended")
                    },
                    {
                        Log.e(TAG, "Stomp error", it)
                    }
                )
        )
    }

    @Suppress("unused")
    fun sendGet() {
        val url = URL("http://37.192.212.41:5000/actuator")

        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "GET"  // optional default is GET

            println("\nSent 'GET' request to URL : $url; Response Code : $responseCode")

            inputStream.bufferedReader().use {
                it.lines().forEach { line ->
                    println(line)
                }
            }
        }
    }


    override fun onCleared() {
        super.onCleared()

        mStompClient?.disconnect()
        compositeDisposable.dispose()
    }

    init {
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, SOCKET_URL)
            .withServerHeartbeat(30000)
        initFlag.set(false)
        initConnection()
        authFlag.set(0)
    }
}
