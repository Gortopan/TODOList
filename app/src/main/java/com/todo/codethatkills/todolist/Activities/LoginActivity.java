package com.todo.codethatkills.todolist.Activities;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.todo.codethatkills.todolist.R;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    private static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;

    private String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
    private String mEmail; // Received from newChooseAccountIntent(); passed to getToken()
    private boolean isRecover;
    private ProgressBar pb;
    private Button authButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        authButton = (Button) findViewById(R.id.authButton);
        pb = (ProgressBar) findViewById(R.id.loginProgressBar);
        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickUserAccount();
            }
        });
        SharedPreferences myPrefs = this.getSharedPreferences(getString(R.string.token), Context.MODE_PRIVATE);
        String token = myPrefs.getString(getString(R.string.token), "");
        if (!token.equals("")){//check if user log in
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void pickUserAccount() {

        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            // Receiving a result from the AccountPicker
            if (resultCode == RESULT_OK) {
                mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                // With the account name acquired, go get the auth token
                getUsername();
            } else if (resultCode == RESULT_CANCELED) {
                // The account picker dialog closed without selecting an account.
                // Notify users that they must pick an account to proceed.
                Toast.makeText(this, getString(R.string.pick_an_account), Toast.LENGTH_SHORT).show();
            }
        } else if ((requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR)
                && resultCode == RESULT_OK) {
            // Receiving a result that follows a GoogleAuthException, try auth again
            getUsername();
        }


    }

    private void getUsername() {
        if (mEmail == null) {
            pickUserAccount();
        } else {
            if (isDeviceOnline()) {
                new GetUsernameTask(LoginActivity.this, mEmail, SCOPE).execute();
            } else {
                Toast.makeText(this, getString(R.string.you_are_offline), Toast.LENGTH_LONG).show();
            }
        }
    }

    public boolean isDeviceOnline(){
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public class GetUsernameTask extends AsyncTask<Void,Void,Void> {
        Activity activity;
        String accountName;
        String scope;
        String status;
        String token;

        GetUsernameTask(Activity activity, String name, String scope) {
            this.activity = activity;
            this.scope = scope;
            this.accountName = name;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isRecover = false;
            pb.setVisibility(View.VISIBLE);
            authButton.setEnabled(false);
        }

        @Override
        protected Void doInBackground(Void... params){
            token = fetchToken();
            if(token != null){
                //Access Token sent
                status = "Access Token Acquired";
            }
            return null;
        }



        public String fetchToken(){
            try{
                return GoogleAuthUtil.getToken(activity, accountName, scope);
            }
            catch(final UserRecoverableAuthException userRecoverableError){
                status = "User Recoverable Error";
                handleException(userRecoverableError);
            }
            catch(GoogleAuthException googleAuthException){
                status = "Google Auth Exception";
            }
            catch(IOException ioException) {
                status = "IO Exception";
            }
            return null;
        }


        protected void onPostExecute(Void result) {
            if (!isRecover){
                SharedPreferences prefs = getSharedPreferences(getString(R.string.token), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(getString(R.string.token), token);
                editor.commit();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
            authButton.setEnabled(true);
            pb.setVisibility(View.GONE);
        }
    }

    private void handleException(final Exception e){
        isRecover = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e instanceof GooglePlayServicesAvailabilityException) {
                    // The Google Play services APK is old, disabled, or not present.
                    // Show a dialog created by Google Play services that allows
                    // the user to update the APK
                    int statusCode = ((GooglePlayServicesAvailabilityException)e)
                            .getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
                            LoginActivity.this,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                    dialog.show();
                } else if (e instanceof UserRecoverableAuthException) {
                    // Unable to authenticate, such as when the user has not yet granted
                    // the app access to the account, but the user can fix this.
                    // Forward the user to an activity in Google Play services.
                    Intent intent = ((UserRecoverableAuthException)e).getIntent();
                    startActivityForResult(intent,
                            REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                }
            }
        });
    }
}
