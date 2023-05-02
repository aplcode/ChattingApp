package com.example.myapplication.activity.authorization

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.material.textfield.TextInputEditText
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import kotlin.io.path.Path

class Authorization : AppCompatActivity() {
    private val webSocket: Resolver = ResolverFactory.instance.getImplResolver()

    private val mapper = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorization)

        supportActionBar?.hide()

        switcherSettingUp()

        loginButtonSettingUp()
        signUpButtonSettingUp()
    }

    private fun switcherSettingUp() {
        val switcherSingUp: TextView = findViewById(R.id.activityAuthorization_switcherSingUp)
        val switcherLogIn: TextView = findViewById(R.id.activityAuthorization_switcherLogIn)
        val singUpLayout: LinearLayout = findViewById(R.id.activityAuthorization_singUpLayout)
        val logInLayout: LinearLayout = findViewById(R.id.activityAuthorization_logInLayout)

        switcherSingUp.setOnClickListener {
            switcherSingUp.background = ResourcesCompat.getDrawable(resources, R.drawable.switch_trcks, null)
            switcherSingUp.setTextColor(resources.getColor(R.color.textColor, null))
            switcherLogIn.background = null
            singUpLayout.visibility = View.VISIBLE
            logInLayout.visibility = View.GONE
            switcherLogIn.setTextColor(resources.getColor(R.color.pinkColor, null))
        }

        switcherLogIn.setOnClickListener {
            switcherSingUp.background = null
            switcherSingUp.setTextColor(resources.getColor(R.color.pinkColor, null))
            switcherLogIn.background =
                ResourcesCompat.getDrawable(resources, R.drawable.switch_trcks, null)
            singUpLayout.visibility = View.GONE
            logInLayout.visibility = View.VISIBLE
            switcherLogIn.setTextColor(resources.getColor(R.color.textColor, null))
        }
    }

    private fun loginButtonSettingUp() {
        val loginButton: Button = findViewById(R.id.activityAuthorization_btnLogin)
        loginButton.setOnClickListener {
            val credentialsLogIn = getCredentialsLogIn()
            if (credentialsLogIn != null) {
                login(credentialsLogIn)
            }
        }
    }

    private fun signUpButtonSettingUp() {
        val signUpButton: Button = findViewById(R.id.activityAuthorization_btnSignIn)
        signUpButton.setOnClickListener {
            val credentialsSignUp = getCredentialsSignUp()
            if (credentialsSignUp != null) {
                signUp(credentialsSignUp)
            }
        }
    }

    private fun getCacheFilePath() = (cacheDir.path + LoginCache.FILE_PATH + LoginCache.FILE_NAME).also { Log.d(this::class.simpleName, "Cache login file [$it]") }

    private fun getCacheLoginDirectoryPath() = Path(cacheDir.path + LoginCache.FILE_PATH)

    private fun setSessionIntoCacheFile(username: String) {
        val loginCacheDirectory = getCacheLoginDirectoryPath()
        if (!Files.isDirectory(loginCacheDirectory)) {
            Files.createDirectory(loginCacheDirectory)
        }
        FileWriter(getCacheFilePath(), true).use {
            it.appendLine(mapper.writeValueAsString(getSessionInformation(username)))
        }
    }

    private fun login(credentials: CustomerLogInInfoDto) {
        setButtonInactive()
        webSocket.logIn(credentials,
            object : ListenableFuture<ResponseDto> {
                override fun onSuccessful(result: ResponseDto) {
                    username = credentials.login

                    setSessionIntoCacheFile(username)
                    preprocessAllCache()

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

    private fun preprocessAllCache() {
        FileReader(getCacheFilePath()).use { reader ->
            loginCache = LoginCache(reader.readLines().map<String, LoginCache.LoginInformation> { mapper.readValue(it) }.reversed())
        }
    }

    private fun signUp(credentials: CustomerSignUpInfoDto) {
        setButtonInactive()
        webSocket.signUp(credentials,
            object : ListenableFuture<ResponseDto> {
                override fun onSuccessful(result: ResponseDto) {
                    username = credentials.username
                    val intent = Intent(this@Authorization, DialogActivity::class.java)

                    setSessionIntoCacheFile(username)
                    preprocessAllCache()

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

        val loginUsernameText: TextInputEditText = findViewById(R.id.activityAuthorization_LoginUsername)
        val loginPasswordText: TextInputEditText = findViewById(R.id.activityAuthorization_LoginPassword)

        val username = loginUsernameText.text.toString()
        val password = loginPasswordText.text.toString()

        if (username.isBlank()) {
            loginUsernameText.setHintTextColor(Color.RED)
            androidWidgetToast("Username must not be empty")
            return null
        }

        if (password.isBlank()) {
            loginPasswordText.setHintTextColor(Color.RED)
            androidWidgetToast("Password must not be empty")
            return null
        }

        return CustomerLogInInfoDto(
            login = username,
            password = password,
        )
    }

    private fun getCredentialsSignUp(): CustomerSignUpInfoDto? {
        val nameText: TextInputEditText = findViewById(R.id.activityAuthorization_SignInName)
        val surnameText: TextInputEditText = findViewById(R.id.activityAuthorization_SignInSurname)
        val usernameText: TextInputEditText = findViewById(R.id.activityAuthorization_SignInUsername)
        val passwordText: TextInputEditText = findViewById(R.id.activityAuthorization_SignInPassword)
        val confirmPasswordText: TextInputEditText = findViewById(R.id.activityAuthorization_SignInConfirmPassword)

        val name = nameText.text.toString()
        val surname = surnameText.text.toString()
        val username = usernameText.text.toString()
        val password = passwordText.text.toString()
        val conformPassword = confirmPasswordText.text.toString()

        if (name.isBlank()) {
            nameText.setHintTextColor(Color.RED)
            androidWidgetToast("Name must not be empty")
            return null
        }

        if (surname.isBlank()) {
            surnameText.setHintTextColor(Color.RED)
            androidWidgetToast("Surname must not be empty")
            return null
        }

        if (username.contains(" ")) {
            usernameText.setHintTextColor(Color.RED)
            usernameText.setTextColor(Color.RED)
            androidWidgetToast("Username cannot contain spaces")
            return null
        }

        if (password.isBlank()) {
            passwordText.setHintTextColor(Color.RED)
            androidWidgetToast("Password must not be empty")
            return null
        }

        if (password != conformPassword) {
            confirmPasswordText.setHintTextColor(Color.RED)
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
        findViewById<Button>(R.id.activityAuthorization_btnSignIn).isClickable = false
        findViewById<Button>(R.id.activityAuthorization_btnLogin).isClickable = false
    }

    private fun setButtonActive() {
        findViewById<Button>(R.id.activityAuthorization_btnSignIn).isClickable = true
        findViewById<Button>(R.id.activityAuthorization_btnLogin).isClickable = true
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
        private lateinit var loginCache: LoginCache

        fun getLoginCacheIsInit(): LoginCache? = if (this::loginCache.isInitialized) loginCache else null

        fun getUsernameIsInit(): String? = if (this::username.isInitialized) username else null
    }
}