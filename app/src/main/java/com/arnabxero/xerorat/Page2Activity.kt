package com.arnabxero.xerorat

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Page2Activity : AppCompatActivity() {
    private lateinit var userIdTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page2_layout)

        userIdTextView = findViewById(R.id.userIdTextView)

        // Retrieve the user_id from SharedPreferences
        val userId = getUserIdFromLocalStorage()

        // Set the user_id in the TextView
        userIdTextView.text = "User ID: $userId"
    }

    private fun getUserIdFromLocalStorage(): String? {
        val sharedPref: SharedPreferences = getSharedPreferences("xerorat_user_id", Context.MODE_PRIVATE)
        return sharedPref.getString("user_id", null)
    }
}
