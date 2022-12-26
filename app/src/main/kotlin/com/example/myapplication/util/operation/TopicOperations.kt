package com.example.myapplication.util.operation

import android.content.ContentValues
import android.util.Log
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
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.provider.OkHttpConnectionProvider

class TopicOperations {
    private val stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, SOCKET_URL)
        .withServerHeartbeat(30000)
    private val mapper = jacksonObjectMapper()
    private var compositeDisposable = CompositeDisposable()
    private var compositeDisposableChats = CompositeDisposable()

    init {
        initConnection()
    }

    fun authorizationListenTopicAndSend(
        listenFrom: String, sendTo: String, data: Any,
        listener: ListenableFuture<ResponseDto>,
    ) {
        initConnection()
        authorizationTopicListener(listenFrom, listener)
        webSocketSend(sendTo, data)
    }

    private fun authorizationTopicListener(
        topicName: String,
        listener: ListenableFuture<ResponseDto>,
    ) {
        resetTemporarySubscriptions()
        compositeDisposable.add(authSubscribe(topicName, listener))
    }

    fun topicListenerResponseDto(
        personalTopicPath: String,
        listener: ListenableFuture<ResponseDto>,
    ) {
        initConnection()
        resetTemporarySubscriptions()
        compositeDisposable.add(subscribe(getTopicUrlPersonalToken(personalTopicPath), listener))
    }

    fun topicListenerDialogDto(
        temporaryTopicPath: String,
        permanentlyTopicPath: String,
        listener: ListenableFuture<List<DialogDto>>,
    ) {
        initConnection()

        resetTemporarySubscriptions()
        compositeDisposable.add(subscribe(getTopicUrlPersonalToken(temporaryTopicPath), listener))

        resetChatSubscriptions()
        compositeDisposableChats.add(subscribe(getTopicUrlPersonalToken(permanentlyTopicPath), listener))
    }

    fun topicListenerMessageDto(
        temporaryTopicPath: String,
        permanentlyTopicPath: String,
        listener: ListenableFuture<List<MessageDto>>,
    ) {
        initConnection()

        resetTemporarySubscriptions()
        compositeDisposable.add(subscribe(getTopicUrlPersonalToken(temporaryTopicPath), listener))

        resetChatSubscriptions()
        compositeDisposableChats.add(subscribe(getTopicUrlPersonalToken(permanentlyTopicPath), listener))
    }

    private inline fun <reified T> subscribe(topicName: String, listener: ListenableFuture<T>) =
        stompClient.topic(topicName)
            .subscribeOn(Schedulers.io(), false)
            .observeOn(AndroidSchedulers.mainThread())
            .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .subscribe({
                try {
                    Log.d(ContentValues.TAG, "Receive message from $topicName : [${it.payload}]")
                    Log.d(ContentValues.TAG, "Serialize to ${T::class.java.name}")
                    listener.onSuccessful(mapper.readValue(it.payload))
                } catch (e: Exception) {
                    Log.e(ContentValues.TAG, "Get response from [$topicName] with exception :", e)
                    listener.onUnsuccessful()
                }
            }, listener::onException)

    private fun authSubscribe(topicName: String, listener: ListenableFuture<ResponseDto>) =
        stompClient.topic(topicName)
            .subscribeOn(Schedulers.io(), false)
            .observeOn(AndroidSchedulers.mainThread())
            .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .subscribe({
                try {
                    Log.d(ContentValues.TAG, "Receive message from $topicName : [${it.payload}]")
                    Log.d(ContentValues.TAG, "Serialize to ${ResponseDto::class.java.name}")
                    val responseDto = mapper.readValue<ResponseDto>(it.payload)

                    if (responseDto.status == 0) {
                        currentUserToken = responseDto.personalToken
                        listener.onSuccessful(responseDto)
                    } else {
                        listener.onUnsuccessful()
                    }
                } catch (e: Exception) {
                    Log.e(ContentValues.TAG, "Get response from [$topicName] with exception :", e)
                    listener.onUnsuccessful()
                }
            }, listener::onException)


    fun topicListenerUserDto(
        temporaryTopicPath: String,
        listener: ListenableFuture<List<UserDto>>,
    ) {
        initConnection()

        resetTemporarySubscriptions()
        compositeDisposable.add(subscribe(getTopicUrlPersonalToken(temporaryTopicPath), listener))
    }

    private fun webSocketSend(url: String, data: Any) =
        sendCompletable(stompClient.send(url, mapper.writeValueAsString(data)))

    fun webSocketSendPersonalToken(url: String, data: Any?) =
        sendCompletable(stompClient.send(getWebSocketUrlPersonalToken(url), mapper.writeValueAsString(data)))

    private fun sendCompletable(request: Completable) {
        val callback = request.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { Log.d(OkHttpConnectionProvider.TAG, "Stomp sent") },
                { Log.e(OkHttpConnectionProvider.TAG, "Stomp error", it) }
            )

        compositeDisposable.add(callback)
    }

    private fun initConnection() {
        if (isStompConnected.get()) {
            return
        }

        isStompConnected.set(true)
        resetTemporarySubscriptions()

        val lifecycleSubscribe = stompClient.lifecycle()
            .subscribeOn(Schedulers.io(), false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                when (it.type ?: LifecycleEvent.Type.CLOSED) {
                    LifecycleEvent.Type.OPENED -> Log.d(OkHttpConnectionProvider.TAG, "Stomp connection opened")
                    LifecycleEvent.Type.ERROR -> Log.e(OkHttpConnectionProvider.TAG, "Error", it.exception)
                    LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT,
                    LifecycleEvent.Type.CLOSED,
                    -> {
                        isStompConnected.set(false)
                        Log.i(OkHttpConnectionProvider.TAG, "Stomp connection closed")
                    }
                }
            }

        compositeDisposable.add(lifecycleSubscribe)

        if (!stompClient.isConnected) {
            stompClient.connect()
        }
    }

    private fun resetTemporarySubscriptions() {
        Log.i(OkHttpConnectionProvider.TAG, "Reset temporary subscriptions")
        compositeDisposable.dispose()
        compositeDisposable = CompositeDisposable()
    }

    private fun resetChatSubscriptions() {
        Log.i(OkHttpConnectionProvider.TAG, "Reset chat subscriptions")
        compositeDisposableChats.dispose()
        compositeDisposableChats = CompositeDisposable()
    }

    private fun getTopicUrlPersonalToken(path: String) = "/topic/?token=$currentUserToken/$path"

    private fun getWebSocketUrlPersonalToken(url: String) = url + currentUserToken

    companion object {
        private var currentUserToken: String? = null

        private val isStompConnected = AtomicBoolean(false)

        private const val WS_PROTOCOL = "ws://"
        private const val URL = "37.192.212.41:5000"

        private const val PATH = "/api/v1/chat"
        private const val SOCKET_URL = "$WS_PROTOCOL$URL$PATH/websocket"

        private const val TIMEOUT_SECONDS = 5L
    }
}