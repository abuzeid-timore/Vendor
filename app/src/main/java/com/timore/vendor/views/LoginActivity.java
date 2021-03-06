package com.timore.vendor.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.timore.vendor.R;
import com.timore.vendor.control.App;
import com.timore.vendor.control.NetworkLoading;
import com.timore.vendor.control.Retrofit;
import com.timore.vendor.control.SuperActivity;
import com.timore.vendor.control.VAR;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginActivity extends SuperActivity implements View.OnClickListener {

    TextView forgetPassTv;
    TextView signUpTv;
    Button submit;
    EditText userNameEt;
    EditText userPassEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        forgetPassTv = (TextView) findViewById(R.id.login_forgetPassword);
        signUpTv = (TextView) findViewById(R.id.login_signUp);
        submit = (Button) findViewById(R.id.login_submit);
        userNameEt = (EditText) findViewById(R.id.login_userName);
        userPassEt = (EditText) findViewById(R.id.login_userPassword);

//        super.setToolBar(findViewById(R.id.toolbar));
        submit.setOnClickListener(this);
        forgetPassTv.setOnClickListener(this);
        signUpTv.setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_submit:
                login();
                break;
            case R.id.login_forgetPassword:
                new ForgetPasswordDialog(this).show();
                break;

            case R.id.login_signUp:
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                break;
            default:
                System.err.println("ONCLICK DEFAULT CASE");
                break;
        }
    }

    private void login() {
        App.hideSoftInput(this);
        if (validInputs())
            completeLogin();
        else
            Snackbar.make(findViewById(R.id.main_layout), getString(R.string.checkInputs), Snackbar.LENGTH_SHORT).show();
    }

    private void completeLogin() {
        NetworkLoading.startLoading(this);
        Retrofit.getInstance().login(userNameEt.getText().toString().trim(),
                userPassEt.getText().toString().trim(),
                new Callback<JsonArray>() {
                    @Override
                    public void success(JsonArray s, Response response) {
                        System.err.println(s);
                        Retrofit.res(s + "", response);
                        NetworkLoading.stopLoading();

                        int id = 0;
                        JsonObject jsonObject = s.get(0).getAsJsonObject();
                        JsonElement temp = jsonObject.get("id");
                        if ((id = temp.getAsInt()) > 0) {
                            App.userId = id;

                            getSharedPreferences(VAR.PREF_NAME, 0).edit().putInt(VAR.KEY_USER_ID, id).commit();
                            Intent main = new Intent(LoginActivity.this, MainActivity.class);
                            main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(main);
                        } else {
                            Snackbar.make(findViewById(R.id.main_layout), getString(R.string.invalid_user), Snackbar.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        NetworkLoading.stopLoading();
                        Retrofit.failure(error);
                        if (!App.isConnected(LoginActivity.this))
                            Snackbar.make(findViewById(R.id.main_layout), getString(R.string.no_net), Snackbar.LENGTH_LONG).show();

                    }
                });

    }

    private boolean validInputs() {
        if (userNameEt.getText().toString().isEmpty()) {
            userNameEt.setError(getString(R.string.enter_field));
            return false;
        } else {
            userNameEt.setError(null);
        }
        if (userPassEt.getText().toString().isEmpty()) {
            userPassEt.setError(getString(R.string.enter_field));
            return false;
        } else {
            userPassEt.setError(null);
        }
        return true;
    }
}
