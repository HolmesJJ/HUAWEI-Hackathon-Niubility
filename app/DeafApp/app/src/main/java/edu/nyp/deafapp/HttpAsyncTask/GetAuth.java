package edu.nyp.deafapp.HttpAsyncTask;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.nyp.deafapp.Interface.OnTaskCompleted;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */

public class GetAuth extends AsyncTask<String, Void, String> {

    private final static String TAG = "Get";
    private OnTaskCompleted listener;
    private int requestId;

    public GetAuth(OnTaskCompleted listener, int requestId) {
        this.listener = listener;
        this.requestId = requestId;
    }

    public static String GET(String urlString, String token) {
        String result = "";
        try {
            Log.d(TAG, "Sending url["+urlString+"]");
            // create HttpPost
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = null;
            try {
                urlConnection.setRequestMethod("GET");

                urlConnection.setRequestProperty("Authorization", "bearer " + token);

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                    // receive response as inputStream
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    result = convertInputStreamToString(inputStream);
                }
                else if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    result = "UNAUTHORIZED";
                }
                else if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                    result = "FORBIDDEN";
                }
                else {
                    result = "Did not work!";
                }
            } finally {
                if (inputStream!=null)
                    inputStream.close();
                if (urlConnection!=null)
                    urlConnection.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream)throws IOException {
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

    // doInBackground execute tasks when asynctask is run
    @Override
    protected String doInBackground(String... urls) {
        return GET(urls[0], urls[1]);
    }
    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String response) {
        Log.d(TAG, response);
        listener.onTaskCompleted(response, requestId);
    }
}
