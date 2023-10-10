package com.arnabxero.xerorat

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class UserCredentials(val username: String, val password: String)

data class ApiResponse(val message: String, val user_id: String)

interface ApiService {
    @POST("api/createUser") // Replace with your actual API endpoint
    fun login(@Body credentials: UserCredentials): Call<ApiResponse>
}

class MainActivity : AppCompatActivity() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var createUserButton: Button
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        createUserButton = findViewById(R.id.createUserButton)

        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://xerorat-server.vercel.app/") // Replace with your base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        createUserButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Call the API with Retrofit
            val credentials = UserCredentials(username, password)
            val call = apiService.login(credentials)

            call.enqueue(object : Callback<ApiResponse> {
                override fun onResponse(
                    call: Call<ApiResponse>,
                    response: Response<ApiResponse>
                ) {
                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse != null) {
                            // Process the response
                            val userId = apiResponse.user_id
                            saveUserIdToLocalStorage(userId) // Save user_id to SharedPreferences
                            Toast.makeText(
                                this@MainActivity,
                                "Message: ${apiResponse.message}, User ID: $userId",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Start Page2Activity
                            val intent = Intent(this@MainActivity, Page2Activity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        // Handle the error
                        Toast.makeText(
                            this@MainActivity,
                            "Error: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    // Handle the network failure
                    t.printStackTrace()
                    Toast.makeText(
                        this@MainActivity,
                        "Failed to send data to server",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }

    private fun saveUserIdToLocalStorage(userId: String) {
        val sharedPref: SharedPreferences = getSharedPreferences("xerorat_user_id", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString("user_id", userId)
        editor.apply()
    }
}
