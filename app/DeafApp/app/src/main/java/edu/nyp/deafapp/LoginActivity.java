package edu.nyp.deafapp;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.UUID;

import edu.nyp.deafapp.CustomView.CustomToast;
import edu.nyp.deafapp.Demo.DemoMode;
import edu.nyp.deafapp.HttpAsyncTask.Get;
import edu.nyp.deafapp.HttpAsyncTask.Post;
import edu.nyp.deafapp.Interface.OnTaskCompleted;
import edu.nyp.deafapp.SharedPreferences.GetSetSharedPreferences;
import edu.nyp.deafapp.Utils.RSAUtils;

public class LoginActivity extends AppCompatActivity implements OnTaskCompleted {

    private static final int POST_DEVICE_ID = 1;
    private static final int POST_LOGIN_ID = 2;
    private static final int POST_GET_EMAIL_VALIDATION_CODE_ID = 3;
    private static final int POST_LOGIN_WITH_VALIDATION_CODE_ID = 4;
    private static final int POST_GET_PUBLIC_KEY_ID = 5;
    private static final int PERMISSION_ALL = 100;
    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private LinearLayout emailValidationForm;
    private ProgressBar loginProgress;
    private Toolbar mToolbar;
    private EditText mUsername;
    private EditText mPassword;
    private EditText mIpAddress;
    private EditText mValidationCode;
    private Button mIpAddressButton;
    private Button mSignInButton;
    private Button mValidationCodeButton;
    private Button mOtherLoginMethodButton;
    private Button mForgetPasswordButton;
    private Button mDemoStartButton;
    private Button mDemoCloseButton;

    private String username;
    private String password;
    private String ipAddress;
    private String validationCode;
    private boolean isNewDevice = true;
    private String deviceUuid;
    private String publicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();

        if(GetSetSharedPreferences.getDefaults("deviceUuid", getApplicationContext()) != null) {
            deviceUuid = GetSetSharedPreferences.getDefaults("deviceUuid", getApplicationContext());
        }

        if (!hasPermissions(LoginActivity.this, PERMISSIONS)) {
            LoginActivity.this.requestPermissions(PERMISSIONS, PERMISSION_ALL);
        }

        if (GetSetSharedPreferences.getDefaults("ipAddress", getApplicationContext()) != null) {
            ipAddress = GetSetSharedPreferences.getDefaults("ipAddress", getApplicationContext());
            IPAddress.setIpAddress(ipAddress);
            mIpAddress.setText(ipAddress);
            showProgress(true);
            getPublicKey();
        } else {
            ipAddress = IPAddress.getIpAddress();
            mIpAddress.setText(ipAddress);
            mOtherLoginMethodButton.setEnabled(false);
            mForgetPasswordButton.setEnabled(false);
        }

        if (GetSetSharedPreferences.getDefaults("userId", getApplicationContext()) != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        if (GetSetSharedPreferences.getDefaults("username", getApplicationContext()) != null) {
            username = GetSetSharedPreferences.getDefaults("username", getApplicationContext());
            mUsername.setText(username);
        }

        if (GetSetSharedPreferences.getDefaults("publicKey", getApplicationContext()) != null) {
            publicKey = GetSetSharedPreferences.getDefaults("publicKey", getApplicationContext());
        }

        mIpAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ipAddress = mIpAddress.getText().toString();
                GetSetSharedPreferences.setDefaults("ipAddress", ipAddress, getApplicationContext());
                IPAddress.setIpAddress(ipAddress);

                showProgress(true);
                getPublicKey();
            }
        });

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deviceUuid = UUID.randomUUID().toString();
                GetSetSharedPreferences.setDefaults("deviceUuid", deviceUuid, getApplicationContext());
                System.out.println("deviceUuid: " + deviceUuid);

                boolean isUsername = false;
                boolean isPassword = false;
                boolean isValidationCode = false;

                username = mUsername.getText().toString();
                password = mPassword.getText().toString();

                if(username.equals("")) {
                    mUsername.setError("Please enter correct username");
                }
                else {
                    isUsername = true;
                }

                if(password.equals("")) {
                    mPassword.setError("Please enter correct password");
                }
                else if(password.length() <= 8) {
                    mPassword.setError("The length of password must > 8");
                }
                else {
                    isPassword = true;
                }

                if(isNewDevice) {
                    validationCode = mValidationCode.getText().toString();
                    if(validationCode.equals("")) {
                        mValidationCode.setError("Please enter correct validation code");
                    }
                    else {
                        isValidationCode = true;
                    }

                    if(isUsername && isPassword && isValidationCode) {
                        showProgress(true);
                        postLoginDataWithValidationCode();
                    }
                }
                else {
                    if(isUsername && isPassword) {
                        showProgress(true);
                        postLoginData();
                    }
                }
            }
        });

        mDemoStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DemoMode.setDemoMode(true);
                System.out.println("Demo Mode: " + DemoMode.isDemoMode());
            }
        });

        mDemoCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DemoMode.setDemoMode(false);
                System.out.println("Demo Mode: " + DemoMode.isDemoMode());
            }
        });

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

        mOtherLoginMethodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, FaceRecognitionActivity.class);
                startActivity(intent);
            }
        });

        mForgetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomToast.show(LoginActivity.this, "Forget Password");
            }
        });
    }

    private void initView() {
        emailValidationForm = (LinearLayout) findViewById(R.id.email_validation_form);
        loginProgress = (ProgressBar) findViewById(R.id.loginProgress);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.activity_login);
        mUsername = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);
        mIpAddress = (EditText) findViewById(R.id.ip_address);
        mValidationCode = (EditText) findViewById(R.id.validation_code);
        mIpAddressButton = (Button) findViewById(R.id.ip_address_button);
        mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mDemoStartButton = (Button) findViewById(R.id.demo_start_button);
        mDemoCloseButton = (Button) findViewById(R.id.demo_close_button);
        mValidationCodeButton = (Button) findViewById(R.id.validation_code_button);
        mOtherLoginMethodButton = (Button) findViewById(R.id.other_login_method_button);
        mForgetPasswordButton = (Button) findViewById(R.id.forget_password_button);
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == PERMISSION_ALL) {
            if (grantResults.length > 0) {
                int j = 0;
                for(int i=0; i<grantResults.length; i++) {
                    if(grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        j++;
                        break;
                    }
                }
                if(j != 0) {
                    CustomToast.show(LoginActivity.this, "Please turn on your permissions manually");
                }
            }
            else {
                CustomToast.show(LoginActivity.this, "Please turn on your permissions manually");
            }
        }
    }

    private void postDeviceId(){
        String parameters = getDeviceIdParameters();
        System.out.println("parameters: " + parameters);
        Post postLoginTask = new Post(LoginActivity.this, POST_DEVICE_ID);
        postLoginTask.execute(IPAddress.getIpAddress() + "/api/deviceIdValidation", parameters);
    }

    private void getPublicKey(){
        Get getPublicKeyTask = new Get(LoginActivity.this, POST_GET_PUBLIC_KEY_ID);
        getPublicKeyTask.execute(IPAddress.getIpAddress() + "/api/getPublicKey");
    }

    private void postLoginData(){
        String parameters = getLoginParameters();
        System.out.println("parameters: " + parameters);
        Post postLoginTask = new Post(LoginActivity.this, POST_LOGIN_ID);
        postLoginTask.execute(IPAddress.getIpAddress() + "/api/login", parameters);
    }

    private void postLoginDataWithValidationCode(){
        String parameters = getLoginWithValidationCodeParameters();
        System.out.println("parameters: " + parameters);
        Post postLoginTask = new Post(LoginActivity.this, POST_LOGIN_WITH_VALIDATION_CODE_ID);
        postLoginTask.execute(IPAddress.getIpAddress() + "/api/loginWithValidationCode", parameters);
    }

    private void postGetEmailValidationCode(){
        String parameters = getEmailValidationCodeParameters();
        System.out.println("parameters: " + parameters);
        Post postLoginTask = new Post(LoginActivity.this, POST_GET_EMAIL_VALIDATION_CODE_ID);
        postLoginTask.execute(IPAddress.getIpAddress() + "/api/emailValidation", parameters);
    }

    private String getDeviceIdParameters() {
        String parameters = "";
        String deviceUuidEncryption = "";
        try {
            deviceUuidEncryption = RSAUtils.encryptByPublicKey(deviceUuid, publicKey);
            parameters = "deviceId=" + URLEncoder.encode(deviceUuidEncryption, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parameters;
    }

    private String getLoginParameters() {
        String parameters = "";
        String usernameEncryption = "";
        String passwordEncryption = "";
        String deviceUuidEncryption = "";
        try {
            usernameEncryption = RSAUtils.encryptByPublicKey(username, publicKey);
            passwordEncryption = RSAUtils.encryptByPublicKey(password, publicKey);
            deviceUuidEncryption = RSAUtils.encryptByPublicKey(deviceUuid, publicKey);
            String firstParameter = "username=" + URLEncoder.encode(usernameEncryption, "UTF-8");
            String secondParameter = "password=" + URLEncoder.encode(passwordEncryption, "UTF-8");
            String thirdParameter = "deviceId=" + URLEncoder.encode(deviceUuidEncryption, "UTF-8");
            String fourthParameter = "grant_type=" + URLEncoder.encode("password", "UTF-8");
            parameters = firstParameter + "&" + secondParameter + "&" + thirdParameter + "&" + fourthParameter;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parameters;
    }

    private String getLoginWithValidationCodeParameters() {
        String parameters = "";
        String usernameEncryption = "";
        String passwordEncryption = "";
        String validationCodeEncryption = "";
        String deviceUuidEncryption = "";
        try {
            usernameEncryption = RSAUtils.encryptByPublicKey(username, publicKey);
            passwordEncryption = RSAUtils.encryptByPublicKey(password, publicKey);
            validationCodeEncryption = RSAUtils.encryptByPublicKey(validationCode, publicKey);
            deviceUuidEncryption = RSAUtils.encryptByPublicKey(deviceUuid, publicKey);
            String firstParameter = "username=" + URLEncoder.encode(usernameEncryption, "UTF-8");
            String secondParameter = "password=" + URLEncoder.encode(passwordEncryption, "UTF-8");
            String thirdParameter = "validationCode=" + URLEncoder.encode(validationCodeEncryption, "UTF-8");
            String fourthParameter = "deviceId=" + URLEncoder.encode(deviceUuidEncryption, "UTF-8");
            String fifthParameter = "grant_type=" + URLEncoder.encode("password", "UTF-8");
            parameters = firstParameter + "&" + secondParameter + "&" + thirdParameter + "&" + fourthParameter + "&" + fifthParameter;
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

            loginProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            loginProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loginProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            loginProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onTaskCompleted(String response, int requestId) {
        if(requestId == POST_GET_PUBLIC_KEY_ID) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                String Status = jsonObject.getString("status");

                if(Status.equals("Succeeded")) {
                    publicKey = jsonObject.getString("publicKey");
                    System.out.println("publicKey: " + publicKey);
                    GetSetSharedPreferences.setDefaults("publicKey", publicKey, getApplicationContext());

                    mOtherLoginMethodButton.setEnabled(true);
                    mForgetPasswordButton.setEnabled(true);

                    if(deviceUuid != null) {
                        postDeviceId();
                    }
                    else {
                        mOtherLoginMethodButton.setEnabled(false);
                        emailValidationForm.setVisibility(View.VISIBLE);
                        showProgress(false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                CustomToast.show(LoginActivity.this, "Server error");
            }
        }
        if(requestId == POST_DEVICE_ID) {
            showProgress(false);
            try {
                JSONObject jsonObject = new JSONObject(response);
                String Status = jsonObject.getString("status");

                if(Status.equals("Succeeded")) {
                    isNewDevice = false;
                }
                else {
                    isNewDevice = true;
                    mOtherLoginMethodButton.setEnabled(false);
                    emailValidationForm.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                isNewDevice = true;
                mOtherLoginMethodButton.setEnabled(false);
                emailValidationForm.setVisibility(View.VISIBLE);
            }
        }
        if(requestId == POST_GET_EMAIL_VALIDATION_CODE_ID) {
            showProgress(false);
            try {
                JSONObject jsonObject = new JSONObject(response);
                String Status = jsonObject.getString("status");

                if(Status.equals("Succeeded")) {
                    CustomToast.show(LoginActivity.this, "Succeeded");
                } else if (Status.equals("Email Empty")) {
                    CustomToast.show(LoginActivity.this, "Email Empty");
                } else if (Status.equals("Internal Server Error")) {
                    CustomToast.show(LoginActivity.this, "Internal Server Error");
                } else {
                    mUsername.setError("Please enter correct username");
                }
            } catch (Exception e) {
                e.printStackTrace();
                CustomToast.show(LoginActivity.this, "Internal Server Error");
            }
        }
        if(requestId == POST_LOGIN_ID) {
            showProgress(false);
            try {
                JSONObject jsonObject = new JSONObject(response);
                String Status = jsonObject.getString("status");

                if(Status.equals("Succeeded")) {
                    int UserId = jsonObject.getInt("userId");
                    String Token = jsonObject.getString("token");
                    String RefreshToken = jsonObject.getString("refresh_token");

                    GetSetSharedPreferences.setDefaults("username", username, getApplicationContext());
                    GetSetSharedPreferences.setDefaults("userId", String.valueOf(UserId), getApplicationContext());
                    GetSetSharedPreferences.setDefaults("token", Token, getApplicationContext());
                    GetSetSharedPreferences.setDefaults("refresh_token", RefreshToken, getApplicationContext());

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                else {
                    String Error = jsonObject.getString("error");
                    if(Error.equals("Wrong")) {
                        CustomToast.show(LoginActivity.this, "Login failed");
                    }
                    else {
                        CustomToast.show(LoginActivity.this, "Your account has been frozen");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                CustomToast.show(LoginActivity.this, "Server error");
            }
        }
        if(requestId == POST_LOGIN_WITH_VALIDATION_CODE_ID) {
            showProgress(false);
            try {
                JSONObject jsonObject = new JSONObject(response);
                String Status = jsonObject.getString("status");

                if(Status.equals("Succeeded")) {
                    int UserId = jsonObject.getInt("userId");
                    String Token = jsonObject.getString("token");
                    String RefreshToken = jsonObject.getString("refresh_token");

                    GetSetSharedPreferences.setDefaults("username", username, getApplicationContext());
                    GetSetSharedPreferences.setDefaults("userId", String.valueOf(UserId), getApplicationContext());
                    GetSetSharedPreferences.setDefaults("token", Token, getApplicationContext());
                    GetSetSharedPreferences.setDefaults("refresh_token", RefreshToken, getApplicationContext());

                    Intent intent = new Intent(LoginActivity.this, FaceRecognitionActivity.class);
                    startActivity(intent);
                }
                else {
                    String Error = jsonObject.getString("error");
                    if(Error.equals("Wrong")) {
                        CustomToast.show(LoginActivity.this, "Login failed");
                    }
                    else {
                        CustomToast.show(LoginActivity.this, "Your account has been frozen");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                CustomToast.show(LoginActivity.this, "Server error");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (GetSetSharedPreferences.getDefaults("username", getApplicationContext()) != null) {
            username = GetSetSharedPreferences.getDefaults("username", getApplicationContext());
            mUsername.setText(username);
        }

        if(deviceUuid != null) {
            showProgress(true);
            postDeviceId();
        }
        else {
            mOtherLoginMethodButton.setEnabled(false);
            emailValidationForm.setVisibility(View.VISIBLE);
        }
    }
}
