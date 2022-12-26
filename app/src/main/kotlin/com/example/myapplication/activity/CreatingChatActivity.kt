package com.example.myapplication.activity

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapters.UserAdapter
import com.example.myapplication.dto.UserDto
import com.example.myapplication.util.WebSocketResolver
import com.example.myapplication.util.getCurrentUsername
import com.example.myapplication.util.operation.ListenableFuture
import kotlinx.android.synthetic.main.activity_select_user.*


class CreatingChatActivity : AppCompatActivity() {
    private val webSocket = WebSocketResolver.getInstance()
    private val userList = mutableListOf<UserDto>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_user)

        val userAdapter = UserAdapter(this, userList)

        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        usersRecyclerView.adapter = userAdapter

        webSocket.getUsers(
            object : ListenableFuture<List<UserDto>> {
                override fun onSuccessful(result: List<UserDto>) {
                    Log.i(ContentValues.TAG, "Users list size: [${result.size}]")

                    if (result.isEmpty()) {
                        activity_select_user_textBar.text = "Нет других пользователей!"
                        activity_select_user_textBar.visibility = View.VISIBLE
                    } else {
                        userList.addAll(result.filter { user -> user.username != getCurrentUsername() })
                    }

                    userAdapter.notifyItemRangeInserted(userList.size, result.size)
                    Log.i(ContentValues.TAG, "Users are got")

                }
            }
        )
    }
}