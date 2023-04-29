package com.example.myapplication.util

import com.example.myapplication.activity.authorization.Authorization
import com.example.myapplication.dto.DialogDto
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun getCurrentUsername() =
    Authorization.getUsernameIsInit()
    ?: throw RuntimeException("Username is not defined")

fun DialogDto.getPartner(currentUsername: String) =
    if (firstUser == currentUsername) secondUser else firstUser

fun getTime() = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")).toString()