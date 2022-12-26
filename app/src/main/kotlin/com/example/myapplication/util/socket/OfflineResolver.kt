package com.example.myapplication.util.socket

import com.example.myapplication.dto.CustomerLogInInfoDto
import com.example.myapplication.dto.CustomerSignUpInfoDto
import com.example.myapplication.dto.DialogDto
import com.example.myapplication.dto.MessageDto
import com.example.myapplication.dto.ResponseDto
import com.example.myapplication.dto.UserDto
import com.example.myapplication.util.getCurrentUsername
import com.example.myapplication.util.operation.ListenableFuture
import java.time.LocalDateTime

class OfflineResolver : Resolver {
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
        val list = List(NUMBER_OF_USERS) { DialogDto(namePool.random(), currentUsername) }

        listener.onSuccessful(list)
    }

    override fun getUsers(listener: ListenableFuture<List<UserDto>>) {
        val list = List(NUMBER_OF_USERS) { UserDto(namePool.random()) }

        listener.onSuccessful(list)
    }

    override fun getMessageHistory(users: Pair<String, String>, listener: ListenableFuture<List<MessageDto>>) {
        val list = mutableListOf<MessageDto>()

        for (i in 1..NUMBER_OF_MESSAGES) {
            if (i % 3 == 0) list.add(
                MessageDto(
                    users.first, users.second, randomString(), LocalDateTime.now().toString()
                )
            )
            else list.add(MessageDto(users.second, users.first, randomString(), LocalDateTime.now().toString()))
        }

        listener.onSuccessful(list)
    }

    override fun sendMessage(dto: MessageDto, listener: ListenableFuture<ResponseDto>) {
        val response = ResponseDto(status = 0)
        listener.onSuccessful(response)
    }

    private fun randomString() = List(MESSAGE_LENGTH) { charPool.random() }.joinToString("")


    companion object {
        private val namePool = listOf(
            "Liam", "Olivia", "Noah", "Emma",
            "Oliver", "Charlotte", "Elijah", "Amelia",
            "James", "Ava", "William", "Sophia",
            "Benjamin", "Isabella", "Lucas", "Mia",
            "Henry", "Evelyn", "Theodore", "Harper",
        )

        private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        private const val MESSAGE_LENGTH = 22
        private const val NUMBER_OF_USERS = 22
        private const val NUMBER_OF_MESSAGES = 22
    }
}