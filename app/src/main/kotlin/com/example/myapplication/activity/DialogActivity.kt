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
import com.example.myapplication.activity.authorization.Authorization
import com.example.myapplication.adapters.DialogAdapter
import com.example.myapplication.dto.DialogDto
import com.example.myapplication.dto.UserDto
import com.example.myapplication.util.factory.ResolverFactory
import com.example.myapplication.util.getCurrentUsername
import com.example.myapplication.util.operation.ListenableFuture
import kotlinx.android.synthetic.main.activity_dialog.activityDialog_btnStartNewDialog
import kotlinx.android.synthetic.main.activity_dialog.activityDialog_textBar
import kotlinx.android.synthetic.main.activity_dialog.activityDialog_userRecyclerView

class DialogActivity : AppCompatActivity() {
    private val webSocket = ResolverFactory.instance.getImplResolver()

    private val userList = mutableListOf<DialogDto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog)

        activityDialog_btnStartNewDialog.setOnClickListener {
            val intent = Intent(this, CreatingChatActivity::class.java)
            startActivity(intent)
        }

        val adapter = DialogAdapter(this, userList)

        activityDialog_userRecyclerView.layoutManager = LinearLayoutManager(this)
        activityDialog_userRecyclerView.adapter = adapter

        webSocket.getDialogs(
            UserDto(getCurrentUsername()),
            object : ListenableFuture<List<DialogDto>> {
                override fun onSuccessful(result: List<DialogDto>) {
                    Log.i(ContentValues.TAG, "Dialogs is gotten")

                    if (result.isEmpty()) {
                        activityDialog_textBar.text = "В данный момент у вас нет диалогов. Попробуйте начать новый!"
                        activityDialog_textBar.visibility = View.VISIBLE
                    } else {
                        Log.i(ContentValues.TAG, "Dialogs list size: [${result.size}]")
                        userList.addAll(result)
                    }

                    adapter.notifyItemRangeInserted(userList.lastIndex, result.size)
                }
            })
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