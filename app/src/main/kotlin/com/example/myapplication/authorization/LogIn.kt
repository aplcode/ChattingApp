package com.example.myapplication.authorization

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.myapplication.R
import com.example.myapplication.Welcome
import com.example.myapplication.activity.MainActivity
import com.example.myapplication.dto.CustomerLogInInfoDto
import com.example.myapplication.dto.CustomerSignUpInfoDto
import com.example.myapplication.util.WebSocketResolver
import java.util.regex.Pattern
import kotlinx.android.synthetic.main.activity_log_in.activityLogIn_LoginEmail
import kotlinx.android.synthetic.main.activity_log_in.activityLogIn_LoginPassword
import kotlinx.android.synthetic.main.activity_log_in.activityLogIn_SignInEmail
import kotlinx.android.synthetic.main.activity_log_in.activityLogIn_SignInName
import kotlinx.android.synthetic.main.activity_log_in.activityLogIn_SignInPassword
import kotlinx.android.synthetic.main.activity_log_in.activityLogIn_SignInSurname
import kotlinx.android.synthetic.main.activity_log_in.activityLogIn_btnLogin
import kotlinx.android.synthetic.main.activity_log_in.activityLogIn_btnSignIn
import kotlinx.android.synthetic.main.activity_log_in.logIn
import kotlinx.android.synthetic.main.activity_log_in.logInLayout
import kotlinx.android.synthetic.main.activity_log_in.singUp
import kotlinx.android.synthetic.main.activity_log_in.singUpLayout

class LogIn : AppCompatActivity() {
    private val webSocket = WebSocketResolver.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        supportActionBar?.hide()

        singUp.setOnClickListener {
            singUp.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_trcks, null)
            singUp.setTextColor(resources.getColor(R.color.textColor, null))
            logIn.background = null
            singUpLayout.visibility = View.VISIBLE
            logInLayout.visibility = View.GONE
            logIn.setTextColor(resources.getColor(R.color.pinkColor, null))
        }

        logIn.setOnClickListener {
            singUp.background = null
            singUp.setTextColor(resources.getColor(R.color.pinkColor, null))
            logIn.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_trcks, null)
            singUpLayout.visibility = View.GONE
            logInLayout.visibility = View.VISIBLE
            logIn.setTextColor(resources.getColor(R.color.textColor, null))
        }

        activityLogIn_btnLogin.setOnClickListener {
            val credentialsLogIn = getCredentialsLogIn()
            if (credentialsLogIn != null) {
                login(credentialsLogIn)
            }
        }

        activityLogIn_btnSignIn.setOnClickListener {
            val credentialsSignUp = getCredentialsSignUp()
            if (credentialsSignUp != null) {
                signUp(credentialsSignUp)
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

    private fun getCredentialsLogIn(): CustomerLogInInfoDto? {
        val email = activityLogIn_LoginEmail.text.toString()
        val password = activityLogIn_LoginPassword.text.toString()

        if (email.isBlank()) {
            activityLogIn_LoginEmail.setHintTextColor(Color.RED)
            androidWidgetToast("email must not be empty")
            return null
        }

        if (password.isBlank()) {
            activityLogIn_LoginPassword.setHintTextColor(Color.RED)
            androidWidgetToast("Password must not be empty")
            return null
        }

        return CustomerLogInInfoDto(
            login = email,
            password = password,
        )
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

    private fun androidWidgetToast(message: String?) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    companion object {
        private lateinit var username: String
        private val emailValidatePattern = Pattern.compile("^[a-zA-Z0-9]{1,20}@[a-z]{1,20}\\.(ru|com|net)\$")

        fun getUsernameIsInit(): String? = if (this::username.isInitialized) username else null
    }
}