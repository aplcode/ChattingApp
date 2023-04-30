package com.example.myapplication.util.operation

import com.example.myapplication.dto.DialogDto
import com.example.myapplication.dto.MessageDto
import com.example.myapplication.dto.ResponseDto
import com.example.myapplication.dto.UserDto

interface WebSocketOperations {
    fun authorizationListenTopicAndSend(
        listenFrom: String, sendTo: String, data: Any,
        listener: ListenableFuture<ResponseDto>,
    )

    fun topicListenerResponseDto(personalTopicPath: String, listener: ListenableFuture<ResponseDto>)
    fun topicListenerDialogDto(
        temporaryTopicPath: String,
        permanentlyTopicPath: String,
        listener: ListenableFuture<List<DialogDto>>
    )

    fun topicListenerMessageDto(
        temporaryTopicPath: String,
        permanentlyTopicPath: String,
        listener: ListenableFuture<List<MessageDto>>
    )

    fun topicListenerUserDto(temporaryTopicPath: String, listener: ListenableFuture<List<UserDto>>)
    fun webSocketSendPersonalToken(url: String, data: Any?)
}