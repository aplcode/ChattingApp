package com.example.myapplication.util

import java.util.UUID

enum class SessionContext(val id: String) {
    CurrentSession(UUID.randomUUID().toString())
}