package com.example.myapplication.util.socket

import com.example.myapplication.dto.CustomerLogInInfoDto
import com.example.myapplication.dto.CustomerSignUpInfoDto
import com.example.myapplication.dto.DialogDto
import com.example.myapplication.dto.MessageDto
import com.example.myapplication.dto.ResponseDto
import com.example.myapplication.dto.UserDto
import com.example.myapplication.util.SessionContext
import com.example.myapplication.util.operation.ListenableFuture
import com.example.myapplication.util.operation.TopicOperations

class WebSocketResolver private constructor(): Resolver {
    private val topicOperations = TopicOperations()

    override fun logIn(
        credentials: CustomerLogInInfoDto,
        listener: ListenableFuture<ResponseDto>,
    ) = topicOperations.authorizationListenTopicAndSend(
        "$TOPIC_LINK_SOCKET/login",
        LOGIN_LINK_SOCKET,
        credentials,
        listener
    )

    override fun signUp(
        credentials: CustomerSignUpInfoDto,
        listener: ListenableFuture<ResponseDto>,
    ) = topicOperations.authorizationListenTopicAndSend(
        "$TOPIC_LINK_SOCKET/signup",
        SIGNUP_LINK_SOCKET,
        credentials,
        listener
    )

    override fun getDialogs(
        user: UserDto,
        listener: ListenableFuture<List<DialogDto>>,
    ) {
        topicOperations.topicListenerDialogDto("getDialogs", "getNewDialog", listener)
        topicOperations.webSocketSendPersonalToken(getDialogsLinkSocket, user)
    }
    override fun getUsers(
        listener: ListenableFuture<List<UserDto>>,

        ) {
        topicOperations.topicListenerUserDto("getUsers", listener)
        topicOperations.webSocketSendPersonalToken(getUsersLinkSocket, null)
    }



    override fun getMessageHistory(
        users: Pair<String, String>,
        listener: ListenableFuture<List<MessageDto>>,
    ) {
        topicOperations.topicListenerMessageDto("getMessages", "getNewMessage", listener)
        topicOperations.webSocketSendPersonalToken(getMessagesLinkSocket, users)
    }

    override fun sendMessage(
        dto: MessageDto,
        listener: ListenableFuture<ResponseDto>,
    ) {
        topicOperations.topicListenerResponseDto("sendMessage", listener)
        topicOperations.webSocketSendPersonalToken(sendMessageLinkSocket, dto)
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