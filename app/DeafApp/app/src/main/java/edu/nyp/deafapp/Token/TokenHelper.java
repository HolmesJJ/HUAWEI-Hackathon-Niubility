package edu.nyp.deafapp.Token;

import android.content.Context;

import java.net.URLEncoder;

import edu.nyp.deafapp.HttpAsyncTask.Post;
import edu.nyp.deafapp.IPAddress;
import edu.nyp.deafapp.Interface.OnTaskCompleted;
import edu.nyp.deafapp.SharedPreferences.GetSetSharedPreferences;

public class TokenHelper {

    public static void refreshToken(Context context, OnTaskCompleted onTaskCompleted, int refreshTokenId) {
        String parameters = getParameters(context);
        Post refreshTokenTask = new Post(onTaskCompleted, refreshTokenId);
        refreshTokenTask.execute(IPAddress.getIpAddress() + "/api/refreshToken", parameters);
    }

    private static String getParameters(Context context) {
        String parameters = "";
        try {
            String token = GetSetSharedPreferences.getDefaults("token", context.getApplicationContext());
            String refreshToken = GetSetSharedPreferences.getDefaults("refresh_token", context.getApplicationContext());
            String firstParameter = "accessToken=" + URLEncoder.encode(token, "UTF-8");
            String secondParameter = "refreshToken=" + URLEncoder.encode(refreshToken, "UTF-8");
            String thirdParameter = "grant_type=" + URLEncoder.encode("refresh_token", "UTF-8");
            parameters = firstParameter + "&" + secondParameter + "&" + thirdParameter;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parameters;
    }
}
