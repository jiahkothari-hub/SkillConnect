package com.example.broadcastexp10;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

// Receives the custom broadcast sent by the app
// (registered statically in AndroidManifest.xml)
public class MyCustomReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra("message");
        Toast.makeText(context, "Custom Broadcast Received: " + message, Toast.LENGTH_LONG).show();
    }
}
