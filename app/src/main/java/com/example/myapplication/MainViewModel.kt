package com.example.myapplication

import android.util.Log
import androidx.lifecycle.ViewModel
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
import javax.inject.Inject

class MainViewModel @Inject constructor(
) : ViewModel() {
    companion object {
        //указываем endpoint, на который регистрировали сокет, не забываем добавить /websocket
        const val SOCKET_URL = "ws://37.192.212.41:5000/api/v1/chat/websocket"
        const val CHAT_TOPIC = "/topic/chat"
        const val CHAT_LINK_SOCKET = "/api/v1/chat/sock"

        private val initFlag = AtomicBoolean()
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


    init {
        //инициализация WebSocket клиента
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, SOCKET_URL)
            .withServerHeartbeat(30000)
        initFlag.set(false)
        initChat() //инициализация подписок
    }

    private fun initChat() {
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
        initChat()
        val chatSocketMessage = ChatSocketMessage(text = text, author = "Me", datetime = LocalDateTime.now())
        sendCompletable(mStompClient!!.send(CHAT_LINK_SOCKET, gson.toJson(chatSocketMessage)))

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
}
