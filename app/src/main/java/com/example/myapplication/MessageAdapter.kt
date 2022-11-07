package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(private val context: Context, private val messageList: ArrayList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ItemState {
        ITEM_RECEIVE,
        ITEM_SENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            ReceiveViewHolder(LayoutInflater.from(context).inflate(R.layout.receive, parent, false))
        } else {
            SentViewHolder(LayoutInflater.from(context).inflate(R.layout.sent, parent, false))
        }
    }

    override fun getItemCount() = messageList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        when (holder) {
            is SentViewHolder -> {
                holder.sentMessage.text = currentMessage.message
            }

            is ReceiveViewHolder -> {
                holder.receiveMessage.text = currentMessage.message
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if (FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId)) {
            ItemState.ITEM_SENT.ordinal
        } else {
            ItemState.ITEM_RECEIVE.ordinal
        }
    }
    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentMessage: TextView = itemView.findViewById(R.id.txt_sent_message)
    }
    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveMessage: TextView = itemView.findViewById(R.id.txt_receive_message)
    }
}