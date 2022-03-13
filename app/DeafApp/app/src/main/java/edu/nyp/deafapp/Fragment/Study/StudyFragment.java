package edu.nyp.deafapp.Fragment.Study;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
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
import android.widget.TextView;
import android.widget.VideoView;

import com.github.piasy.rxandroidaudio.AudioRecorder;

import org.json.JSONObject;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;

import androidx.annotation.Nullable;
import edu.nyp.deafapp.Adapter.ChartChildAdapter;
import edu.nyp.deafapp.Adapter.CustomViewPager;
import edu.nyp.deafapp.CustomView.CustomToast;
import edu.nyp.deafapp.Demo.DemoMode;
import edu.nyp.deafapp.Fragment.Exercise.ExerciseFragment;
import edu.nyp.deafapp.Fragment.MainFragment;
import edu.nyp.deafapp.HttpAsyncTask.Get;
import edu.nyp.deafapp.HttpAsyncTask.GetAuth;
import edu.nyp.deafapp.IPAddress;
import edu.nyp.deafapp.Interface.OnTaskCompleted;
import edu.nyp.deafapp.LoginActivity;
import edu.nyp.deafapp.Model.AudioUploadResult;
import edu.nyp.deafapp.R;
import edu.nyp.deafapp.SharedPreferences.GetSetSharedPreferences;
import edu.nyp.deafapp.Token.TokenHelper;
import me.yokeyword.fragmentation.SupportFragment;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class StudyFragment extends SupportFragment implements CameraBridgeViewBase.CvCameraViewListener2, OnTaskCompleted {

    private static final String AUDIO_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeafApp/Record";
    private static final String VIDEO_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeafApp/Video";
    private static final String FRAMES_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeafApp/Frames";

    private static final int GET_ENGLISH_ID = 1;
    private static final int REFRESH_TOKEN_TASK_ID = 100;

    private static final String ENGLISH_ID = "englishId";
    private static final String CONTENT = "content";

    private CustomViewPager lineChartViewPager;
    private Toolbar mToolbar;
    private VideoView tutorialVideo;
    private VideoView organVideo;
    private JavaCameraView mJavaCameraView;
    private ImageView mouthShape;
    private ImageView organImg;
    private ImageButton organPlay;
    private TextView tutorialTxt;
    private ImageView leftBtn;
    private ImageView rightBtn;
    private Button tutorialBtn;
    private Button exercise1Btn;
    private Button exercise2Btn;
    private Button startBtn;
    private RelativeLayout recordContainer;
    private ImageView ivVolume;
    private ProgressBar mProgressView;
    private Button mCorrectDemoButton;
    private Button mWrongDemoButton;

    private int englishId;
    private String content;

    private Mat frame;
    private int gifFrame = 1;

    private String TUTORIAL = "tutorial";
    private String EXERCISE = "exercise";
    private String ORGAN = "organ";
    private String token = "";

    private File framesFolder;
    private Bitmap frameBitmap;

    private AudioRecorder mAudioRecorder;

    private Handler mHandler = new Handler();

    public static StudyFragment newInstance(int englishId, String content) {
        Bundle args = new Bundle();
        StudyFragment fragment = new StudyFragment();
        args.putInt(ENGLISH_ID, englishId);
        args.putString(CONTENT, content);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_study, container, false);

        token = GetSetSharedPreferences.getDefaults("token", _mActivity.getApplicationContext());

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            englishId = bundle.getInt(ENGLISH_ID, 0);
            content = bundle.getString(CONTENT);
        }

        initView(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void initView(View view) {
        lineChartViewPager = (CustomViewPager) view.findViewById(R.id.line_chart_container);
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        leftBtn = (ImageView) view.findViewById(R.id.left_btn);
        rightBtn = (ImageView) view.findViewById(R.id.right_btn);
        tutorialBtn = (Button) view.findViewById(R.id.tutorial_btn);
        exercise1Btn = (Button) view.findViewById(R.id.exercise1_btn);
        exercise2Btn = (Button) view.findViewById(R.id.exercise2_btn);
        startBtn = (Button) view.findViewById(R.id.start_btn);
        tutorialVideo = (VideoView) view.findViewById(R.id.tutorial_video);
        tutorialVideo.setZOrderOnTop(true);
        tutorialVideo.setZOrderMediaOverlay(true);
        organVideo = (VideoView) view.findViewById(R.id.organ_video);
        organPlay = (ImageButton) view.findViewById(R.id.organ_play);
        mJavaCameraView = (JavaCameraView) view.findViewById(R.id.open_cv_camera);
        mouthShape = (ImageView) view.findViewById(R.id.mouth_shape);
        organImg = (ImageView) view.findViewById(R.id.organ_img);
        tutorialTxt = (TextView) view.findViewById(R.id.tutorial_txt);
        recordContainer = (RelativeLayout) view.findViewById(R.id.record_container);
        ivVolume = (ImageView) view.findViewById(R.id.iv_volume);
        mProgressView = (ProgressBar) view.findViewById(R.id.main_loading_progress);
        mCorrectDemoButton = (Button) view.findViewById(R.id.correct_demo_button);
        mWrongDemoButton = (Button) view.findViewById(R.id.wrong_demo_button);
        lineChartViewPager.setScanScroll(false);
        tutorialTxt.setMovementMethod(new ScrollingMovementMethod());
        mToolbar.setTitle(content);
        leftBtn.setEnabled(false);
        leftBtn.setVisibility(View.INVISIBLE);
        rightBtn.setEnabled(false);
        rightBtn.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        initDelayView();
    }

    private void initDelayView() {
        showProgress(true);

        String organVideoPath = VIDEO_DIR + "/" + ORGAN + ".mp4";
        Bitmap organBitmap = getVideoThumbnail(organVideoPath);
        organImg.setImageBitmap(organBitmap);

        framesFolder = new File(FRAMES_DIR);
        playVideo(TUTORIAL);

        staticLoadCVLibraries();

        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lineChartViewPager.getCurrentItem() == 1) {
                    lineChartViewPager.setCurrentItem(4);
                }
                else if(lineChartViewPager.getCurrentItem() == 2) {
                    lineChartViewPager.setCurrentItem(1);
                }
                else if(lineChartViewPager.getCurrentItem() == 3) {
                    lineChartViewPager.setCurrentItem(2);
                }
                else if(lineChartViewPager.getCurrentItem() == 4) {
                    lineChartViewPager.setCurrentItem(3);
                }
            }
        });

        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lineChartViewPager.getCurrentItem() == 1) {
                    lineChartViewPager.setCurrentItem(2);
                }
                else if(lineChartViewPager.getCurrentItem() == 2) {
                    lineChartViewPager.setCurrentItem(3);
                }
                else if(lineChartViewPager.getCurrentItem() == 3) {
                    lineChartViewPager.setCurrentItem(4);
                }
                else if(lineChartViewPager.getCurrentItem() == 4) {
                    lineChartViewPager.setCurrentItem(1);
                }
            }
        });

        organPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                organVideo.setVisibility(View.VISIBLE);
                organImg.setVisibility(View.GONE);
                organPlay.setVisibility(View.GONE);
                organVideo.setVideoURI(Uri.parse(organVideoPath));
                organVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mPlayer) {
                        organVideo.stopPlayback();
                        organVideo.setOnCompletionListener(null);
                        organImg.setVisibility(View.VISIBLE);
                        organPlay.setVisibility(View.VISIBLE);
                        organVideo.setVisibility(View.GONE);
                    }
                });
                organVideo.start();
            }
        });

        tutorialBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVideo(TUTORIAL);
                stopCamera();
            }
        });

        exercise1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVideo(EXERCISE);
                startCamera();
            }
        });

        exercise2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNextFragment();
            }
        });

        startBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    AudioUploadResult.setUploadResult(false);
                    leftBtn.setEnabled(false);
                    leftBtn.setVisibility(View.INVISIBLE);
                    rightBtn.setEnabled(false);
                    rightBtn.setVisibility(View.INVISIBLE);
                    lineChartViewPager.setCurrentItem(0);
                    tutorialVideo.pause();
                    mAudioRecorder = AudioRecorder.getInstance();
                    File mAudioFile = new File(AUDIO_DIR + "/record.mp3");
                    mAudioRecorder.prepareRecord(
                            MediaRecorder.AudioSource.MIC,
                            MediaRecorder.OutputFormat.MPEG_4,
                            MediaRecorder.AudioEncoder.AAC,
                            mAudioFile);
                    mAudioRecorder.startRecord();
                    if(mAudioRecorder.isStarted()) {
                        recordContainer.setVisibility(View.VISIBLE);
                        mHandler.post(mPollTask);
                    }
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    leftBtn.setEnabled(true);
                    leftBtn.setVisibility(View.VISIBLE);
                    rightBtn.setEnabled(true);
                    rightBtn.setVisibility(View.VISIBLE);
                    recordContainer.setVisibility(View.GONE);
                    mAudioRecorder.stopRecord();
                    mHandler.removeCallbacks(mPollTask);
                    CustomToast.show(_mActivity, "Recorded successfully!");
                    lineChartViewPager.setCurrentItem(4);
                }
                return false;
            }
        });

        mCorrectDemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DemoMode.setCorrectDemo(true);
                System.out.println("Demo Mode: " + DemoMode.isDemoMode());
                System.out.println("Correct Demo: " + DemoMode.isCorrectPronunciationDemo());
            }
        });

        mWrongDemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DemoMode.setCorrectDemo(false);
                System.out.println("Demo Mode: " + DemoMode.isDemoMode());
                System.out.println("Correct Demo: " + DemoMode.isCorrectPronunciationDemo());
            }
        });

        getEnglish();
    }

    Runnable mPollTask = new Runnable() {
        @Override
        public void run() {
            updateVolume(getVolume());
            mHandler.postDelayed(mPollTask, 100);
        }
    };

    private void getEnglish() {
        GetAuth getEnglishTask = new GetAuth(StudyFragment.this, GET_ENGLISH_ID);
        getEnglishTask.execute(IPAddress.getIpAddress() + "/api/english/" + englishId, token);
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

    private void playVideo(String videoType) {
        String tutorialVideoPath = VIDEO_DIR + "/" + videoType + ".mp4";
        tutorialVideo.setVideoURI(Uri.parse(tutorialVideoPath));
        tutorialVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mPlayer) {
                mPlayer.start();
                mPlayer.setLooping(true);
            }
        });
        MediaController mediaController = new MediaController(_mActivity);
        mediaController.setAnchorView(tutorialVideo);
        mediaController.setMediaPlayer(tutorialVideo);
        tutorialVideo.setMediaController(mediaController);
        tutorialVideo.start();
    }

    // OpenCV库静态加载并初始化
    private void staticLoadCVLibraries() {
        boolean load = OpenCVLoader.initDebug();
        if(load) {
            System.out.println("OpenCV Libraries loaded...");
        }
    }

    private void startCamera() {
        mouthShape.setVisibility(View.VISIBLE);
        mJavaCameraView.setVisibility(SurfaceView.VISIBLE);
        mJavaCameraView.setCvCameraViewListener(StudyFragment.this);
        mJavaCameraView.setCameraIndex(1);
        mJavaCameraView.enableView();
    }

    private void stopCamera() {
        if(mJavaCameraView != null) {
            mJavaCameraView.disableView();
            mJavaCameraView.setVisibility(SurfaceView.GONE);
        }
        if(mouthShape != null) {
            mouthShape.setVisibility(View.GONE);
        }
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
        if(this.getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            Core.rotate(frame, frame, Core.ROTATE_90_COUNTERCLOCKWISE);
            Core.flip(frame, frame, 1);
        }

        String[] children = framesFolder.list();

        final String countFrameTxt;
        if(gifFrame >= 1 && gifFrame < 10) {
            countFrameTxt = "0" + String.valueOf(gifFrame);
        }
        else if(gifFrame >= 10 && gifFrame <= children.length) {
            countFrameTxt = String.valueOf(gifFrame);
        }
        else {
            gifFrame = 1;
            countFrameTxt = "0" + String.valueOf(gifFrame);
        }
        _mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                frameBitmap = BitmapFactory.decodeFile(FRAMES_DIR + "/capture_" + countFrameTxt + ".png");
                mouthShape.setImageBitmap(frameBitmap);
            }
        });
        gifFrame++;
        return frame;
    }

    @Override
    public void onTaskCompleted(String response, int requestId) {
        if(response.equals("FORBIDDEN")) {
            TokenHelper.refreshToken(_mActivity, this::onTaskCompleted, REFRESH_TOKEN_TASK_ID);
        }
        else if(response.equals("UNAUTHORIZED")) {
            TokenHelper.refreshToken(_mActivity, this::onTaskCompleted, REFRESH_TOKEN_TASK_ID);
        }
        else {
            if(requestId == GET_ENGLISH_ID) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String Content = jsonObject.getString("content");
                    String Detail = jsonObject.getString("detail");

                    lineChartViewPager.setAdapter(new ChartChildAdapter(getChildFragmentManager(), englishId, Content,
                            getString(R.string.initialization),
                            getString(R.string.standard),
                            getString(R.string.test),
                            getString(R.string.standard_test),
                            getString(R.string.result)));

                    lineChartViewPager.setCurrentItem(0);

                    tutorialTxt.setText(Detail.replace("\\n", "\n"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
                showProgress(false);
            }
            else if(requestId == REFRESH_TOKEN_TASK_ID) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String Status = jsonObject.getString("status");

                    if(Status.equals("Succeeded")) {
                        String newToken = jsonObject.getString("token");
                        GetSetSharedPreferences.setDefaults("token", newToken, _mActivity.getApplicationContext());
                        token = GetSetSharedPreferences.getDefaults("token", _mActivity.getApplicationContext());
                        getEnglish();
                    } else {
                        goToNextActivity();
                    }
                } catch (Exception e) {
                    goToNextActivity();
                }
            }
        }
    }

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

    public Bitmap getVideoThumbnail(String videoPath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        File videoFile = new File(videoPath);
        mmr.setDataSource(videoFile.getAbsolutePath());
        return mmr.getFrameAtTime();
    }

    private void goToNextFragment() {
        stopCamera();
        startWithPop(ExerciseFragment.newInstance(englishId, content));
    }

    private void goToNextActivity() {
        stopCamera();
        Intent intent = new Intent(_mActivity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onPause() {
        if(mJavaCameraView != null) {
            mJavaCameraView.disableView();
        }
        if(tutorialVideo != null) {
            tutorialVideo.stopPlayback();
            tutorialVideo.setOnCompletionListener(null);
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mJavaCameraView != null) {
            mJavaCameraView.setCameraIndex(1);
            mJavaCameraView.enableView();
        }
        playVideo(TUTORIAL);
    }

    @Override
    public void onDestroyView() {
        stopCamera();
        mJavaCameraView = null;
        if(organVideo != null) {
            organVideo.stopPlayback();
            organVideo.setOnCompletionListener(null);
            organVideo = null;
        }
        if(tutorialVideo != null) {
            tutorialVideo.stopPlayback();
            tutorialVideo.setOnCompletionListener(null);
            tutorialVideo = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        super.onDestroyView();
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
