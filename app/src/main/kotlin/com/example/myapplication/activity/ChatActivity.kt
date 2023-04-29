package com.example.myapplication.activity

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapters.MessageAdapter
import com.example.myapplication.dto.MessageDto
import com.example.myapplication.dto.ResponseDto
import com.example.myapplication.util.factory.ResolverFactory
import com.example.myapplication.util.getCurrentUsername
import com.example.myapplication.util.getTime
import com.example.myapplication.util.operation.ListenableFuture

//import kotlinx.android.synthetic.main.activity_chat.activityChat_chatRecyclerView
//import kotlinx.android.synthetic.main.activity_chat.activityChat_messageBox
//import kotlinx.android.synthetic.main.activity_chat.activityChat_sendButton

class ChatActivity : AppCompatActivity() {
    private val webSocket = ResolverFactory.instance.getImplResolver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val messageList = mutableListOf<MessageDto>()
        val partnerUsername = getPartnerUsernameFromContext()
        val username = getCurrentUsername()

        supportActionBar?.title = partnerUsername

        val messageAdapter = MessageAdapter(this, messageList)

        val activityChat_chatRecyclerView = findViewById<RecyclerView>(R.id.activityChat_chatRecyclerView)

        activityChat_chatRecyclerView.layoutManager = LinearLayoutManager(this)
        activityChat_chatRecyclerView.adapter = messageAdapter

        webSocket.getMessageHistory(username to partnerUsername,
            object : ListenableFuture<List<MessageDto>> {
                override fun onSuccessful(result: List<MessageDto>) {
                    Log.i(ContentValues.TAG, "Old messages is got")

                    if (result.isEmpty()) {
                        Log.i(ContentValues.TAG, "Empty message history")
                    } else {
                        Log.i(ContentValues.TAG, "Message history size: [${result.size}]")
                        messageList.addAll(result)
                        activityChat_chatRecyclerView.smoothScrollToPosition(messageList.size - 1)
                    }

                    messageAdapter.notifyItemRangeInserted(messageList.lastIndex, result.size)
                }
            })

        val activityChat_sendButton = findViewById<ImageView>(R.id.activityChat_sendButton)
        val activityChat_messageBox = findViewById<EditText>(R.id.activityChat_messageBox)
        activityChat_sendButton.setOnClickListener {
            val message = activityChat_messageBox.text.toString()
            if (message.isBlank()) {
                return@setOnClickListener
            }

            val messageDto = MessageDto(
                username,
                partnerUsername,
                message,
                getTime(),
                MessageDto.MessageStatus.SENDING
            )

            val messagePosition = messageList.size + 1
            messageList.add(messageDto)
            messageAdapter.notifyItemInserted(messagePosition)
            activityChat_chatRecyclerView.smoothScrollToPosition(messageList.size - 1)

            webSocket.sendMessage(messageDto,
                object : ListenableFuture<ResponseDto> {
                    override fun onSuccessful(result: ResponseDto) {
                        Log.i(ContentValues.TAG, "Message sent successfully")
                        if (result.status == 0) {
                            messageDto.status = MessageDto.MessageStatus.UNREAD
                            messageAdapter.notifyItemChanged(messagePosition)
                        } else {
                            onUnsuccessful()
                        }
                    }
                })

            activityChat_messageBox.setText("")
        }
    }

    private fun getPartnerUsernameFromContext() =
        intent.getStringExtra("partnerUsername") ?: throw RuntimeException("Exception in context")
}