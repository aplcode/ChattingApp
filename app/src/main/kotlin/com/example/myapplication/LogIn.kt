package com.example.myapplication

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.dto.ResponseDto
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_log_in.*
import java.time.LocalDateTime

class LogIn : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    private val gson: Gson = GsonBuilder().registerTypeAdapter(
        LocalDateTime::class.java,
        GsonLocalDateTimeAdapter()
    ).create()

    private val mainViewModel: WebSocketResolver = WebSocketResolver.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        supportActionBar?.hide()

        mAuth = FirebaseAuth.getInstance()

        btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val email = edt_email.text.toString()
            val password = edt_password.text.toString()

            login(email, password)
        }
    }

    private fun login(email: String, password: String) {
        mainViewModel.login(email, password,
            {
                Log.d(TAG, it.payload)
                val message = gson.fromJson(it.payload, ResponseDto::class.java)
                if (message.status == 0) {
                    val intent = Intent(this@LogIn, MainActivity::class.java)
                    finish()
                    startActivity(intent)
                } else {
                    Toast.makeText(this@LogIn, "User does not exist", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(this@LogIn, it.message, Toast.LENGTH_SHORT).show()
            })
    }
}