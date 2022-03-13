package edu.nyp.deafapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.json.JSONObject;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.samples.facedetect.DetectionBasedTracker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.RawRes;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.enums.RuntimeABI;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;

import edu.nyp.deafapp.Constants.Constants;
import edu.nyp.deafapp.CustomView.CustomToast;
import edu.nyp.deafapp.Demo.DemoMode;
import edu.nyp.deafapp.HttpAsyncTask.Post;
import edu.nyp.deafapp.Interface.OnTaskCompleted;
import edu.nyp.deafapp.SharedPreferences.GetSetSharedPreferences;
import edu.nyp.deafapp.Utils.RSAUtils;

import static org.opencv.core.Core.FONT_HERSHEY_COMPLEX;

/**
 * @author Administrator
 * @des ${TODO}
 * @verson $Rev$
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class FaceRecognitionActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, OnTaskCompleted {

    private static final String TAG = "FaceRecognitionActivity";

    private static final int POST_GET_ACCESS_TOKEN_ID = 1;
    private static final int POST_GET_USER_IMAGE_ID = 2;

    /**
     * 人脸类型
     */
    private static final int TYPE_ENROLLED = 0;
    private static final int TYPE_CAPTURED = 1;

    // Demo 所需的动态库文件
    private static final String[] ARCSOFT_LIBRARIES = new String[]{
            // 人脸相关
            "libarcsoft_face_engine.so",
            "libarcsoft_face.so",
            // 图像库相关
            "libarcsoft_image_util.so"
    };

    private Toolbar mToolbar;
    private RelativeLayout faceRecognitionCameraContainer;
    private JavaCameraView mJavaCameraView;
    private ImageView mUserImage;
    private EditText mUsername;
    private Button mSignInButton;

    private float mRelativeFaceSize = 0.2f;
    private int mAbsolutionFaceSize = 0;
    private Mat frame;
    private Mat mGray;
    private Mat leftEye_tpl;
    private Mat rightEye_tpl;

    private DetectionBasedTracker mNativeDetector;
    private CascadeClassifier eyeDetector;
    private Scalar TEXT_COLOR = new Scalar(0, 255, 0);
    private Scalar FACE_COLOR = new Scalar(255, 0, 0);
    private Scalar EYE_COLOR = new Scalar(0, 0, 255);
    private Scalar EYEBALL_BORDER_COLOR = new Scalar(0, 255, 255);

    private FaceEngine faceEngine;
    private FaceFeature enrolledFaceFeature = null;
    private FaceFeature capturedFaceFeature = null;

    private double containerWidth;
    private double containerHeight;
    private boolean isArcsoftEngineActivated = false;
    private boolean isStartFaceRecognition = false;
    private boolean isStartLogin = false;
    private String token = "";
    private String deviceUuid;
    private String publicKey;
    private String username;
    private int faceEngineCode = -1;
    private int age = -1;
    private int gender = -1;
    private float score = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);
        initView();

        publicKey = GetSetSharedPreferences.getDefaults("publicKey", getApplicationContext());

        if(GetSetSharedPreferences.getDefaults("username", getApplicationContext()) != null) {
            username = GetSetSharedPreferences.getDefaults("username", getApplicationContext());
            mUsername.setText(username);
        }

        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = mUsername.getText().toString();
                if(username.equals("")) {
                    mUsername.setError("Please enter your username");
                }
                else {
                    mSignInButton.setEnabled(false);

                    TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
                    if (ActivityCompat.checkSelfPermission(FaceRecognitionActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    deviceUuid = UUID.randomUUID().toString();

                    GetSetSharedPreferences.setDefaults("deviceUuid", deviceUuid, getApplicationContext());
                    System.out.println("deviceUuid: " + deviceUuid);

                    if (!DemoMode.isDemoMode()) {
                        isStartLogin = true;
                    } else {
                        postLoginWithUsernameData();
                    }
                }
            }
        });

        staticLoadArcsoftLibraries();
        staticLoadCVLibraries();
        startCamera();

        new Thread(new Runnable() {
            @Override
            public void run() {
                activeArcsoftEngine();
            }
        }).start();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.activity_face_recognition);
        faceRecognitionCameraContainer = (RelativeLayout) findViewById(R.id.face_recognition_camera_container);
        mJavaCameraView = (JavaCameraView) findViewById(R.id.face_recognition_camera);
        mUserImage = (ImageView) findViewById(R.id.user_image);
        mUsername = (EditText) findViewById(R.id.username);
        mSignInButton = (Button) findViewById(R.id.sign_in_button);
    }

    // OpenCV库静态加载并初始化
    private void staticLoadCVLibraries() {
        boolean load = OpenCVLoader.initDebug();
        if(load) {
            Log.i(TAG, "OpenCV Libraries loaded...");
        }
        System.loadLibrary("face_detection");
    }

    // 虹软库静态加载并初始化
    private void staticLoadArcsoftLibraries() {
        boolean load = checkSoFile(ARCSOFT_LIBRARIES);
        if (load) {
            Log.i(TAG, "Arcsoft Libraries loaded...");
        }
    }

    /**
     * 检查能否找到动态链接库，如果找不到，请修改工程配置
     *
     * @param libraries 需要的动态链接库
     * @return 动态库是否存在
     */
    private boolean checkSoFile(String[] libraries) {
        File dir = new File(getApplicationInfo().nativeLibraryDir);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }
        List<String> libraryNameList = new ArrayList<>();
        for (File file : files) {
            libraryNameList.add(file.getName());
        }
        boolean exists = true;
        for (String library : libraries) {
            exists &= libraryNameList.contains(library);
        }
        return exists;
    }

    /**
     * 激活虹软引擎
     *
     */
    public void activeArcsoftEngine() {
        RuntimeABI runtimeABI = FaceEngine.getRuntimeABI();
        Log.i(TAG, "getRuntimeABI: " + runtimeABI);
        long start = System.currentTimeMillis();
        int activeCode = FaceEngine.activeOnline(FaceRecognitionActivity.this, Constants.APP_ID, Constants.SDK_KEY);
        Log.i(TAG, "Active Time Cost: " + (System.currentTimeMillis() - start));
        if (activeCode == ErrorInfo.MOK) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initEngine();
                    CustomToast.show(FaceRecognitionActivity.this, "Arcsoft Engine Activated Success");
                    isArcsoftEngineActivated = true;
                }
            });
        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    initEngine();
                    CustomToast.show(FaceRecognitionActivity.this, "Arcsoft Engine Already Activated");
                    isArcsoftEngineActivated = true;
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CustomToast.show(FaceRecognitionActivity.this, "Arcsoft Engine Activated Failed");
                    isArcsoftEngineActivated = false;
                }
            });
        }
    }

    private void initEngine() {
        faceEngine = new FaceEngine();
        faceEngineCode = faceEngine.init(this, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY,
                16, 6, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_AGE | FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE);

        Log.i(TAG, "initEngine: " + faceEngineCode);

        if (faceEngineCode != ErrorInfo.MOK) {
            Log.i(TAG, "Face Engine init failed: " + faceEngineCode);
        }
    }

    private void unInitEngine() {
        if (faceEngine != null) {
            faceEngineCode = faceEngine.unInit();
            faceEngine = null;
            Log.i(TAG, "unInitEngine: " + faceEngineCode);
        }
    }

    private void startCamera() {
        mJavaCameraView.setVisibility(SurfaceView.VISIBLE);
        mJavaCameraView.setCvCameraViewListener(FaceRecognitionActivity.this);
        mJavaCameraView.setCameraIndex(1);
        mJavaCameraView.enableView();

        initFaceDetector();
        initEyeDetector();
    }

    private void stopCamera() {
        if(mJavaCameraView != null) {
            mJavaCameraView.disableView();
            mJavaCameraView.setVisibility(SurfaceView.GONE);
            mJavaCameraView = null;
        }
    }

    private void initFaceDetector() {
        File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + "haarcascade_frontalface.xml");
        readAndWriteXML(file, R.raw.haarcascade_frontalface_alt_tree);
        mNativeDetector = new DetectionBasedTracker(file.getAbsolutePath(),0);
    }

    private void initEyeDetector() {
        File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + "haarcascade_eye_tree_eyeglasses.xml");
        readAndWriteXML(file, R.raw.haarcascade_eye_tree_eyeglasses);
        eyeDetector = new CascadeClassifier(file.getAbsolutePath());
    }

    public void readAndWriteXML(File file, @RawRes int id) {
        InputStream ip = getResources().openRawResource(id);
        InputStreamReader ir = null;
        FileOutputStream op = null;
        BufferedReader br = null;
        OutputStreamWriter ow = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;
        String line = null;
        try {
            ir = new InputStreamReader(ip);
            br = new BufferedReader(ir);
            op = new FileOutputStream(file);
            ow = new OutputStreamWriter(op);
            bw = new BufferedWriter(ow);
            pw = new PrintWriter(bw);
            while ((line = br.readLine()) != null ) {
                pw.println(line);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (br!=null) {
                    br.close();
                }
                if (ir!=null) {
                    ir.close();
                }
                if (ip!=null) {
                    ip.close();
                }
                if (pw!=null) {
                    pw.close();
                }
                if (bw!=null) {
                    bw.close();
                }
                if (ow!=null) {
                    ow.close();
                }
                if (op!=null) {
                    op.close();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // 获取容器大小
        containerWidth = faceRecognitionCameraContainer.getWidth();
        containerHeight = faceRecognitionCameraContainer.getHeight();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        leftEye_tpl = new Mat();
        rightEye_tpl = new Mat();
        frame = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        frame.release();
        mGray.release();
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
        // 第一帧过来的时候
        if(mAbsolutionFaceSize == 0) {
            int height = frame.rows();
            if(Math.round(height * mRelativeFaceSize) > 0) {
                mAbsolutionFaceSize = Math.round(height * mRelativeFaceSize);
            }
            mNativeDetector.setMinFaceSize(mAbsolutionFaceSize);
            mNativeDetector.start();
        }
        Imgproc.cvtColor(getFrame, mGray, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.equalizeHist(mGray, mGray);
        MatOfRect faces = new MatOfRect();
        mNativeDetector.detect(mGray, faces);
        Rect[] faceList = faces.toArray();
        System.out.println("faceList.length: " + faceList.length);
        if(faceList.length > 0) {
            if(isStartLogin && isArcsoftEngineActivated && faceList.length == 1 && !isStartFaceRecognition) {
                isStartFaceRecognition = true;
                capturedFaceFeature = null;
                new Thread() {
                    @Override
                    public void run() {
                        Bitmap bmp = Bitmap.createBitmap(getFrame.cols(), getFrame.rows(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(getFrame, bmp);
                        capturedFaceFeature = getFaceFeature(bmp, TYPE_CAPTURED);
                        bmp.recycle();
                        if (enrolledFaceFeature != null) {
                            faceComparison();
                        } else {
                            postGetUserImageData();
                        }

                    }
                }.start();
            }
            for (int i=0; i<faceList.length; i++) {
                Imgproc.rectangle(getFrame, faceList[i].tl(), faceList[i].br(), FACE_COLOR, 2, 8, 0);
                String line1 = "";
                if(gender != -1) {
                    if (GenderInfo.MALE == gender) {
                        line1 = "Male";
                    } else if (GenderInfo.FEMALE == gender) {
                        line1 = "Female";
                    }
                }
                if (age != -1) {
                    if (!line1.equals("")) {
                        line1 = line1 + ", " + age;
                        Imgproc.putText(getFrame, line1, new Point(faceList[i].tl().x, faceList[i].tl().y - 10), FONT_HERSHEY_COMPLEX, 1.5, TEXT_COLOR, 2);
                    } else {
                        line1 = String.valueOf(age);
                        Imgproc.putText(getFrame, line1, new Point(faceList[i].tl().x, faceList[i].tl().y - 10), FONT_HERSHEY_COMPLEX, 1.5, TEXT_COLOR, 2);
                    }
                }
                String line2 = "";
                if (score != -1) {
                    if(username != null && !username.equals("")) {
                        line2 = username + ", " + String.format("%.2f", score);
                        Imgproc.putText(getFrame, line2, new Point(faceList[i].tl().x, faceList[i].tl().y - 60), FONT_HERSHEY_COMPLEX, 1.5, TEXT_COLOR, 2);
                    }
                }
                findEyeArea(faceList[i], getFrame);
            }
        } else {
            gender = -1;
            age = -1;
        }
        faces.release();
    }

    private void findEyeArea(Rect faceROI, Mat getFrame) {
        // Step One
        int offy = (int) (faceROI.height * 0.33f);
        int offx = (int) (faceROI.width * 0.15f);
        int sh = (int) (faceROI.height * 0.17f);
        int sw = (int) (faceROI.width * 0.32f);
        int gap = (int) (faceROI.width * 0.025f);
        Point lp_eye = new Point(faceROI.tl().x + offx, faceROI.tl().y + offy);
        Point lp_end = new Point(lp_eye.x + sw - gap, lp_eye.y + sh);

        int right_offx = (int) (faceROI.width * 0.095f);
        int rew = (int) (sw * 0.81f);
        Point rp_eye = new Point(faceROI.tl().x + faceROI.width / 2 + right_offx, faceROI.tl().y + offy);
        Point rp_end = new Point(rp_eye.x + rew, rp_eye.y + sh);

        Imgproc.rectangle(getFrame, lp_eye, lp_end, EYE_COLOR, 2);
        Imgproc.rectangle(getFrame, rp_eye, rp_end, EYE_COLOR, 2);

        // Step Two
        MatOfRect eyes = new MatOfRect();

        Rect left_eye_roi = new Rect();
        left_eye_roi.x = (int) lp_eye.x;
        left_eye_roi.y = (int) lp_eye.y;
        left_eye_roi.width = (int) (lp_end.x - lp_eye.x);
        left_eye_roi.height = (int) (lp_end.y - lp_eye.y);

        Rect right_eye_roi = new Rect();
        right_eye_roi.x = (int) rp_eye.x;
        right_eye_roi.y = (int) rp_eye.y;
        right_eye_roi.width = (int) (rp_end.x - rp_eye.x);
        right_eye_roi.height = (int) (rp_end.y - rp_eye.y);

        // 级联分类器
        Mat leftEye = getFrame.submat(left_eye_roi);
        Mat rightEye = getFrame.submat(right_eye_roi);

        eyeDetector.detectMultiScale(mGray.submat(left_eye_roi), eyes, 1.15, 2, 0, new Size(30, 30), new Size());
        Rect[] eyesArray = eyes.toArray();
        for (int i=0; i<eyesArray.length; i++) {
            leftEye.submat(eyesArray[i]).copyTo(leftEye_tpl);
            Imgproc.rectangle(leftEye, eyesArray[i].tl(), eyesArray[i].br(), EYEBALL_BORDER_COLOR, 2);
        }
        if(eyesArray.length == 0) {
            Rect left_roi = matchWithTemplate(leftEye, true);
            if(left_roi != null) {
                Imgproc.rectangle(leftEye, left_roi.tl(), left_roi.br(), EYEBALL_BORDER_COLOR, 2);
            }
        }
        eyes.release();

        eyes = new MatOfRect();
        eyeDetector.detectMultiScale(mGray.submat(right_eye_roi), eyes, 1.15, 2, 0, new Size(30, 30), new Size());
        eyesArray = eyes.toArray();
        for (int i=0; i<eyesArray.length; i++) {
            rightEye.submat(eyesArray[i]).copyTo(rightEye_tpl);
            Imgproc.rectangle(rightEye, eyesArray[i].tl(), eyesArray[i].br(), EYEBALL_BORDER_COLOR, 2);
        }
        if(eyesArray.length == 0) {
            Rect right_roi = matchWithTemplate(rightEye, false);
            if(right_roi != null) {
                Imgproc.rectangle(rightEye, right_roi.tl(), right_roi.br(), EYEBALL_BORDER_COLOR, 2);
            }
        }
        eyes.release();
    }

    private Rect matchWithTemplate(Mat src, boolean left) {
        Mat tpl = left ? leftEye_tpl : rightEye_tpl;
        if(tpl.cols() == 0 || tpl.rows() == 0) {
            return null;
        }
        int height = src.rows() - tpl.rows() + 1;
        int width = src.cols() - tpl.cols() + 1;
        if(height < 1 || width < 1) {
            return null;
        }
        Mat result = new Mat(height, width, CvType.CV_32FC1);

        // 模板匹配
        int method = Imgproc.TM_CCOEFF_NORMED;
        Imgproc.matchTemplate(src, tpl, result, method);
        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(result);
        Point maxloc = minMaxLocResult.maxLoc;

        // ROI
        Rect rect = new Rect();
        rect.x = (int) maxloc.x;
        rect.y = (int) maxloc.y;
        rect.width = tpl.cols();
        rect.height = tpl.rows();
        result.release();

        return rect;
    }

    private FaceFeature getFaceFeature(Bitmap bitmap, int faceType) {
        if (bitmap == null) {
            if (faceType == TYPE_CAPTURED) {
                gender = -1;
                age = -1;
            }
            return null;
        }

        if (faceEngine == null) {
            if (faceType == TYPE_CAPTURED) {
                gender = -1;
                age = -1;
            }
            return null;
        }

        bitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);

        if (bitmap == null) {
            if (faceType == TYPE_CAPTURED) {
                gender = -1;
                age = -1;
            }
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // bitmap转bgr24
        byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
        int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
        if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
            if (faceType == TYPE_CAPTURED) {
                gender = -1;
                age = -1;
            }
            return null;
        }

        if (bgr24 != null) {
            List<FaceInfo> faceInfoList = new ArrayList<>();
            //人脸检测
            int detectCode = faceEngine.detectFaces(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList);
            if (detectCode != 0 || faceInfoList.size() == 0) {
                if (faceType == TYPE_CAPTURED) {
                    gender = -1;
                    age = -1;
                }
                return null;
            }

            int faceProcessCode = faceEngine.process(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList, FaceEngine.ASF_AGE | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE);
            Log.i(TAG, "processImage: " + faceProcessCode);
            if (faceProcessCode != ErrorInfo.MOK) {
                if (faceType == TYPE_CAPTURED) {
                    gender = -1;
                    age = -1;
                }
                return null;
            }

            //年龄信息结果
            List<AgeInfo> ageInfoList = new ArrayList<>();
            //性别信息结果
            List<GenderInfo> genderInfoList = new ArrayList<>();
            //三维角度结果
            List<Face3DAngle> face3DAngleList = new ArrayList<>();
            //获取年龄、性别、三维角度
            int ageCode = faceEngine.getAge(ageInfoList);
            int genderCode = faceEngine.getGender(genderInfoList);
            int face3DAngleCode = faceEngine.getFace3DAngle(face3DAngleList);

            if (ageCode != ErrorInfo.MOK || genderCode != ErrorInfo.MOK || face3DAngleCode != ErrorInfo.MOK) {
                if (faceType == TYPE_CAPTURED) {
                    gender = -1;
                    age = -1;
                }
                return null;
            }

            //人脸比对数据显示
            if (faceInfoList.size() == 1) {
                if (faceType == TYPE_CAPTURED) {
                    gender = genderInfoList.get(0).getGender();
                    age = ageInfoList.get(0).getAge();
                }
            } else {
                if (faceType == TYPE_CAPTURED) {
                    gender = -1;
                    age = -1;
                }
                return null;
            }

            FaceFeature faceFeature = new FaceFeature();
            int res = faceEngine.extractFaceFeature(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList.get(0), faceFeature);
            if (res != ErrorInfo.MOK) {
                return null;
            } else {
                return faceFeature;
            }
        } else {
            return null;
        }
    }

    private void postGetUserImageData(){
        String parameters = getGetUserImageParameters();
        System.out.println("parameters: " + parameters);
        Post postGetUserImageTask = new Post(FaceRecognitionActivity.this, POST_GET_USER_IMAGE_ID);
        postGetUserImageTask.execute(IPAddress.getIpAddress() + "/api/userImage", parameters);
    }

    private String getGetUserImageParameters() {
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

    private void postLoginWithUsernameData() {
        String parameters = getLoginWithUsernameParameters();
        System.out.println("parameters: " + parameters);
        Post postLoginWithUsernameTask = new Post(FaceRecognitionActivity.this, POST_GET_ACCESS_TOKEN_ID);
        postLoginWithUsernameTask.execute(IPAddress.getIpAddress() + "/api/frontendMatching/getAccessToken", parameters);
    }


    private String getLoginWithUsernameParameters() {
        String parameters = "";
        String usernameEncryption = "";
        String deviceUuidEncryption = "";
        try {
            usernameEncryption = RSAUtils.encryptByPublicKey(username, publicKey);
            deviceUuidEncryption = RSAUtils.encryptByPublicKey(deviceUuid, publicKey);
            String firstParameter = "username=" + URLEncoder.encode(usernameEncryption, "UTF-8");
            String secondParameter = "deviceId=" + URLEncoder.encode(deviceUuidEncryption, "UTF-8");
            String thirdParameter = "grant_type=" + URLEncoder.encode("password", "UTF-8");
            parameters = firstParameter + "&" + secondParameter + "&" + thirdParameter;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parameters;
    }

    private void faceComparison() {
        if (enrolledFaceFeature != null && capturedFaceFeature != null) {
            FaceSimilar faceSimilar = new FaceSimilar();
            int compareResult = faceEngine.compareFaceFeature(enrolledFaceFeature, capturedFaceFeature, faceSimilar);
            if (compareResult == ErrorInfo.MOK) {
                score = faceSimilar.getScore();
                if (score > 0.9f) {
                    Log.i(TAG, "Compare Result: " + score);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CustomToast.show(FaceRecognitionActivity.this, "Welcome! " + username);
                        }
                    });
                    postLoginWithUsernameData();
                } else {
                    isStartFaceRecognition = false;
                }
            } else {
                isStartFaceRecognition = false;
            }
        } else {
            isStartFaceRecognition = false;
        }
    }

    private void goToNextActivity() {
        stopCamera();
        Intent intent = new Intent(FaceRecognitionActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onTaskCompleted(String response, int requestId) {
        if(requestId == POST_GET_USER_IMAGE_ID) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                String Status = jsonObject.getString("status");

                if(Status.equals("Succeeded")) {
                    String userImageBase64 = jsonObject.getString("userImage");
                    byte[] decodedUserImage = Base64.decode(userImageBase64, Base64.DEFAULT);
                    Bitmap userImage = BitmapFactory.decodeByteArray(decodedUserImage, 0, decodedUserImage.length);
                    mUserImage.setImageBitmap(userImage);

                    if (userImage != null) {
                        enrolledFaceFeature = getFaceFeature(userImage, TYPE_ENROLLED);
                        faceComparison();
                    } else {
                        isStartFaceRecognition = false;
                    }
                }
                else {
                    String Error = jsonObject.getString("error");
                    CustomToast.show(FaceRecognitionActivity.this, "Get user image failed");
                    isStartFaceRecognition = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                CustomToast.show(FaceRecognitionActivity.this, "Server error");
                isStartFaceRecognition = false;
            }

        } else if (requestId == POST_GET_ACCESS_TOKEN_ID) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                String Status = jsonObject.getString("status");

                if(Status.equals("Succeeded")) {
                    int UserId = jsonObject.getInt("userId");
                    token = jsonObject.getString("token");
                    String RefreshToken = jsonObject.getString("refresh_token");

                    GetSetSharedPreferences.setDefaults("username", username, getApplicationContext());
                    GetSetSharedPreferences.setDefaults("userId", String.valueOf(UserId), getApplicationContext());
                    GetSetSharedPreferences.setDefaults("token", token, getApplicationContext());
                    GetSetSharedPreferences.setDefaults("refresh_token", RefreshToken, getApplicationContext());

                    CustomToast.show(FaceRecognitionActivity.this, "Welcome! " + username);
                    goToNextActivity();
                }
                else {
                    String Error = jsonObject.getString("error");
                    CustomToast.show(FaceRecognitionActivity.this, "Login failed");
                }
            } catch (Exception e) {
                e.printStackTrace();
                CustomToast.show(FaceRecognitionActivity.this, "Server error");
            }
        }
    }

    @Override
    public void onPause() {
        if(mJavaCameraView != null) {
            mJavaCameraView.disableView();
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
    }

    @Override
    public void onDestroy() {
        unInitEngine();
        stopCamera();
        super.onDestroy();
    }
}
