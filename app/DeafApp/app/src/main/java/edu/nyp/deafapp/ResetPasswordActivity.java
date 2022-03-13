package edu.nyp.deafapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import org.json.JSONObject;

import java.net.URLEncoder;

import edu.nyp.deafapp.CustomView.CustomToast;
import edu.nyp.deafapp.HttpAsyncTask.Post;
import edu.nyp.deafapp.Interface.OnTaskCompleted;
import edu.nyp.deafapp.SharedPreferences.GetSetSharedPreferences;
import edu.nyp.deafapp.Utils.RSAUtils;

public class ResetPasswordActivity extends AppCompatActivity implements OnTaskCompleted {

    private static final int POST_RESET_PASSWORD_ID = 1;
    private static final int POST_GET_EMAIL_VALIDATION_CODE_ID = 2;

    private ProgressBar resetPasswordProgress;
    private Toolbar mToolbar;
    private EditText mUsername;
    private EditText mNewPassword;
    private EditText mConfirmPassword;
    private EditText mValidationCode;
    private Button mValidationCodeButton;
    private Button mResetPasswordButton;

    private String username;
    private String newPassword;
    private String confirmPassword;
    private String validationCode;
    private String publicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        initView();

        if (GetSetSharedPreferences.getDefaults("username", getApplicationContext()) != null) {
            username = GetSetSharedPreferences.getDefaults("username", getApplicationContext());
            mUsername.setText(username);
        }

        if (GetSetSharedPreferences.getDefaults("publicKey", getApplicationContext()) != null) {
            publicKey = GetSetSharedPreferences.getDefaults("publicKey", getApplicationContext());
        }

        mValidationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = mUsername.getText().toString();
                if(username.equals("")) {
                    mUsername.setError("Please enter correct username");
                }
                else {
                    showProgress(true);
                    postGetEmailValidationCode();
                }
            }
        });

        mResetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isUsername = false;
                boolean isPassword = false;
                boolean isPasswordEqualConfirmPassword = false;
                boolean isValidationCode = false;

                username = mUsername.getText().toString();
                newPassword = mNewPassword.getText().toString();
                confirmPassword = mConfirmPassword.getText().toString();
                validationCode = mValidationCode.getText().toString();

                if(username.equals("")) {
                    mUsername.setError("Please enter correct username");
                }
                else {
                    isUsername = true;
                }

                if(newPassword.equals("")) {
                    mNewPassword.setError("Please enter correct password");
                }
                else if(newPassword.length() <= 8) {
                    mNewPassword.setError("The length of password must > 8");
                }
                else {
                    isPassword = true;
                }

                if(!newPassword.equals(confirmPassword)) {
                    mConfirmPassword.setError("Please enter correct confirm password");
                }
                else if(confirmPassword.length() <= 8) {
                    mConfirmPassword.setError("The length of confirm password must > 8");
                }
                else {
                    isPasswordEqualConfirmPassword = true;
                }

                if(validationCode.equals("")) {
                    mValidationCode.setError("Please enter correct validation code");
                }
                else {
                    isValidationCode = true;
                }

                if(isUsername && isPassword && isPasswordEqualConfirmPassword && isValidationCode) {
                    showProgress(true);
                    postResetPasswordData();
                }
            }
        });
    }

    private void initView() {
        resetPasswordProgress = (ProgressBar) findViewById(R.id.reset_password_progress);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.activity_reset_password);
        mUsername = (EditText) findViewById(R.id.username);
        mNewPassword = (EditText) findViewById(R.id.new_password);
        mConfirmPassword = (EditText) findViewById(R.id.confirm_password);
        mValidationCode = (EditText) findViewById(R.id.validation_code);
        mValidationCodeButton = (Button) findViewById(R.id.validation_code_button);
        mResetPasswordButton = (Button) findViewById(R.id.reset_password_button);
    }

    private void postGetEmailValidationCode(){
        String parameters = getEmailValidationCodeParameters();
        System.out.println("parameters: " + parameters);
        Post postLoginTask = new Post(ResetPasswordActivity.this, POST_GET_EMAIL_VALIDATION_CODE_ID);
        postLoginTask.execute(IPAddress.getIpAddress() + "/api/emailValidation", parameters);
    }

    private void postResetPasswordData(){
        String parameters = getResetPasswordParameters();
        System.out.println("parameters: " + parameters);
        Post postLoginTask = new Post(ResetPasswordActivity.this, POST_RESET_PASSWORD_ID);
        postLoginTask.execute(IPAddress.getIpAddress() + "/api/resetPassword", parameters);
    }

    private String getResetPasswordParameters() {
        String parameters = "";
        String usernameEncryption = "";
        String newPasswordEncryption = "";
        String validationCodeEncryption = "";
        try {
            usernameEncryption = RSAUtils.encryptByPublicKey(username, publicKey);
            newPasswordEncryption = RSAUtils.encryptByPublicKey(newPassword, publicKey);
            validationCodeEncryption = RSAUtils.encryptByPublicKey(validationCode, publicKey);
            String firstParameter = "username=" + URLEncoder.encode(usernameEncryption, "UTF-8");
            String secondParameter = "newPassword=" + URLEncoder.encode(newPasswordEncryption, "UTF-8");
            String thirdParameter = "validationCode=" + URLEncoder.encode(validationCodeEncryption, "UTF-8");
            parameters = firstParameter + "&" + secondParameter + "&" + thirdParameter;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parameters;
    }

    private String getEmailValidationCodeParameters() {
        String parameters = "";
        String usernameEncryption = "";
        try {
            usernameEncryption = RSAUtils.encryptByPublicKey(username, publicKey);
            parameters = "username=" + URLEncoder.encode(usernameEncryption, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parameters;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            resetPasswordProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            resetPasswordProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    resetPasswordProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            resetPasswordProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onTaskCompleted(String response, int requestId) {
        if(requestId == POST_RESET_PASSWORD_ID) {
            showProgress(false);
            try {
                JSONObject jsonObject = new JSONObject(response);
                String Status = jsonObject.getString("status");

                if(Status.equals("Succeeded")) {
                    CustomToast.show(ResetPasswordActivity.this, "Reset password successfully!");
                    finish();
                }
                else {
                    CustomToast.show(ResetPasswordActivity.this, "Reset password failed!");
                }
            } catch (Exception e) {
                CustomToast.show(ResetPasswordActivity.this, "Reset password failed!");
            }
        }
        if(requestId == POST_GET_EMAIL_VALIDATION_CODE_ID) {
            showProgress(false);
            try {
                JSONObject jsonObject = new JSONObject(response);
                String Status = jsonObject.getString("status");

                if(Status.equals("Succeeded")) {

                }
                else {
                    mUsername.setError("Please enter correct username");
                }
            } catch (Exception e) {
                e.printStackTrace();
                CustomToast.show(ResetPasswordActivity.this, "Server error");
            }
        }
    }
}
