package edu.nyp.deafapp.Fragment.Exercise;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Comparator;

import edu.nyp.deafapp.Camera.CameraOverlap;
import edu.nyp.deafapp.Camera2.Camera2Helper;
import edu.nyp.deafapp.Camera2.Camera2Listener;
import edu.nyp.deafapp.Fragment.Study.StudyFragment;
import edu.nyp.deafapp.R;
import edu.nyp.deafapp.SharedPreferences.GetSetSharedPreferences;
import edu.nyp.deafapp.Thread.CustomThreadPool;
import edu.nyp.deafapp.Utils.ImageUtils;
import me.yokeyword.fragmentation.SupportFragment;
import com.zeusee.main.hyperlandmark.jni.FaceTracking;

public class ExerciseFragmentBackup2 extends SupportFragment implements ViewTreeObserver.OnGlobalLayoutListener, Camera2Listener {

    private static final String TAG = "ExerciseFragmentBackup2";

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

    // 默认打开的CAMERA
    private static final String CAMERA_ID = Camera2Helper.CAMERA_ID_FRONT;

    private static final String ENGLISH_ID = "englishId";
    private static final String CONTENT = "content";

    private Toolbar mToolbar;
    private ProgressBar mProgressView;
    private TextureView mTvCamera;
    private ImageView mIvPreview;

    private Camera2Helper camera2Helper;
    private FaceTracking mMultiTrack106 = null;

    private int userId;
    private int englishId;
    private String content;

    // 预览宽度
    private int mPreviewW = -1;
    // 预览高度
    private int mPreviewH = -1;
    // 颜色通道
    private byte[] y;
    private byte[] u;
    private byte[] v;
    // 步长
    private int stride;
    // 显示的旋转角度
    private int displayOrientation;
    // 是否手动镜像预览
    private boolean isMirrorPreview;
    // 实际打开的cameraId
    private String openedCameraId;
    // 图像帧数据，全局变量避免反复创建，降低gc频率
    private byte[] mRGBCameraTrackNv21;
    // 帧处理
    private volatile boolean mIsRGBCameraNv21Ready;

    // 线程池
    private static CustomThreadPool sThreadPoolRGBTrack = new CustomThreadPool(Thread.NORM_PRIORITY);

    private static SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    private static class CompareSizeByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum( (long)(lhs.getWidth() * lhs.getHeight()) -
                    (long)(rhs.getWidth() * rhs.getHeight()));
        }
    }

    public static ExerciseFragmentBackup2 newInstance(int englishId, String content) {
        ExerciseFragmentBackup2 fragment = new ExerciseFragmentBackup2();
        Bundle args = new Bundle();
        args.putInt(ENGLISH_ID, englishId);
        args.putString(CONTENT, content);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise_backup2, container, false);

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
        mTvCamera = (TextureView) view.findViewById(R.id.tv_camera);
        mTvCamera.getViewTreeObserver().addOnGlobalLayoutListener(this);
        mIvPreview = (ImageView) view.findViewById(R.id.iv_preview);
    }

    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        super.onEnterAnimationEnd(savedInstanceState);
        init();
        initDelayView();
    }

    private void init() {
        initModelFiles();

        FaceTracking.getInstance().FaceTrackingInit(MODEL_PATH, CameraOverlap.PREVIEW_HEIGHT, CameraOverlap.PREVIEW_WIDTH);
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
                if (!file.mkdir()) {
                    Log.d("mkdir","can't make folder");
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
            e.printStackTrace();
        }
    }

    private void initDelayView() {

    }

    private void initCamera() {
        camera2Helper = new Camera2Helper.Builder()
                .cameraListener(this)
                .maxPreviewSize(new Point(1920, 1080))
                .minPreviewSize(new Point(1280, 720))
                .specificCameraId(CAMERA_ID)
                .context(_mActivity)
                .previewOn(mTvCamera)
                .previewViewSize(new Point(mTvCamera.getWidth(), mTvCamera.getHeight()))
                .rotation(_mActivity.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        camera2Helper.start();
    }

    private void startTrackRGBTask() {
        sThreadPoolRGBTrack.execute(() -> {
            if (mIsRGBCameraNv21Ready) {
                // 回传数据是YUV422
                if (y.length / u.length == 2) {
                    ImageUtils.yuv422ToYuv420sp(y, u, v, mRGBCameraTrackNv21, stride, mPreviewH);
                }
                // 回传数据是YUV420
                else if (y.length / u.length == 4) {
                    ImageUtils.yuv420ToYuv420sp(y, u, v, mRGBCameraTrackNv21, stride, mPreviewH);
                }
                YuvImage yuvImage = new YuvImage(mRGBCameraTrackNv21, ImageFormat.NV21, stride, mPreviewH, null);

                // ByteArrayOutputStream的close中其实没做任何操作，可不执行
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                // 由于某些stride和previewWidth差距大的分辨率，[0,previewWidth)是有数据的，而[previewWidth,stride)补上的U、V均为0，因此在这种情况下运行会看到明显的绿边
                // yuvImage.compressToJpeg(new Rect(0, 0, stride, previewSize.getHeight()), 100, byteArrayOutputStream);

                // 由于U和V一般都有缺损，因此若使用方式，可能会有个宽度为1像素的绿边
                yuvImage.compressToJpeg(new Rect(0, 0, mPreviewW, mPreviewH), 100, byteArrayOutputStream);

                // 为了删除绿边，抛弃一行像素
                // yuvImage.compressToJpeg(new Rect(0, 0, previewSize.getWidth() - 1, previewSize.getHeight()), 100, byteArrayOutputStream);

                byte[] jpgBytes = byteArrayOutputStream.toByteArray();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                // 原始预览数据生成的bitmap
                final Bitmap originalBitmap = BitmapFactory.decodeByteArray(jpgBytes, 0, jpgBytes.length, options);
                Matrix matrix = new Matrix();
                // 预览相对于原数据可能有旋转
                matrix.postRotate(Camera2Helper.CAMERA_ID_BACK.equals(openedCameraId) ? displayOrientation : -displayOrientation);

                // 对于前置数据，镜像处理；若手动设置镜像预览，则镜像处理；若都有，则不需要镜像处理
                if (Camera2Helper.CAMERA_ID_FRONT.equals(openedCameraId) ^ isMirrorPreview) {
                    matrix.postScale(-1, 1);
                }

                // 和预览画面相同的bitmap
                final Bitmap previewBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, false);
                _mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mIvPreview.setImageBitmap(previewBitmap);
                    }
                });
                mIsRGBCameraNv21Ready = false;
            }
        });
    }

    private void goToNextFragment() {
        startWithPop(StudyFragment.newInstance(englishId, content));
    }

    @Override
    public void onGlobalLayout() {
        mTvCamera.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        initCamera();
    }

    @Override
    public void onCameraOpened(CameraDevice cameraDevice, String cameraId, final Size previewSize, final int displayOrientation, boolean isMirror) {
        Log.i(TAG, "onCameraOpened:  previewSize = " + previewSize.getWidth() + "x" + previewSize.getHeight());
        this.displayOrientation = displayOrientation;
        this.isMirrorPreview = isMirror;
        this.openedCameraId = cameraId;
    }

    @Override
    public void onPreview(final byte[] y, final byte[] u, final byte[] v, final Size previewSize, final int stride) {

        if (mRGBCameraTrackNv21 == null) {
            mRGBCameraTrackNv21 = new byte[stride * previewSize.getHeight() * 3 / 2];
        }

        if (!mIsRGBCameraNv21Ready) {
            mIsRGBCameraNv21Ready = true;
            mPreviewW = previewSize.getWidth();
            mPreviewH = previewSize.getHeight();
            this.y = y;
            this.u = u;
            this.v = v;
            this.stride = stride;
            startTrackRGBTask();
        }
    }

    @Override
    public void onCameraClosed() {
        Log.i(TAG, "onCameraClosed: ");
    }

    @Override
    public void onCameraError(Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (camera2Helper != null) {
            camera2Helper.start();
        }
    }

    @Override
    public void onPause() {
        if (camera2Helper != null) {
            camera2Helper.stop();
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        if (camera2Helper != null) {
            camera2Helper.release();
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
