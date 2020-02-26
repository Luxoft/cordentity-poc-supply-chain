package com.luxoft.supplychain.sovrinagentapp.data

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import java.net.URL

private val TAG = VerifierBackendState::class.simpleName

class VerifierBackendState(
        private val backendUrl: URL)
{
    private val client = HttpClient(Android)

    fun resetDemo() {
        Log.i(TAG, "Started resetting demo")
        runBlocking {
            val url: URL = backendUrl.toURI().resolve("demo/reset").toURL()
            Log.i(TAG, "Use reset url: $url")

            val response = client.post<Any?>(url){}

            Log.i(TAG, "backend response: `$response`")
        }

    }
}