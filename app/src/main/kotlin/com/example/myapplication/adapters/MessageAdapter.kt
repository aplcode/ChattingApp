package com.example.myapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.dto.MessageDto
import com.example.myapplication.dto.MessageDto.MessageStatus.*
import com.example.myapplication.util.getCurrentUsername
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class MessageAdapter(private val context: Context, private val messageList: List<MessageDto>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val currentUsername = getCurrentUsername()

    enum class ItemState {
        ITEM_RECEIVE, ITEM_SENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            SentViewHolder(LayoutInflater.from(context).inflate(R.layout.sent, parent, false))
        } else {
            ReceiveViewHolder(LayoutInflater.from(context).inflate(R.layout.receive, parent, false))
        }
    }

    override fun getItemCount() = messageList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        when (holder) {
            is SentViewHolder -> {
                holder.sentMessage.text = currentMessage.text
                holder.messageTimestamp.text = currentMessage.timestamp.toTimestampString()
                when (currentMessage.status) {
                    UNREAD -> holder.setIndicatorUnread()
                    SENDING -> holder.setIndicatorSending()
                    READ -> holder.setIndicatorRead()
                    else -> holder.setIndicatorSending()
                }
            }

            is ReceiveViewHolder -> {
                holder.receiveMessage.text = currentMessage.text
                holder.messageTimestamp.text = currentMessage.timestamp.toTimestampString()
                if (currentMessage.status == UNREAD) {
                    sendReadCallback()
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if (currentMessage.fromEmailAddress == currentUsername) {
            ItemState.ITEM_SENT.ordinal
        } else {
            ItemState.ITEM_RECEIVE.ordinal
        }
    }

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessage: TextView = itemView.findViewById(R.id.send_txtSentMessageBox)
        val messageTimestamp: TextView = itemView.findViewById(R.id.send_timestamp)
        private val messageStatusIndicatorSending: ImageView = itemView.findViewById(R.id.messageStatusIndicatorSending)
        private val messageStatusIndicatorUnread: ImageView = itemView.findViewById(R.id.messageStatusIndicatorUnread)
        private val messageStatusIndicatorRead: ImageView = itemView.findViewById(R.id.messageStatusIndicatorRead)

        fun setIndicatorSending() {
            messageStatusIndicatorUnread.visibility = View.INVISIBLE
            messageStatusIndicatorRead.visibility = View.INVISIBLE
            messageStatusIndicatorSending.visibility = View.VISIBLE
        }

        fun setIndicatorUnread() {
            messageStatusIndicatorUnread.visibility = View.VISIBLE
            messageStatusIndicatorRead.visibility = View.INVISIBLE
            messageStatusIndicatorSending.visibility = View.INVISIBLE
        }

        fun setIndicatorRead() {
            messageStatusIndicatorUnread.visibility = View.INVISIBLE
            messageStatusIndicatorRead.visibility = View.VISIBLE
            messageStatusIndicatorSending.visibility = View.INVISIBLE
        }
    }

    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveMessage: TextView = itemView.findViewById(R.id.receive_txtReceiveMessageBox)
        val messageTimestamp: TextView = itemView.findViewById(R.id.receive_timestamp)
    }

    private fun sendReadCallback() {
        // TODO
    }

    private fun ZonedDateTime.toTimestampString() = toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")).toString()
}