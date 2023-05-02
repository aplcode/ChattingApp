package com.example.myapplication.dto.cache

import java.time.ZonedDateTime

data class LoginCache(
    val list: List<LoginInformation>,
) {
    data class LoginInformation(
        val applicationInformation: ApplicationInformation,
        val deviceInformation: DeviceInformation,
        val location: LocationInformation,
    ) {
        data class ApplicationInformation(
            val username: String,
            val sessionId: String,
            val applicationVersion: String,
        )

        data class DeviceInformation(
            val brand: String,
            val model: String,
            val deviceId: String,
        )

        data class LocationInformation(
            val zonedDateTime: ZonedDateTime,
        )
    }

    companion object {
        const val FILE_PATH = "/login/"
        const val FILE_NAME = "loginCache"
    }
}