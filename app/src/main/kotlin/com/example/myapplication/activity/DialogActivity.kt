package com.example.myapplication.activity

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.activity.authorization.Authorization
import com.example.myapplication.adapters.DialogAdapter
import com.example.myapplication.dto.DialogDto
import com.example.myapplication.dto.UserDto
import com.example.myapplication.util.factory.ResolverFactory
import com.example.myapplication.util.getCurrentUsername
import com.example.myapplication.util.operation.ListenableFuture

class DialogActivity : AppCompatActivity() {
    private val webSocket = ResolverFactory.instance.getImplResolver()

    private val userList = mutableListOf<DialogDto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog)

        newDialogButtonSettingUp()

        imButtonSettingUp()

        uploadDialogs()
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

    private fun imButtonSettingUp() {
        val imButton: Button = findViewById(R.id.imButton)
        imButton.text = getCurrentUsername().first().toString()

        imButton.setOnClickListener {
            val intent = Intent(this, ImActivity::class.java)
            startActivity(intent)
        }
    }

    private fun newDialogButtonSettingUp() {
        val startNewDialogButton: Button = findViewById(R.id.activityDialog_btnStartNewDialog)

        startNewDialogButton.setOnClickListener {
            val intent = Intent(this, CreatingChatActivity::class.java)
            startActivity(intent)
        }
    }

    private fun uploadDialogs() {
        val adapter = DialogAdapter(this, userList)

        val activityDialogUsers: RecyclerView = findViewById(R.id.activityDialog_userRecyclerView)

        activityDialogUsers.layoutManager = LinearLayoutManager(this)
        activityDialogUsers.adapter = adapter

        webSocket.getDialogs(
            UserDto(getCurrentUsername()),
            object : ListenableFuture<List<DialogDto>> {
                override fun onSuccessful(result: List<DialogDto>) {
                    Log.i(ContentValues.TAG, "Dialogs got")

                    if (result.isNotEmpty()) {
                        Log.i(ContentValues.TAG, "Dialogs list size: [${result.size}]")
                        userList.addAll(result)
                    }

                    adapter.notifyItemRangeInserted(userList.lastIndex, result.size)
                }
            })
    }
}