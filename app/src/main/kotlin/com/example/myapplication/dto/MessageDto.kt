package com.example.myapplication.dto

import java.time.ZonedDateTime

data class MessageDto(
    val id: String? = null, // TODO
    val fromEmailAddress: String,
    val toEmailAddress: String,
    val text: String,
    val timestamp: ZonedDateTime,
    var status: MessageStatus? = null,
) {
    enum class MessageStatus {
        SENDING,
        UNREAD,
        READ,
    }
}