package com.example.myapplication.util.factory

import com.example.myapplication.util.socket.OfflineResolver
import com.example.myapplication.util.socket.Resolver
import com.example.myapplication.util.socket.WebSocketResolver

class ResolverFactory private constructor() {
    private val resolver: Resolver

    init {
        val str = String(this.javaClass.classLoader!!.getResource("application.properties").openStream().readBytes())
        val properties = str.split("=")
        resolver = if (properties[1] == "offline") {
            OfflineResolver()
        } else {
            WebSocketResolver.getInstance()
        }
    }

    fun getImplResolver() = resolver

    companion object {
        val instance = ResolverFactory()
    }
}