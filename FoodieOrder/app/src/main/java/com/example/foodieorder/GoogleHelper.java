package com.example.foodieorder;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

// Google Sign-In + Firebase (used by Login screen and Firebase screen)
public class GoogleHelper {

    public static final int RC_GOOGLE = 100;

    public static void startSignIn(Activity a) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(a.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(a, gso);
        a.startActivityForResult(client.getSignInIntent(), RC_GOOGLE);
    }

    // call from onActivityResult. onSuccess runs after Firebase login works.
    public static void handleResult(Activity a, Intent data, Runnable onSuccess) {
        try {
            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    AppData.prefs(a).edit()
                            .putString("user_name", account.getDisplayName())
                            .putString("user_email", account.getEmail())
                            .apply();
                    Toast.makeText(a, "Signed in as " + account.getEmail(), Toast.LENGTH_SHORT).show();
                    onSuccess.run();
                } else {
                    Toast.makeText(a, "Firebase Google login failed", Toast.LENGTH_LONG).show();
                }
            });
        } catch (ApiException e) {
            Toast.makeText(a, "Google sign in failed (code " + e.getStatusCode()
                    + "). Add your google-services.json and SHA-1 in Firebase.", Toast.LENGTH_LONG).show();
        }
    }
}
