package com.example.myapplication.util.socket

import com.example.myapplication.dto.*
import com.example.myapplication.util.getCurrentUsername
import com.example.myapplication.util.getTime
import com.example.myapplication.util.operation.ListenableFuture

class OfflineResolver : Resolver {
    private val namePool = listOf(
        "OFFLINE Liam",
        "MODE Olivia",
        "OFFLINE Noah",
        "MODE Emma",
        "OFFLINE Oliver",
        "MODE Charlotte",
        "OFFLINE Elijah",
        "MODE Amelia",
        "OFFLINE James",
        "MODE Ava",
        "OFFLINE William",
        "MODE Sophia",
        "OFFLINE Benjamin",
        "MODE Isabella",
        "OFFLINE Lucas",
        "MODE Mia",
        "OFFLINE Henry",
        "MODE Evelyn",
        "OFFLINE Theodore",
        "MODE Harper",
    )

    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9') + ' '
    private val messageLength = 22
    private val numberOfUsers = 22
    private val numberOfMessages = 22

    override fun logIn(credentials: CustomerLogInInfoDto, listener: ListenableFuture<ResponseDto>) {
        val response = ResponseDto(status = 0)
        listener.onSuccessful(response)
    }

    override fun signUp(credentials: CustomerSignUpInfoDto, listener: ListenableFuture<ResponseDto>) {
        val response = ResponseDto(status = 0)
        listener.onSuccessful(response)
    }

    override fun getDialogs(user: UserDto, listener: ListenableFuture<List<DialogDto>>) {
        val currentUsername = getCurrentUsername()
        val list = List(numberOfUsers) { DialogDto(namePool.random(), currentUsername) }

        listener.onSuccessful(list)
    }

    override fun getUsers(listener: ListenableFuture<List<UserDto>>) {
        val list = List(numberOfUsers) { UserDto(namePool.random()) }

        listener.onSuccessful(list)
    }

    override fun getMessageHistory(users: Pair<String, String>, listener: ListenableFuture<List<MessageDto>>) {
        val list = mutableListOf<MessageDto>()

        for (i in 1..numberOfMessages) {
            if (i % 3 == 0) list.add(
                MessageDto(
                    users.first, users.second, randomString(), getTime(), MessageDto.MessageStatus.READ
                )
            )
            else list.add(MessageDto(users.second, users.first, randomString(), getTime()))
        }

        listener.onSuccessful(list)
    }

    override fun sendMessage(dto: MessageDto, listener: ListenableFuture<ResponseDto>) {
        val response = ResponseDto(status = 0)
        listener.onSuccessful(response)
    }

    private fun randomString() = List(messageLength) { charPool.random() }.joinToString("") + "\n#OFFLINE MODE"
}