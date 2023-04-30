package com.example.myapplication.util

import java.util.*

enum class SessionContext(val id: String) {
    CurrentSession(UUID.randomUUID().toString()),
    CurrentApplicationVersion("0.0.1"),
}