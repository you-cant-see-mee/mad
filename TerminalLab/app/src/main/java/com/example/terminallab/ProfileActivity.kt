package com.example.terminallab

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var username: String
    private lateinit var editTextName: EditText
    private lateinit var textViewDob: TextView
    private lateinit var textViewAge: TextView
    private lateinit var imageViewProfile: ImageView
    private lateinit var buttonSaveProfile: Button

    private val CAMERA_REQUEST_CODE = 1001
    private val CAMERA_PERMISSION_CODE = 2001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        dbHelper = DBHelper(this)

        editTextName = findViewById(R.id.editTextName)
        textViewDob = findViewById(R.id.textViewDOB)
        textViewAge = findViewById(R.id.textViewAge)
        imageViewProfile = findViewById(R.id.imageViewProfile)
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile)

        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        username = sharedPreferences.getString("LoggedInUser", null)
            ?: intent.getStringExtra("username")
                    ?: ""

        Log.d("ProfileActivity", "Logged-in username: $username")

        if (username.isNotEmpty()) {
            val user = dbHelper.getUserDetails(username)
            Log.d("ProfileActivity", "User details: $user")

            if (user != null) {
                editTextName.setText(user.name ?: "No name available")
                textViewDob.text = user.dob ?: "No DOB available"
                textViewAge.text = user.age?.toString() ?: "No age available"
            } else {
                Toast.makeText(this, "User details not found", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
        }

        imageViewProfile.setOnClickListener {
            // Ask for camera permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            } else {
                openCamera()
            }
        }

        textViewDob.setOnClickListener {
            showDatePickerDialog()
        }

        buttonSaveProfile.setOnClickListener {
            val name = editTextName.text.toString()
            val dob = textViewDob.text.toString()
            val age = textViewAge.text.toString().toIntOrNull() ?: 0

            if (dbHelper.updateUserProfile(username, name, dob, age)) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val dob = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                textViewDob.text = dob
                val age = calculateAge(selectedYear, selectedMonth, selectedDay)
                textViewAge.text = age.toString()
            }, year, month, day)

        datePickerDialog.show()
    }

    private fun calculateAge(year: Int, month: Int, day: Int): Int {
        val dob = Calendar.getInstance()
        dob.set(year, month, day)

        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        return age
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            val photo: Bitmap = data?.extras?.get("data") as Bitmap
            imageViewProfile.setImageBitmap(photo)
        }
    }
}
