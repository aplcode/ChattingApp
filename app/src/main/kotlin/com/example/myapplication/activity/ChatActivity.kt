package com.example.myapplication.activity

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapters.MessageAdapter
import com.example.myapplication.dto.MessageDto
import com.example.myapplication.util.WebSocketResolver
import com.example.myapplication.util.getCurrentUsername
import java.time.LocalDateTime
import kotlinx.android.synthetic.main.activity_chat.activityChat_chatRecyclerView
import kotlinx.android.synthetic.main.activity_chat.activityChat_messageBox
import kotlinx.android.synthetic.main.activity_chat.activityChat_sendButton

open class ChatActivity : AppCompatActivity() {
    private val webSocket = WebSocketResolver.getInstance()
    private val messageList = mutableListOf<MessageDto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val partnerUsername = getPartnerUsernameFromContext()
        val username = getCurrentUsername()

        supportActionBar?.title = partnerUsername

        val messageAdapter = MessageAdapter(this, messageList)

        activityChat_chatRecyclerView.layoutManager = LinearLayoutManager(this)
        activityChat_chatRecyclerView.adapter = messageAdapter

        webSocket.getOldMessages(username to partnerUsername,
            object: WebSocketResolver.ListenableFuture<List<MessageDto>> {
                override fun onSuccessful(result: List<MessageDto>) {
                    Log.i(ContentValues.TAG, "Old messages is got")

                    if (result.isEmpty()) {
                        Log.i(ContentValues.TAG, "Empty message history")
                    } else {
                        Log.i(ContentValues.TAG, "Message history size: [${result.size}]")
                        messageList.addAll(result)
                    }

                    messageAdapter.notifyItemRangeInserted(messageList.lastIndex, result.size)
                }
            })

        activityChat_sendButton.setOnClickListener {
            val message = activityChat_messageBox.text.toString()
            val messageDto = MessageDto(username, partnerUsername, message, LocalDateTime.now().toString())

            webSocket.sendMessage(messageDto,
            object: WebSocketResolver.ListenableFuture<Nothing?> {
                override fun onSuccessful(result: Nothing?) {
                    Log.i(ContentValues.TAG, "Message sent successfully")
                    messageList.add(messageDto)
                    messageAdapter.notifyItemInserted(messageList.lastIndex)
                }
            })

            activityChat_messageBox.setText("")
        }
    }

    private fun getPartnerUsernameFromContext() =
        intent.getStringExtra("partnerUsername") ?: throw RuntimeException("Exception in context")
}