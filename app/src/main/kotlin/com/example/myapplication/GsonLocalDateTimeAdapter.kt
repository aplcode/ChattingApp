package com.example.myapplication

import com.google.gson.*
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// this class can't be static
class GsonLocalDateTimeAdapter : JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    @Synchronized
    override fun serialize(
        date: LocalDateTime,
        type: Type,
        jsonSerializationContext: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(date.format(DateTimeFormatter.ISO_DATE_TIME))

    }

    @Synchronized
    override fun deserialize(
        jsonElement: JsonElement,
        type: Type,
        jsonDeserializationContext: JsonDeserializationContext
    ): LocalDateTime {
        return LocalDateTime.parse(jsonElement.asString)
    }
}