package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        supportActionBar?.hide()

        findViewById<Button>(R.id.btnSignUp).setOnClickListener {
            val name = findViewById<EditText>(R.id.edt_name).text.toString()
            val email = findViewById<EditText>(R.id.edt_email).text.toString()
            val password = findViewById<EditText>(R.id.edt_password).text.toString()

            signUp(name, email, password)
        }
    }

    private fun signUp(name: String, email: String, password: String) =
        FirebaseAuth.getInstance().also {
            it.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    addUserToDatabase(name, email, it.currentUser?.uid!!)
                    finish()
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    println("-".repeat(10) + task.exception)
                    Toast.makeText(this, task.exception?.message ?: "Error", Toast.LENGTH_LONG).show()
                }
            }
        }

    private fun addUserToDatabase(name: String, email: String, uid: String) =
        FirebaseDatabase.getInstance().reference.child("user").child(uid).setValue(User(name, email, uid))
}