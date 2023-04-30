package com.example.myapplication.activity.authorization

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.myapplication.R
import com.example.myapplication.activity.DialogActivity
import com.example.myapplication.dto.CustomerLogInInfoDto
import com.example.myapplication.dto.CustomerSignUpInfoDto
import com.example.myapplication.dto.ResponseDto
import com.example.myapplication.dto.cache.LoginCache
import com.example.myapplication.util.SessionContext
import com.example.myapplication.util.factory.ResolverFactory
import com.example.myapplication.util.getTime
import com.example.myapplication.util.operation.ListenableFuture
import com.example.myapplication.util.socket.Resolver
import com.google.android.material.textfield.TextInputEditText
import java.io.FileWriter

class Authorization : AppCompatActivity() {
    private val webSocket: Resolver = ResolverFactory.instance.getImplResolver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorization)

        supportActionBar?.hide()

        val activityAuthorization_switcherSingUp = findViewById<TextView>(R.id.activityAuthorization_switcherSingUp)
        val activityAuthorization_switcherLogIn = findViewById<TextView>(R.id.activityAuthorization_switcherLogIn)
        val activityAuthorization_singUpLayout = findViewById<LinearLayout>(R.id.activityAuthorization_singUpLayout)
        val activityAuthorization_logInLayout = findViewById<LinearLayout>(R.id.activityAuthorization_logInLayout)

        activityAuthorization_switcherSingUp.setOnClickListener {
            activityAuthorization_switcherSingUp.background =
                ResourcesCompat.getDrawable(resources, R.drawable.switch_trcks, null)
            activityAuthorization_switcherSingUp.setTextColor(resources.getColor(R.color.textColor, null))
            activityAuthorization_switcherLogIn.background = null
            activityAuthorization_singUpLayout.visibility = View.VISIBLE
            activityAuthorization_logInLayout.visibility = View.GONE
            activityAuthorization_switcherLogIn.setTextColor(resources.getColor(R.color.pinkColor, null))
        }

        activityAuthorization_switcherLogIn.setOnClickListener {
            activityAuthorization_switcherSingUp.background = null
            activityAuthorization_switcherSingUp.setTextColor(resources.getColor(R.color.pinkColor, null))
            activityAuthorization_switcherLogIn.background =
                ResourcesCompat.getDrawable(resources, R.drawable.switch_trcks, null)
            activityAuthorization_singUpLayout.visibility = View.GONE
            activityAuthorization_logInLayout.visibility = View.VISIBLE
            activityAuthorization_switcherLogIn.setTextColor(resources.getColor(R.color.textColor, null))
        }

        val activityAuthorization_btnLogin = findViewById<Button>(R.id.activityAuthorization_btnLogin)
        activityAuthorization_btnLogin.setOnClickListener {
            val credentialsLogIn = getCredentialsLogIn()
            if (credentialsLogIn != null) {
                login(credentialsLogIn)
            }
        }

        val activityAuthorization_btnSignIn = findViewById<Button>(R.id.activityAuthorization_btnSignIn)
        activityAuthorization_btnSignIn.setOnClickListener {
            val credentialsSignUp = getCredentialsSignUp()
            if (credentialsSignUp != null) {
                signUp(credentialsSignUp)
            }
        }
    }

    private fun getCacheFilePath() = cacheDir.path + "/fileLoggingCache"

    private fun setSessionIntoCacheFile(username: String) {
        FileWriter(getCacheFilePath(), true).use {
            it.appendLine(getSessionInformation(username).toString())
        }
    }

    private fun login(credentials: CustomerLogInInfoDto) {
        setButtonInactive()
        webSocket.logIn(credentials,
            object : ListenableFuture<ResponseDto> {
                override fun onSuccessful(result: ResponseDto) {
                    username = credentials.login

                    setSessionIntoCacheFile(username)

                    val intent = Intent(this@Authorization, DialogActivity::class.java)
                    finish()
                    startActivity(intent)
                }

                override fun onUnsuccessful() {
                    androidWidgetToast("Wrong login or password")
                    super.onUnsuccessful()
                    setButtonActive()
                }

                override fun onException(exception: Throwable) {
                    androidWidgetToast(exception.message)
                    super.onException(exception)
                    setButtonActive()
                }
            })
    }

    private fun signUp(credentials: CustomerSignUpInfoDto) {
        setButtonInactive()
        webSocket.signUp(credentials,
            object : ListenableFuture<ResponseDto> {
                override fun onSuccessful(result: ResponseDto) {
                    username = credentials.username
                    val intent = Intent(this@Authorization, DialogActivity::class.java)
                    finish()
                    startActivity(intent)
                }

                override fun onUnsuccessful() {
                    androidWidgetToast("Username is already in use")
                    super.onUnsuccessful()
                    setButtonActive()
                }

                override fun onException(exception: Throwable) {
                    androidWidgetToast(exception.message)
                    super.onException(exception)
                    setButtonActive()
                }
            })
    }

    private fun getCredentialsLogIn(): CustomerLogInInfoDto? {

        val activityAuthorization_LoginUsername =
            findViewById<TextInputEditText>(R.id.activityAuthorization_LoginUsername)
        val activityAuthorization_LoginPassword =
            findViewById<TextInputEditText>(R.id.activityAuthorization_LoginPassword)

        val email = activityAuthorization_LoginUsername.text.toString()
        val password = activityAuthorization_LoginPassword.text.toString()

        if (email.isBlank()) {
            activityAuthorization_LoginUsername.setHintTextColor(Color.RED)
            androidWidgetToast("email must not be empty")
            return null
        }

        if (password.isBlank()) {
            activityAuthorization_LoginPassword.setHintTextColor(Color.RED)
            androidWidgetToast("Password must not be empty")
            return null
        }

        return CustomerLogInInfoDto(
            login = email,
            password = password,
        )
    }

    private fun getCredentialsSignUp(): CustomerSignUpInfoDto? {
        val activityAuthorization_SignInName = findViewById<TextInputEditText>(R.id.activityAuthorization_SignInName)
        val activityAuthorization_SignInSurname =
            findViewById<TextInputEditText>(R.id.activityAuthorization_SignInSurname)
        val activityAuthorization_SignInUsername =
            findViewById<TextInputEditText>(R.id.activityAuthorization_SignInUsername)
        val activityAuthorization_SignInPassword =
            findViewById<TextInputEditText>(R.id.activityAuthorization_SignInPassword)
        val activityAuthorization_SignInConfirmPassword =
            findViewById<TextInputEditText>(R.id.activityAuthorization_SignInConfirmPassword)

        val name = activityAuthorization_SignInName.text.toString()
        val surname = activityAuthorization_SignInSurname.text.toString()
        val username = activityAuthorization_SignInUsername.text.toString()
        val password = activityAuthorization_SignInPassword.text.toString()
        val conformPassword = activityAuthorization_SignInConfirmPassword.text.toString()

        if (name.isBlank()) {
            activityAuthorization_SignInName.setHintTextColor(Color.RED)
            androidWidgetToast("Name must not be empty")
            return null
        }

        if (surname.isBlank()) {
            activityAuthorization_SignInSurname.setHintTextColor(Color.RED)
            androidWidgetToast("Surname must not be empty")
            return null
        }

        if (username.contains(" ")) {
            activityAuthorization_SignInUsername.setHintTextColor(Color.RED)
            activityAuthorization_SignInUsername.setTextColor(Color.RED)
            androidWidgetToast("Username cannot contain spaces")
            return null
        }

        if (password.isBlank()) {
            activityAuthorization_SignInPassword.setHintTextColor(Color.RED)
            androidWidgetToast("Password must not be empty")
            return null
        }

        if (password != conformPassword) {
            activityAuthorization_SignInConfirmPassword.setHintTextColor(Color.RED)
            androidWidgetToast("Confirm password is wrong!")
            return null
        }

        return CustomerSignUpInfoDto(
            firstname = name,
            lastname = surname,
            username = username,
            password = password,
        )
    }

    private fun setButtonInactive() {
        val activityAuthorization_btnSignIn = findViewById<Button>(R.id.activityAuthorization_btnSignIn)
        val activityAuthorization_btnLogin = findViewById<Button>(R.id.activityAuthorization_btnLogin)

        activityAuthorization_btnSignIn.isClickable = false
        activityAuthorization_btnLogin.isClickable = false
    }

    private fun setButtonActive() {
        val activityAuthorization_btnSignIn = findViewById<Button>(R.id.activityAuthorization_btnSignIn)
        val activityAuthorization_btnLogin = findViewById<Button>(R.id.activityAuthorization_btnLogin)

        activityAuthorization_btnSignIn.isClickable = true
        activityAuthorization_btnLogin.isClickable = true
    }

    private fun androidWidgetToast(message: String?) {
        Looper.myLooper() ?: Looper.prepare()
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getSessionInformation(username: String) = LoginCache.LoginInformation(
        LoginCache.LoginInformation.ApplicationInformation(
            username, SessionContext.CurrentSession.id, SessionContext.CurrentApplicationVersion.id,
        ),
        LoginCache.LoginInformation.DeviceInformation(
            Build.BRAND,
            Build.MODEL,
            Build.ID,
        ),
        LoginCache.LoginInformation.LocationInformation(
            getTime(),
        )
    )

    companion object {
        private lateinit var username: String

        fun getUsernameIsInit(): String? = if (this::username.isInitialized) username else null
    }
}