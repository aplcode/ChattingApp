package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LogIn : AppCompatActivity() {
    private lateinit var btnLogin: Button
    private lateinit var btnSignUp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        supportActionBar?.hide()

        btnLogin = findViewById(R.id.btnLogin)
        btnSignUp = findViewById(R.id.btnSignUp)

        btnSignUp.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }

        btnLogin.setOnClickListener {
            val email = findViewById<EditText>(R.id.edt_email).text.toString()
            val passwd = findViewById<EditText>(R.id.edt_password).text.toString()

            if (email.isBlank() || passwd.isBlank()) {
                Toast.makeText(this, "Email or password is blank", Toast.LENGTH_SHORT).show()
            } else {
                login(email, passwd)
            }
        }
    }

    private fun login(email: String, password: String) =
        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    finish()
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    Toast.makeText(this, "Email or password is wrong", Toast.LENGTH_SHORT).show()
                }
            }
}