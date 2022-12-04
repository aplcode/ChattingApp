package com.example.myapplication.authorization

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.activity.MainActivity
import com.example.myapplication.R
import com.example.myapplication.dto.CustomerLogInInfoDto
import com.example.myapplication.util.WebSocketResolver
import kotlinx.android.synthetic.main.activity_log_in.activityLogIn_btnLogin
import kotlinx.android.synthetic.main.activity_log_in.activityLogIn_editEmail
import kotlinx.android.synthetic.main.activity_log_in.activityLogIn_editPassword
import kotlinx.android.synthetic.main.activity_sign_up.activityLogIn_btnSignUp

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
        setButtonInactive()
        webSocket.logIn(credentials, {
            username = credentials.login
            val intent = Intent(this, MainActivity::class.java)
            finish()
            startActivity(intent)
        }, {
            androidWidgetToast("Wrong login or password")
            setButtonActive()
        }, {
            androidWidgetToast(it.message)
            setButtonActive()
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

    private fun setButtonInactive() {
        activityLogIn_btnLogin.isClickable = false
    }

    private fun setButtonActive() {
        activityLogIn_btnLogin.isClickable = true
    }


    private fun androidWidgetToast(message: String?) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    companion object {
        private lateinit var username: String

        fun getUsernameIsInit(): String? = if (this::username.isInitialized) username else null
    }
}