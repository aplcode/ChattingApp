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
            val credentials = getCredentials()
            if (credentials != null) {
                signUp(credentials)
            }
        }
    }

    private fun signUp(credentials: CustomerSignUpInfoDto) {
        setButtonInactive()
        webSocket.signUp(credentials, {
            val intent = Intent(this, MainActivity::class.java)
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

    private fun getCredentials(): CustomerSignUpInfoDto? {
        val name = activitySignUp_editName.text.toString()
        val surname = activitySignUp_editSurname.text.toString()
        val email = activitySignUp_editEmail.text.toString()
        val password = activitySignUp_editPassword.text.toString()

        if (name.isBlank()) {
            activitySignUp_editName.setHintTextColor(Color.RED)
            androidWidgetToast("Name must not be empty")
            return null
        }

        if (surname.isBlank()) {
            activitySignUp_editSurname.setHintTextColor(Color.RED)
            androidWidgetToast("Surname must not be empty")
            return null
        }

        if (!emailValidatePattern.matcher(email).matches()) {
            activitySignUp_editEmail.setHintTextColor(Color.RED)
            activitySignUp_editEmail.setTextColor(Color.RED)
            androidWidgetToast("email template is email@sandbox.com")
            return null
        }

        if (password.isBlank()) {
            activitySignUp_editPassword.setHintTextColor(Color.RED)
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

    private fun androidWidgetToast(message: String?) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun setButtonInactive() {
        activityLogIn_btnSignUp.isClickable = false
    }

    private fun setButtonActive() {
        activityLogIn_btnSignUp.isClickable = true
    }

    companion object {
        private lateinit var username: String
        private val emailValidatePattern = Pattern.compile("^[a-zA-Z0-9]{1,20}@[a-z]{1,20}\\.(ru|com|net)\$")

        fun getUsernameIsInit(): String? = if (this::username.isInitialized) username else null
    }
}