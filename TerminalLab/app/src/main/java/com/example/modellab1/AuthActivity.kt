package com.example.modellab1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AuthActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        dbHelper = DBHelper(this)
        sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)

        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val buttonSignup = findViewById<Button>(R.id.buttonSignup)

        buttonSignup.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                val success = dbHelper.insertUser(username, password)
                if (success) {
                    Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "User already exists!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        }

        buttonLogin.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (dbHelper.checkUser(username, password)) {
                // Save the logged-in username in SharedPreferences
                sharedPreferences.edit().putString("LoggedInUser", username).apply()

                // Pass the username with the intent to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("username", username)  // Pass username to MainActivity
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
