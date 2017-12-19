package com.repoapps.assignment1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "GoogleSigninTest";

    private TextView tvStatus;
    private Button btnLogin;
    private ProgressBar pbrWait;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = findViewById(R.id.lbl_status);

        btnLogin = findViewById(R.id.btnLogin);

        pbrWait = findViewById(R.id.pbrWait);

        btnLogin.setOnClickListener(btnSignInClickListener);

    }

    final View.OnClickListener btnSignInClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            signInwithGoogle();
            pbrWait.setVisibility(View.VISIBLE);
        }
    };


    final GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.d(TAG, connectionResult.getErrorMessage());
        }
    };

    protected void signInwithGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, connectionFailedListener)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount acct = result.getSignInAccount();
                String personName = acct.getDisplayName();
                String personEmail = acct.getEmail();
                String personId = acct.getId();
                Log.d(TAG, personName);
                Log.d(TAG, personEmail);
                Log.d(TAG, personId);

                firebaseAuthWithGoogle(acct);
            } else {
                Toast.makeText(getApplicationContext(), "There was a trouble signing in-Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }


    final OnCompleteListener signinCompleteListener = new OnCompleteListener<AuthResult>() {

        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if (!task.isSuccessful()) {
                updateUI(null);
            } else {
                FirebaseUser user = mAuth.getCurrentUser();
                updateUI(user);
            }
        }
    };

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        mAuth = FirebaseAuth.getInstance();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(signinCompleteListener);

    }

    private void updateUI(FirebaseUser user) {
        pbrWait.setVisibility(View.INVISIBLE);

        if (user == null) {
            tvStatus.setText(R.string.str_loginfail);

            btnLogin.setEnabled(true);
        } else {
            tvStatus.setText(getString(R.string.str_loginsuccess) + user.getDisplayName());
            btnLogin.setEnabled(false);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(MainActivity.this,
                            LocalPlacesActivity.class));
                    finish();
                }
            },2000);
        }

    }

    private void signOut() {
        mAuth.signOut();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        signOut();
        super.onDestroy();
    }
}
