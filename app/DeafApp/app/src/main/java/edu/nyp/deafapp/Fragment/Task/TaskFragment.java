package edu.nyp.deafapp.Fragment.Task;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import androidx.annotation.Nullable;
import edu.nyp.deafapp.Adapter.TaskAdapter;
import edu.nyp.deafapp.CustomView.CircleProgressBar;
import edu.nyp.deafapp.CustomView.CustomToast;
import edu.nyp.deafapp.Fragment.MainFragment;
import edu.nyp.deafapp.Fragment.Study.StudyFragment;
import edu.nyp.deafapp.HttpAsyncTask.Get;
import edu.nyp.deafapp.HttpAsyncTask.GetAuth;
import edu.nyp.deafapp.IPAddress;
import edu.nyp.deafapp.Interface.OnTaskCompleted;
import edu.nyp.deafapp.LoginActivity;
import edu.nyp.deafapp.MainActivity;
import edu.nyp.deafapp.Model.Task;
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
public class TaskFragment extends SupportFragment implements OnTaskCompleted {

    private static final String DEAF_APP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeafApp";
    private static final String MODEL_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeafApp/ZeuseesFaceTracking";
    private static final String AUDIO_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeafApp/Record";
    private static final String VIDEO_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeafApp/Video";
    private static final String FRAMES_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeafApp/Frames";
    private static final String CAPTURE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DeafApp/Capture";
    private static final String CAPTURE_VIDEO_DIR = CAPTURE_DIR + "/Video";
    private static final String CAPTURE_AUDIO_DIR = CAPTURE_DIR + "/Audio";
    private static final String CAPTURE_COMBINE_DIR = CAPTURE_DIR + "/Combine";

    private static final int GET_TASK_ID = 5;
    private static final int GET_TUTORIAL_VIDEO_TASK_ID = 1;
    private static final int GET_EXERCISE_VIDEO_TASK_ID = 2;
    private static final int GET_FRAMES_TASK_ID = 3;
    private static final int GET_ORGANS_TASK_ID = 4;
    private static final int REFRESH_TOKEN_TASK_ID = 100;

    private Context context;
    private Toolbar mToolbar;
    private ListView mTaskListView;
    private TaskAdapter mTaskAdapter;
    private ProgressBar mProgressView;
    private CircleProgressBar mtasksView;

    private List<Task> mTaskList = new ArrayList<>();

    private File deafAppFolder;
    private File modelFolder;
    private File audioFolder;
    private File videoFolder;
    private File framesFolder;
    private File captureFolder;
    private File captureVideoFolder;
    private File captureAudioFolder;
    private File captureCombineFolder;

    private int selectedId = 0;
    private int userId = 0;
    private String selectedContent;
    private String token = "";
    private int currentRequestId = 0;

    public static TaskFragment newInstance() {
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task, container, false);
        initView(view);

        token = GetSetSharedPreferences.getDefaults("token", _mActivity.getApplicationContext());

        return view;
    }

    private void initView(View view) {
        context = getContext();
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mTaskListView = (ListView) view.findViewById(R.id.task_list_view);
        mProgressView = (ProgressBar) view.findViewById(R.id.loading_progress);
        mtasksView = (CircleProgressBar) view.findViewById(R.id.tasks_view);
        mToolbar.setTitle(R.string.fragment_task);
    }

    @Override
    public void onEnterAnimationEnd(Bundle savedInstanceState) {
        super.onEnterAnimationEnd(savedInstanceState);
        initDelayView();
    }

    private void initDelayView() {
        userId = Integer.parseInt(GetSetSharedPreferences.getDefaults("userId", _mActivity.getApplicationContext()));
        showProgress(true);
        deafAppFolder = new File(DEAF_APP_DIR);
        if (!deafAppFolder.exists()) {
            deafAppFolder.mkdirs();
        }
        modelFolder = new File(MODEL_DIR);
        if (!modelFolder.exists()) {
            modelFolder.mkdirs();
        }
        captureFolder = new File(CAPTURE_DIR);
        if (!captureFolder.exists()) {
            captureFolder.mkdirs();
        }
        captureVideoFolder = new File(CAPTURE_VIDEO_DIR);
        if (!captureVideoFolder.exists()) {
            captureVideoFolder.mkdirs();
        }
        captureAudioFolder = new File(CAPTURE_AUDIO_DIR);
        if (!captureAudioFolder.exists()) {
            captureAudioFolder.mkdirs();
        }
        captureCombineFolder = new File(CAPTURE_COMBINE_DIR);
        if (!captureCombineFolder.exists()) {
            captureCombineFolder.mkdirs();
        }
        currentRequestId = GET_TASK_ID;
        getTask();
    }

    private void getTask(){
        GetAuth getTaskTask = new GetAuth(TaskFragment.this, GET_TASK_ID);
        getTaskTask.execute(IPAddress.getIpAddress() + "/api/userId/" + userId + "/schedule/progress", token);
    }

    @Override
    public void onDestroyView() {
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

    @Override
    public void onTaskCompleted(String response, int requestId) {
        if(response.equals("FORBIDDEN")) {
            TokenHelper.refreshToken(_mActivity, this::onTaskCompleted, REFRESH_TOKEN_TASK_ID);
        }
        else if(response.equals("UNAUTHORIZED")) {
            TokenHelper.refreshToken(_mActivity, this::onTaskCompleted, REFRESH_TOKEN_TASK_ID);
        }
        else {
            if(requestId == GET_TASK_ID) {
                try {
                    JSONArray jsonArray = new JSONArray(response);

                    int countDone = 0;

                    for(int i=0; i<jsonArray.length(); i++) {
                        JSONObject jsonObject = new JSONObject(jsonArray.getString(i));
                        System.out.println(jsonObject.getInt("progressTimes"));
                        if(jsonObject.getInt("progressTimes") == 0) {
                            mTaskList.add(new Task(jsonObject.getInt("englishId"), jsonObject.getString("content"), false));
                        } else {
                            countDone++;
                            mTaskList.add(new Task(jsonObject.getInt("englishId"), jsonObject.getString("content"), true));
                        }
                    }

                    mtasksView.setProgress(countDone, jsonArray.length());

                    mTaskAdapter = new TaskAdapter(context, mTaskList);
                    mTaskListView.setAdapter(mTaskAdapter);

                    mTaskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            selectedContent = mTaskList.get(position).getContent();
                            selectedId = mTaskList.get(position).getTaskId();
                            currentRequestId = GET_TUTORIAL_VIDEO_TASK_ID;

                            int downloadedId = 0;
                            if(GetSetSharedPreferences.getDefaults("downloadedId", _mActivity.getApplicationContext()) != null) {
                                downloadedId = Integer.valueOf(GetSetSharedPreferences.getDefaults("downloadedId", _mActivity.getApplicationContext()));
                            }

                            if(selectedId != downloadedId) {
                                showProgress(true);
                                audioFolder = new File(AUDIO_DIR);
                                if (!audioFolder.exists()) {
                                    audioFolder.mkdirs();
                                }
                                else {
                                    String[] children = audioFolder.list();
                                    for (int i = 0; i < children.length; i++)  {
                                        new File(audioFolder, children[i]).delete();
                                    }
                                    audioFolder.mkdirs();
                                }

                                videoFolder = new File(VIDEO_DIR);
                                if (!videoFolder.exists()) {
                                    videoFolder.mkdirs();
                                }
                                else {
                                    String[] children = videoFolder.list();
                                    for (int i = 0; i < children.length; i++)  {
                                        new File(videoFolder, children[i]).delete();
                                    }
                                    videoFolder.mkdirs();
                                }

                                framesFolder = new File(FRAMES_DIR);
                                if (!framesFolder.exists()) {
                                    framesFolder.mkdirs();
                                }
                                else {
                                    String[] children = framesFolder.list();
                                    for (int i = 0; i < children.length; i++)  {
                                        new File(framesFolder, children[i]).delete();
                                    }
                                    framesFolder.mkdirs();
                                }
                                downloadTutorial(selectedId);
                            }
                            else {
                                ((MainFragment) getParentFragment()).startBrotherFragment(StudyFragment.newInstance(selectedId, selectedContent));
                            }
                        }
                    });
                }
                catch (Exception e){
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
                        if(currentRequestId == GET_TASK_ID) {
                            getTask();
                        } else {
                            downloadTutorial(selectedId);
                        }
                        currentRequestId = 0;
                    } else {
                        Intent intent = new Intent(_mActivity, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    Intent intent = new Intent(_mActivity, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        }
    }

    private void downloadTutorial(int englishId) {
        DownLoadVideoTask downLoadCountingVideoTask = new DownLoadVideoTask(englishId, videoFolder, "tutorial", GET_TUTORIAL_VIDEO_TASK_ID);
        downLoadCountingVideoTask.execute();
    }

    private void downloadExercise(int englishId) {
        DownLoadVideoTask downLoadCountingVideoTask = new DownLoadVideoTask(englishId, videoFolder, "exercise", GET_EXERCISE_VIDEO_TASK_ID);
        downLoadCountingVideoTask.execute();
    }

    private void downloadFrames(int englishId) {
        DownLoadVideoTask downLoadCountingVideoTask = new DownLoadVideoTask(englishId, framesFolder, "frames", GET_FRAMES_TASK_ID);
        downLoadCountingVideoTask.execute();
    }

    private void downloadOrgans(int englishId) {
        DownLoadVideoTask downLoadCountingVideoTask = new DownLoadVideoTask(englishId, videoFolder, "organ", GET_ORGANS_TASK_ID);
        downLoadCountingVideoTask.execute();
    }

    public class DownLoadVideoTask extends AsyncTask<Void, Void, String> {
        private int englishId;
        private File downloadFolder;
        private String fileType;
        private int requestId;

        public DownLoadVideoTask(int englishId, File downloadFolder, String fileType, int requestId) {
            this.englishId = englishId;
            this.downloadFolder = downloadFolder;
            this.fileType = fileType;
            this.requestId = requestId;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = "failed";
            File file;

            if(requestId == GET_TUTORIAL_VIDEO_TASK_ID || requestId == GET_EXERCISE_VIDEO_TASK_ID || requestId == GET_ORGANS_TASK_ID) {
                file = new File(downloadFolder, fileType + ".mp4");
            }
            else {
                file = new File(downloadFolder, fileType + ".zip");
            }

            try {
                URL url;
                if(requestId == GET_TUTORIAL_VIDEO_TASK_ID || requestId == GET_EXERCISE_VIDEO_TASK_ID || requestId == GET_ORGANS_TASK_ID) {
                    url = new URL(IPAddress.getIpAddress() + "/api/englishId/" + englishId + "/" + fileType + "Video");
                }
                else {
                    url = new URL(IPAddress.getIpAddress() + "/api/englishId/" + englishId + "/Frames");
                }
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                FileOutputStream fos = null;
                InputStream inputStream = null;
                try {
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Authorization", "bearer " + token);
                    urlConnection.setReadTimeout(3000);
                    urlConnection.setConnectTimeout(3000);
                    urlConnection.setRequestProperty("Accept-Encoding", "identity");

                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                        double fileLength = urlConnection.getContentLength();
                        // receive response as inputStream
                        fos = new FileOutputStream(file);
                        inputStream = urlConnection.getInputStream();
                        // video大小
                        if(fileLength > 0) {
                            // 建立一个byte数组作为缓冲区，等下把读取到的数据储存在这个数组
                            byte[] buffer = new byte[8192];
                            int len = 0;
                            while ((len = inputStream.read(buffer)) != -1) {
                                fos.write(buffer, 0, len);
                            }
                            fos.flush();
                            result = "Succeeded";
                        }
                    }
                    else if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        System.out.println("HTTP_FAILED: " + HttpURLConnection.HTTP_UNAUTHORIZED);
                        result = "UNAUTHORIZED";
                    }
                    else if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                        System.out.println("HTTP_FAILED: " + HttpURLConnection.HTTP_FORBIDDEN);
                        result = "FORBIDDEN";
                    }
                    else {
                        result = "Did not work!";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (file.exists()) {
                        file.delete();
                    }
                } finally {
                    if(fos!=null)
                        fos.close();
                    if (inputStream!=null)
                        inputStream.close();
                    if (urlConnection!=null)
                        urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (file.exists()) {
                    file.delete();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if(result.equals("FORBIDDEN")) {
                TokenHelper.refreshToken(_mActivity, TaskFragment.this::onTaskCompleted, REFRESH_TOKEN_TASK_ID);
            }
            else if(result.equals("UNAUTHORIZED")) {
                TokenHelper.refreshToken(_mActivity, TaskFragment.this::onTaskCompleted, REFRESH_TOKEN_TASK_ID);
            }
            if(result.equals("Succeeded")) {
                if(requestId == 1) {
                    downloadExercise(selectedId);
                }
                else if(requestId == 2) {
                    downloadFrames(selectedId);
                }
                else if(requestId == 3) {
                    downloadOrgans(selectedId);
                }
                else {
                    try {
                        File zipFile = new File(FRAMES_DIR, "frames.zip");
                        UnZip(zipFile, new File(FRAMES_DIR));
                        zipFile.delete();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    showProgress(false);
                    GetSetSharedPreferences.setDefaults("downloadedId", String.valueOf(selectedId), _mActivity.getApplicationContext());
                    ((MainFragment) getParentFragment()).startBrotherFragment(StudyFragment.newInstance(selectedId, selectedContent));
                }
            }
            else {
                CustomToast.show(_mActivity, "Server error");
            }
        }
    }

    private void UnZip(File zipFile, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs()) {
                    throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
                }
                if (ze.isDirectory()) {
                    continue;
                }
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1) {
                        fout.write(buffer, 0, count);
                    }
                } finally {
                    fout.close();
                }
            }
        } finally {
            zis.close();
        }
    }
}
