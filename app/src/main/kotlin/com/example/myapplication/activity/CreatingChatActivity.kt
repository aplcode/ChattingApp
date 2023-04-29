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

//import kotlinx.android.synthetic.main.activity_select_user.activity_select_user_textBar
//import kotlinx.android.synthetic.main.activity_select_user.usersRecyclerView

class CreatingChatActivity : AppCompatActivity() {
    private val webSocket = ResolverFactory.instance.getImplResolver()
    private lateinit var fullUserList: List<UserDto>
    private val userList = mutableListOf<UserDto>()
    private lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_user)

        userAdapter = UserAdapter(this, userList)

        val usersRecyclerView = findViewById<RecyclerView>(R.id.usersRecyclerView)

        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        usersRecyclerView.adapter = userAdapter

        val findUsernameBox: EditText = findViewById(R.id.startEnterUsername)
        findUsernameBox.doOnTextChanged { text, _, _, _ ->
            change(text!!)
        }

        val activity_select_user_textBar = findViewById<TextView>(R.id.activity_select_user_textBar)

        webSocket.getUsers(
            object : ListenableFuture<List<UserDto>> {
                override fun onSuccessful(result: List<UserDto>) {
                    Log.i(ContentValues.TAG, "Users list size: [${result.size}]")

                    if (result.isEmpty()) {
                        activity_select_user_textBar.text = "Нет других пользователей!"
                        activity_select_user_textBar.visibility = View.VISIBLE
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