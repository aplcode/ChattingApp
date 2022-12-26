package com.example.myapplication.util

import com.example.myapplication.activity.authorization.Authorization
import com.example.myapplication.dto.DialogDto

fun getCurrentUsername() =
    Authorization.getUsernameIsInit()
    ?: throw RuntimeException("Username is not defined")

fun DialogDto.getPartner(currentUsername: String) =
    if (firstUser == currentUsername) secondUser else firstUser