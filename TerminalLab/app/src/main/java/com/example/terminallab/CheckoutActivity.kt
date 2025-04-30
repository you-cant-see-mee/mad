package com.example.terminallab

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.*

class CheckoutActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private val SMS_PERMISSION_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)

        val textViewCartItems = findViewById<TextView>(R.id.textViewCartItems)
        val radioGroupPayment = findViewById<RadioGroup>(R.id.radioGroupPayment)
        val editTextName = findViewById<EditText>(R.id.editTextName)
        val editTextPhone = findViewById<EditText>(R.id.editTextPhone)
        val editTextAddress = findViewById<EditText>(R.id.editTextAddress)
        val editTextLatitude = findViewById<EditText>(R.id.editTextLatitude)
        val editTextLongitude = findViewById<EditText>(R.id.editTextLongitude)
        val buttonFetchLocation = findViewById<Button>(R.id.buttonFetchLocation)
        val textViewResolvedAddress = findViewById<TextView>(R.id.textViewResolvedAddress)
        val buttonPay = findViewById<Button>(R.id.buttonPay)

        // ✅ Retrieve cart items safely
        // Retrieve cart items safely
        val cartItems = sharedPreferences.getStringSet("CartItems", emptySet()) ?: emptySet()
        textViewCartItems.text = if (cartItems.isNotEmpty()) {
            "Selected Items:\n" + cartItems.joinToString("\n")
        } else {
            "No items in cart!"
        }


        // ✅ Fetch location from address
        buttonFetchLocation.setOnClickListener {
            val addressInput = editTextAddress.text.toString().trim()

            if (addressInput.isEmpty()) {
                Toast.makeText(this, "Please enter an address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val geocoder = Geocoder(this, Locale.getDefault())
            try {
                val addressList: List<Address>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocationName(addressInput, 1) // Newer API usage
                } else {
                    geocoder.getFromLocationName(addressInput, 1)
                }

                if (!addressList.isNullOrEmpty()) {
                    val address = addressList[0]
                    val latitude = address.latitude
                    val longitude = address.longitude

                    editTextLatitude.setText(latitude.toString())
                    editTextLongitude.setText(longitude.toString())
                    textViewResolvedAddress.text = "Resolved Address: ${address.getAddressLine(0)}"
                } else {
                    Toast.makeText(this, "Address not found!", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                Toast.makeText(this, "Error fetching location: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // ✅ Handle payment & SMS confirmation
        buttonPay.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val phone = editTextPhone.text.toString().trim()
            val latitude = editTextLatitude.text.toString().trim()
            val longitude = editTextLongitude.text.toString().trim()
            val selectedPaymentId = radioGroupPayment.checkedRadioButtonId

            if (name.isEmpty() || phone.isEmpty() || latitude.isEmpty() || longitude.isEmpty() || selectedPaymentId == -1) {
                Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedPayment = findViewById<RadioButton>(selectedPaymentId).text.toString()
            val orderMessage = """
                Order Confirmed for $name!
                Items: ${cartItems.joinToString(", ")}
                Payment: $selectedPayment
                Location: $latitude, $longitude
            """.trimIndent()

            // ✅ Request SMS permission before sending SMS
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                sendSMS(phone, orderMessage)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SMS_PERMISSION_CODE)
            }

            sharedPreferences.edit().remove("CartItems").apply()
            finish()
        }
    }

    // ✅ Function to send SMS
    private fun sendSMS(phone: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phone, null, message, null, null)
            Toast.makeText(this, "Order confirmed! SMS sent.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "SMS failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ✅ Handle SMS permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted. Please press Pay again.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "SMS permission denied. Unable to send confirmation.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
