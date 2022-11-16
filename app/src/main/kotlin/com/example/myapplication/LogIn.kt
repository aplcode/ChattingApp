package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.dto.CustomerLogInInfoDto
import kotlinx.android.synthetic.main.activity_log_in.*

class LogIn : AppCompatActivity() {
    private val webSocket = WebSocketResolver.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        supportActionBar?.hide()

        activityLogIn_btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        activityLogIn_btnLogin.setOnClickListener {
            val credentials = getCredentials()
            if (credentials != null) {
                login(credentials)
            }
        }
    }

    private fun login(credentials: CustomerLogInInfoDto) {
        webSocket.logIn(credentials, {
            val intent = Intent(this, MainActivity::class.java)
            finish()
            startActivity(intent)
        }, {
            androidWidgetToast("Wrong login or password")
        }, {
            androidWidgetToast(it.message)
        })
    }

    private fun getCredentials(): CustomerLogInInfoDto? {
        val email = activityLogIn_editEmail.text.toString()
        val password = activityLogIn_editPassword.text.toString()

        if (email.isBlank()) {
            activityLogIn_editEmail.setHintTextColor(Color.RED)
            androidWidgetToast("email must not be empty")
            return null
        }

        if (password.isBlank()) {
            activityLogIn_editPassword.setHintTextColor(Color.RED)
            androidWidgetToast("Password must not be empty")
            return null
        }

        return CustomerLogInInfoDto(
            login = email,
            password = password,
        )
    }

    private fun androidWidgetToast(message: String?) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}