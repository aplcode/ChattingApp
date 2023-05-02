package com.example.myapplication.activity

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapters.LastLoginAdapter
import com.example.myapplication.util.getCurrentUsername
import com.example.myapplication.util.getLoginCache

class ImActivity : AppCompatActivity() {
    private val username = getCurrentUsername()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_im)

        imImageSettingUp()
        usernameTextSettingUp()
        clearCacheButtonSettingUp()
        lastLoginsListSettingUp()
    }

    private fun imImageSettingUp() {
        findViewById<TextView>(R.id.imImage).text = username.first().toString()
    }

    private fun usernameTextSettingUp() {
        findViewById<TextView>(R.id.usernameText).text = username
    }

    private fun clearCacheButtonSettingUp() { // TODO
//        val btn: TextView = findViewById(R.id.clearCacheButton)
//        val v1024 = BigDecimal.valueOf(1024)
//        btn.text = "Clear cache ${Path(cacheDir.path).fileSize().toBigDecimal().divide(v1024).divide(v1024)} Mb"
    }

    private fun lastLoginsListSettingUp() {
        val lastLoginList: RecyclerView = findViewById(R.id.lastLoginsList)
        lastLoginList.layoutManager = LinearLayoutManager(this)
        lastLoginList.adapter =
            LastLoginAdapter(this@ImActivity, getLoginCache()).apply { this.notifyItemRangeInserted(0, itemCount) }
    }
}