package com.example.myapplication.activity

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapters.DialogAdapter
import com.example.myapplication.authorization.Authorization
import com.example.myapplication.dto.DialogDto
import com.example.myapplication.dto.UserDto
import com.example.myapplication.util.WebSocketResolver
import com.example.myapplication.util.getCurrentUsername
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val webSocket = WebSocketResolver.getInstance()

    private val userList = mutableListOf<DialogDto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityMain_btnStartNewDialog.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            finish()
            startActivity(intent)
        }

        val adapter = DialogAdapter(this, userList)

        activityMain_userRecyclerView.layoutManager = LinearLayoutManager(this)
        activityMain_userRecyclerView.adapter = adapter

        webSocket.getDialogs(
            UserDto(getCurrentUsername()),
            {
                Log.i(ContentValues.TAG, "Dialogs list size: [${it.size}]")

                if (it.isEmpty()) {
                    activityMain_textBar.text = "В данный момент у вас нет диалогов. Попробуйте начать новый!"
                    activityMain_textBar.visibility = View.VISIBLE
                } else {
                    userList.addAll(it)
                }

                adapter.notifyDataSetChanged()
                Log.i(ContentValues.TAG, "Dialogs is getted")
            }, {
                Log.e(ContentValues.TAG, "unsuccessful ${this.javaClass.name}")
            }, {
                Log.e(ContentValues.TAG, "error ${this.javaClass.name}")
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.logout) {
            finish()
            startActivity(Intent(this, Authorization::class.java))
        }

        return true
    }
}