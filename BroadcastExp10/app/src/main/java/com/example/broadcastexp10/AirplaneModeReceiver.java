package com.example.broadcastexp10;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

// Receives the system broadcast sent when Airplane Mode is switched on or off
// (registered dynamically in MainActivity)
public class AirplaneModeReceiver extends BroadcastReceiver {

    TextView tvStatus, tvLog;

    public AirplaneModeReceiver(TextView tvStatus, TextView tvLog) {
        this.tvStatus = tvStatus;
        this.tvLog = tvLog;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
            boolean isOn = intent.getBooleanExtra("state", false);
            String text = isOn ? "Airplane Mode: ON" : "Airplane Mode: OFF";

            tvStatus.setText(text);
            tvLog.append(text + "\n");
            Toast.makeText(context, "Broadcast received - " + text, Toast.LENGTH_SHORT).show();
        }
    }
}
