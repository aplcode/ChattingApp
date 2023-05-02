package com.example.myapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.dto.cache.LoginCache
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class LastLoginAdapter(private val context: Context, private val logins: LoginCache) :
    RecyclerView.Adapter<LastLoginAdapter.LoginHistoryDto>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LoginHistoryDto(LayoutInflater.from(context).inflate(R.layout.last_login_layout, parent, false))

    override fun getItemCount() = logins.list.size

    override fun onBindViewHolder(holder: LoginHistoryDto, position: Int) {
        val element = logins.list[position]

        holder.time.text = element.getTimeString()
        holder.device.text = element.getDeviceName()
        holder.version.text = element.applicationInformation.applicationVersion
    }

    class LoginHistoryDto(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val time: TextView = itemView.findViewById(R.id.timeText)
        val device: TextView = itemView.findViewById(R.id.deviceText)
        val version: TextView = itemView.findViewById(R.id.versionText)
    }

    private fun LoginCache.LoginInformation.getTimeString() =
        this.location.zonedDateTime.withZoneSameLocal(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-DD HH:mm:ss"))

    private fun LoginCache.LoginInformation.getDeviceName() =
        "Phone: ${deviceInformation.brand} ${deviceInformation.model}"
}