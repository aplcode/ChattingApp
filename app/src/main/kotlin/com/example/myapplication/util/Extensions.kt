package com.example.myapplication.util

import com.example.myapplication.activity.authorization.Authorization
import com.example.myapplication.dto.DialogDto
import java.time.ZonedDateTime

@Throws(RuntimeException::class)
fun getCurrentUsername() =
    Authorization.getUsernameIsInit()
        ?: throw RuntimeException("Username is not defined")

@Throws(RuntimeException::class)
fun getLoginCache() =
    Authorization.getLoginCacheIsInit()
        ?: throw RuntimeException("LoginCache is not defined")

fun DialogDto.getPartner(currentUsername: String) =
    if (firstUser == currentUsername) secondUser else firstUser

fun getTime(): ZonedDateTime = ZonedDateTime.now()