package com.lu.luandroid.foodnotes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.lu.luandroid.foodnotes.config.ipconfig;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Login extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "Login";

    @Bind(R.id.login_goolge)
    SignInButton loginGoolge;

    private static final int RC_SIGN_IN = 100;

    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;

    private SharedPreferences storeAccount;
    private String strAccount = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        Log.i(TAG, "onCreate: 1");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        Log.i(TAG, "onCreate: 2");
        loginGoolge.setScopes(gso.getScopeArray());
        loginGoolge.setOnClickListener(view -> signIn());

        storeAccount = getSharedPreferences("login", 0);
        strAccount = storeAccount.getString("account", "");
        Log.i(TAG, "onCreate: " + strAccount);
        if (!"".equals(strAccount)) {
            Log.i(TAG, "onCreate: 已註冊過");
            startActivity(new Intent(Login.this, MainActivity.class));
            finish();
        }


    }


    private void signIn() {
        Log.i(TAG, "signIn: ");
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(intent, RC_SIGN_IN);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignResult(result);
        }
    }

    private void handleSignResult(GoogleSignInResult result) {
        hideDialog();
        if (result.isSuccess()) {
            GoogleSignInAccount acc = result.getSignInAccount();
            Log.i(TAG, "handleSignResult: " + acc.getEmail());
            String account = acc.getEmail();
            MemberLogin(account);
        } else {
            Toast.makeText(this, "登入錯誤",Toast.LENGTH_SHORT).show();
        }

    }

    private void MemberLogin(final String account) {
        showDialog();

        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("account", account)
                .build();
        Request request = new Request.Builder()
                .post(requestBody)
                .url(ipconfig.login)
                .build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Log.i(TAG, "onFailure: " + e.getMessage());
                MemberLogin(account);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String strResponse = response.body().string();
                Log.i(TAG, "onResponse: " + strResponse);
                Login.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideDialog();
                    }
                });

                int success;
                try {
                    JSONObject json = new JSONObject(strResponse);
                    success = json.getInt("success");
                    if (success == 1) {
                        startActivity(new Intent(Login.this, MainActivity.class));
                        storeAccount.edit().putString("account", account).apply();
                        finish();
                    } else if (success == 2) {
                        startActivity(new Intent(Login.this, MainActivity.class));
                        storeAccount.edit().putString("account", account).apply();
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void showDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("登入中");
            mProgressDialog.setCancelable(true);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();


    }

    private void hideDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideDialog();
    }
}
