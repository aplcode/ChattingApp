package com.example.myapplication.util

import com.example.myapplication.authorization.LogIn
import com.example.myapplication.authorization.SignUp
import com.example.myapplication.dto.DialogDto

fun getCurrentUsername() =
    LogIn.getUsernameIsInit() ?: SignUp.getUsernameIsInit()
    ?: throw RuntimeException("Username is not defined")

fun DialogDto.getPartner(currentUsername: String) =
    if (firstUser == currentUsername) secondUser else firstUser