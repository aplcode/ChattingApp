package com.example.myapplication.util.socket

import com.example.myapplication.dto.*
import com.example.myapplication.util.SessionContext
import com.example.myapplication.util.operation.ListenableFuture
import com.example.myapplication.util.operation.TopicOperations
import com.example.myapplication.util.operation.WebSocketOperations

class WebSocketResolver private constructor(): Resolver {
    private val webSocketOperations: WebSocketOperations = TopicOperations()

    override fun logIn(
        credentials: CustomerLogInInfoDto,
        listener: ListenableFuture<ResponseDto>,
    ) = webSocketOperations.authorizationListenTopicAndSend(
        "$TOPIC_LINK_SOCKET/login",
        LOGIN_LINK_SOCKET,
        credentials,
        listener
    )

    override fun signUp(
        credentials: CustomerSignUpInfoDto,
        listener: ListenableFuture<ResponseDto>,
    ) = webSocketOperations.authorizationListenTopicAndSend(
        "$TOPIC_LINK_SOCKET/signup",
        SIGNUP_LINK_SOCKET,
        credentials,
        listener
    )

    override fun getDialogs(
        user: UserDto,
        listener: ListenableFuture<List<DialogDto>>,
    ) {
        webSocketOperations.topicListenerDialogDto("getDialogs", "getNewDialog", listener)
        webSocketOperations.webSocketSendPersonalToken(getDialogsLinkSocket, user)
    }
    override fun getUsers(
        listener: ListenableFuture<List<UserDto>>,

        ) {
        webSocketOperations.topicListenerUserDto("getUsers", listener)
        webSocketOperations.webSocketSendPersonalToken(getUsersLinkSocket, null)
    }

    override fun getMessageHistory(
        users: Pair<String, String>,
        listener: ListenableFuture<List<MessageDto>>,
    ) {
        webSocketOperations.topicListenerMessageDto("getMessages", "getNewMessage", listener)
        webSocketOperations.webSocketSendPersonalToken(getMessagesLinkSocket, users)
    }

    override fun sendMessage(
        dto: MessageDto,
        listener: ListenableFuture<ResponseDto>,
    ) {
        webSocketOperations.topicListenerResponseDto("sendMessage", listener)
        webSocketOperations.webSocketSendPersonalToken(sendMessageLinkSocket, dto)
    }

    companion object {
        private val instance = WebSocketResolver()
        fun getInstance() = instance

        private val sessionId = SessionContext.CurrentSession.id

        private const val PATH = "/api/v1/chat"

        private val LOGIN_LINK_SOCKET = "$PATH/login/?token=$sessionId"
        private val SIGNUP_LINK_SOCKET = "$PATH/signup/?token=$sessionId"
        private val TOPIC_LINK_SOCKET = "/topic/?token=$sessionId"

        private const val getDialogsLinkSocket = "$PATH/getDialogs/?token="
        private const val getMessagesLinkSocket = "$PATH/getMessages/?token="
        private const val sendMessageLinkSocket = "$PATH/sendMessage/?token="
        private const val getUsersLinkSocket = "$PATH/getUsers/?token="
    }
}