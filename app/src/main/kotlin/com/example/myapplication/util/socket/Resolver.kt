package com.example.myapplication.util.socket

import com.example.myapplication.dto.CustomerLogInInfoDto
import com.example.myapplication.dto.CustomerSignUpInfoDto
import com.example.myapplication.dto.DialogDto
import com.example.myapplication.dto.MessageDto
import com.example.myapplication.dto.ResponseDto
import com.example.myapplication.dto.UserDto
import com.example.myapplication.util.operation.ListenableFuture

interface Resolver {
    fun logIn(credentials: CustomerLogInInfoDto, listener: ListenableFuture<ResponseDto>)

    fun signUp(credentials: CustomerSignUpInfoDto, listener: ListenableFuture<ResponseDto>)

    fun getDialogs(user: UserDto, listener: ListenableFuture<List<DialogDto>>)

    fun getUsers(listener: ListenableFuture<List<UserDto>>)

    fun getMessageHistory(users: Pair<String, String>, listener: ListenableFuture<List<MessageDto>>)

    fun sendMessage(dto: MessageDto, listener: ListenableFuture<ResponseDto>)
}