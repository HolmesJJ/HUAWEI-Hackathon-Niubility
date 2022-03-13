package edu.nyp.deafapp.Encoder;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.util.ArrayList;

import edu.nyp.deafapp.Interface.OnTaskCompleted;

public class GenerateVideoBackup extends AsyncTask<ArrayList<Mat>, Void, String> {

    private static final String TAG = "GenerateVideoBackup";
    private static final String CAPTURE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeafApp/Capture";
    private static final String CAPTURE_VIDEO_DIR = CAPTURE_DIR + "/Video";
    private static final String CAPTURE_VIDEO_PATH = CAPTURE_VIDEO_DIR + "/video.mp4";

    private OnTaskCompleted listener;
    private int requestId;

    public GenerateVideoBackup(OnTaskCompleted listener, int requestId) {
        this.listener = listener;
        this.requestId = requestId;
    }

    public static String GENERATE(ArrayList<Mat> listFrames) {
        try {
            Log.d(TAG, "listFrames: " + listFrames.size());
            Log.d(TAG, "frame width: " + listFrames.get(0).width());
            Log.d(TAG, "frame height: " + listFrames.get(0).height());
            File mCaptureFile = new File(CAPTURE_VIDEO_PATH);
            EncoderConfig encoderConfig = new AvcEncoderConfig(mCaptureFile.getAbsolutePath(), 480, 480, 25, 16000000);
            FrameEncoder frameEncoder = new FrameEncoder(encoderConfig);
            frameEncoder.start();
            for (int i = 0; i < listFrames.size(); i++) {
                Bitmap bmp = Bitmap.createBitmap(listFrames.get(i).cols(), listFrames.get(i).rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(listFrames.get(i), bmp);
                frameEncoder.createFrame(bmp);
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
    protected String doInBackground(ArrayList<Mat>... listFrames) {
        return GENERATE(listFrames[0]);
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String response) {
        Log.d(TAG, response);
        listener.onTaskCompleted(response, requestId);
    }
}
