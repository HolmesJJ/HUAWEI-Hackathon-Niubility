package edu.nyp.deafapp.Encoder;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.opencv.android.Utils;

import java.io.File;
import java.util.ArrayList;

import edu.nyp.deafapp.Interface.OnTaskCompleted;
import edu.nyp.deafapp.Utils.BitmapUtils;

public class GenerateVideo extends AsyncTask<ArrayList<Bitmap>, Void, String> {

    private static final String TAG = "GenerateVideo";
    private static final String CAPTURE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeafApp/Capture";
    private static final String CAPTURE_VIDEO_DIR = CAPTURE_DIR + "/Video";
    private static final String CAPTURE_VIDEO_PATH = CAPTURE_VIDEO_DIR + "/video.mp4";

    private OnTaskCompleted listener;
    private int requestId;

    public GenerateVideo(OnTaskCompleted listener, int requestId) {
        this.listener = listener;
        this.requestId = requestId;
    }

    public static String GENERATE(ArrayList<Bitmap> frameList) {
        try {
            Log.d(TAG, "frameList: " + frameList.size());
            File mCaptureFile = new File(CAPTURE_VIDEO_PATH);
            EncoderConfig encoderConfig = new AvcEncoderConfig(mCaptureFile.getAbsolutePath(), 480, 640, 25, 16000000);
            FrameEncoder frameEncoder = new FrameEncoder(encoderConfig);
            frameEncoder.start();
            for (int i = 0; i < frameList.size(); i++) {
                frameEncoder.createFrame(frameList.get(i));
            }
            frameEncoder.release();
        } catch (Exception e) {
            e.fillInStackTrace();
            Log.d(TAG, "Failed: " + e.getMessage());
            return "Failed";
        }
        return "Succeeded";
    }


    @Override
    protected String doInBackground(ArrayList<Bitmap>... frameLists) {
        return GENERATE(frameLists[0]);
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String response) {
        Log.d(TAG, response);
        listener.onTaskCompleted(response, requestId);
    }
}
