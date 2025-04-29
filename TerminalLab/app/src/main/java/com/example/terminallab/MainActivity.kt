package com.example.terminallab

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    private val airplaneModeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
                val isAirplaneModeOn = intent.getBooleanExtra("state", false)
                Toast.makeText(
                    context,
                    if (isAirplaneModeOn) "Airplane Mode ON" else "Airplane Mode OFF",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            Toast.makeText(this, "Long press to Logout", Toast.LENGTH_SHORT).show()
        }

        toolbar.setOnLongClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Logout")
            builder.setMessage("Are you sure you want to logout?")
            builder.setPositiveButton("Logout") { _, _ ->
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
            builder.setNegativeButton("Cancel", null)
            builder.show()
            true
        }


        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)

        val loggedInUser = sharedPreferences.getString("LoggedInUser", null)



        if (loggedInUser == null) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Welcome, $loggedInUser!", Toast.LENGTH_SHORT).show()
        }

        val dbHelper = DBHelper(this)
        val productList = dbHelper.getProducts()
        val linearLayout = findViewById<LinearLayout>(R.id.linearLayoutProducts)
        val selectedProducts = mutableListOf<String>()

        for ((name, price) in productList) {
            val checkBox = CheckBox(this)
            checkBox.text = "$name - Rs. $price" // Convert Pair to String properly

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedProducts.add(name)  // Only store the name in selectedProducts
                } else {
                    selectedProducts.remove(name)
                }
            }
            linearLayout.addView(checkBox)
        }


        val checkoutButton = findViewById<Button>(R.id.buttonCheckout)
        checkoutButton.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putStringSet("CartItems", HashSet(selectedProducts)) // FIXED LINE
            editor.apply()
            startActivity(Intent(this, CheckoutActivity::class.java))
        }

        val locateStoresBtn: Button = findViewById(R.id.buttonLocateStores)
        locateStoresBtn.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                Toast.makeText(this, "Settings Clicked", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.menu_graphics -> {
                val intent = Intent(this, CheckoutActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.menu_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                val currentUsername = sharedPreferences.getString("LoggedInUser", null)
                if (currentUsername != null) {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("username",currentUsername)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "No logged-in user!", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.action_contact_us -> {
                val intent = Intent(this, ContactUsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_about_us -> {
                val intent = Intent(this, AboutUsActivity::class.java)
                startActivity(intent)
                true
            }


            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(airplaneModeReceiver, IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(airplaneModeReceiver)
    }

}
