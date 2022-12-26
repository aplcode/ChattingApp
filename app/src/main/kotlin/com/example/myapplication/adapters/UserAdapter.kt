package com.example.myapplication.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.activity.ChatActivity
import com.example.myapplication.dto.UserDto

class UserAdapter (private val context: Context, private val userList: List<UserDto>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.txt_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        UserViewHolder(LayoutInflater.from(context).inflate(R.layout.user_layout, parent, false))


    override fun getItemCount() = userList.size


    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.textName.text = userList[position].username
        holder.itemView.setOnClickListener {
            context.startActivity(Intent(context, ChatActivity::class.java).apply {
                putExtra("partnerUsername", userList[position].username)
            })
        }
    }
}