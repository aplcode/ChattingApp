package com.example.myapplication.dto

data class MessageDto(
    val fromEmailAddress: String,
    val toEmailAddress: String,
    val text: String,
    val timestamp: String,
)