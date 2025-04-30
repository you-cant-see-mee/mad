package com.example.terminallab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class ProductListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        val dbHelper = DBHelper(this)
        val products = dbHelper.getProducts()

        val checkboxContainer: LinearLayout = findViewById(R.id.checkboxContainer)
        val inflater = LayoutInflater.from(this)

        for (i in products.indices) {
            val product = products[i]
            val checkBox = CheckBox(this)
            checkBox.text = "${product.first} ($${product.second})"

            // Apply animation
            val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_down)
            checkBox.startAnimation(animation)

            // Add the CheckBox to the LinearLayout
            checkboxContainer.addView(checkBox)

            // You might want to add a small delay for each animation to create a staggered effect
            checkBox.postDelayed({
                checkBox.visibility = View.VISIBLE // Make it visible after animation starts
            }, i * 100L) // Adjust the delay as needed
            checkBox.visibility = View.INVISIBLE // Initially hide the CheckBox
        }
    }
}