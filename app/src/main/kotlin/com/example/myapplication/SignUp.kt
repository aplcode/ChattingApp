package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignUp : AppCompatActivity() {

    private val mainViewModel = WebSocketResolver.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        supportActionBar?.hide()

        findViewById<Button>(R.id.btnSignUp).setOnClickListener {
            val name = findViewById<EditText>(R.id.edt_name).text.toString()
            val surname = findViewById<EditText>(R.id.edt_surname).text.toString()
            val email = findViewById<EditText>(R.id.edt_email).text.toString()
            val password = findViewById<EditText>(R.id.edt_password).text.toString()

            signUp(name, surname, email, password)
        }
    }

    private fun signUp(name: String, surname : String, email: String, password: String) =
        Thread {
            mainViewModel.signup(name, surname, email, password)
            while (WebSocketResolver.authFlag.get() == 0) {
            }
            if (WebSocketResolver.authFlag.get() == 1) {
                val intent = Intent(this@SignUp, MainActivity::class.java)
                finish()
                startActivity(intent)
            } else {
                Looper.prepare()
                Toast.makeText(this@SignUp, "User already exist", Toast.LENGTH_SHORT).show()
            }
        }.start()

}