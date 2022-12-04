package com.example.myapplication.dto

data class ResponseDto(
    val status: Int,
    val field: String? = null,
    val personalToken: String? = null,
)