package com.example.myapplication.util.operation

import android.content.ContentValues
import android.util.Log

interface ListenableFuture<T> {
    fun onSuccessful(result: T)

    fun onUnsuccessful() {
        Log.e(ContentValues.TAG, "Unsuccessful ${this.javaClass.name}")
    }

    fun onException(exception: Throwable) {
        Log.e(ContentValues.TAG, "Exception in ${this.javaClass.name}", exception)
    }
}