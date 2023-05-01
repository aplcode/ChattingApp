package com.example.myapplication.activity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapters.UserAdapter
import com.example.myapplication.dto.UserDto
import com.example.myapplication.util.factory.ResolverFactory
import com.example.myapplication.util.getCurrentUsername
import com.example.myapplication.util.operation.ListenableFuture

class CreatingChatActivity : AppCompatActivity() {
    private val webSocket = ResolverFactory.instance.getImplResolver()
    private lateinit var fullUserList: List<UserDto>
    private val userList = mutableListOf<UserDto>()
    private lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_user)

        usersViewSettingUp()
        findUsernameBoxSettingUp()

        uploadUsers()
    }

    private fun usersViewSettingUp() {
        userAdapter = UserAdapter(this, userList)

        val usersView: RecyclerView = findViewById(R.id.usersRecyclerView)

        usersView.layoutManager = LinearLayoutManager(this)
        usersView.adapter = userAdapter
    }

    private fun findUsernameBoxSettingUp() {
        val findUsernameBox: EditText = findViewById(R.id.startEnterUsername)
        findUsernameBox.doOnTextChanged { text, _, _, _ ->
            change(text!!)
        }
    }

    private fun uploadUsers() {
        val selectUserTextView: TextView = findViewById(R.id.activity_select_user_textBar)

        webSocket.getUsers(
            object : ListenableFuture<List<UserDto>> {
                override fun onSuccessful(result: List<UserDto>) {
                    Log.i(ContentValues.TAG, "Users list size: [${result.size}]")

                    if (result.isEmpty()) {
                        selectUserTextView.text = "Нет других пользователей!"
                        selectUserTextView.visibility = View.VISIBLE
                    } else {
                        fullUserList = result
                        userList.addAll(fullUserList.filter { user -> user.username != getCurrentUsername() })
                    }

                    userAdapter.notifyItemRangeInserted(userList.size, fullUserList.size)

                    Log.i(ContentValues.TAG, "Users are got")
                }
            }
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun change(text: CharSequence) {
        if (!this::userAdapter.isInitialized) return

        restoreUserList()

        val filteredList = fullUserList.filter { it.username.lowercase().contains(text.toString().lowercase()) }
        userList.removeIf { !filteredList.contains(it) }

        userAdapter.notifyDataSetChanged()
    }

    private fun restoreUserList() {
        userList.clear()
        userList.addAll(fullUserList.filter { user -> user.username != getCurrentUsername() })
    }
}