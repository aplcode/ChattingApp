package com.example.myapplication.authorization

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.activity.MainActivity
import com.example.myapplication.R
import com.example.myapplication.util.WebSocketResolver
import com.example.myapplication.dto.CustomerSignUpInfoDto
import kotlinx.android.synthetic.main.activity_log_in.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.regex.Pattern

class SignUp : AppCompatActivity() {
    private val webSocket = WebSocketResolver.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        supportActionBar?.hide()

        setButtonActive()
        activityLogIn_btnSignUp.setOnClickListener {
            val credentials = getCredentialsSignUp()
            if (credentials != null) {
                signUp(credentials)
            }
        }
    }

    private fun signUp(credentials: CustomerSignUpInfoDto) {
        setButtonInactive()
        webSocket.signUp(credentials, {
            val intent = Intent(this, Welcome::class.java)
            finish()
            startActivity(intent)
        }, {
            androidWidgetToast("Error registration")
            setButtonActive()
        }, {
            androidWidgetToast(it.message)
            setButtonActive()
        })
    }

    private fun getCredentialsSignUp(): CustomerSignUpInfoDto? {
        val name = activityLogIn_SignInName.text.toString()
        val surname = activityLogIn_SignInSurname.text.toString()
        val email = activityLogIn_SignInEmail.text.toString()
        val password = activityLogIn_SignInPassword.text.toString()

        if (name.isBlank()) {
            activityLogIn_SignInName.setHintTextColor(Color.RED)
            androidWidgetToast("Name must not be empty")
            return null
        }

        if (surname.isBlank()) {
            activityLogIn_SignInSurname.setHintTextColor(Color.RED)
            androidWidgetToast("Surname must not be empty")
            return null
        }

        if (!emailValidatePattern.matcher(email).matches()) {
            activityLogIn_SignInEmail.setHintTextColor(Color.RED)
            activityLogIn_SignInEmail.setTextColor(Color.RED)
            androidWidgetToast("email template is email@sandbox.com")
            return null
        }

        if (password.isBlank()) {
            activityLogIn_SignInPassword.setHintTextColor(Color.RED)
            androidWidgetToast("Password must not be empty")
            return null
        }

        return CustomerSignUpInfoDto(
            firstname = name,
            lastname = surname,
            emailAddress = email,
            password = password,
        )
    }


    private fun setButtonInactive() {
        activityLogIn_btnSignIn.isClickable = false
    }

    private fun setButtonActive() {
        activityLogIn_btnSignIn.isClickable = true
    }

    companion object {
        private lateinit var username: String
        private val emailValidatePattern = Pattern.compile("^[a-zA-Z0-9]{1,20}@[a-z]{1,20}\\.(ru|com|net)\$")

        fun getUsernameIsInit(): String? = if (this::username.isInitialized) username else null
    }
    private fun androidWidgetToast(message: String?) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}