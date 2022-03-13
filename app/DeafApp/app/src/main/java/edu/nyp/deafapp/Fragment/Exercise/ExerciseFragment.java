package edu.nyp.deafapp.Fragment.Exercise;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.nyp.deafapp.Camera.CameraOverlap;
import edu.nyp.deafapp.CustomView.CustomToast;
import edu.nyp.deafapp.Encoder.GenerateVideo;
import edu.nyp.deafapp.Fragment.Study.StudyFragment;
import edu.nyp.deafapp.HyperLandmark.EGLUtils;
import edu.nyp.deafapp.HyperLandmark.GLBitmap;
import edu.nyp.deafapp.HyperLandmark.GLFrame;
import edu.nyp.deafapp.HyperLandmark.GLFramebuffer;
import edu.nyp.deafapp.HyperLandmark.GLPoints;
import edu.nyp.deafapp.IPAddress;
import edu.nyp.deafapp.Interface.OnTaskCompleted;
import edu.nyp.deafapp.R;
import edu.nyp.deafapp.SharedPreferences.GetSetSharedPreferences;
import edu.nyp.deafapp.Thread.CustomThreadPool;
import edu.nyp.deafapp.Utils.BitmapUtils;
import me.yokeyword.fragmentation.SupportFragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.zeusee.main.hyperlandmark.jni.Face;
import com.zeusee.main.hyperlandmark.jni.FaceTracking;

public class ExerciseFragment extends SupportFragment implements Camera.PreviewCallback, OnTaskCompleted {

    private static final String TAG = "ExerciseFragment";

    private static final String CAPTURE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeafApp/Capture";
    private static final String MODEL_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeafApp/ZeuseesFaceTracking";
    private static final String MODEL_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeafApp/ZeuseesFaceTracking/models";
    private static final String ASSET_PATH = "ZeuseesFaceTracking";
    private static final String CAPTURE_VIDEO_DIR = CAPTURE_DIR + "/Video";
    private static final String CAPTURE_AUDIO_DIR = CAPTURE_DIR + "/Audio";
    private static final String CAPTURE_COMBINE_DIR = CAPTURE_DIR + "/Combine";
    private static final String CAPTURE_VIDEO_PATH = CAPTURE_VIDEO_DIR + "/video.mp4";
    private static final String CAPTURE_AUDIO_PATH = CAPTURE_AUDIO_DIR + "/audio.mp4";
    private static final String CAPTURE_COMBINE_PATH = CAPTURE_COMBINE_DIR + "/combine.mp4";

    private static final int GENERATE_VIDEO_ID = 1;

    private static final String ENGLISH_ID = "englishId";
    private static final String CONTENT = "content";

    private final Object lockObj = new Object();

    private Toolbar mToolbar;
    private ProgressBar mProgressView;
    private SurfaceView mSvCamera;
    private SurfaceView mSvOverlap;
    private RelativeLayout recordContainer;
    private ImageView ivVolume;
    private ImageButton mCaptureBtn;
    private Button mAnalyzeBtn;
    private Button mPronounceBtn;
    private RelativeLayout cameraContainer;
    private RelativeLayout videoContainer;
    private VideoView captureVideo;
    private ImageView ivDemo;
    private Button btnCorrectDemo;
    private Button btnWrongDemo;

    private CameraOverlap cameraOverlap;
    private AudioRecorder mAudioRecorder;
    private ScriptIntrinsicYuvToRGB mScriptIntrinsicYuvToRGB;
    private Allocation mInAllocation, mOutAllocation;
    private Bitmap mSourceBitmap;
    private Paint mPaint;
    private Matrix mMatrix;

    private EGLUtils mEglUtils;
    private GLFramebuffer mFramebuffer;
    private GLFrame mFrame;
    private GLPoints mPoints;
    private GLBitmap mBitmap;

    private int userId;
    private int englishId;
    private String content;
    private byte[] mRGBCameraTrackNv21;
    private boolean mIsRGBCameraNv21Ready = false;
    private boolean isRecording = false;
    private ArrayList<byte[]> dataList = new ArrayList<>();
    private ArrayList<Bitmap> frameList = new ArrayList<>();

    private OkHttpClient client;

    private static CustomThreadPool sThreadPoolRGBTrack = new CustomThreadPool(Thread.NORM_PRIORITY);
    private static CustomThreadPool sThreadPoolNv21ToBtm = new CustomThreadPool(Thread.MAX_PRIORITY);
    private Handler mHandler = new Handler();

    public static ExerciseFragment newInstance(int englishId, String content) {
        ExerciseFragment fragment = new ExerciseFragment();
        Bundle args = new Bundle();
        args.putInt(ENGLISH_ID, englishId);
        args.putString(CONTENT, content);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise, container, false);

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
        mSvCamera = (SurfaceView) view.findViewById(R.id.sv_camera);
        mSvOverlap = (SurfaceView) view.findViewById(R.id.sv_overlap);
        mSvOverlap.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        recordContainer = (RelativeLayout) view.findViewById(R.id.record_container);
        ivVolume = (ImageView) view.findViewById(R.id.iv_volume);
        mAnalyzeBtn = (Button) view.findViewById(R.id.analyze_btn);
        mPronounceBtn = (Button) view.findViewById(R.id.pronounce_btn);
        mCaptureBtn = (ImageButton) view.findViewById(R.id.capture_btn);
        cameraContainer = (RelativeLayout) view.findViewById(R.id.camera_container);
        videoContainer = (RelativeLayout) view.findViewById(R.id.video_container);
        captureVideo = (VideoView) view.findViewById(R.id.capture_video);
        ivDemo = (ImageView) view.findViewById(R.id.iv_demo);
        btnCorrectDemo = (Button) view.findViewById(R.id.btn_correct_demo);
        btnWrongDemo = (Button) view.findViewById(R.id.btn_wrong_demo);
    }

    private void initDelayView() {
        mAnalyzeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (captureVideo != null) {
                    captureVideo.stopPlayback();
                    captureVideo.setOnCompletionListener(null);
                }
                mCaptureBtn.setEnabled(false);
                mAnalyzeBtn.setEnabled(false);
                mPronounceBtn.setEnabled(false);
                videoContainer.setVisibility(View.VISIBLE);
                cameraContainer.setVisibility(View.GONE);
                showProgress(true);
                analyzeVideo();
            }
        });
        mPronounceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCaptureBtn.setEnabled(true);
                mAnalyzeBtn.setEnabled(false);
                mPronounceBtn.setEnabled(true);
                ivDemo.setVisibility(View.GONE);
                cameraContainer.setVisibility(View.VISIBLE);
                if (captureVideo != null) {
                    captureVideo.stopPlayback();
                    captureVideo.setOnCompletionListener(null);
                }
                videoContainer.setVisibility(View.GONE);
            }
        });
        mCaptureBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    dataList.clear();
                    frameList.clear();
                    mCaptureBtn.setImageResource(R.drawable.ic_capture_on);
                    mCaptureBtn.setEnabled(true);
                    mAnalyzeBtn.setEnabled(false);
                    mPronounceBtn.setEnabled(false);
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
                    mAnalyzeBtn.setEnabled(false);
                    mPronounceBtn.setEnabled(false);
                    isRecording = false;
                    showProgress(true);
                    generateVideo();
                }
                return false;
            }
        });
        btnCorrectDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCaptureBtn.setEnabled(false);
                mAnalyzeBtn.setEnabled(false);
                mPronounceBtn.setEnabled(true);
                cameraContainer.setVisibility(View.GONE);
                if (captureVideo != null) {
                    captureVideo.stopPlayback();
                    captureVideo.setOnCompletionListener(null);
                }
                videoContainer.setVisibility(View.GONE);
                ivDemo.setVisibility(View.VISIBLE);
                ivDemo.setImageResource(R.drawable.correct_demo);
            }
        });
        btnWrongDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCaptureBtn.setEnabled(false);
                mAnalyzeBtn.setEnabled(false);
                mPronounceBtn.setEnabled(true);
                cameraContainer.setVisibility(View.GONE);
                if (captureVideo != null) {
                    captureVideo.stopPlayback();
                    captureVideo.setOnCompletionListener(null);
                }
                videoContainer.setVisibility(View.GONE);
                ivDemo.setVisibility(View.VISIBLE);
                ivDemo.setImageResource(R.drawable.wrong_demo);
            }
        });
    }

    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        super.onEnterAnimationEnd(savedInstanceState);

        initDelayView();
        initCamera();
    }

    private void initCamera() {
        initModelFiles();

        FaceTracking.getInstance().FaceTrackingInit(MODEL_PATH, CameraOverlap.PREVIEW_HEIGHT, CameraOverlap.PREVIEW_WIDTH);

        cameraOverlap = new CameraOverlap(_mActivity);
        cameraOverlap.setPreviewCallback(this);
        mFramebuffer = new GLFramebuffer();
        mFrame = new GLFrame();
        mPoints = new GLPoints();
        mBitmap = new GLBitmap(_mActivity, R.drawable.minions); // 任意定义一张图
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mMatrix = new Matrix();
        int strokeWidth = Math.max(CameraOverlap.PREVIEW_HEIGHT / 240, 2);
        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        if(mSvCamera.getHolder().getSurface() != null && mSvCamera.getWidth() > 0){
            sThreadPoolRGBTrack.execute(() -> {
                if (mEglUtils != null) {
                    mEglUtils.release();
                    mEglUtils = null;
                }
                mEglUtils = new EGLUtils();
                mEglUtils.initEGL(mSvCamera.getHolder().getSurface());
                mFramebuffer.initFramebuffer();
                mFrame.initFrame();
                mFrame.setSize(mSvCamera.getWidth(), mSvCamera.getHeight(), CameraOverlap.PREVIEW_HEIGHT, CameraOverlap.PREVIEW_WIDTH );
                mPoints.initPoints();
                mBitmap.initFrame(CameraOverlap.PREVIEW_HEIGHT, CameraOverlap.PREVIEW_WIDTH);
                mMatrix.setScale(mSvCamera.getWidth() / (float) CameraOverlap.PREVIEW_HEIGHT, mSvCamera.getHeight() / (float) CameraOverlap.PREVIEW_WIDTH);
                cameraOverlap.openCamera(mFramebuffer.getSurfaceTexture());
            });
        }
    }

    private void initModelFiles() {
        copyFilesFromAssets(_mActivity, ASSET_PATH, MODEL_DIR);
    }

    private void copyFilesFromAssets(Context context, String oldPath, String newPath) {
        try {
            String[] fileNames = context.getAssets().list(oldPath);
            if (fileNames.length > 0) {
                // directory
                File file = new File(newPath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                for (String fileName : fileNames) {
                    copyFilesFromAssets(context, oldPath + "/" + fileName,
                            newPath + "/" + fileName);
                }
            } else {
                // file
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private float view2openglX(int x, int width) {
        float centerX = width / 2.0f;
        float t = x - centerX;
        return t / centerX;
    }
    private float view2openglY(int y, int height) {
        float centerY = height / 2.0f;
        float s = centerY - y;
        return s / centerY;
    }

    private void startTrackRGBTask() {
        sThreadPoolRGBTrack.execute(() -> {
            if (mEglUtils == null) {
                return;
            }
            mFrame.setS(100 / 100.0f);
            mFrame.setH(0 / 360.0f);
            mFrame.setL(100 / 100.0f - 1);

            FaceTracking.getInstance().Update(mRGBCameraTrackNv21, CameraOverlap.PREVIEW_HEIGHT, CameraOverlap.PREVIEW_WIDTH);

            List<Face> faceActions = FaceTracking.getInstance().getTrackingInfo();
            if (faceActions != null && faceActions.size() > 0) {
                drawRectangle();
            } else {
                clearCanvas();
            }
            drawLips(faceActions);

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
                byte[] data = new byte[CameraOverlap.PREVIEW_WIDTH * CameraOverlap.PREVIEW_HEIGHT * 2];
                System.arraycopy(mRGBCameraTrackNv21, 0, data, 0, mRGBCameraTrackNv21.length);
                dataList.add(data);
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
            mIsRGBCameraNv21Ready = false;
        });
    }

    private void drawRectangle() {
        Canvas canvas = mSvOverlap.getHolder().lockCanvas();
        if (canvas == null) {
            return;
        }
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.setMatrix(mMatrix);
        Rect rectangle = new Rect(CameraOverlap.PREVIEW_HEIGHT / 4, CameraOverlap.PREVIEW_WIDTH / 8 * 5, CameraOverlap.PREVIEW_HEIGHT / 4 * 3, CameraOverlap.PREVIEW_WIDTH / 8 * 7);
        canvas.drawRect(rectangle, mPaint);
        mSvOverlap.getHolder().unlockCanvasAndPost(canvas);
    }

    public void clearCanvas() {
        if (mSvOverlap == null) return;
        Canvas canvas = mSvOverlap.getHolder().lockCanvas();
        if (canvas != null) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            mSvOverlap.getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void drawLips(List<Face> faceActions) {
        if (faceActions != null) {
            boolean rotate270 = cameraOverlap.getOrientation() == 270;
            float[] lipPoints = null;
            for (Face r : faceActions) {
                // 嘴巴只有20个点，需要一笔画完整个嘴巴，因此多出了4个重复点
                lipPoints = new float[24 * 2];
                for(int i = 0 ; i < 106 ; i++) {
                    int x;
                    if (rotate270) {
                        x = r.landmarks[i * 2];
                    } else {
                        x = CameraOverlap.PREVIEW_HEIGHT - r.landmarks[i * 2];
                    }
                    int y = r.landmarks[i * 2 + 1];
                    // 画出嘴巴特征点
                    // 嘴巴左边
                    // i == 45, i == 61
                    // 嘴巴右边
                    // i == 42, i == 50
                    // 上嘴唇上半部分
                    // i == 37, i == 39, i == 38, i == 26, i == 33
                    // 上嘴唇下半部分
                    // i == 40, i == 36, i == 25
                    // 下嘴唇上半部分
                    // i == 63, i == 103, i == 2
                    // 下嘴唇下半部分
                    // i == 65, i == 64, i == 32, i == 30, i == 4
                    if (i == 45) {
                        lipPoints[0] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[1] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                        lipPoints[24] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[25] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                    }
                    if (i == 37) {
                        lipPoints[2] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[3] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                    }
                    if (i == 39) {
                        lipPoints[4] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[5] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                    }
                    if (i == 38) {
                        lipPoints[6] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[7] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                    }
                    if (i == 26) {
                        lipPoints[8] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[9] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                    }
                    if (i == 33) {
                        lipPoints[10] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[11] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                    }
                    if (i == 50) {
                        lipPoints[12] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[13] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                        lipPoints[36] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[37] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                    }
                    if (i == 42) {
                        lipPoints[14] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[15] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                        lipPoints[38] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[39] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                    }
                    if (i == 25) {
                        lipPoints[16] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[17] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                    }
                    if (i == 36) {
                        lipPoints[18] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[19] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                    }
                    if (i == 40) {
                        lipPoints[20] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[21] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                    }
                    if (i == 61) {
                        lipPoints[22] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[23] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                        lipPoints[46] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[47] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                    }
                    if (i == 65) {
                        lipPoints[26] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[27] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                    }
                    if (i == 64) {
                        lipPoints[28] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[29] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                    }
                    if (i == 32) {
                        lipPoints[30] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[31] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                    }
                    if (i == 30) {
                        lipPoints[32] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[33] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                    }
                    if (i == 4) {
                        lipPoints[34] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[35] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                    }
                    if (i == 2) {
                        lipPoints[40] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[41] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                    }
                    if (i == 103) {
                        lipPoints[42] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[43] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                    }
                    if (i == 63) {
                        lipPoints[44] = view2openglX(x, CameraOverlap.PREVIEW_HEIGHT);
                        lipPoints[45] = view2openglY(y, CameraOverlap.PREVIEW_WIDTH);
                    }
                }
            }
            int tid = 3;
            mFrame.drawFrame(tid, mFramebuffer.drawFrameBuffer(), mFramebuffer.getMatrix());
            if(lipPoints != null){
                mPoints.setPoints(lipPoints);
                mPoints.drawLipPoints();
                mPoints.drawLipLine();
            }
            mEglUtils.swap();
        }
    }

    private void generateVideo() {
        if (dataList.size() > 0) {
            sThreadPoolNv21ToBtm.execute(() -> {
                for (int i = 0; i < dataList.size(); i++) {
                    Bitmap sceneBtm = getSceneBtm(dataList.get(i), CameraOverlap.PREVIEW_WIDTH, CameraOverlap.PREVIEW_HEIGHT);
                    Bitmap rotateFaceBtm = BitmapUtils.rotateBitmap(sceneBtm, 270, true, false);
                    frameList.add(rotateFaceBtm);
                }
                dataList.clear();
                GenerateVideo generateVideoTask = new GenerateVideo(ExerciseFragment.this, GENERATE_VIDEO_ID);
                generateVideoTask.execute(frameList);
            });
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
                        mCaptureBtn.setEnabled(false);
                        mAnalyzeBtn.setEnabled(true);
                        mPronounceBtn.setEnabled(true);
                        playVideo();
                        showProgress(false);
                    }
                });
            }
        });
    }

    /**
     * 根据nv21数据生成bitmap
     */
    private Bitmap getSceneBtm(byte[] nv21Bytes, int width, int height) {

        if (nv21Bytes == null) {
            return null;
        }

        if (mInAllocation == null) {
            initRenderScript(width, height);
        }
        long s = SystemClock.uptimeMillis();
        mInAllocation.copyFrom(nv21Bytes);
        mScriptIntrinsicYuvToRGB.setInput(mInAllocation);
        mScriptIntrinsicYuvToRGB.forEach(mOutAllocation);
        if (mSourceBitmap == null || mSourceBitmap.getWidth() * mSourceBitmap.getHeight() != width * height) {
            mSourceBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        mOutAllocation.copyTo(mSourceBitmap);
        return mSourceBitmap;
    }

    private void initRenderScript(int width, int height) {

        RenderScript mRenderScript = RenderScript.create(_mActivity);
        mScriptIntrinsicYuvToRGB = ScriptIntrinsicYuvToRGB.create(mRenderScript,
                Element.U8_4(mRenderScript));

        Type.Builder yuvType = new Type.Builder(mRenderScript, Element.U8(mRenderScript))
                .setX(width * height * 3 / 2);
        mInAllocation = Allocation.createTyped(mRenderScript,
                yuvType.create(),
                Allocation.USAGE_SCRIPT);

        Type.Builder rgbaType = new Type.Builder(mRenderScript, Element.RGBA_8888(mRenderScript))
                .setX(width).setY(height);
        mOutAllocation = Allocation.createTyped(mRenderScript,
                rgbaType.create(),
                Allocation.USAGE_SCRIPT);
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

    private void playVideo() {
        cameraContainer.setVisibility(View.GONE);
        videoContainer.setVisibility(View.VISIBLE);
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

    private void goToNextFragment() {
        startWithPop(StudyFragment.newInstance(englishId, content));
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (data == null) {
            return;
        }
        if (mRGBCameraTrackNv21 == null) {
            mRGBCameraTrackNv21 = new byte[CameraOverlap.PREVIEW_WIDTH * CameraOverlap.PREVIEW_HEIGHT * 2];
        }
        if (!mIsRGBCameraNv21Ready) {
            synchronized (lockObj) {
                System.arraycopy(data, 0, mRGBCameraTrackNv21, 0, data.length);
            }
            mIsRGBCameraNv21Ready = true;
            startTrackRGBTask();
        }
    }

    @Override
    public void onTaskCompleted(String response, int requestId) {
        if (requestId == GENERATE_VIDEO_ID) {
            frameList.clear();
            CustomToast.show(_mActivity, response);
            showProgress(false);
            mCaptureBtn.setEnabled(false);
            mAnalyzeBtn.setEnabled(true);
            mPronounceBtn.setEnabled(true);
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

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (cameraOverlap != null) {
            cameraOverlap.setPreviewCallback(null);
            cameraOverlap.release();
            cameraOverlap = null;
        }
        if (mFramebuffer != null) {
            mFramebuffer.release();
            mFramebuffer = null;
        }
        if (mFrame != null) {
            mFrame.release();
            mFrame = null;
        }
        if (mPoints != null) {
            mPoints.release();
            mPoints = null;
        }
        if (mBitmap != null) {
            mBitmap.release();
            mBitmap = null;
        }
        if (mEglUtils != null) {
            mEglUtils.release();
            mEglUtils = null;
        }
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
