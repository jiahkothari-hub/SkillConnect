package com.example.broadcastexp10;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    public static final String MY_CUSTOM_ACTION = "com.example.broadcastexp10.MY_CUSTOM_ACTION";

    TextView tvAirplaneStatus, tvLog;
    EditText etMessage;
    Button btnSend;
    AirplaneModeReceiver airplaneModeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvAirplaneStatus = findViewById(R.id.tvAirplaneStatus);
        tvLog = findViewById(R.id.tvLog);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        // show the current airplane mode state when the app opens
        boolean isOn = Settings.Global.getInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
        tvAirplaneStatus.setText(isOn ? "Airplane Mode: ON" : "Airplane Mode: OFF");

        airplaneModeReceiver = new AirplaneModeReceiver(tvAirplaneStatus, tvLog);

        // Broadcast sender: sends a custom broadcast to MyCustomReceiver
        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (message.isEmpty()) message = "Hello from Broadcast Sender";

            Intent intent = new Intent(this, MyCustomReceiver.class);
            intent.setAction(MY_CUSTOM_ACTION);
            intent.putExtra("message", message);
            sendBroadcast(intent);

            tvLog.append("Custom broadcast sent: " + message + "\n");
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Dynamic registration for the Airplane Mode system broadcast
        IntentFilter filter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(airplaneModeReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(airplaneModeReceiver);
    }
}
