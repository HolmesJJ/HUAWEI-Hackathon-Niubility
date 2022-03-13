package edu.nyp.deafapp.Fragment.Exercise;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.coremedia.iso.boxes.Container;
import com.github.piasy.rxandroidaudio.AudioRecorder;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import org.json.JSONObject;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import edu.nyp.deafapp.CustomView.CustomToast;
import edu.nyp.deafapp.Encoder.GenerateVideoBackup;
import edu.nyp.deafapp.Fragment.Study.StudyFragment;
import edu.nyp.deafapp.IPAddress;
import edu.nyp.deafapp.Interface.OnTaskCompleted;
import edu.nyp.deafapp.R;
import edu.nyp.deafapp.SharedPreferences.GetSetSharedPreferences;
import me.yokeyword.fragmentation.SupportFragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ExerciseFragmentBackup extends SupportFragment implements CameraBridgeViewBase.CvCameraViewListener2, OnTaskCompleted {

    private static final String CAPTURE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeafApp/Capture";
    private static final String CAPTURE_VIDEO_DIR = CAPTURE_DIR + "/Video";
    private static final String CAPTURE_AUDIO_DIR = CAPTURE_DIR + "/Audio";
    private static final String CAPTURE_COMBINE_DIR = CAPTURE_DIR + "/Combine";
    private static final String CAPTURE_VIDEO_PATH = CAPTURE_VIDEO_DIR + "/video.mp4";
    private static final String CAPTURE_AUDIO_PATH = CAPTURE_AUDIO_DIR + "/audio.mp4";
    private static final String CAPTURE_COMBINE_PATH = CAPTURE_COMBINE_DIR + "/combine.mp4";

    private static final int GENERATE_VIDEO_ID = 1;

    private static final String ENGLISH_ID = "englishId";
    private static final String CONTENT = "content";

    private Toolbar mToolbar;
    private ProgressBar mProgressView;
    private ImageButton mCaptureBtn;
    private Button mAnalyzeBtn;
    private ImageView mMinions;
    private VideoView captureVideo;
    private RelativeLayout mCameraContainer;
    private JavaCameraView mJavaCameraView;
    private RelativeLayout recordContainer;
    private ImageView ivVolume;

    private Mat frame;
    private Scalar MOUTH_COLOR = new Scalar(0, 255, 0);
    private Point mouthTL;
    private Point mouthBR;
    private Rect croppedRect;

    private int userId;
    private int englishId;
    private String content;
    private boolean isRecording = false;
    private double containerWidth;
    private double containerHeight;
    private ArrayList<Mat> listFrames = new ArrayList<>();

    private AudioRecorder mAudioRecorder;

    private OkHttpClient client;

    private Handler mHandler = new Handler();

    public static ExerciseFragmentBackup newInstance(int englishId, String content) {
        ExerciseFragmentBackup fragment = new ExerciseFragmentBackup();
        Bundle args = new Bundle();
        args.putInt(ENGLISH_ID, englishId);
        args.putString(CONTENT, content);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise_backup, container, false);

        userId = Integer.valueOf(GetSetSharedPreferences.getDefaults("userId", _mActivity.getApplicationContext()));

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            englishId = bundle.getInt(ENGLISH_ID, 0);
            content = bundle.getString(CONTENT);
        }

        initView(view);
        return view;
    }

    private void initView(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mToolbar.setTitle(content + " " + getString(R.string.fragment_exercise));
        mProgressView = (ProgressBar) view.findViewById(R.id.main_loading_progress);
        mCameraContainer = (RelativeLayout) view.findViewById(R.id.camera_container);
        ViewTreeObserver observer = mCameraContainer.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                // 获取容器大小
                containerWidth = mCameraContainer.getWidth();
                containerHeight = mCameraContainer.getHeight();
                return true;
            }
        });
        mJavaCameraView = (JavaCameraView) view.findViewById(R.id.mouth_capture_camera);
        mCaptureBtn = (ImageButton) view.findViewById(R.id.capture_btn);
        mMinions = (ImageView) view.findViewById(R.id.minions);
        captureVideo = (VideoView) view.findViewById(R.id.capture_video);
        mAnalyzeBtn = (Button) view.findViewById(R.id.analyze_btn);
        recordContainer = (RelativeLayout) view.findViewById(R.id.record_container);
        ivVolume = (ImageView) view.findViewById(R.id.iv_volume);
    }

    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        super.onEnterAnimationEnd(savedInstanceState);
        initDelayView();
    }

    private void initDelayView() {
        mAnalyzeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (captureVideo != null) {
                    captureVideo.stopPlayback();
                    captureVideo.setOnCompletionListener(null);
                    captureVideo.setVisibility(View.GONE);
                }
                mMinions.setVisibility(View.VISIBLE);
                mCaptureBtn.setEnabled(false);
                mAnalyzeBtn.setEnabled(false);
                showProgress(true);
                analyzeVideo();
            }
        });
        mCaptureBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    mCaptureBtn.setImageResource(R.drawable.ic_capture_on);
                    listFrames.clear();
                    if (captureVideo != null) {
                        captureVideo.stopPlayback();
                        captureVideo.setOnCompletionListener(null);
                        captureVideo.setVisibility(View.GONE);
                    }
                    mMinions.setVisibility(View.VISIBLE);
                    mAnalyzeBtn.setEnabled(false);
                    mAudioRecorder = AudioRecorder.getInstance();
                    File mAudioFile = new File(CAPTURE_AUDIO_PATH);
                    mAudioRecorder.prepareRecord(
                            MediaRecorder.AudioSource.MIC,
                            MediaRecorder.OutputFormat.MPEG_4,
                            MediaRecorder.AudioEncoder.AAC,
                            mAudioFile);
                    isRecording = true;
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    mCaptureBtn.setImageResource(R.drawable.ic_capture_off);
                    mCaptureBtn.setEnabled(false);
                    isRecording = false;
                    showProgress(true);
                    generateVideo();
                }
                return false;
            }
        });
        startCamera();
    }

    private void startCamera() {
        mJavaCameraView.setVisibility(SurfaceView.VISIBLE);
        mJavaCameraView.setCvCameraViewListener(ExerciseFragmentBackup.this);
        mJavaCameraView.setCameraIndex(1);
        mJavaCameraView.enableView();
    }

    private void stopCamera() {
        if(mJavaCameraView != null) {
            mJavaCameraView.disableView();
            mJavaCameraView.setVisibility(SurfaceView.GONE);
            mJavaCameraView = null;
        }
    }

    public boolean combineVideo(String videoPath, String audioPath, String outputPath) {
        Movie video;
        Movie audio;
        try {
            video = new MovieCreator().build(videoPath);
            audio = new MovieCreator().build(audioPath);

            Track audioTrack = audio.getTracks().get(0);
            video.addTrack(audioTrack);

            Container out = new DefaultMp4Builder().build(video);
            FileOutputStream fos = new FileOutputStream(outputPath);
            out.writeContainer(fos.getChannel());
            fos.close();
            return true;
        } catch (Exception e) {
            e.fillInStackTrace();
            return false;
        }
    }

    private void generateVideo() {
        if (listFrames.size() > 0) {
            GenerateVideoBackup generateVideoBackupTask = new GenerateVideoBackup(ExerciseFragmentBackup.this, GENERATE_VIDEO_ID);
            generateVideoBackupTask.execute(listFrames);
        }
    }

    private void analyzeVideo(){
        File file = new File(CAPTURE_COMBINE_PATH);
        String file_path = file.getAbsolutePath();

        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        RequestBody file_body = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        RequestBody request_body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("userId", String.valueOf(userId))
                .addFormDataPart("englishId", String.valueOf(englishId))
                .addFormDataPart("captureFile", file_path.substring(file_path.lastIndexOf("/")+1), file_body)
                .build();

        Request request = new Request.Builder()
                .url(IPAddress.getIpAddress() + "/api/analyze")
                .post(request_body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                _mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                System.out.println("onResponse: " + response);
                int statusCode = response.code();
                String result = response.body().string();
                _mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(statusCode == HttpURLConnection.HTTP_OK) {
                            try {
                                JSONObject jsonObject = new JSONObject(result);
                                String Status = jsonObject.getString("status");
                                CustomToast.show(_mActivity, Status);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            CustomToast.show(_mActivity, result);
                        }
                        mCaptureBtn.setEnabled(true);
                        mAnalyzeBtn.setEnabled(true);
                        playVideo();
                        showProgress(false);
                    }
                });
            }
        });
    }

    private void goToNextFragment() {
        stopCamera();
        startWithPop(StudyFragment.newInstance(englishId, content));
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        frame = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        frame.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        frame = inputFrame.rgba();

        // Resize 图像
        double originFrameWidth = frame.width();
        double originFrameHeight = frame.height();
        double newHeight = originFrameWidth / originFrameHeight * containerWidth;
        double newWidth = originFrameHeight / originFrameWidth * containerHeight;
        if(this.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            if(newWidth >= containerWidth) {
                newWidth = containerWidth;
            }
            else {
                newHeight = containerHeight;
            }
            Size sz = new Size(newHeight, newWidth);
            Imgproc.resize(frame, frame, sz);
            Core.rotate(frame, frame, Core.ROTATE_90_COUNTERCLOCKWISE);
            // 前置摄像头会镜像颠倒
            Core.flip(frame, frame, 1);
        }
        process(frame);
        return frame;
    }

    private void process(Mat getFrame) {
        if (mouthTL == null) {
            mouthTL = new Point((getFrame.width() - 480 - 4) / 2, (getFrame.height() - 480 - 4) / 3);
        }
        if (mouthBR == null) {
            mouthBR = new Point(getFrame.width() - (getFrame.width() - 480 - 4) / 2, getFrame.height() - (getFrame.height() - 480 - 4) / 3 * 2);
        }
        if (croppedRect == null) {
            croppedRect = new Rect((int)mouthTL.x + 2, (int)mouthTL.y + 2, (int)(mouthBR.x - mouthTL.x - 4), (int)(mouthBR.y - mouthTL.y - 4));
        }
        Imgproc.rectangle(getFrame, mouthTL, mouthBR, MOUTH_COLOR, 2, 8, 0);

        if (isRecording) {
            if (mAudioRecorder != null && !mAudioRecorder.isStarted()) {
                mAudioRecorder.startRecord();
                mHandler.post(mPollTask);
                _mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recordContainer.setVisibility(View.VISIBLE);
                    }
                });
            }
            Mat croppedFrame = new Mat(getFrame, croppedRect);
            listFrames.add(croppedFrame);
        } else {
            if(mAudioRecorder != null && mAudioRecorder.isStarted()) {
                _mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recordContainer.setVisibility(View.GONE);
                    }
                });
                mAudioRecorder.stopRecord();
                mHandler.removeCallbacks(mPollTask);
            }
        }
    }

    private void playVideo() {
        mMinions.setVisibility(View.GONE);
        captureVideo.setVisibility(View.VISIBLE);
        captureVideo.setVideoURI(Uri.parse(CAPTURE_COMBINE_PATH));
        captureVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mPlayer) {
                mPlayer.start();
                mPlayer.setLooping(true);
            }
        });
        MediaController mediaController = new MediaController(_mActivity);
        mediaController.setAnchorView(captureVideo);
        mediaController.setMediaPlayer(captureVideo);
        captureVideo.setMediaController(mediaController);
        captureVideo.start();
    }

    // 更新音量图
    private void updateVolume(int volume) {
        System.out.println("=====volume====:" + volume);
        switch (volume) {
            case 1:
                ivVolume.setImageResource(R.drawable.p1);
                break;
            case 2:
                ivVolume.setImageResource(R.drawable.p2);
                break;
            case 3:
                ivVolume.setImageResource(R.drawable.p3);
                break;
            case 4:
                ivVolume.setImageResource(R.drawable.p4);
                break;
            case 5:
                ivVolume.setImageResource(R.drawable.p5);
                break;
            case 6:
                ivVolume.setImageResource(R.drawable.p6);
                break;
            case 7:
                ivVolume.setImageResource(R.drawable.p7);
                break;
            default:
                break;
        }
    }

    Runnable mPollTask = new Runnable() {
        @Override
        public void run() {
            updateVolume(getVolume());
            mHandler.postDelayed(mPollTask, 100);
        }
    };

    // 获取音量值，只是针对录音音量
    public int getVolume() {
        int volume = 0;
        // 录音
        if (mAudioRecorder != null) {
            volume = mAudioRecorder.getMaxAmplitude() / 650;
            if (volume != 0) {
                volume = (int) (10 * Math.log10(volume)) / 3;
            }
        }
        return volume;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mJavaCameraView != null) {
            mJavaCameraView.setCameraIndex(1);
            mJavaCameraView.enableView();
        }
    }

    @Override
    public void onPause() {
        if(mJavaCameraView != null) {
            mJavaCameraView.disableView();
        }
        if (captureVideo != null) {
            captureVideo.stopPlayback();
            captureVideo.setOnCompletionListener(null);
            captureVideo.setVisibility(View.GONE);
        }
        mMinions.setVisibility(View.VISIBLE);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        stopCamera();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        super.onDestroyView();
    }

    @Override
    public boolean onBackPressedSupport() {
        goToNextFragment();
        return true;
    }

    @Override
    public void onTaskCompleted(String response, int requestId) {
        if (requestId == GENERATE_VIDEO_ID) {
            listFrames.clear();
            CustomToast.show(_mActivity, response);
            showProgress(false);
            mAnalyzeBtn.setEnabled(true);
            mCaptureBtn.setEnabled(true);
            boolean isCombined = combineVideo(CAPTURE_VIDEO_PATH, CAPTURE_AUDIO_PATH, CAPTURE_COMBINE_PATH);
            if (isCombined) {
                _mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CustomToast.show(_mActivity, "Combined Success");
                    }
                });
            } else {
                _mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CustomToast.show(_mActivity, "Combined Failed");
                    }
                });
            }
            playVideo();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
